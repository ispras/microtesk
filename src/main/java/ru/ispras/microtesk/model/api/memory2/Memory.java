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

public abstract class Memory {
  
  public enum Kind {
    REG, MEM, VAR
  }
  
  public static Memory newMemory(
      Kind kind, String name, Type type, int length) {
    return new MemoryDirect(kind, name, type, length);
  }

  public static Memory newMemoryAlias(
      Kind kind, String name, Type type, int length, Memory source, int sourceIndex) {

    checkNotNull(source);

    return null; // new Memory(kind, name, type, length);
  }

  public static Memory newMemoryAlias(
      Kind kind, String name, Type type, int length, Location source) {

    checkNotNull(source);

    return null; //new Memory(kind, name, type, length);
  }

  private static MemoryAccessHandler handler = null;

  private final Kind kind;
  private final String name;
  private final Type type;
  private final int length;
  private final MemoryStorage storage; 

  protected Memory(
      Kind kind, String name, Type type, int length, MemoryStorage storage) {

    checkNotNull(kind);
    checkNotNull(name);
    checkNotNull(type);
    checkGreaterThanZero(length);
    checkNotNull(storage);

    this.kind = kind;
    this.name = name;
    this.type = type;
    this.length = length;
    this.storage = storage;
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

  final MemoryStorage getStorage() {
    return storage;
  }

  public final Location access() {
    return access(0);
  }

  public abstract Location access(int index);

  public abstract void reset();

  public static void setHandler(MemoryAccessHandler value) {
    handler = value;
  }

  public static MemoryAccessHandler getHandler() {
    return handler;
  }

  protected static void checkNotNull(Object o) {
    if (null == o) {
      throw new NullPointerException();
    }
  }

  protected static void checkGreaterThanZero(int n) {
    if (n <= 0) {
      throw new IllegalArgumentException();      
    }
  }

  protected final void checkBounds(int index) {
    if (!(0 <= index && index < getLength())) {
      throw new IndexOutOfBoundsException();
    }
  }
}

final class MemoryDirect extends Memory {

  MemoryDirect(Kind kind, String name, Type type, int length) {
    super(kind, name, type, length, newMemoryStorage(type, length));
  }

  private static MemoryStorage newMemoryStorage(Type type, int length) {
    checkNotNull(type);
    checkGreaterThanZero(length);
    return new MemoryStorage(length, type.getBitSize());
  }

  @Override
  public Location access(int index) {
    checkBounds(index);

    return Location.newLocationForRegion(
        getType(), getStorage(), index, getKind() == Kind.MEM);
  }

  @Override
  public void reset() {
    getStorage().reset();
  }
}

final class MemoryAlias extends Memory {

  MemoryAlias(Kind kind, String name, Type type, int length, Memory source, int sourceIndex) {
    super(kind, name, type, length, source.getStorage());
    // TODO Auto-generated constructor stub
  }

  @Override
  public Location access(int index) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void reset() {
    // TODO Auto-generated method stub
    
  }
}
