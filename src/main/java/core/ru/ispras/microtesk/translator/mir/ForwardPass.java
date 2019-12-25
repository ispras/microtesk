package ru.ispras.microtesk.translator.mir;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;

public class ForwardPass extends Pass {
  private Map<String, BigInteger> presets = Collections.emptyMap();

  public ForwardPass initValues(final Map<String, BigInteger> valueMap) {
    this.presets = new java.util.HashMap<>(valueMap);

    return this;
  }

  @Override
  public MirContext apply(final MirContext source) {
    final MirContext ctx = Pass.copyOf(source);
    InsnRewriter.rewrite(ctx, this.presets);

    return ctx;
  }
}
