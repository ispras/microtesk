package ru.ispras.microtesk.translator.mir;

import java.util.List;
import java.util.Map;

public final class Frame {
  public final Map<String, List<Operand>> globals;
  public final List<Operand> locals = new java.util.ArrayList<>();

  public Frame(final Map<String, List<Operand>> globals) {
    this.globals = globals;
  }

  public Operand get(final String name, final int version) {
    final List<Operand> variants = globals.get(name);
    if (variants != null && variants.size() >= version) {
      return variants.get(version - 1);
    }
    return VoidTy.VALUE;
  }
}
