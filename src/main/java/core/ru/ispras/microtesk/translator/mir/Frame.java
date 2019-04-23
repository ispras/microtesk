package ru.ispras.microtesk.translator.mir;

import java.util.List;
import java.util.Map;

public final class Frame {
  public final Map<String, Operand> globals;
  public final List<Operand> locals = new java.util.ArrayList<>();

  public Frame(final Map<String, Operand> globals) {
    this.globals = globals;
  }
}
