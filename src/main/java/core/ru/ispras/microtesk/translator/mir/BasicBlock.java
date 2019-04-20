package ru.ispras.microtesk.translator.mir;

import java.util.List;

public final class BasicBlock {
  public final List<Instruction> insns = new java.util.ArrayList<>();
  public final List<Origin> origins = new java.util.ArrayList<>();

  public BasicBlock() {
    this.origins.add(new Origin(0, 0));
  }

  public static BasicBlock copyOf(final BasicBlock bb) {
    final BasicBlock copy = new BasicBlock();

    copy.insns.addAll(bb.insns);
    copy.origins.clear();
    for (final Origin org : bb.origins) {
      copy.origins.add(new Origin(org.range, org.value));
    }
    return copy;
  }

  public int getOrigin(final int index) {
    for (int i = origins.size() - 1; i >= 0; --i) {
      final Origin org = origins.get(i);
      if (index >= org.range) {
        return org.value;
      }
    }
    throw new IllegalStateException();
  }

  public static final class Origin {
    public int range;
    public int value;

    public Origin(int range, int value) {
      this.range = range;
      this.value = value;
    }
  }
}
