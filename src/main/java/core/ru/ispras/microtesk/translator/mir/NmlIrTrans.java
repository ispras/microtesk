package ru.ispras.microtesk.translator.mir;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.ExprTreeWalker;
import ru.ispras.fortress.expression.ExprTreeVisitorDefault;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.Location;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationSource;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationSourceMemory;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationSourcePrimitive;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;
import ru.ispras.microtesk.translator.nml.ir.expr.TypeCast;
import ru.ispras.microtesk.translator.nml.ir.primitive.Instance;
import ru.ispras.microtesk.translator.nml.ir.primitive.InstanceArgument;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAssignment;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAttributeCall;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementCondition;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementFunctionCall;
import ru.ispras.microtesk.translator.nml.ir.shared.Alias;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryExpr;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class NmlIrTrans {
  private static final class MirContext {
    private final BasicBlock bb;
    private final List<BasicBlock> blocks;
    private final Locals locals;

    public static MirContext newMir() {
      return new MirContext(new BasicBlock(), new ArrayList<BasicBlock>(), new Locals());
    }

    public MirContext fork(final BasicBlock bb) {
      return new MirContext(bb, this.blocks, this.locals);
    }

    private MirContext(
        final BasicBlock bb,
        final List<BasicBlock> blocks,
        final Locals locals) {
      this.bb = bb;
      this.blocks = blocks;
      this.locals = locals;

      blocks.add(bb);
    }

    public Local newLocal(final DataType type) {
      locals.type.add(type);
      return new Local(locals.type.size());
    }

    public Local getNamedLocal(final String name) {
      for (final LocalInfo info : locals.info.values()) {
        if (name.equals(info.name)) {
          return new Local(info.id);
        }
      }
      return null;
    }

    public Assignment assign(final Lvalue lhs, final Rvalue rhs) {
      return append(new Assignment(lhs, rhs));
    }

    public Local assignLocal(final Operand op) {
      if (op instanceof Local) {
        return (Local) op;
      }
      return assignLocal(Opcode.Use.make(op));
    }

    public Local assignLocal(final Rvalue rhs) {
      final Local lhs = newLocal(null); // FIXME
      assign(lhs, rhs);
      return lhs;
    }

    public <T extends Instruction> T append(final T insn) {
      bb.insns.add(insn);
      return insn;
    }
  }

  private static class Locals {
    public final List<DataType> type = new ArrayList();
    public final Map<Integer, LocalInfo> info = new HashMap<>();
  }

  private static class LocalInfo {
    public final int id;
    public final String name;

    public LocalInfo(final int id, final String name) {
      this.id = id;
      this.name = name;
    }
  }

  private static void translate(final MirContext ctx, final List<Statement> code) {
    for (final Statement s : code) {
      switch (s.getKind()) {
      case ASSIGN:
        translate(ctx, (StatementAssignment) s);
        break;

      default:
        break;
      }
    }
  }

  private static void translate(final MirContext ctx, final StatementAssignment s) {
    if (s.getLeft().isInternalVariable()) {
      return;
    }
    final Rvalue rhs = Opcode.Use.make(translate(ctx, s.getRight().getNode()));
    final Node node = s.getLeft().getNode();
    if (ExprUtils.isVariable(node)) {
      translateAccess(ctx, locationOf(node), new WriteAccess(ctx, rhs));
    } else if (ExprUtils.isOperation(node)) {
      final int size = sizeOf(node);
      final List<Node> operands = operandsOf(node);
      final Local rvalue = ctx.assignLocal(rhs);

      int offset = size;
      for (final Node lhs : operands) {
        offset -= sizeOf(lhs);

        final Rvalue field = Opcode.Shr.make(rvalue, valueOf(offset, size));
        translateAccess(ctx, locationOf(lhs), new WriteAccess(ctx, field));
      }
    }
  }

  private static List<Node> operandsOf(final Node node) {
    if (ExprUtils.isOperation(node)) {
      return ((NodeOperation) node).getOperands();
    }
    return Collections.emptyList();
  }

  private static Operand translate(final MirContext ctx, final Node node) {
    if (ExprUtils.isOperation(node)) {
      final TransRvalue visitor = new TransRvalue(ctx);
      new ExprTreeWalker(visitor).visit(node);

      return visitor.getResult();
    } else if (ExprUtils.isVariable(node)) {
      return newStatic(node);
    } else if (ExprUtils.isValue(node)) {
      return newConstant(node);
    }
    throw new IllegalStateException();
  }

  private static Constant newConstant(final Node node) {
    return new Constant(((NodeValue) node).getData());
  }

  private static Static newStatic(final Node node) {
    return new Static(((NodeVariable) node).getName());
  }

  static Opcode mapOpcode(final Enum<?> e) {
    return null;
  }

  private static final class TransRvalue extends ExprTreeVisitorDefault {
    private final MirContext ctx;
    private final Map<Node, Operand> mapped = new java.util.IdentityHashMap<>();
    private Operand result;

    public TransRvalue(final MirContext ctx) {
      this.ctx = ctx;
    }

    public Operand getResult() {
      return result;
    }

    @Override
    public void onOperationEnd(final NodeOperation node) {
      if (!mapped.containsKey(node)) {
        final Operand local;
        if (node.getOperationId().equals(StandardOperation.BVCONCAT)) {
          local = translateConcat(node);
        } else {
          local = translateMapping(node);
        }
        mapped.put(node, local);

        result = local;
      }
    }

    private Operand translateMapping(final NodeOperation node) {
      final Opcode opc = mapOpcode(node.getOperationId());
      final Iterator<Node> it = node.getOperands().iterator();
      final DataType type = node.getDataType();

      Operand op1 = lookUp(it.next());
      if (!it.hasNext()) {
        final Local lhs = ctx.newLocal(type);
        final Rvalue rhs = opc.make(op1);
        ctx.assign(lhs, rhs);

        op1 = lhs;
      }
      while (it.hasNext()) {
        final Local lhs = ctx.newLocal(type);
        final Rvalue rhs = opc.make(op1, lookUp(it.next()));
        ctx.assign(lhs, rhs);

        op1 = lhs;
      }
      return op1;
    }

    private Rvalue translateMapping2(final NodeOperation node) {
      final Opcode opc = mapOpcode(node.getOperationId());
      final Iterator<Node> it = node.getOperands().iterator();
      final DataType type = node.getDataType();

      Operand op1 = lookUp(it.next());
      if (!it.hasNext()) {
        return opc.make(op1);
      }

      Operand op2 = lookUp(it.next());
      while (it.hasNext()) {
        final Local lhs = ctx.newLocal(type);
        final Rvalue rhs = opc.make(op1, op2);
        ctx.assign(lhs, rhs);

        op1 = lhs;
        op2 = lookUp(it.next());
      }
      return opc.make(op1, op2);
    }

    private Operand translateConcat(final NodeOperation node) {
      final Iterator<Node> it = node.getOperands().iterator();
      final DataType type = node.getDataType();

      Operand expr = lookUp(it.next());
      while (it.hasNext()) {
        final Node op = it.next();

        final Rvalue shift =
          Opcode.Shl.make(expr, valueOf(sizeOf(op), sizeOf(type)));

        final Rvalue zext = new Cast(Opcode.Use.make(lookUp(op)), type);

        final Rvalue bitor =
          Opcode.BitOr.make(ctx.assignLocal(shift), ctx.assignLocal(zext));

        expr = ctx.assignLocal(bitor);
      }
      return expr;
    }

    private Operand lookUp(final Node node) {
      switch (node.getKind()) {
      case VALUE:
        return newConstant(node);

      case OPERATION:
        return mapped.get(node);

      case VARIABLE:
        return translateAccess(ctx, locationOf(node), new ReadAccess(ctx));
      }
      throw new UnsupportedOperationException();
    }
  }

  private static Location locationOf(final Node node) {
    return (Location) ((NodeInfo) node.getUserData()).getSource();
  }

  private static Constant valueOf(final long value, final int size) {
    return new Constant(Data.newBitVector(BigInteger.valueOf(value), size));
  }

  private static int sizeOf(final Node node) {
    return sizeOf(node.getDataType());
  }

  private static int sizeOf(final Data data) {
    return sizeOf(data.getType());
  }

  private static int sizeOf(final DataType type) {
    return type.getSize();
  }

  private static Lvalue translateAccess(
      final MirContext ctx,
      final Location l,
      final Accessor client) {
    final LocationSource source = l.getSource();
    final Access access = new Access(ctx, l);

    if (source instanceof LocationSourcePrimitive) {
      final Primitive p = ((LocationSourcePrimitive) source).getPrimitive();
      final Local arg = ctx.getNamedLocal(l.getName());

      if (p.getKind() == Primitive.Kind.MODE) {
        return client.accessMode(arg, access, p);
      } else {
        return client.accessLocal(arg, access);
      }
    } else if (source instanceof LocationSourceMemory) {
      final MemoryExpr mem = sourceToMemory(source);
      final Lvalue ref = new Static(mem.getName());
      return client.accessMemory(ref, access);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  private static MemoryExpr sourceToMemory(final LocationSource source) {
    return ((LocationSourceMemory) source).getMemory();
  }

  private static abstract class Accessor {
    protected final MirContext ctx;

    Accessor(final MirContext ctx) {
      this.ctx = ctx;
    }

    abstract Lvalue accessMode(Local source, Access access, Primitive p);
    abstract Lvalue accessLocal(Local source, Access access);
    abstract Lvalue accessMemory(Lvalue source, Access access);

    protected Lvalue index(final Lvalue src, final Access access) {
      if (access.index != null) {
        return new Index(src, access.index);
      }
      return src;
    }

    protected Lvalue extract(final Lvalue src, final Access access) {
      if (access.lo != null) {
        final Rvalue expr = Opcode.Shr.make(src, access.lo);
        final Rvalue rhs = new Cast(expr, DataType.bitVector(access.size));
        return ctx.assignLocal(rhs);
      }
      return src;
    }
  }

  private static class Access {
    public final Local index;
    public final Operand lo;
    public final Operand hi;
    public final int size;

    public Access(final MirContext ctx, final Location l) {
      if (l.getBitfield() != null) {
        final Location.Bitfield bits = l.getBitfield();
        this.lo = translate(ctx, bits.getFrom().getNode());
        this.hi = translate(ctx, bits.getTo().getNode());
        this.size = bits.getType().getBitSize();
      } else {
        this.lo = null;
        this.hi = null;
        this.size = l.getType().getBitSize();
      }

      BigInteger length = BigInteger.ZERO;
      if (l.getIndex() != null) {
        final MemoryExpr mem = sourceToMemory(l.getSource());
        length = mem.getSize();
      }
      if (length.compareTo(BigInteger.ONE) > 0) {
        this.index = ctx.assignLocal(translate(ctx, l.getIndex().getNode()));
      } else {
        this.index = null;
      }
    }
  }

  private static class ReadAccess extends Accessor {
    ReadAccess(final MirContext ctx) {
      super(ctx);
    }

    @Override
    public Lvalue accessLocal(final Local source, final Access access) {
      return extract(index(source, access), access);
    }

    @Override
    public Lvalue accessMode(final Local source, final Access access, final Primitive p) {
      final DataType type = TypeCast.getFortressDataType(p.getReturnType());
      final Local value = ctx.newLocal(type);
      ctx.append(new Call(source, Collections.<Operand>emptyList(), value));

      return accessLocal(value, access);
    }

    @Override
    public Lvalue accessMemory(final Lvalue mem, final Access access) {
      final Lvalue source = index(mem, access);
      final Local target = ctx.newLocal(null);
      ctx.append(new Load(source, target));

      return extract(target, access);
    }
  }

  private static class WriteAccess extends Accessor {
    final Rvalue value;

    WriteAccess(final MirContext ctx, final Rvalue value) {
      super(ctx);
      this.value = value;
    }

    @Override
    public Lvalue accessLocal(final Local target, final Access access) {
      final Lvalue dst = index(target, access);
      write(dst, access);
      return dst;
    }

    @Override
    public Lvalue accessMode(final Local target, final Access access, final Primitive p) {
      final DataType type = TypeCast.getFortressDataType(p.getReturnType());
      final Local value = ctx.newLocal(type);
      accessLocal(value, access);

      final Call call =
        new Call(target, Collections.<Operand>singletonList(value), null);
      ctx.append(call);
      return value;
    }

    @Override
    public Lvalue accessMemory(final Lvalue mem, final Access access) {
      final Lvalue target = index(mem, access);
      final Local value = ctx.newLocal(null);
      if (access.lo != null) {
        ctx.append(new Load(target, value));
        write(value, access);
      } else {
        ctx.assign(value, this.value);
      }
      ctx.append(new Store(target, value));

      return target;
    }

    private void write(final Lvalue target, final Access access) {
      if (access.lo != null) {
        final Rvalue zext = new Cast(value, null); // Cast(value, dst.getType())
        final Rvalue rhs = Opcode.Shl.make(ctx.assignLocal(zext), access.lo);

        final Operand mask = createMask(target, access);
        final Rvalue clear = Opcode.BitAnd.make(target, mask);
        final Rvalue store =
          Opcode.BitOr.make(ctx.assignLocal(clear), ctx.assignLocal(rhs));

        ctx.assign(target, store);
      } else {
        ctx.assign(target, value);
      }
    }

    private Operand createMask(final Lvalue source, final Access access) {
      // final Constant ones = valueOf(-1, 1024);
      // final Rvalue not = Opcode.BitXor.make();
      return null;
    }
  }
}
