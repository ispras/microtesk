package ru.ispras.microtesk.translator.mir;

import java.util.List;
import java.util.Map;

public abstract class Pass {
  protected final Map<String, MirContext> source;
  protected final Map<String, MirContext> result = new java.util.HashMap<>();

  public Pass(final Map<String, MirContext> source) {
    this.source = source;
  }

  public abstract MirContext apply(MirContext ctx);

  protected static MirContext copyOf(final MirContext src) {
    final FuncTy signature = src.getSignature();

    final MirContext ctx = new MirContext(src.name, signature);
    ctx.locals.addAll(tailList(src.locals, signature.params.size()));

    for (final BasicBlock bb : src.blocks) {
      final BasicBlock bbCopy = new BasicBlock();
      bbCopy.insns.addAll(bb.insns);
      ctx.blocks.add(bbCopy);
    }
    for (final BasicBlock bb : ctx.blocks) {
      final int index = bb.insns.size() - 1;
      final Instruction insn = bb.insns.get(index);
      if (insn instanceof Branch) {
        final Branch br = (Branch) insn;
        if (br.successors.size() == 1) {
          bb.insns.set(index, new Branch(retargetBlock(br.successors.get(0), src, ctx)));
        } else {
          final BasicBlock taken = retargetBlock(br.successors.get(0), src, ctx);
          final BasicBlock other = retargetBlock(br.successors.get(1), src, ctx);

          bb.insns.set(index, new Branch(br.guard, taken, other));
        }
      }
    }
    return ctx;
  }

  private static BasicBlock retargetBlock(
      final BasicBlock bb,
      final MirContext src,
      final MirContext dst) {
    return dst.blocks.get(src.blocks.indexOf(bb));
  }

  static <T> List<T> tailList(final List<T> list, final int index) {
    return list.subList(index, list.size());
  }
}
