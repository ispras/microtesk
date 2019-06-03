package ru.ispras.microtesk.translator.mir;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;

public class ForwardPass extends Pass {
  private final Map<String, BigInteger> presets;

  public ForwardPass() {
    this(Collections.<String, BigInteger>emptyMap());
  }

  public ForwardPass(final Map<String, BigInteger> presets) {
    this.presets = presets;
  }

  @Override
  public MirContext apply(final MirContext source) {
    final MirContext ctx = Pass.copyOf(source);
    InsnRewriter.rewrite(ctx, this.presets);

    return ctx;
  }
}
