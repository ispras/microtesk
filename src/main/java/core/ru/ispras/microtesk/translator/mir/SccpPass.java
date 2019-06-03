package ru.ispras.microtesk.translator.mir;

public class SccpPass extends Pass {
  @Override
  public MirContext apply(final MirContext source) {
    final MirContext ctx = Pass.copyOf(source);
    final SparseCCP sccp = new SparseCCP();

    for (final BasicBlock bb : ctx.blocks) {
      for (final Instruction insn : bb.insns) {
        insn.accept(sccp);
      }
    }
    return ctx;
  }
}
