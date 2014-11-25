/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.memory2;

import ru.ispras.microtesk.model.api.metadata.MetaLocationStore;
import ru.ispras.microtesk.model.api.type.Type;

import static ru.ispras.microtesk.utils.InvariantChecks.*;

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

  private static Memory newMemory(
      Kind kind, String name, Type type, int length, Location alias) {
    if (null == alias) {
      return new MemoryDirect(kind, name, type, length);
    } else {
      return new MemoryAlias(kind, name, type, length, alias);
    }
  }

  private final Kind kind;
  private final String name;
  private final Type type;
  private final int length;
  private final boolean isAlias;
  
  private static final MemoryAccessHandlerEngine handlerEngine = new MemoryAccessHandlerEngine();
  private static boolean isHandlingEnabled = true;

  static MemoryAccessHandler getGlobalHandler() {
    return isHandlingEnabled ? getHandlerEngine() : null;
  }

  protected static MemoryAccessHandlerEngine getHandlerEngine() {
    return handlerEngine;
  }

  public static void setHandlingEnabled(boolean value) {
    isHandlingEnabled = value;
  }

  protected Memory(
      Kind kind, String name, Type type, int length, boolean isAlias) {

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
  public abstract void setHandler(MemoryAccessHandler handler);
}

final class MemoryDirect extends Memory {
  private final MemoryStorage storage;

  MemoryDirect(Kind kind, String name, Type type, int length) {
    super(kind, name, type, length, false);
    this.storage = new MemoryStorage(name, length, type.getBitSize());
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
  public void setHandler(MemoryAccessHandler handler) {
    checkNotNull(handler);
    getHandlerEngine().registerHandler(storage, handler);
  }
}

final class MemoryAlias extends Memory {
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
    final int end = start + locationBitSize;

    final Location bitField = source.bitField(start, end);
    return bitField.castTo(getType().getTypeId());
  }

  @Override
  public void reset() {
    // Does not work for aliases (and should not be called)
  }

  @Override
  public void setHandler(MemoryAccessHandler handler) {
    // Does not work for aliases (and should not be called)
  }
}
