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
    final Frame frame = EvalContext.propagatePhi(ctx, Map.of()).getFrame();
    final StoreVisitor visitor = new StoreVisitor();
    final VersionVisitor indexer = new VersionVisitor();

    for (final BasicBlock bb : EvalContext.topologicalOrder(ctx)) {
      for (final Instruction insn : bb.insns) {
        insn.accept(visitor);
        insn.accept(indexer.walker);
      }
    }
    this.analysisFrame = frame;
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
    Operand ref = analysisFrame.get(name, version);
    while (ref instanceof Local) {
      var local = (Local) ref;
      var value = analysisFrame.locals.get(local.id);
      if (value == VoidTy.VALUE) {
        break;
      }
      ref = value;
    }
    return ref;
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
    private final Map<String, Static> lastAssigned = new java.util.HashMap<>();
    
    @Override
    public void visit(final Store insn) {
      if (insn.target instanceof Static) {
        assign((Static) insn.target);
      }
    }

    @Override
    public void visit(final SsaStore insn) {
      assign(insn.targetDef);
    }

    @Override
    public void visit(final Phi insn) {
      assign(insn.target);
    }

    private void assign(final Static mem) {
      lastAssigned.put(mem.name, mem);
    }
  }
}
