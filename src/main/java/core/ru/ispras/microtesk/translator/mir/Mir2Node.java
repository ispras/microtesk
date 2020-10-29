/*
    Copyright 2019 ISP RAS (http://www.ispras.ru)

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

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.Nodes;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static ru.ispras.microtesk.translator.mir.Instruction.*;
import static ru.ispras.microtesk.translator.mir.GlobalNumbering.*;

public class Mir2Node extends Pass {
  static final NodeValue BIT_ONE = NodeValue.newBitVector(1, 1);
  static final NodeValue BIT_ZERO = NodeValue.newBitVector(0, 1);

  private final Map<String, Integer> versionBase = new java.util.HashMap<>();
  private final Map<String, Integer> versionMax = new java.util.HashMap<>();
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
    versionBase.putAll(versionMax);
    this.nodes = visitor.nodes;

    return mir;
  }

  private class Insn2Node extends InsnVisitor {
    private final List<Node> nodes = new java.util.ArrayList<>();
    private final OperandWalker<Node> opnd2Node =
        new OperandWalker<>(new Opnd2Node(versionBase, versionMax));

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
      final Node op;
      if (opc == StandardOperation.BVROR || opc == StandardOperation.BVROL) {
        op = rotate(opc, insn.op1, insn.op2);
      } else {
        op = new NodeOperation(opc, dispatch(insn.op1), dispatch(insn.op2));
      }
      if (insn.opc instanceof CmpOpcode) {
        assign(insn.lhs, Nodes.ite(op, BIT_ONE, BIT_ZERO));
      } else {
        assign(insn.lhs, op);
      }
    }

    private NodeOperation rotate(Enum<?> opc, Operand op1, Operand op2) {
      final Node value = dispatch(op1);
      final Node amount = dispatch(op2);
      if (op2 instanceof Constant) {
        return new NodeOperation(opc, amount, value);
      }
      final Node nbits = NodeValue.newBitVector(sizeOf(op2), sizeOf(op2));
      final Node modAmount = Nodes.bvurem(amount, nbits);
      final Node invAmount = Nodes.bvsub(nbits, modAmount);
      if (opc == StandardOperation.BVROR) {
        return Nodes.bvor(Nodes.bvlshr(value, modAmount), Nodes.bvlshl(value, invAmount));
      } else {
        return Nodes.bvor(Nodes.bvlshl(value, modAmount), Nodes.bvlshr(value, invAmount));
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

    @Override
    public void visit(final Conditional insn) {
      final Node guard = dispatch(insn.guard);
      final Node taken = dispatch(insn.taken);
      final Node other = dispatch(insn.other);
      assign(insn.lhs, Nodes.ite(Nodes.eq(guard, BIT_ONE), taken, other));
    }
  }

  private static class Opnd2Node extends OperandVisitor<Node> {
    final Map<String, Integer> versionBase;
    final Map<String, Integer> versionMax;

    Opnd2Node(
        final Map<String, Integer> versionBase,
        final Map<String, Integer> versionMap) {
      this.versionBase = versionBase;
      this.versionMax = versionMap;
    }

    private int rebase(final String name, final int ver, final int offset) {
      final Integer base = versionBase.get(name);
      final int newver = ver + ((base != null) ? base + offset : 0);

      final Integer max = versionMax.get(name);
      final int newmax = Math.max(newver, (max != null) ? max : 1);
      versionMax.put(name, newmax);

      return newver;
    }

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
      final int newver = rebase("%", opnd.id, 0);
      final String varname = String.format("%%%d", newver);
      return NodeVariable.newBitVector(varname, sizeOf(opnd));
    }

    @Override
    public Node visitStatic(final Static opnd) {
      // FIXME explicitely request base version increment
      final int newver = rebase(opnd.name, opnd.version, 0);
      final String varname = String.format("%s!%d", opnd.name, newver);
      return new NodeVariable(varname, typeOf(opnd.getType()));
    }

    @Override
    public Node visitIndex(final Index opnd, final Node base, final Node index) {
      return Nodes.select(base, index);
    }

    @Override
    public Node visitIte(final Ite opnd, final Node guard, final Node taken, final Node other) {
      return Nodes.ite(Nodes.eq(guard, BIT_ONE), taken, other);
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

  public static String stringOf(final MirTy type) {
    if (type instanceof IntTy) {
      return String.format("(_ BitVec %d)", type.getSize());
    }
    if (type instanceof MirArray) {
      final MirArray atype = (MirArray) type;
      final int indexBits = atype.indexBitLength();
      return String.format("(Array (_ BitVec %d) %s)",
          indexBits, stringOf(atype.ref.type));
    }
    return "Int";
  }

  private static int sizeOf(final Operand opnd) {
    return opnd.getType().getSize();
  }
}
