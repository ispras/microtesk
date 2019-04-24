package ru.ispras.microtesk.translator.mir;

import java.util.Iterator;
import java.util.List;

public class InsnRewriter extends InsnVisitor {
  private final Frame frame;
  private final List<BasicBlock> blocks = new java.util.ArrayList<>();

  private MirBlock block;
  private int nskip;

  public static void rewrite(final MirContext ctx) {
    final InsnRewriter worker = new InsnRewriter(EvalContext.eval(ctx));

    final int nblocks = ctx.blocks.size();
    for (int i = 0; i < nblocks; ++i) {
      worker.blocks.add(new BasicBlock());
    }
    for (int i = 0; i < nblocks; ++i) {
      worker.block = new MirBlock(ctx, worker.blocks.get(i));
      worker.nskip = 0;
      for (final Instruction insn : ctx.blocks.get(i).insns) {
        insn.accept(worker);
      }
    }

    final int nlocals = ctx.locals.size();
    final Iterator<MirTy> it = localsOf(ctx).iterator();
    for (int i = ctx.getSignature().params.size() + 1; i < nlocals; ++i) {
      it.next();
      if (!worker.isAlive(i)) {
        it.remove();
      }
    }
    ctx.blocks.clear();
    ctx.blocks.addAll(worker.blocks);
  }

  private static List<MirTy> localsOf(final MirContext ctx) {
    return Pass.tailList(ctx.locals, ctx.getSignature().params.size() + 1);
  }

  private InsnRewriter(final Frame frame) {
    this.frame = frame;
  }

  private Operand rewrite(final Operand source) {
    for (final Local opnd = cast(source, Local.class); opnd != null; ) {
      final int index = indexOf(opnd);
      return (isAlive(index))
          ? rebase(opnd)
          : offsetDirect(frame.locals.get(index));
    }
    for (final Index opnd = cast(source, Index.class); opnd != null; ) {
      return new Index((Lvalue) rewrite(opnd.base), rewrite(opnd.index));
    }
    for (final Field opnd = cast(source, Field.class); opnd != null; ) {
      return new Field((Lvalue) rewrite(opnd.base), opnd.name);
    }
    for (final Closure opnd = cast(source, Closure.class); opnd != null; ) {
      return new Closure(opnd.callee, rewriteAll(opnd.upvalues));
    }
    return source;
  }

  private static <T> T cast(final Object o, final Class<T> cls) {
    if (cls.isInstance(o)) {
      return cls.cast(o);
    }
    return null;
  }

  private List<Operand> rewriteAll(final List<Operand> opnds) {
    final List<Operand> ret = new java.util.ArrayList<>();
    for (final Operand opnd : opnds) {
      ret.add(rewrite(opnd));
    }
    return ret;
  }

  private <T extends Operand> T rebase(final T source) {
    if (source instanceof Local) {
      return (T) new Local(offsetIndex(indexOf(source)), source.getType());
    }
    return source;
  }

  private Operand offsetDirect(final Operand source) {
    for (final Local local = cast(source, Local.class); local != null; ) {
      return new Local(offsetIndex(local.id), local.getType());
    }
    return source;
  }

  private int offsetIndex(final int index) {
    int offset = 0;
    for (int i = 0; i < index; ++i) {
      offset += (isAlive(i)) ? 0 : -1;
    }
    return index + offset;
  }

  private int indexOf(final Operand opnd) {
    final Local local = (Local) opnd;
    final BasicBlock bb = getBlockOrigin(block.bb);

    return local.id + bb.getOrigin(block.bb.insns.size() + nskip);
  }

  private BasicBlock getBlockOrigin(final BasicBlock bb) {
    return block.ctx.blocks.get(blocks.indexOf(bb));
  }

  private BasicBlock getBlockImage(final BasicBlock bb) {
    return blocks.get(block.ctx.blocks.indexOf(bb));
  }

  private boolean isAlive(final Operand opnd) {
    for (final Local local = cast(opnd, Local.class); local != null; ) {
      return isAlive(indexOf(local));
    }
    return true;
  }

  private boolean isAlive(final int index) {
    return frame.locals.get(index).equals(VoidTy.VALUE);
  }

  @Override
  public void visit(final Assignment insn) {
    if (isAlive(insn.lhs)) {
      final Operand op1 = rewrite(insn.op1);
      final Operand op2 = rewrite(insn.op2);
      block.assign(rebase(insn.lhs), insn.opc.make(op1, op2));
    } else {
      ++nskip;
    }
  }

  @Override
  public void visit(final Concat insn) {
    block.append(new Concat(rebase(insn.lhs), rewriteAll(insn.rhs)));
  }

  @Override
  public void visit(final Extract insn) {
    block.append(new Extract(rebase(insn.lhs), rewrite(insn.rhs), rewrite(insn.lo), rewrite(insn.hi)));
  }

  public void visit(final Sext insn) {
    block.append(new Sext(rebase(insn.lhs), rewrite(insn.rhs)));
  }

  public void visit(final Zext insn) {
    block.append(new Zext(rebase(insn.lhs), rewrite(insn.rhs)));
  }

  public void visit(final Branch insn) {
    if (insn.successors.size() == 1) {
      block.append(new Branch(getBlockImage(insn.other)));
    } else {
      final Operand guard = rewrite(insn.guard);
      block.append(new Branch(guard, getBlockImage(insn.target.get(1)), getBlockImage(insn.other)));
    }
  }

  public void visit(final Return insn) {
    block.append(new Return(rewrite(insn.value)));
  }

  public void visit(final Exception insn) {
    block.append(insn); // TODO
  }

  public void visit(final Call insn) {
    block.call(rewrite(insn.callee), insn.method, rewriteAll(insn.args), (Local) rebase(insn.ret));
  }

  public void visit(final Invoke insn) {
    block.append(insn); // TODO
  }

  public void visit(final Load insn) {
    block.append(new Load((Lvalue) rewrite(insn.source), (Local) rebase(insn.target))); // FIXME
  }

  public void visit(final Store insn) {
    block.append(new Store((Lvalue) rewrite(insn.target), rewrite(insn.source))); // FIXME
  }

  public void visit(final Disclose insn) {
    if (isAlive(insn.target)) {
      block.append(new Disclose((Local) rebase(insn.target), rewrite(insn.source), insn.indices));
    } else {
      ++nskip;
    }
  }
}
