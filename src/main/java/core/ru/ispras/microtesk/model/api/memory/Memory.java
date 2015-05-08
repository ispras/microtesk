/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.memory;

import static ru.ispras.fortress.util.InvariantChecks.checkBounds;
import static ru.ispras.fortress.util.InvariantChecks.checkGreaterThanZero;
import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.model.api.metadata.MetaLocationStore;
import ru.ispras.microtesk.model.api.type.Type;

public abstract class Memory {

  public static enum Kind {
    REG, MEM, VAR
  }

  public static Memory REG(String name, Type type, int length) {
    return REG(name, type, length, null);
  }

  public static Memory REG(String name, Type type, int length, Location alias) {
    return newMemory(Kind.REG, name, type, length, alias);
  }

  public static Memory MEM(String name, Type type, int length) {
    return MEM(name, type, length, null);
  }

  public static Memory MEM(String name, Type type, int length, Location alias) {
    return newMemory(Kind.MEM, name, type, length, alias);
  }

  public static Memory VAR(String name, Type type, int length) {
    return VAR(name, type, length, null);
  }

  public static Memory VAR(String name, Type type, int length, Location alias) {
    return newMemory(Kind.VAR, name, type, length, alias);
  }

  private static final Map<String, Memory> memoryTable = new HashMap<String, Memory>();

  private static Memory newMemory(
      Kind kind, String name, Type type, int length, Location alias) {

    checkNotNull(name);
    if (memoryTable.containsKey(name)) {
      throw new IllegalArgumentException(name + " is already defined!");
    }

    final Memory result = (null == alias) ?
        new MemoryDirect(kind, name, type, length) :
        new MemoryAlias(kind, name, type, length, alias);

    memoryTable.put(name, result);
    return result;
  }

  public static Memory getMemory(String name) {
    checkNotNull(name);

    final Memory result = memoryTable.get(name);
    if (null == result) {
      throw new IllegalArgumentException(name + " is not defined!");
    }

    return result;
  }

  private final Kind kind;
  private final String name;
  private final Type type;
  private final int length;
  private final boolean isAlias;

  private Memory(Kind kind, String name, Type type, int length, boolean isAlias) {

    checkNotNull(kind);
    checkNotNull(name);
    checkNotNull(type);
    checkGreaterThanZero(length);

    this.kind = kind;
    this.name = name;
    this.type = type;
    this.length = length;
    this.isAlias = isAlias;
  }

  public final MetaLocationStore getMetaData() {
    return new MetaLocationStore(name, getLength());
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

  public final int getLength() {
    return length;
  }

  public final boolean isAlias() {
    return isAlias;
  }

  public final Location access() {
    return access(0);
  }

  public abstract Location access(int index);

  public abstract void reset();

  public abstract MemoryAllocator newAllocator(int addressableUnitBitSize);

  @Override
  public String toString() {
    return String.format("%s %s[%d, %s], alias=%b",
        kind.name().toLowerCase(), name, length, type, isAlias);
  }

  static final class MemoryDirect extends Memory {
    private final MemoryStorage storage;

    MemoryDirect(Kind kind, String name, Type type, int length) {
      super(kind, name, type, length, false);
      this.storage = new MemoryStorage(length, type.getBitSize()).setId(name);
    }

    @Override
    public Location access(int index) {
      checkBounds(index, getLength());
      return Location.newLocationForRegion(getType(), storage, index);
    }

    @Override
    public void reset() {
      storage.reset();
    }

    @Override
    public MemoryAllocator newAllocator(int addressableUnitBitSize) {
      return new MemoryAllocator(storage, addressableUnitBitSize);
    }
  }

  static final class MemoryAlias extends Memory {
    private final Location source;

    MemoryAlias(Kind kind, String name, Type type, int length, Location source) {
      super(kind, name, type, length, true);
      checkNotNull(source);

      final int totalBitSize = type.getBitSize() * length;
      if (source.getBitSize() != totalBitSize) {
        throw new IllegalArgumentException();
      }

      this.source = source;
    }

    @Override
    public Location access(int index) {
      checkBounds(index, getLength());

      final int locationBitSize = getType().getBitSize();
      final int start = locationBitSize * index;
      final int end = start + locationBitSize - 1;

      final Location bitField = source.bitField(start, end);
      return bitField.castTo(getType().getTypeId());
    }

    @Override
    public void reset() {
      // Does not work for aliases (and should not be called)
    }

    @Override
    public MemoryAllocator newAllocator(int addressableUnitBitSize) {
      throw new UnsupportedOperationException(
          "Memory allocators are not supported for aliases.");
    }
  }
}
