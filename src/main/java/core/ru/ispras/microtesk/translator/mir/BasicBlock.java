package ru.ispras.microtesk.translator.mir;

import java.util.List;

public final class BasicBlock {
  public int origin = 0;
  public final List<Instruction> insns = new java.util.ArrayList<>();
}
