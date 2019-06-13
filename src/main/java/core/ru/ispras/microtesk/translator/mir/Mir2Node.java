package ru.ispras.microtesk.translator.mir;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.Nodes;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import static ru.ispras.microtesk.translator.mir.GlobalNumbering.*;

public class Mir2Node extends Pass {
  private List<Node> nodes;

  public List<Node> getFormulae() {
    return nodes;
  }

  @Override
  public MirContext apply(final MirContext mir) {
    final Insn2Node visitor = new Insn2Node();
    for (final BasicBlock bb : mir.blocks) {
      for (final Instruction insn : bb.insns) {
        insn.accept(visitor);
      }
    }
    this.nodes = visitor.nodes;

    return mir;
  }

  private static class Insn2Node extends InsnVisitor {
    public static final NodeValue BIT_ONE = NodeValue.newBitVector(1, 1);
    public static final NodeValue BIT_ZERO = NodeValue.newBitVector(0, 1);

    private final List<Node> nodes = new java.util.ArrayList<>();
    private final OperandWalker<Node> opnd2Node =
      new OperandWalker<>(new Opnd2Node());

    private Node dispatch(final Operand opnd) {
      return opnd2Node.dispatch(opnd);
    }

    private List<Node> dispatchAll(final List<Operand> opnds) {
      return opnd2Node.dispatchAll(opnds);
    }

    private void assign(final Operand lhs, final Node value) {
      nodes.add(Nodes.eq(dispatch(lhs), value));
    }

    public void visit(final Assignment insn) {
      final Enum<?> opc = NmlIrTrans.MIR2NODE_MAP.get(insn.opc);
      final Node op = new NodeOperation(opc, dispatch(insn.op1), dispatch(insn.op2));
      if (insn.opc instanceof CmpOpcode) {
        assign(insn.lhs, Nodes.ite(op, BIT_ONE, BIT_ZERO));
      } else {
        assign(insn.lhs, op);
      }
    }

    public void visit(final Concat insn) {
      assign(insn.lhs, Nodes.bvconcat(dispatchAll(insn.rhs)));
    }

    public void visit(final Extract insn) {
      if (insn.hi instanceof Constant && insn.lo instanceof Constant) {
        final int hi = ((Constant) insn.hi).getValue().intValue();
        final int lo = ((Constant) insn.lo).getValue().intValue();
        assign(insn.lhs, Nodes.bvextract(Math.max(hi, lo), Math.min(hi, lo), dispatch(insn.rhs)));
      } else {
        final Node amount =
          Nodes.bvzeroext(sizeOf(insn.rhs) - sizeOf(insn.lo), dispatch(insn.lo));
        final Node shr = Nodes.bvlshr(dispatch(insn.rhs), amount);
        assign(insn.lhs, Nodes.bvextract(sizeOf(insn.lhs) - 1, 0, shr));
      }
    }

    public void visit(final Sext insn) {
      final int diff = sizeOf(insn.lhs) - sizeOf(insn.rhs);
      assign(insn.lhs, Nodes.bvsignext(diff, dispatch(insn.rhs)));
    }

    public void visit(final Zext insn) {
      final int diff = sizeOf(insn.lhs) - sizeOf(insn.rhs);
      assign(insn.lhs, Nodes.bvzeroext(diff, dispatch(insn.rhs)));
    }

    public void visit(final Load insn) {
      assign(insn.target, dispatch(insn.source));
    }

    public void visit(final Store insn) {
      assign(insn.target, dispatch(insn.source));
    }

    public void visit(final Phi insn) {
      assign(insn.target, dispatch(insn.value));
    }

    public void visit(final SsaStore insn) {
      if (insn.target.getType() instanceof MirArray) {
        // TODO have to walk recursively
        final NodeOperation select = (NodeOperation) dispatch(insn.origin.target);
        final Node array = select.getOperand(0);

        Node key = select.getOperand(1);
        if (ExprUtils.isValue(key)) {
          final MirArray atype = (MirArray) insn.target.getType();
          final int indexBits = atype.indexBitLength();
          key = NodeValue.newBitVector(((NodeValue) key).getBitVector().resize(indexBits, false)); 
        }
        assign(
          insn.target,
          Nodes.store(array, key, dispatch(insn.origin.source)));
      } else {
        assign(insn.target, dispatch(insn.origin.source));
      }
    }
 }

  private static class Opnd2Node extends OperandVisitor<Node> {
    @Override
    public Node visitOperand(final Operand opnd) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Node visitConst(final Constant opnd) {
      return NodeValue.newBitVector(opnd.getValue(), sizeOf(opnd));
    }

    @Override
    public Node visitLocal(final Local opnd) {
      return NodeVariable.newBitVector(opnd.toString(), sizeOf(opnd));
    }

    @Override
    public Node visitStatic(final Static opnd) {
      return new NodeVariable(opnd.toString(), typeOf(opnd.getType()));
    }

    @Override
    public Node visitIndex(final Index opnd, final Node base, final Node index) {
      return Nodes.select(base, index);
    }

    @Override
    public Node visitIte(final Ite opnd, final Node guard, final Node taken, final Node other) {
      return Nodes.ite(Nodes.eq(guard, Insn2Node.BIT_ONE), taken, other);
    }
  }

  private static DataType typeOf(final MirTy type) {
    if (type instanceof IntTy) {
      return DataType.bitVector(type.getSize());
    }
    if (type instanceof MirArray) {
      final MirArray atype = (MirArray) type;
      final int indexBits = atype.indexBitLength();
      return DataType.map(DataType.bitVector(indexBits), typeOf(atype.ref.type));
    }
    return DataType.UNKNOWN;
  }

  private static int sizeOf(final Operand opnd) {
    return opnd.getType().getSize();
  }
}
