package ru.ispras.microtesk.translator.simnml.coverage;

import java.util.Collection;
import java.util.Collections;

public final class SsaForm {
  private final Block entry;
  private final Block exit;
  private final Collection<Block> blocks;

  SsaForm(Block entry, Block exit, Collection<Block> blocks) {
    this.entry = entry;
    this.exit = exit;
    this.blocks = blocks;
  }

  public Block getEntryPoint() {
    return entry;
  }

  public Collection<Block> getExitPoints() {
    return Collections.singleton(exit);
  }

  public Collection<Block> getBlocks() {
    return blocks;
  }
}
