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

import java.util.List;

import static ru.ispras.microtesk.translator.mir.GlobalNumbering.Ite;
import static ru.ispras.microtesk.translator.mir.GlobalNumbering.Phi;
import static ru.ispras.microtesk.translator.mir.Instruction.*;

class OperandWalker<T> extends InsnVisitor {
  private final OperandVisitor<T> visitor;

  public OperandWalker(final OperandVisitor<T> visitor) {
    this.visitor = visitor;
  }

  public final T dispatch(final Operand opnd) {
    if (opnd instanceof Field) {
      final Field field = (Field) opnd;
      return visitor.visitField(field, dispatch(field.base));
    }
    if (opnd instanceof Index) {
      final Index index = (Index) opnd;
      return visitor.visitIndex(index, dispatch(index.base), dispatch(index.index));
    }
    if (opnd instanceof Closure) {
      final Closure c = (Closure) opnd;
      return visitor.visitClosure(c, dispatchAll(c.upvalues));
    }
    if (opnd instanceof Local) {
      return visitor.visitLocal((Local) opnd);
    }
    if (opnd instanceof Constant) {
      return visitor.visitConst((Constant) opnd);
    }
    if (opnd instanceof Static) {
      return visitor.visitStatic((Static) opnd);
    }
    if (opnd instanceof Ite) {
      final Ite ite = (Ite) opnd;
      return visitor.visitIte(
        ite, dispatch(ite.guard), dispatch(ite.taken), dispatch(ite.other));
    }
    return visitor.visitOperand(opnd);
  }

  public final List<T> dispatchAll(final List<Operand> operands) {
    final List<T> values = new java.util.ArrayList<>(operands.size());
    for (final Operand opnd : operands) {
      values.add(dispatch(opnd));
    }
    return values;
  }

  @Override
  public void visit(final Conditional insn) {
    dispatch(insn.guard);
    dispatch(insn.taken);
    dispatch(insn.other);
    visitor.visitLhs(insn.lhs);
  }

  public void visit(final Assignment insn) {
    dispatch(insn.op1);
    dispatch(insn.op2);
    visitor.visitLhs(insn.lhs);
  }

  public void visit(final Concat insn) {
    dispatchAll(insn.rhs);
    visitor.visitLhs(insn.lhs);
  }

  public void visit(final Extract insn) {
    dispatch(insn.rhs);
    dispatch(insn.lo);
    dispatch(insn.hi);
    visitor.visitLhs(insn.lhs);
  }

  public void visit(final Sext insn) {
    dispatch(insn.rhs);
    visitor.visitLhs(insn.lhs);
  }

  public void visit(final Zext insn) {
    dispatch(insn.rhs);
    visitor.visitLhs(insn.lhs);
  }

  public void visit(final Branch insn) {
    dispatch(insn.guard);
  }

  public void visit(final Return insn) {
    dispatch(insn.value);
  }

  public void visit(final Instruction.Exception insn) { /* TODO */ }

  public void visit(final Call insn) {
    dispatchAll(insn.args);
    dispatch(insn.callee);
    visitor.visitLhs(insn.ret);
  }

  public void visit(final Invoke insn) {
    visit(insn.call);
  }

  public void visit(final Load insn) {
    dispatch(insn.source);
    visitor.visitLhs(insn.target);
  }

  public void visit(final Store insn) {
    dispatch(insn.source);
    dispatch(insn.target);
  }

  public void visit(final Disclose insn) {
    dispatch(insn.source);
    visitor.visitLhs(insn.target);
  }

  public void visit(final Phi insn) {
    if (insn.value != null) {
      dispatch(insn.value);
    }
    visitor.visitLvalue(insn.target);
  }

  public void visit(final GlobalNumbering.SsaStore insn) {
    visit(insn.origin);
    visitor.visitLvalue(insn.target);
  }
}
