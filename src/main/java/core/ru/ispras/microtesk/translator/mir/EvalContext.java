package ru.ispras.microtesk.translator.mir;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class EvalContext extends InsnVisitor {
  private final Map<String, Operand> globals;
  private final Frame frame;

  private int origin = 0;

  public static EvalContext eval(final MirContext mir) {
    final Map<String, Operand> globals = new java.util.HashMap<>();
    return new EvalContext(globals).evalInternal(mir);
  }

  EvalContext() {
    this(new java.util.HashMap<String, Operand>());
  }

  private EvalContext(final Map<String, Operand> globals) {
    this.globals = globals;
    this.frame = new Frame(globals);
  }

  private EvalContext evalInternal(final MirContext mir) {
    eval(mir.locals.size(), mir.blocks);
    return this;
  }

  Frame getFrame() {
    return frame;
  }

  Frame eval(final int nlocals, final Collection<BasicBlock> blocks) {
    final int n = nlocals - frame.locals.size();
    if (n > 0) {
      frame.locals.addAll(Collections.nCopies(n, VoidTy.VALUE));
    }
    for (final BasicBlock bb : blocks) {
      int i = 0;
      for (final Instruction insn : bb.insns) {
        this.origin = bb.getOrigin(i++);
        insn.accept(this);
      }
    }
    return frame;
  }

  @Override
  public void visit(final Assignment insn) {
    if (insn.opc.equals(UnOpcode.Use)) {
      setLocal(indexOf(insn.lhs), getValueRec(insn.op1));
    }
  }

  @Override
  public void visit(final Disclose insn) {
    int i = 0;
    Operand opnd = getValueRec(insn.source);
    while (i < insn.indices.size() && opnd instanceof Closure) {
      final BigInteger index = insn.indices.get(i++).getValue();
      opnd = getValueDirect(((Closure) opnd).upvalues.get(index.intValue()));
    }
    if (i >= insn.indices.size()) {
      setLocal(indexOf(insn.target), opnd);
    }
  }

  private Operand getValueRec(final Operand opnd) {
    if (opnd instanceof Closure) {
      final Closure closure = (Closure) opnd;
      final List<Operand> upvalues = new java.util.ArrayList<>(closure.upvalues.size());
      for (final Operand upval : closure.upvalues) {
        upvalues.add(getValueRec(upval));
      }
      return new Closure(closure.callee, upvalues);
    } else if (opnd instanceof Local) {
      final int index = indexOf(opnd);
      final Operand value = getLocal(index);
      if (value.equals(VoidTy.VALUE)) {
        return new Local(index, opnd.getType());
      }
      return value;
    }
    return opnd;
  }

  private Operand getValueDirect(final Operand opnd) {
    if (opnd instanceof Local) {
      final Local local = (Local) opnd;
      final Operand value = getLocal(local.id);
      return (value.equals(VoidTy.VALUE)) ? local : value;
    }
    return opnd;
  }

  private int indexOf(final Operand opnd) {
    return ((Local) opnd).id + this.origin;
  }

  Operand getLocal(final int index) {
    return frame.locals.get(index);
  }

  private void setLocal(final int index, final Operand value) {
    frame.locals.set(index, value);
  }

  private static <T> T cast(final Object o, final Class<T> cls) {
    if (cls.isInstance(o)) {
      return cls.cast(o);
    }
    return null;
  }
}
