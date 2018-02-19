/*
 * Copyright 2014-2018 ISP RAS (http://www.ispras.ru)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.model.memory;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.data.Data;
import ru.ispras.microtesk.model.data.Type;

import java.math.BigInteger;

public abstract class Memory {
  private final Kind kind;
  private final String name;
  private final Type type;
  private final BigInteger length;
  private final boolean isAlias;
  private final int addressBitSize;

  public static enum Kind {
    REG, MEM, VAR
  }

  public static Memory def(
      final Kind kind,
      final String name,
      final Type type,
      final long length) {
    return def(kind, name, type, BigInteger.valueOf(length));
  }

  public static Memory def(
      final Kind kind,
      final String name,
      final Type type,
      final BigInteger length) {
    switch (kind) {
      case MEM:
        return new PhysicalMemory(name, type, length);

      case REG:
        return new RegisterFile(name, type, length);

      case VAR:
        return new VariableArray(name, type, length);
    }

    throw new IllegalArgumentException("Unknown memory kind: " + kind);
  }

  public static Memory def(
      final Kind kind,
      final String name,
      final Type type,
      final long length,
      final Location alias) {
    return def(kind, name, type, BigInteger.valueOf(length), alias);
  }

  public static Memory def(
      final Kind kind,
      final String name,
      final Type type,
      final BigInteger length,
      final Location alias) {
    if (null == alias) {
      return def(kind, name, type, length);
    }
    return new AliasForLocation(kind, name, type, length, alias);
  }

  public static Memory def(
      final Kind kind,
      final String name,
      final Type type,
      final long length,
      final Memory memory,
      final int min,
      final int max) {
    return def(kind, name, type, BigInteger.valueOf(length), memory, min, max);
  }

  public static Memory def(
      final Kind kind,
      final String name,
      final Type type,
      final BigInteger length,
      final Memory memory,
      final int min,
      final int max) {
    return new AliasForMemory(kind, name, type, length, memory, min, max);
  }

  protected Memory(
      final Kind kind,
      final String name,
      final Type type,
      final BigInteger length,
      final boolean isAlias) {
    InvariantChecks.checkNotNull(kind);
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkGreaterThan(length, BigInteger.ZERO);

    this.kind = kind;
    this.name = name;
    this.type = type;
    this.length = length;
    this.isAlias = isAlias;
    this.addressBitSize = MemoryStorage.calculateAddressSize(length);
  }

  protected Memory(final Memory other) {
    InvariantChecks.checkNotNull(other);

    this.kind = other.kind;
    this.name = other.name;
    this.type = other.type;
    this.length = other.length;
    this.isAlias = other.isAlias;
    this.addressBitSize = other.addressBitSize;
  }

  protected static int getIndexBitSize(final int count) {
    InvariantChecks.checkGreaterOrEqZero(count);

    if (count <= 1) {
      return 1;
    }

    final int result = 31 - Integer.numberOfLeadingZeros(count);
    return result == Integer.numberOfTrailingZeros(count) ? result : result + 1;
  }

  public void initAllocator(
      final int addressableUnitBitSize, final BigInteger baseAddress) {
    throw new UnsupportedOperationException(
        "initAllocator is not supported for " + toString());
  }

  public MemoryAllocator getAllocator() {
    throw new UnsupportedOperationException(
        "getAllocator is not supported for " + toString());
  }

  public MemoryDevice setHandler(final MemoryDevice handler) {
    throw new UnsupportedOperationException(
        "setHandler is not supported for " + toString());
  }

  public final Kind getKind() {
    return kind;
  }

  public final String getName() {
    return name;
  }

  public final Type getType() {
    return type;
  }

  public final BigInteger getLength() {
    return length;
  }

  public final boolean isAlias() {
    return isAlias;
  }

  public final int getAddressBitSize() {
    return addressBitSize;
  }

  public final Location access() {
    return access(0);
  }

  public abstract Location access(int address);

  public abstract Location access(long address);

  public abstract Location access(BigInteger address);

  public abstract Location access(Data address);

  public abstract Memory copy();

  public abstract void reset();

  @Override
  public String toString() {
    return String.format(
        "%s %s[%d, %s], alias=%b",
        kind.name().toLowerCase(),
        name,
        length,
        type,
        isAlias
        );
  }
}
