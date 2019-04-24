package ru.ispras.microtesk.translator.mir;

public class ForwardPass extends Pass {
  @Override
  public MirContext apply(final MirContext source) {
    final MirContext ctx = Pass.copyOf(source);
    ctx.blocks.clear();
    ctx.blocks.addAll(InsnRewriter.rewrite(source));

    return ctx;
  }
}
