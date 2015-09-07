package ru.ispras.microtesk.model.api.memory;

import static ru.ispras.fortress.util.InvariantChecks.checkBounds;
import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.math.BigInteger;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.type.Type;

final class AliasForLocation extends Memory {
  private final Location source;

  public AliasForLocation(
      final Kind kind,
      final String name,
      final Type type,
      final BigInteger length,
      final Location source) {
    super(kind, name, type, length, true);
    checkNotNull(source);

    final int totalBitSize = type.getBitSize() * length.intValue();
    if (source.getBitSize() != totalBitSize) {
      throw new IllegalArgumentException();
    }

    this.source = source;
  }

  @Override
  public Location access(final int index) {
    checkBounds(index, getLength().intValue());

    final int locationBitSize = getType().getBitSize();
    final int start = locationBitSize * index;
    final int end = start + locationBitSize - 1;

    final Location bitField = source.bitField(start, end);
    return bitField.castTo(getType().getTypeId());
  }

  @Override
  public Location access(final long address) {
    return access((int) address);
  }

  @Override
  public Location access(final BigInteger address) {
    return access(address.intValue());
  }

  @Override
  public Location access(final Data address) {
    return access(address.getRawData().intValue());
  }

  @Override
  public void reset() {
    // Does not work for aliases (and should not be called)
  }

  @Override
  public void setUseTempCopy(boolean value) {
    // Do nothing.
  }
}
