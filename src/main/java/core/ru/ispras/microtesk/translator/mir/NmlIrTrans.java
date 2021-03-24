/*
    Copyright 2019-2021 ISP RAS (http://www.ispras.ru)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package ru.ispras.microtesk.translator.mir;

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.ExprTreeWalker;
import ru.ispras.fortress.expression.ExprTreeVisitorDefault;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.translator.nml.antlrex.ExprReducer;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.Location;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationSource;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationSourceMemory;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationSourcePrimitive;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;
import ru.ispras.microtesk.translator.nml.ir.expr.Operator;
import ru.ispras.microtesk.translator.nml.ir.primitive.Instance;
import ru.ispras.microtesk.translator.nml.ir.primitive.InstanceArgument;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAnd;
import ru.ispras.microtesk.translator.nml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAssignment;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAttributeCall;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementCondition;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementFunctionCall;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryResource;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static ru.ispras.microtesk.translator.mir.Instruction.*;

public final class NmlIrTrans {
  private final PrimitiveAnd source;
  private final Pattern nonword = Pattern.compile("\\W+");

  private NmlIrTrans(final PrimitiveAnd source) {
    this.source = source;
  }

  public static MirContext translate(
      final PrimitiveAnd p, final String name, final List<Statement> body) {

    final NmlIrTrans worker = new NmlIrTrans(p);
    return worker.translate(name, body);
  }

  public static ModeAccess translateMode(final PrimitiveAnd p) {
    return new ModeAccess(newReadAccess(p), newWriteAccess(p));
  }

  public static final class ModeAccess {
    public final MirContext read;
    public final MirContext write;

    public ModeAccess(final MirContext read, final MirContext write) {
      this.read = read;
      this.write = write;
    }
  }

  private static MirContext newReadAccess(final PrimitiveAnd p) {
    final NmlIrTrans worker = new NmlIrTrans(p);
    final MirContext ctx = newContext("read", p);

    final MirBlock bb = ctx.newBlock();
    bb.append(new Return(worker.translate(bb, p.getReturnExpr().getNode())));

    return ctx;
  }

  private static MirContext newWriteAccess(final PrimitiveAnd p) {
    final NmlIrTrans worker = new NmlIrTrans(p);
    final MirContext ctx = newWriteContext("write", p);

    final MirBlock bb = ctx.newBlock();
    worker.translateAssignment(
        bb,
        p.getReturnExpr().getNode(),
        bb.getNamedLocal(".value"));
    bb.append(new Return(null));

    return ctx;
  }

  private MirContext translate(final String name, final List<Statement> body) {
    final MirContext ctx = newContext(name, this.source);
    final List<MirBlock> terminals = translate(ctx.newBlock(), body);
    for (final MirBlock bb : terminals) {
      bb.append(new Return(null));
    }

    return ctx;
  }

  private static MirContext newContext(final String attr, final PrimitiveAnd p) {
    final String ctxName = String.format("%s.%s", p.getName(), attr);
    final MirContext ctx = new MirContext(ctxName, (FuncTy) typeOf(p));

    int index = 0;
    for (final String name : p.getArguments().keySet()) {
      ctx.renameParameter(index++, name);
    }
    return ctx;
  }

  private static MirContext newWriteContext(final String attr, final PrimitiveAnd p) {
    final String ctxName = String.format("%s.%s", p.getName(), attr);
    final FuncTy readType = (FuncTy) typeOf(p);

    final List<MirTy> params = new java.util.ArrayList<>(readType.params);
    params.add(readType.ret);
    final FuncTy writeType = new FuncTy(VoidTy.VALUE, params);

    final MirContext ctx = new MirContext(ctxName, writeType);
    int index = 0;
    for (final String name : p.getArguments().keySet()) {
      ctx.renameParameter(index++, name);
    }
    ctx.renameParameter(index, ".value");

    return ctx;
  }

  static MirTy typeOf(final Primitive p) {
    if (p.getKind().equals(Primitive.Kind.IMM)) {
      return returnTypeOf(p);
    } else {
      return new FuncTy(returnTypeOf(p), getParameterList(p));
    }
  }

  private static List<MirTy> getParameterList(final Primitive p) {
    if (!p.isOrRule()) {
      return ((PrimitiveAnd) p).getArguments().values().stream()
          .map(x -> typeOf(x))
          .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  private static MirTy returnTypeOf(final Primitive p) {
    if (p.getReturnType() != null) {
      return new IntTy(p.getReturnType().getBitSize());
    }
    return VoidTy.VALUE;
  }

  private List<MirBlock> translate(final MirBlock entry, final List<Statement> code) {
    MirBlock ctx = entry;
    List<MirBlock> terminals = Collections.singletonList(ctx);

    for (final Statement s : code) {
      if (terminals.size() > 1) {
        final MirBlock sink = entry.ctx.newBlock();
        for (final MirBlock block : terminals) {
          block.jump(sink);
        }
        ctx = sink;
        terminals = Collections.singletonList(ctx);
      }

      switch (s.getKind()) {
      case ASSIGN:
        translate(ctx, (StatementAssignment) s);
        break;

      case COND:
        terminals = translate(ctx, (StatementCondition) s);
        break;

      case CALL:
        translate(ctx, (StatementAttributeCall) s);
        break;

      case FUNCALL:
        translatePathQualifier(ctx, (StatementFunctionCall) s);
        break;

      case FORMAT:
        break;
      }
    }
    return terminals;
  }

  private void translatePathQualifier(
      final MirBlock ctx, final StatementFunctionCall s) {
    final String markName;
    final String callee = s.getName();

    final List<String> supported =
        Arrays.asList("undefined", "unpredicted", "mark", "exception");
    if (supported.contains(callee)) {
      final String name;
      if (s.getArgumentCount() > 0) {
        name = String.format("$%s_%s",
            callee, sanitizeName(s.getArgument(0).toString()));
      } else {
        name = "$" + callee;
      }
      ctx.append(new Store(new Static(name, Types.BIT), valueOf(1, 1)));
    }
  }

  private String sanitizeName(final String input) {
    return nonword.matcher(input).replaceAll("_");
  }

  private void translate(final MirBlock block, final StatementAttributeCall s) {
    if (s.isThisCall()) {
      final int nargs = block.ctx.getSignature().params.size();
      final List<Operand> args = new java.util.ArrayList<>();
      for (int i = 0; i < nargs; ++i) {
        args.add(block.getLocal(i + 1));
      }
      final String method = String.format("%s.%s",
        block.ctx.name.split("\\.")[0],
        s.getAttributeName());
      block.thiscall(method, args, null);
    } else if (s.isInstanceCall()) {
      final Instance instance = s.getCalleeInstance();
      final String method = String.format("%s.%s",
        instance.getPrimitive().getName(), s.getAttributeName());
      block.call(
        newClosure(block, instance),
        method,
        Collections.<Operand>emptyList(),
        null);
    } else {
      final String method = String.format("%s.%s",
        source.getArguments().get(s.getCalleeName()).getName(), s.getAttributeName());
      block.call(
        block.getNamedLocal(s.getCalleeName()),
        method,
        Collections.<Operand>emptyList(),
        null);
    }
  }

  private Closure newClosure(final MirBlock block, final Instance src) {
    final List<Operand> upvalues = new java.util.ArrayList<>();
    int argIndex = 0;
    for (final InstanceArgument arg : src.getArguments()) {
      switch (arg.getKind()) {
      case INSTANCE:
        upvalues.add(newClosure(block, arg.getInstance()));
        break;

      case PRIMITIVE:
        final PrimitiveAnd proto = src.getPrimitive();
        final Primitive param = getParameter(proto, argIndex);
        if (param.getKind().equals(Primitive.Kind.IMM)) {
          final Location loc =
            Location.createPrimitiveBased(arg.getName(), arg.getPrimitive());
          final Operand value = translateRead(block, loc);
          upvalues.add(value);
        } else {
          upvalues.add(block.getNamedLocal(arg.getName()));
        }
        break;

      case EXPR:
        upvalues.add(translate(block, arg.getExpr().getNode()));
        break;
      }
      ++argIndex;
    }
    return new Closure(src.getPrimitive().getName(), upvalues);
  }

  private static Primitive getParameter(final PrimitiveAnd p, final int index) {
    final Iterator<Primitive> it = p.getArguments().values().iterator();
    for (int i = 0; i < index; ++i) {
      it.next();
    }
    return it.next();
  }

  private void translate(final MirBlock ctx, final StatementAssignment s) {
    if (s.getLeft().isInternalVariable() || s.getRight().isInternalVariable()) {
      return;
    }
    final Operand operand = translate(ctx, s.getRight().getNode());
    translateAssignment(ctx, s.getLeft().getNode(), operand);
  }

  private void translateAssignment(final MirBlock ctx, final Node node, final Operand operand) {
    final Rvalue rhs = rvalueOf(operand);

    if (ExprUtils.isVariable(node)) {
      translateWrite(ctx, locationOf(node), rhs);
    } else if (ExprUtils.isOperation(node, StandardOperation.BVCONCAT)) {
      final int size = sizeOf(node);
      final List<Node> operands = operandsOf(node);
      final Local rvalue = ctx.assignLocal(rhs);

      int offset = size;
      for (final Node lhs : operands) {
        final int fieldSize = sizeOf(lhs);
        offset -= fieldSize;

        final Rvalue shr = BvOpcode.Lshr.make(rvalue, valueOf(offset, size));
        final Local field = ctx.extract(
            ctx.assignLocal(shr),
            fieldSize,
            Constant.zero(size),
            Constant.valueOf(size, fieldSize - 1));
        translateWrite(ctx, locationOf(lhs), rvalueOf(field));
      }
    }
  }

  private static List<Node> operandsOf(final Node node) {
    if (ExprUtils.isOperation(node)) {
      return ((NodeOperation) node).getOperands();
    }
    return Collections.emptyList();
  }

  private Operand translate(final MirBlock ctx, final Node node) {
    if (ExprUtils.isOperation(node)) {
      final TransRvalue visitor = new TransRvalue(ctx);
      new ExprTreeWalker(visitor).visit(node);

      return visitor.getResult();
    } else if (ExprUtils.isVariable(node)) {
      return translateRead(ctx, locationOf(node));
    } else if (ExprUtils.isValue(node)) {
      return newConstant(node);
    }
    throw new IllegalStateException();
  }

  private static Constant newConstant(final Node node) {
    final NodeValue value = (NodeValue) node;
    if (node.isType(DataType.INTEGER)) {
      // FIXME unsound int -> bv64 conversion
      return Constant.valueOf(64, value.getInteger());
    } else if (node.isType(DataType.BOOLEAN)) {
      // FIXME should be fixed on higher level
      return Constant.bitOf(value.getBoolean() ? 1 : 0);
    }
    final BitVector bv = value.getBitVector();
    return Constant.valueOf(bv.getBitSize(), bv.bigIntegerValue());
  }

  static BinOpcode mapOpcode(final NodeOperation node) {
    final Enum<?> e = node.getOperationId();
    if (OPCODE_MAPPING.containsKey(e)) {
      return OPCODE_MAPPING.get(e);
    }
    Logger.warning("missing opcode mapping: %s in '%s'", e, node.toString());
    return BvOpcode.Add;
  }

  private final class TransRvalue extends ExprTreeVisitorDefault {
    private final MirBlock ctx;
    private final Map<Node, Operand> mapped = new java.util.IdentityHashMap<>();
    private Operand result;

    public TransRvalue(final MirBlock ctx) {
      this.ctx = ctx;
    }

    public Operand getResult() {
      return result;
    }

    @Override
    public void onOperationEnd(final NodeOperation node) {
      if (!mapped.containsKey(node)) {
        final Operand local;
        if (node.getOperationId() instanceof StandardOperation) {
          switch ((StandardOperation) node.getOperationId()) {
          default:
            local = translateMapping(node);
            break;

          case BVCONCAT:
            local = translateConcat2(node);
            break;

          case BVREPEAT:
            local = translateRepeat(node);
            break;

          case BVSIGNEXT: {
            final int diff = ((NodeValue) node.getOperand(0)).getInteger().intValue();
            final Operand rhs = lookUp(node.getOperand(1));
            final Local lhs = ctx.newLocal(rhs.getType().getSize() + diff);
            ctx.append(new Sext(lhs, rhs));

            local = lhs;
            break;
          }

          case BVZEROEXT: {
            final int diff = ((NodeValue) node.getOperand(0)).getInteger().intValue();
            final Operand rhs = lookUp(node.getOperand(1));
            final Local lhs = ctx.newLocal(rhs.getType().getSize() + diff);
            ctx.append(new Zext(lhs, rhs));

            local = lhs;
            break;
          }

          case BVEXTRACT:
            final Node hiNode = node.getOperand(0);
            final Node loNode = node.getOperand(1);
            final Operand value = lookUp(node.getOperand(2));
            local =
                ctx.extract(value, evaluateBitSize(loNode, hiNode), lookUp(loNode), lookUp(hiNode));
            break;

          case ITE:
            local = translateIte(node);
            break;

          case BVLSHL:
          case BVLSHR:
          case BVASHL:
          case BVASHR:
            local = translateShift(node);
            break;

          case BVROR:
          case BVROL:
            local = translateRot(node);
            break;

          case BVNEG:
            local = translateNeg(node);
            break;
          }
        } else if (node.getOperationId().equals(Operator.CAST)) {
          local = lookUp(node.getOperand(1));
        } else {
          local = translateMapping(node);
        }
        mapped.put(node, local);

        result = local;
      }
    }

    private Local translateNeg(final NodeOperation node) {
      final Operand rhs = lookUp(node.getOperand(0));
      final Local not =
        ctx.assignLocal(BvOpcode.Xor.make(rhs, valueAssignable(-1, rhs)));
      return ctx.assignLocal(BvOpcode.Add.make(not, valueAssignable(1, not)));
    }

    private Local translateRepeat(final NodeOperation node) {
      final Node ntimes = node.getOperand(0);
      final Local lhs = ctx.newLocal(node.getDataType().getSize());
      if (ntimes.getKind() == Node.Kind.VALUE) {
        final int n = ((NodeValue) ntimes).getInteger().intValue();
        final Operand opnd = lookUp(node.getOperand(1));
        ctx.append(new Concat(lhs, Collections.nCopies(n, opnd)));
      } else {
        Logger.warning(
          "Unsupported expression form: non-numeral parameter in '%s', replace with '%s = %s 0'",
          node, lhs, lhs.getType());
        ctx.assign(lhs, valueAssignable(0, lhs));
      }
      return lhs;
    }

    private Local translateShift(final NodeOperation node) {
      // TODO actually verify it is the same (NML rotations has inverse arg order)
      return translateRot(node);
    }

    private Local translateRot(final NodeOperation node) {
      final Operand value = lookUp(node.getOperand(0));
      final Operand amount = lookUp(node.getOperand(1));

      final Local tmp = ctx.newLocal(value.getType());
      final Local lhs = ctx.newLocal(value.getType());
      ctx.append(new Zext(tmp, amount));
      ctx.assign(lhs, mapOpcode(node).make(value, tmp));

      return lhs;
    }

    private Local translateIte(final NodeOperation node) {
      final Operand guard = lookUp(node.getOperand(0));
      final Operand taken = lookUp(node.getOperand(1));
      final Operand other = lookUp(node.getOperand(2));

      final Local lhs = ctx.newLocal(taken.getType());
      ctx.append(new Conditional(lhs, guard, taken, other));

      return lhs;
    }
    private int evaluateBitSize(final Node loNode, final Node hiNode) {
      final Expr lo = new Expr(loNode);
      final Expr hi = new Expr(hiNode);

      if (lo.isConstant() && hi.isConstant()) {
        return Math.abs(hi.integerValue() - lo.integerValue()) + 1;
      }
      final ExprReducer.Reduced reducedLo = ExprReducer.reduce(lo);
      final ExprReducer.Reduced reducedHi = ExprReducer.reduce(hi);

      if (reducedHi.polynomial.equals(reducedLo.polynomial)) {
        return Math.abs(reducedHi.constant - reducedLo.constant) + 1;
      }
      throw new IllegalArgumentException();
    }

    private Operand translateMapping(final NodeOperation node) {
      final BinOpcode opc = mapOpcode(node);
      final Iterator<Node> it = node.getOperands().iterator();

      Operand op1 = lookUp(it.next());
      final MirTy type = opc.typeOf(op1, null);

      if (!it.hasNext()) {
        final Local lhs = ctx.newLocal(type);
        final Rvalue rhs = opc.make(op1, null);
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
      final BinOpcode opc = mapOpcode(node);
      final Iterator<Node> it = node.getOperands().iterator();

      Operand op1 = lookUp(it.next());
      final MirTy type = opc.typeOf(op1, null);

      if (!it.hasNext()) {
        return opc.make(op1, null);
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

    private Operand translateConcat2(final NodeOperation node) {
      final Local lhs = ctx.newLocal(node.getDataType().getSize());
      final List<Operand> rhs = new ArrayList<>();
      for (final Node operand : node.getOperands()) {
        rhs.add(lookUp(operand));
      }
      ctx.append(new Concat(lhs, rhs));

      return lhs;
    }

    private Operand translateConcat(final NodeOperation node) {
      final Iterator<Node> it = node.getOperands().iterator();
      final DataType type = node.getDataType();

      Operand expr = lookUp(it.next());
      while (it.hasNext()) {
        final Node op = it.next();

        final Rvalue shift =
          BvOpcode.Shl.make(expr, valueOf(sizeOf(op), sizeOf(type)));

        final Local zext = ctx.newLocal(type.getSize());
        ctx.append(new Zext(zext, lookUp(op)));

        final Rvalue bitor =
          BvOpcode.Or.make(ctx.assignLocal(shift), zext);

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
        return translateRead(ctx, locationOf(node));
      }
      throw new UnsupportedOperationException();
    }
  }

  private static Location locationOf(final Node node) {
    return (Location) ((NodeInfo) node.getUserData()).getSource();
  }

  private static Constant valueAssignable(final int value, final Operand target) {
    return valueOf(value, target.getType().getSize());
  }

  private static Constant valueOf(final long value, final int size) {
    return Constant.valueOf(size, BigInteger.valueOf(value));
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

  private Local translateRead(final MirBlock ctx, final Location l) {
    return translateAccess(ctx, l, new ReadAccess(ctx));
  }

  private void translateWrite(final MirBlock ctx, final Location l, final Rvalue value) {
    translateAccess(ctx, l, new WriteAccess(ctx, value));
  }

  private Local translateAccess(
      final MirBlock ctx,
      final Location l,
      final Accessor client) {
    final LocationSource source = l.getSource();
    final Access access = new Access(ctx, l);

    if (source instanceof LocationSourcePrimitive) {
      final Primitive p = ((LocationSourcePrimitive) source).getPrimitive();
      final String name = l.getName();

      final Local src;
      if (name.contains(".")) {
        src = extractField(ctx, name);
      } else {
        src = ctx.getNamedLocal(name);
      }
      final Local arg = ctx.getNamedLocal(l.getName());

      if (p.getKind() == Primitive.Kind.MODE) {
        return client.accessMode(src, access, p);
      } else {
        return client.accessLocal(src, access);
      }
    } else if (source instanceof LocationSourceMemory) {
      final MemoryResource mem = sourceToMemory(source);
      final Lvalue ref = new Static(mem.getName(), typeOf(mem));
      return client.accessMemory(ref, access);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  private Local extractField(final MirBlock block, final String pathname) {
    final List<String> path = Arrays.asList(pathname.split("\\."));

    final Iterator<String> it = path.iterator();
    Primitive p = this.source.getArguments().get(it.next());
    final List<Constant> indices = new java.util.ArrayList<>();
    while (it.hasNext()) {
      final String s = it.next();
      final PrimitiveAnd parent = (PrimitiveAnd) p;
      // FIXME why 32 bits?
      indices.add(Constant.valueOf(32, argumentIndex(s, parent)));

      p = parent.getArguments().get(s);
    }
    final Local target = block.newLocal(p.getReturnType().getBitSize());
    block.append(new Disclose(target, block.getNamedLocal(path.get(0)), indices));

    return target;
  }

  private static int argumentIndex(final String s, final PrimitiveAnd p) {
    int index = 0;
    for (final String name : p.getArguments().keySet()) {
      if (name.equals(s)) {
        return index;
      }
      ++index;
    }
    return -1;
  }

  static MirTy typeOf(final MemoryResource mem) {
    final MirTy type = new IntTy(mem.getType().getBitSize());
    final BigInteger length = mem.getSize();
    if (length.compareTo(BigInteger.ONE) > 0) {
      return new MirArray(length, new TyRef(type));
    }
    return type;
  }

  private static MemoryResource sourceToMemory(final LocationSource source) {
    return ((LocationSourceMemory) source).getMemory();
  }

  private static abstract class Accessor {
    protected final MirBlock ctx;

    Accessor(final MirBlock ctx) {
      this.ctx = ctx;
    }

    abstract Local accessMode(Local source, Access access, Primitive p);
    abstract Local accessLocal(Local source, Access access);
    abstract Local accessMemory(Lvalue source, Access access);

    protected Lvalue index(final Lvalue src, final Access access) {
      if (access.index != null) {
        return new Index(src, access.index);
      }
      return src;
    }

    protected Local extract(final Local src, final Access access) {
      if (access.lo != null) {
        return ctx.extract(src, access.size, access.lo, access.hi);
      }
      return src;
    }
  }

  private class Access {
    public final Local index;
    public final Operand lo;
    public final Operand hi;
    public final int size;

    public Access(final MirBlock ctx, final Location l) {
      if (l.getBitfield() != null) {
        final Location.Bitfield bits = l.getBitfield();
        this.lo = translate(ctx, bits.getTo().getNode());
        this.hi = translate(ctx, bits.getFrom().getNode());
        this.size = bits.getType().getBitSize();
      } else {
        this.lo = null;
        this.hi = null;
        this.size = l.getType().getBitSize();
      }

      BigInteger length = BigInteger.ZERO;
      if (l.getIndex() != null) {
        final MemoryResource mem = sourceToMemory(l.getSource());
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
    ReadAccess(final MirBlock ctx) {
      super(ctx);
    }

    @Override
    public Local accessLocal(final Local source, final Access access) {
      return extract(source, access);
    }

    @Override
    public Local accessMode(final Local source, final Access access, final Primitive p) {
      final Local value = ctx.newLocal(p.getReturnType().getBitSize());
      final String method = String.format("%s.read", p.getName());;
      ctx.append(new Call(source, method, Collections.<Operand>emptyList(), value));

      return accessLocal(value, access);
    }

    @Override
    public Local accessMemory(final Lvalue mem, final Access access) {
      final Lvalue source = index(mem, access);
      final Local target = ctx.newLocal(source.getType());
      ctx.append(new Load(source, target));

      return extract(target, access);
    }
  }

  private static class WriteAccess extends Accessor {
    final Rvalue value;

    WriteAccess(final MirBlock ctx, final Rvalue value) {
      super(ctx);
      this.value = value;
    }

    @Override
    public Local accessLocal(final Local target, final Access access) {
      /* NOTE write access to local is FORBIDDEN, skip */
      return target;
    }

    @Override
    public Local accessMode(final Local target, final Access access, final Primitive p) {
      final Local newval = ctx.newLocal(p.getReturnType().getBitSize());
      if (access.lo != null) {
        final Local oldval = ctx.newLocal(newval.getType());
        final String method = String.format("%s.read", p.getName());;
        ctx.append(new Call(target, method, Collections.<Operand>emptyList(), oldval));
        write(newval, oldval, access);
      } else {
        ctx.assign(newval, value);
      }

      final String method = String.format("%s.write", p.getName());
      final Call call =
        new Call(target, method, Collections.<Operand>singletonList(newval), null);
      ctx.append(call);

      return newval;
    }

    @Override
    public Local accessMemory(final Lvalue mem, final Access access) {
      final Lvalue target = index(mem, access);
      final Local newval = ctx.newLocal(target.getType());;
      if (access.lo != null) {
        final Local oldval = ctx.newLocal(target.getType());
        ctx.append(new Load(target, oldval));
        write(newval, oldval, access);
      } else {
        ctx.assign(newval, this.value);
      }
      ctx.append(new Store(target, newval));

      return null; // memory store, return nothing
    }

    private void write(final Local target, final Operand oldval, final Access access) {
      if (access.lo != null) {
        final Local zext = ctx.newLocal(target.getType());
        ctx.append(new Zext(zext, ctx.assignLocal(value)));

        final Rvalue rhs = shiftLeft(zext, access.lo);

        final Operand mask = createMask(target, access);
        final Rvalue clear = BvOpcode.And.make(oldval, mask);
        final Rvalue store =
          BvOpcode.Or.make(ctx.assignLocal(clear), ctx.assignLocal(rhs));

        ctx.assign(target, store);
      } else {
        ctx.assign(target, value);
      }
    }

    private Rvalue shiftLeft(final Operand value, final Operand amount) {
      final Operand n;
      if (amount.getType().getSize() < value.getType().getSize()) {
        final Local local = ctx.newLocal(value.getType());
        ctx.append(new Zext(local, amount));
        n = local;
      } else {
        n = amount;
      }
      return BvOpcode.Shl.make(value, n);
    }

    private Operand createMask(final Local source, final Access access) {
      final int nbits = source.getType().getSize();
      final Constant background = Constant.ones(nbits);
      final Constant region = Constant.ones(nbits, access.size);
      final Rvalue nmask = shiftLeft(region, access.lo);
      final Rvalue mask = BvOpcode.Xor.make(ctx.assignLocal(nmask), background);

      return ctx.assignLocal(mask);
    }
  }

  private List<MirBlock> translate(final MirBlock ctx, final StatementCondition s) {
    final List<MirBlock> blocks = new ArrayList<>();

    MirBlock current = ctx;
    for (int i = 0; i < s.getBlockCount(); ++i) {
      final Block block = new Block(s.getBlock(i));
      if (!block.isElseBlock()) {
        final Operand cond = translate(current, block.guard.getNode());
        final Pair<MirBlock, MirBlock> target = current.branch(cond);

        blocks.addAll(translate(target.first, block.body));

        current = target.second;
      } else {
        blocks.addAll(translate(current, block.body));
      }
    }
    if (!Block.isElseBlock(s.getBlock(s.getBlockCount() - 1))) {
      blocks.add(current);
    }
    return blocks;
  }

  private static final class Block {
    private final Expr guard;
    private final List<Statement> body;

    public Block(final Pair<Expr, List<Statement>> input) {
      this.guard = input.first;
      this.body = input.second;
    }

    public boolean isElseBlock() {
      return guard == null;
    }

    public static boolean isElseBlock(final Pair<Expr, List<Statement>> input) {
      return input.first == null;
    }
  }

  private static Rvalue rvalueOf(final Operand op) {
    return UnOpcode.Use.make(op);
  }

  static final Map<Enum<?>, BinOpcode> OPCODE_MAPPING =
    new java.util.LinkedHashMap<>();
  static final Map<BinOpcode, Enum<?>> MIR2NODE_MAP =
    new java.util.LinkedHashMap<>();

  static {
    OPCODE_MAPPING.put(StandardOperation.AND, BvOpcode.And);
    OPCODE_MAPPING.put(StandardOperation.OR, BvOpcode.Or);
    OPCODE_MAPPING.put(StandardOperation.NOT, new BinOpcode() {
      @Override
      public Rvalue make(final Operand lhs, final Operand rhs) {
        return BvOpcode.Xor.make(lhs, Constant.bitOf(1));
      }

      @Override
      public MirTy typeOf(final Operand lhs, final Operand rhs) {
        return new IntTy(1);
      }
    });

    OPCODE_MAPPING.put(StandardOperation.EQ, CmpOpcode.Eq);
    OPCODE_MAPPING.put(StandardOperation.NOTEQ, CmpOpcode.Ne);
    OPCODE_MAPPING.put(StandardOperation.BVULT, CmpOpcode.Ult);
    OPCODE_MAPPING.put(StandardOperation.BVUGT, CmpOpcode.Ugt);
    OPCODE_MAPPING.put(StandardOperation.BVULE, CmpOpcode.Ule);
    OPCODE_MAPPING.put(StandardOperation.BVUGE, CmpOpcode.Uge);
    OPCODE_MAPPING.put(StandardOperation.BVSLT, CmpOpcode.Slt);
    OPCODE_MAPPING.put(StandardOperation.BVSGT, CmpOpcode.Sgt);
    OPCODE_MAPPING.put(StandardOperation.BVSLE, CmpOpcode.Sle);
    OPCODE_MAPPING.put(StandardOperation.BVSGE, CmpOpcode.Sge);

    OPCODE_MAPPING.put(StandardOperation.BVADD, BvOpcode.Add);
    OPCODE_MAPPING.put(StandardOperation.BVSUB, BvOpcode.Sub);
    OPCODE_MAPPING.put(StandardOperation.BVMUL, BvOpcode.Mul);
    OPCODE_MAPPING.put(StandardOperation.BVXOR, BvOpcode.Xor);
    OPCODE_MAPPING.put(StandardOperation.BVAND, BvOpcode.And);
    OPCODE_MAPPING.put(StandardOperation.BVOR, BvOpcode.Or);
    OPCODE_MAPPING.put(StandardOperation.BVUDIV, BvOpcode.Udiv);
    OPCODE_MAPPING.put(StandardOperation.BVSDIV, BvOpcode.Sdiv);
    OPCODE_MAPPING.put(StandardOperation.BVUREM, BvOpcode.Urem);
    OPCODE_MAPPING.put(StandardOperation.BVSREM, BvOpcode.Srem);
    OPCODE_MAPPING.put(StandardOperation.BVNOT, new BinOpcode() {
      @Override
      public Rvalue make(final Operand lhs, final Operand rhs) {
        return BvOpcode.Xor.make(lhs, valueOf(-1, lhs.getType().getSize()));
      }

      @Override
      public MirTy typeOf(final Operand lhs, final Operand rhs) {
        return BvOpcode.Xor.typeOf(lhs, rhs);
      }
    });

    OPCODE_MAPPING.put(StandardOperation.BVLSHL, BvOpcode.Shl);
    OPCODE_MAPPING.put(StandardOperation.BVASHL, BvOpcode.Shl);
    OPCODE_MAPPING.put(StandardOperation.BVASHR, BvOpcode.Ashr);
    OPCODE_MAPPING.put(StandardOperation.BVLSHR, BvOpcode.Lshr);
    OPCODE_MAPPING.put(StandardOperation.BVROR, BvOpcode.Rotr);
    OPCODE_MAPPING.put(StandardOperation.BVROL, BvOpcode.Rotl);

    for (final Map.Entry<Enum<?>, BinOpcode> entry : OPCODE_MAPPING.entrySet()) {
      final BinOpcode opc = entry.getValue();
      if (opc instanceof BvOpcode || opc instanceof CmpOpcode) {
        MIR2NODE_MAP.put(opc, entry.getKey());
      }
    }
  }
}
