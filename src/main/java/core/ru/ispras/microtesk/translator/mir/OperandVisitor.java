package ru.ispras.microtesk.translator.mir;

import java.util.List;

abstract class OperandVisitor<T> {
  public T visitConst(Constant opnd) { return visitOperand(opnd); }
  public T visitLvalue(Lvalue opnd) { return visitOperand(opnd); }
  public T visitLocal(Local opnd) { return visitOperand(opnd); }
  public T visitField(Field opnd, T base) { return visitOperand(opnd); }
  public T visitIndex(Index opnd, T base, T index) { return visitOperand(opnd); }
  public T visitStatic(Static opnd) { return visitOperand(opnd); }
  public T visitClosure(Closure opnd, List<T> upvalues) { return visitOperand(opnd); }

  public abstract T visitOperand(Operand opnd);
}

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
    return visitor.visitOperand(opnd);
  }

  public final List<T> dispatchAll(final List<Operand> operands) {
    final List<T> values = new java.util.ArrayList<>(operands.size());
    for (final Operand opnd : operands) {
      values.add(dispatch(opnd));
    }
    return values;
  }

  public void visit(final Assignment insn) {
    dispatch(insn.op1);
    dispatch(insn.op2);
    visitor.visitLvalue((Lvalue) insn.lhs);
  }

  public void visit(final Concat insn) {
    dispatchAll(insn.rhs);
    visitor.visitLvalue((Lvalue) insn.lhs);
  }

  public void visit(final Extract insn) {
    dispatch(insn.rhs);
    dispatch(insn.lo);
    dispatch(insn.hi);
    visitor.visitLvalue((Lvalue) insn.lhs);
  }

  public void visit(final Sext insn) {
    dispatch(insn.rhs);
    visitor.visitLvalue((Lvalue) insn.lhs);
  }

  public void visit(final Zext insn) {
    dispatch(insn.rhs);
    visitor.visitLvalue((Lvalue) insn.lhs);
  }

  public void visit(final Branch insn) {
    dispatch(insn.guard);
  }

  public void visit(final Return insn) {
    dispatch(insn.value);
  }

  public void visit(final Exception insn) { /* TODO */ }

  public void visit(final Call insn) {
    dispatchAll(insn.args);
    dispatch(insn.callee);
    visitor.visitLvalue((Lvalue) insn.ret);
  }

  public void visit(final Invoke insn) {
    visit(insn.call);
  }

  public void visit(final Load insn) {
    dispatch(insn.source);
    visitor.visitLvalue((Lvalue) insn.target);
  }

  public void visit(final Store insn) {
    dispatch(insn.source);
    dispatch(insn.target);
  }

  public void visit(final Disclose insn) {
    dispatch(insn.source);
    visitor.visitLvalue((Lvalue) insn.target);
  }

  public void visit(final GlobalNumbering.Phi insn) { /* TODO */ }

  public void visit(final GlobalNumbering.SsaStore insn) {
    visit(insn.origin);
    visitor.visitLvalue(insn.target);
  }
}
