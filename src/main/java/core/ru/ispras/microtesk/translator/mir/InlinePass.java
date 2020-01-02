package ru.ispras.microtesk.translator.mir;

import ru.ispras.castle.util.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static ru.ispras.microtesk.translator.mir.Instruction.*;

public class InlinePass extends Pass {
  @Override
  public MirContext apply(final MirContext src) {
    Logger.debug("PASS: inline '%s'", src.name);
    final MirContext ctx = Pass.copyOf(src);
    final EvalContext evaluator = new EvalContext();
    final List<BasicBlock> blocks = EvalContext.topologicalOrder(ctx);

    int evalIndex = 0;
    for (int i = 0; i < blocks.size(); ++i) {
      final BasicBlock bb = blocks.get(i);
      for (List<Instruction> tail = find(bb.insns, Call.class);
          !tail.isEmpty();
          tail = find(Lists.tailOf(tail, 1), Call.class)) {
        final Call call = (Call) tail.get(0);
        final int origin = bb.getOrigin(bb.insns.indexOf(call));

        evaluator.eval(ctx.locals.size(), blocks.subList(evalIndex, i + 1));
        evalIndex = i + 1;

        final MirContext callee = resolveCallee(ctx, call, origin, evaluator);
        if (callee != null) {
          Logger.debug("PASS: inline call '%s'", callee.name);
          final Inliner inliner = new Inliner(call, bb, ctx, callee);
          final Collection<BasicBlock> newbb = inliner.run();
          blocks.addAll(i + 1, newbb);
          break;
        }
      }
    }
    return ctx;
  }

  protected MirContext resolveCallee(
      final MirContext mir,
      final Call call,
      final int origin,
      final EvalContext evaluator) {
    final String calleeName;
    if (call.callee instanceof Local) {
      final Local callee = (Local) call.callee;
      calleeName = resolveCalleeName(evaluator.getLocal(callee.id + origin), call.method);
    } else {
      calleeName = resolveCalleeName(call.callee, call.method);
    }
    return resolveCallee(calleeName);
  }

  public static String resolveCalleeName(final Call call) {
    return resolveCalleeName(call.callee, call.method);
  }

  private static String resolveCalleeName(final Operand callee, final String methodName) {
    if (callee instanceof Closure) {
      final Closure closure = (Closure) callee;
      return methodName.replaceFirst("\\w+", closure.callee);
    }
    return methodName;
  }

  private static <T> List<T> find(final List<T> source, final Class<? extends T> cls) {
    for (int i = 0; i < source.size(); ++i) {
      if (cls.isInstance(source.get(i))) {
        return Lists.tailOf(source, i);
      }
    }
    return Collections.emptyList();
  }

  private static final class Inliner {
    public final int callOrg;
    public final Call callsite;
    public final BasicBlock target;
    public final MirContext caller;
    public final MirContext callee;

    public Inliner(
        final Call callsite,
        final BasicBlock target,
        final MirContext caller,
        final MirContext callee) {
      this.callOrg = target.getOrigin(target.insns.indexOf(callsite));
      this.callsite = callsite;
      this.target = target;
      this.caller = caller;
      this.callee = Pass.copyOf(callee);
    }

    public Collection<BasicBlock> run() {
      final BasicBlock next = splitCallSite();
      rebase(caller.locals.size() - 1, callee.blocks);
      caller.locals.addAll(Lists.tailOf(callee.locals, 1));

      final List<BasicBlock> blocks = EvalContext.topologicalOrder(callee);

      final BasicBlock entry = linkForward();
      linkBack(next, callsite, callee);

      blocks.add(0, entry);
      blocks.add(next);
      return blocks;
    }

    private BasicBlock splitCallSite() {
      final int index = target.insns.indexOf(callsite);
      final List<Instruction> insnView = Lists.tailOf(target.insns, index + 1);
      final List<BasicBlock.Origin> orgView = getOutrangedOrigins(target, index);

      final BasicBlock bb = new BasicBlock();
      bb.origins.get(0).value = target.getOrigin(index + 1);
      for (final BasicBlock.Origin org : orgView) {
        org.range -= index + 1;
      }

      move(bb.origins, orgView);
      move(bb.insns, insnView);
      target.insns.remove(index);

      caller.blocks.add(bb);
      caller.blocks.addAll(callee.blocks);

      return bb;
    }

    private static <T> void move(final Collection<T> dst, final Collection<? extends T> src) {
      dst.addAll(src);
      src.clear();
    }

    private static List<BasicBlock.Origin> getOutrangedOrigins(final BasicBlock bb, final int index) {
      for (int i = 0; i < bb.origins.size(); ++i) {
        final BasicBlock.Origin org = bb.origins.get(i);
        if (org.range > index + 1) {
          return Lists.tailOf(bb.origins, i);
        }
      }
      return Collections.emptyList();
    }

    private BasicBlock linkForward() {
      final MirBlock source = new MirBlock(this.caller, this.target);
      final Call call = this.callsite;

      final MirBlock bb = source.ctx.newBlock();
      bb.bb.origins.get(0).value = this.callOrg;
      source.jump(bb);

      final BasicBlock entry = callee.blocks.get(0);
      final int origin = entry.getOrigin(0) - this.callOrg;

      final int nparams = callee.getSignature().params.size();
      final int nargs = call.args.size();
      final int nclosed = nparams - nargs;

      for (int i = 0; i < nclosed; ++i) {
        final int index = origin + i + 1;
        bb.disclose(bb.getLocal(index), call.callee, i);
      }
      for (int i = 0; i < nargs; ++i) {
        final int index = origin + nclosed + i + 1;
        bb.assign(bb.getLocal(index), call.args.get(i));
      }
      bb.jump(entry);

      return bb.bb;
    }

    private void linkBack(final BasicBlock next, final Call call, final MirContext callee) {
      for (final BasicBlock bb : callee.blocks) {
        final int index = bb.insns.size() - 1;
        final Instruction insn = bb.insns.get(index);
        if (insn instanceof Return) {
          final Return ret = (Return) insn;
          bb.insns.remove(index);

          if (call.ret != null) {
            final Local lhs = new Local(call.ret.id + this.callOrg - bb.getOrigin(index), call.ret.getType());
            bb.insns.add(new Assignment(lhs, UnOpcode.Use.make(ret.value)));
          }
          bb.insns.add(new Branch(next));
        }
      }
    }

    private static void rebase(final int base, final Collection<BasicBlock> blocks) {
      for (final BasicBlock bb : blocks) {
        for (final BasicBlock.Origin org : bb.origins) {
          org.value += base;
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
