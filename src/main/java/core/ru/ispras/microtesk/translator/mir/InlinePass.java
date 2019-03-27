package ru.ispras.microtesk.translator.mir;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class InlinePass extends Pass {
  public InlinePass(final Map<String, MirContext> storage) {
    super(storage);
  }

  @Override
  public MirContext apply(final MirContext src) {
    final MirContext ctx = Pass.copyOf(src);
    for (final BasicBlock bb : ctx.blocks) {
      for (int i = 0; i < bb.insns.size(); ++i) {
        final Instruction insn = bb.insns.get(i);
        if (insn instanceof Call) {
          final Call call = (Call) insn;
          if (this.result.containsKey(call.method)) {
            final Inliner inliner =
              new Inliner(call, bb, ctx, this.result.get(call.method));
            inliner.run();
          }
        }
      }
    }
    return ctx;
  }

  private static final class Inliner {
    public final Call callsite;
    public final BasicBlock target;
    public final MirContext caller;
    public final MirContext callee;

    public Inliner(
        final Call callsite,
        final BasicBlock target,
        final MirContext caller,
        final MirContext callee) {
      this.callsite = callsite;
      this.target = target;
      this.caller = caller;
      this.callee = Pass.copyOf(callee);
    }

    public void run() {
      final BasicBlock next = splitCallSite();
      linkEntry(this.target, callee);
      linkReturn(next, callsite, callee);
      rebase(caller.locals.size(), callee.blocks);
    }

    private BasicBlock splitCallSite() {
      final int index = target.insns.indexOf(callsite);
      final List<Instruction> tail = Pass.tailList(target.insns, index + 1);

      final BasicBlock bb = new BasicBlock();
      bb.insns.addAll(tail);

      tail.clear();
      target.insns.remove(index);

      return bb;
    }

    private static void linkEntry(final BasicBlock bb, final MirContext callee) {
      bb.insns.add(new Branch(callee.blocks.get(0)));
      // TODO link arguments
    }

    private static void linkReturn(final BasicBlock next, final Call call, final MirContext callee) {
      for (final BasicBlock bb : callee.blocks) {
        final int index = bb.insns.size() - 1;
        final Instruction insn = bb.insns.get(index);
        if (insn instanceof Return) {
          final Return ret = (Return) insn;
          bb.insns.remove(index);

          if (call.ret != null) {
            bb.insns.add(new Assignment(call.ret, UnOpcode.Use.make(ret.value)));
          }
          bb.insns.add(new Branch(next));
        }
      }
    }

    private static void rebase(final int base, final Collection<BasicBlock> blocks) {
      final RebaseOp visitor = new RebaseOp(base);
      for (final BasicBlock bb : blocks) {
        visitor.index = 0;
        visitor.bb = bb;

        for (final Instruction insn : bb.insns) {
          insn.accept(visitor);
          visitor.index++;
        }
      }
    }
  }

  private static final class RebaseOp extends InsnVisitor {
    public final int base;
    public int index = 0;
    public BasicBlock bb = null;

    public RebaseOp(final int base) {
      this.base = base;
    }

    @Override
    public void visit(final Assignment insn) {
      bb.insns.set(index,
        new Assignment(rebase(insn.lhs), insn.opc.make(rebase(insn.op1), rebase(insn.op2))));
    }

    private <T extends Operand> T rebase(final T opnd) {
      if (opnd instanceof Local) {
        final Local lval = (Local) opnd;
        return (T) new Local(this.base + lval.id, lval.getType());
      }
      if (opnd instanceof Index) {
        final Index lval = (Index) opnd;
        return (T) new Index(rebase(lval.base), rebase(lval.index));
      }
      if (opnd instanceof Field) {
        final Field lval = (Field) opnd;
        return (T) new Field(rebase(lval.base), lval.name);
      }
      return opnd;
    }
  }
}
