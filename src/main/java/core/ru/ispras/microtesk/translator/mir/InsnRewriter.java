package ru.ispras.microtesk.translator.mir;

import java.util.List;

public class InsnRewriter extends InsnVisitor {
  private final Frame frame;
  private final List<BasicBlock> blocks = new java.util.ArrayList<>();

  private MirBlock block;

  public static List<BasicBlock> rewrite(final MirContext ctx) {
    final InsnRewriter worker = new InsnRewriter(EvalContext.eval(ctx));

    final int nblocks = ctx.blocks.size();
    for (int i = 0; i < nblocks; ++i) {
      worker.blocks.add(new BasicBlock());
    }
    for (int i = 0; i < nblocks; ++i) {
      worker.block = new MirBlock(ctx, worker.blocks.get(i));
      for (final Instruction insn : ctx.blocks.get(i).insns) {
        insn.accept(worker);
      }
    }
    return worker.blocks;
  }

  private InsnRewriter(final Frame frame) {
    this.frame = frame;
  }

  private Operand rewrite(final Operand source) {
    if (source instanceof Local) {
      return rebase((Local) source);
    }
    return source;
  }

  private List<Operand> rewriteAll(final List<Operand> opnds) {
    final List<Operand> ret = new java.util.ArrayList<>();
    for (final Operand opnd : opnds) {
      ret.add(rewrite(opnd));
    }
    return ret;
  }

  private Lvalue rebase(final Lvalue source) {
    if (source instanceof Local) {
      final Local local = (Local) source;
      final BasicBlock bb = getBlockOrigin(block.bb);

      return new Local(local.id + bb.getOrigin(block.bb.insns.size()), local.getType());
    }
    return source;
  }

  private BasicBlock getBlockOrigin(final BasicBlock bb) {
    return block.ctx.blocks.get(blocks.indexOf(bb));
  }

  private BasicBlock getBlockImage(final BasicBlock bb) {
    return blocks.get(block.ctx.blocks.indexOf(bb));
  }

  @Override
  public void visit(final Assignment insn) {
    final Operand op1 = rewrite(insn.op1);
    final Operand op2 = rewrite(insn.op2);
    block.assign(rebase(insn.lhs), insn.opc.make(op1, op2));
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
    block.append(new Disclose((Local) rebase(insn.target), rewrite(insn.source), insn.indices));
  }
}
