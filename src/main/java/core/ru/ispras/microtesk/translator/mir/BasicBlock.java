package ru.ispras.microtesk.translator.mir;

import java.util.Collections;
import java.util.List;

public final class BasicBlock {
  private final List<Instruction> insns;

  public BasicBlock(final List<Instruction> insns) {
    this.insns = insns;
  }
}
