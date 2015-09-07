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

import static ru.ispras.fortress.util.InvariantChecks.checkGreaterThan;
import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.type.Type;

public abstract class Memory {
  private static final Map<String, Memory> INSTANCES = new HashMap<>();

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
    checkDefined(name);

    final Memory result;
    switch (kind) {
      case MEM:
        result = new PhysicalMemory(name, type, length);
        break;

      case REG:
        result = new RegisterFile(name, type, length);
        break;
        
      case VAR:
        result = new VariableArray(name, type, length);
        break;

      default:
        throw new IllegalArgumentException("Unknown kind: " + kind);
    }

    INSTANCES.put(name, result);
    return result;
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

    checkDefined(name);
    final Memory result = new AliasForLocation(kind, name, type, length, alias);

    INSTANCES.put(name, result);
    return result;
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
    checkDefined(name);
    final Memory result = new AliasForMemory(kind, name, type, length, memory, min, max);

    INSTANCES.put(name, result);
    return result;
  }

  private static void checkDefined(final String name) {
    if (INSTANCES.containsKey(name)) {
      throw new IllegalArgumentException(name + " is already defined!");
    }
  }

  public static Memory get(final String name) {
    final Memory result = INSTANCES.get(name);
    if (null == result) {
      throw new IllegalArgumentException(name + " is not defined!");
    }

    return result;
  }

  protected Memory(
      final Kind kind,
      final String name,
      final Type type,
      final BigInteger length,
      final boolean isAlias) {
    checkNotNull(kind);
    checkNotNull(name);
    checkNotNull(type);
    checkGreaterThan(length, BigInteger.ZERO);

    this.kind = kind;
    this.name = name;
    this.type = type;
    this.length = length;
    this.isAlias = isAlias;
    this.addressBitSize = MemoryStorage.calculateAddressSize(length);
  }

  public static void setUseTempCopies(boolean value) {
    for (final Memory memory : INSTANCES.values()) {
      memory.setUseTempCopy(value);
    }
  }

  public abstract void setUseTempCopy(boolean value);

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

  public abstract void reset();

  @Override
  public String toString() {
    return String.format("%s %s[%d, %s], alias=%b",
        kind.name().toLowerCase(), name, length, type, isAlias);
  }
}
