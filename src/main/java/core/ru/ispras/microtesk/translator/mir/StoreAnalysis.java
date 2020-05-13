package ru.ispras.microtesk.translator.mir;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static ru.ispras.microtesk.translator.mir.Instruction.Store;
import static ru.ispras.microtesk.translator.mir.GlobalNumbering.*;

public class StoreAnalysis extends Pass {
  private Frame analysisFrame;
  private Map<String, Static> lastAssigned;
  private Map<String, Static> lastVersioned;

  @Override
  public MirContext apply(final MirContext ctx) {
    final StoreVisitor visitor = new StoreVisitor();
    final VersionVisitor indexer = new VersionVisitor();

    for (final BasicBlock bb : EvalContext.topologicalOrder(ctx)) {
      for (final Instruction insn : bb.insns) {
        insn.accept(visitor);
        insn.accept(indexer.walker);
      }
    }
    this.analysisFrame = visitor.frame;
    this.lastAssigned = visitor.lastAssigned;
    this.lastVersioned = indexer.lastVersioned;

    return ctx;
  }

  public Map<String, Static> modifiedMap() {
    return Collections.unmodifiableMap(lastAssigned);
  }

  public Map<String, Static> versionMap() {
    return Collections.unmodifiableMap(lastVersioned);
  }

  public Collection<Operand> getOutputValues(final String name) {
    final Operand value = lastValueOf(name);
    if (value instanceof Ite) {
      final Ite ite = (Ite) value;
      return Arrays.asList(ite.taken, ite.other);
    }
    return Collections.singleton(value);
  }

  private Operand lastValueOf(final String name) {
    final int version = lastAssigned.get(name).version;
    return analysisFrame.get(name, version);
  }

  public Operand getCondition(final String name) {
    final Operand value = lastValueOf(name);
    if (value instanceof Ite) {
      return ((Ite) value).guard;
    }
    return Constant.bitOf(1);
  }

  private static final class VersionVisitor extends OperandVisitor<Void> {
    private final Map<String, Static> lastVersioned = new java.util.HashMap<>();
    private final OperandWalker<Void> walker = new OperandWalker<>(this);

    @Override
    public Void visitOperand(final Operand opnd) { return null; }

    @Override
    public Void visitStatic(final Static opnd) {
      final Static mem = lastVersioned.get(opnd.name);
      if (mem == null || opnd.version > mem.version) {
        lastVersioned.put(opnd.name, opnd);
      }
      return null;
    }

    @Override
    public Void visitLvalue(final Lvalue opnd) {
      return walker.dispatch(opnd);
    }
  }

  private static final class StoreVisitor extends InsnVisitor {
    private final Frame frame = new Frame();
    private final Map<String, Static> lastAssigned = new java.util.HashMap<>();
    
    @Override
    public void visit(final Store insn) {
      final Static mem = (Static) insn.target;
      if (!Types.isArray(mem) && mem.version > 0) {
        store(mem, insn.source);
      }
      assign(mem);
    }

    @Override
    public void visit(final SsaStore insn) {
      if (!Types.isArray(insn.target)) {
        store(insn.target, insn.origin.source);
      }
      assign(insn.target);
    }

    @Override
    public void visit(final Phi insn) {
      if (!Types.isArray(insn.target) && insn.values != null) {
        store(insn.target, insn.value);
      }
      assign(insn.target);
    }

    private void assign(final Static mem) {
      lastAssigned.put(mem.name, mem);
    }

    private void store(final Static mem, final Operand val) {
      frame.set(mem.name, mem.version, getValueRec(val));
    }

    private Operand getValueRec(final Operand opnd) {
      if (opnd instanceof Static) {
        final Static mem = (Static) opnd;
        final Operand value = frame.get(mem.name, mem.version);
        return (value.equals(VoidTy.VALUE)) ? opnd : value;
      }
      return opnd;
    }
  }
}
