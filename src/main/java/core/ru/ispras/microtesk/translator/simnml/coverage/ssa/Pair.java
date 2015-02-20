package ru.ispras.microtesk.translator.simnml.coverage.ssa;

final class Pair<T, U> {
  final T first;
  final U second;

  public Pair(T first, U second) {
    this.first = first;
    this.second = second;
  }

  @Override
  public boolean equals(Object object) {
    if (object == null) {
      return false;
    }
    if (object == this) {
      return true;
    }
    if (object instanceof Pair) {
      final Pair rhs = (Pair) object;
      return this.first != null &&
             rhs.first != null &&
             this.first.equals(rhs.first) &&
             this.second != null &&
             rhs.second != null &&
             this.second.equals(rhs.second);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return 31 * first.hashCode() + second.hashCode();
  }

  @Override
  public String toString() {
    return String.format("<%s, %s>", first, second);
  }
}
