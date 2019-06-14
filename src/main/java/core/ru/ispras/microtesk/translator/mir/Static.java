package ru.ispras.microtesk.translator.mir;

public class Static extends Lvalue {
  public final String name;
  public final int version;
  private final MirTy type;

  public Static(final String name, final MirTy type) {
    this(name, 0, type);
  }

  public Static(String name, int version, MirTy type) {
    this.name = name;
    this.version = version;
    this.type = type;
  }

  public boolean isSame(final Operand opnd) {
    if (opnd instanceof Static) {
      return this.name.equals(((Static) opnd).name);
    }
    return false;
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof Static) {
      final Static that = (Static) o;
      return this.name.equals(that.name) && this.version == that.version;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return name.hashCode() * 31 + version;
  }

  @Override
  public MirTy getType() {
    return type;
  }

  @Override
  public MirTy getContainerType() {
    return getType();
  }

  @Override
  public String toString() {
    return (version > 0) ? String.format("%s!%d", name, version) : name;
  }

  public Static newVersion(final int n) {
    return new Static(name, n, type);
  }
}
