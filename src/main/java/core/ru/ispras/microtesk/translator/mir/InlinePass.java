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
    final int nblocks = ctx.blocks.size();
    for (int j = 0; j < nblocks; ++j) {
      final BasicBlock bb = ctx.blocks.get(j);
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
      rebase(caller.locals.size(), callee.blocks);
      caller.locals.addAll(Pass.tailList(callee.locals, 1));

      linkForward(new MirBlock(caller, target), callsite, callee);
      linkBack(next, callsite, callee);
    }

    private BasicBlock splitCallSite() {
      final int index = target.insns.indexOf(callsite);
      final List<Instruction> tail = Pass.tailList(target.insns, index + 1);

      final BasicBlock bb = new BasicBlock();
      bb.insns.addAll(tail);
      caller.blocks.add(bb);
      caller.blocks.addAll(callee.blocks);

      tail.clear();
      target.insns.remove(index);

      return bb;
    }

    private static void linkForward(final MirBlock bb, final Call call, final MirContext callee) {
      final BasicBlock entry = callee.blocks.get(0);
      final int origin = entry.origin;

      for (int i = 0; i < call.args.size(); ++i) {
        bb.assign(bb.getLocal(origin + i + 1), call.args.get(i));
      }
      bb.jump(entry);
    }

    private static void linkBack(final BasicBlock next, final Call call, final MirContext callee) {
      for (final BasicBlock bb : callee.blocks) {
        final int index = bb.insns.size() - 1;
        final Instruction insn = bb.insns.get(index);
        if (insn instanceof Return) {
          final Return ret = (Return) insn;
          bb.insns.remove(index);

          if (call.ret != null) {
            final Local lhs = new Local(call.ret.id - bb.origin, call.ret.getType());
            bb.insns.add(new Assignment(lhs, UnOpcode.Use.make(ret.value)));
          }
          bb.insns.add(new Branch(next));
        }
      }
    }

    private static void rebase(final int base, final Collection<BasicBlock> blocks) {
      for (final BasicBlock bb : blocks) {
        bb.origin += base;
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
