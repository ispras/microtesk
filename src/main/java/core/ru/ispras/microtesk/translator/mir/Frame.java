package ru.ispras.microtesk.translator.mir;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class Frame {
  public final Map<String, List<Operand>> globals;
  public final List<Operand> locals = new java.util.ArrayList<>();

  public Frame() {
    this.globals = new java.util.HashMap<>();
  }

  public Frame(final Map<String, List<Operand>> globals) {
    this.globals = globals;
  }

  public Operand get(final String name, final int version) {
    final List<Operand> variants = getValues(name);
    if (variants.size() >= version) {
      return variants.get(version - 1);
    }
    return VoidTy.VALUE;
  }

  List<Operand> getValues(final String name) {
    return globals.getOrDefault(name, Collections.emptyList());
  }

  void set(final String name, final int version, final Operand value) {
    final List<Operand> values =
        globals.computeIfAbsent(name, k -> new java.util.ArrayList<>());
    final int ndiff = version - values.size();
    if (ndiff > 0) {
      values.addAll(Collections.nCopies(ndiff, VoidTy.VALUE));
    }
    values.set(version - 1, value);
  }
}
