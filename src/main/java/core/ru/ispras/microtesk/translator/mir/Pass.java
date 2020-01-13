package ru.ispras.microtesk.translator.mir;

import java.util.List;
import java.util.Map;

import static ru.ispras.microtesk.translator.mir.Instruction.Branch;

public abstract class Pass {
  private String comment = "";

  Map<String, MirContext> storage;
  final Map<String, MirContext> result = new java.util.HashMap<>();

  public abstract MirContext apply(MirContext ctx);

  public Pass setComment(final String s) {
    this.comment = s;
    return this;
  }

  public String getComment() {
    return this.comment;
  }

  protected MirContext resolveCallee(final String name) {
    return storage.get(name);
  }

  static MirContext copyOf(final MirContext src) {
    return copyOf(src, src.name);
  }

  static MirContext copyOf(final MirContext src, final String name) {
    return inlineContext(new MirContext(name, src.getSignature()), src);
  }

  public static MirContext inlineContext(final MirContext dst, final MirContext src) {
    dst.locals.addAll(Lists.tailOf(src.locals, src.getSignature().params.size() + 1));

    final List<BasicBlock> body = new java.util.ArrayList<>();
    for (final BasicBlock bb : src.blocks) {
      body.add(BasicBlock.copyOf(bb));
    }
    dst.blocks.addAll(body);

    for (final BasicBlock bb : body) {
      final int index = bb.insns.size() - 1;
      final Instruction insn = bb.insns.get(index);
      if (insn instanceof Branch) {
        final Branch br = (Branch) insn;
        if (br.successors.size() == 1) {
          bb.insns.set(index, new Branch(retargetBlock(br.successors.get(0), src.blocks, body)));
        } else {
          final BasicBlock taken = retargetBlock(br.successors.get(0), src.blocks, body);
          final BasicBlock other = retargetBlock(br.successors.get(1), src.blocks, body);

          bb.insns.set(index, new Branch(br.guard, taken, other));
        }
      }
    }
    return dst;
  }

  private static BasicBlock retargetBlock(
      final BasicBlock bb,
      final List<BasicBlock> origins,
      final List<BasicBlock> storage) {
    return storage.get(origins.indexOf(bb));
  }
}
