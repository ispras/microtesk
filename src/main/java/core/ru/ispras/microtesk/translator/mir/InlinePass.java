package ru.ispras.microtesk.translator.mir;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class InlinePass extends Pass {
  public InlinePass(final Map<String, MirContext> storage) {
    super(storage);
  }

  @Override
  public MirContext apply(final MirContext src) {
    final MirContext ctx = Pass.copyOf(src);
    final Queue<BasicBlock> queue = new java.util.ArrayDeque<>(ctx.blocks);
    while (!queue.isEmpty()) {
      final BasicBlock bb = queue.remove();
      final Call call = find(bb.insns, Call.class);
      if (call != null && this.result.containsKey(call.method)) {
        final Inliner inliner =
          new Inliner(call, bb, ctx, this.result.get(call.method));
        final BasicBlock newbb = inliner.run();
        queue.add(newbb);
      }
    }
    return ctx;
  }

  private static <T> T find(final Collection<? super T> source, final Class<T> cls) {
    for (final Object o : source) {
      if (cls.isInstance(o)) {
        return cls.cast(o);
      }
    }
    return null;
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

    public BasicBlock run() {
      final BasicBlock next = splitCallSite();
      rebase(caller.locals.size(), callee.blocks);
      caller.locals.addAll(Pass.tailList(callee.locals, 1));

      linkForward(new MirBlock(caller, target), callsite, callee);
      linkBack(next, callsite, callee);

      return next;
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
