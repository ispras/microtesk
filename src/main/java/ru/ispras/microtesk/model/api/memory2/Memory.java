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

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.metadata.MetaLocationStore;
import ru.ispras.microtesk.model.api.type.Type;

public class Memory {

  public enum Kind {
    REG, MEM, VAR
  }

  public static Memory REG(String name, Type type, int length) {
    return new Memory(Kind.REG, name, type, length, false);
  }

  public static Memory REG(String name, Type type) {
    return REG(name, type, 1);
  }

  public static Memory MEM(String name, Type type, int length) {
    return new Memory(Kind.MEM, name, type, length, false);
  }

  public static Memory MEM(String name, Type type) {
    return MEM(name, type, 1);
  }

  public static Memory VAR(String name, Type type, int length) {
    return new Memory(Kind.VAR, name, type, length, false);
  }

  public static Memory VAR(String name, Type type) {
    return VAR(name, type, 1);
  }

  private final Kind kind;
  private final String name;
  private final Type type;
  private final int length;
  private final boolean isReadOnly;

  private final MemoryStorage storage;
  private MemoryAccessHandler handler;

  private Memory(Kind kind, String name, Type type, int length, boolean isReadOnly) {
    checkNotNull(kind);
    checkNotNull(name);
    checkNotNull(type);

    if (length <= 0) {
      throw new IllegalArgumentException();
    }

    this.kind = kind;
    this.name = name;
    this.type = type;
    this.length = length;
    this.isReadOnly = isReadOnly;

    this.storage = new MemoryStorage(length, type.getBitSize());
    this.handler = null;
  }

  public MetaLocationStore getMetaData() {
    return new MetaLocationStore(name, getLength());
  }

  public Kind getKind() {
    return kind;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public int getLength() {
    return length;
  }

  public boolean isReadOnly() {
    return isReadOnly;
  }

  public void reset() {
    storage.reset();
  }

  public Location access(int index) {
    if (!(0 <= index && index < length)) {
      throw new IndexOutOfBoundsException();
    }

    return new LocationImpl(index);
  }

  public Location access() {
    return access(0);
  }

  public void setHandler(MemoryAccessHandler handler) {
    this.handler = handler;
  }

  public MemoryAccessHandler getHandler() {
    return handler;
  }

  private class LocationImpl implements Location {
    private final int index;

    private LocationImpl(int index) {
      this.index = index;
    }

    @Override
    public Type getType() {
      return type;
    }

    @Override
    public Data load() {
      if (null != handler) {
        final List<MemoryRegion> regions = handler.onLoad(
            Collections.singletonList(new MemoryRegion(storage, index)));
        return new Data(regions.get(0).getData(), type);
      }

      return new Data(storage.read(index), type);
    }

    @Override
    public void store(Data data) {
      if (null != handler) {
        handler.onStore(Collections.singletonList(
            new MemoryRegion(storage, index, data.getRawData())));
        return;
      }

      storage.write(index, data.getRawData());
    }

    @Override
    public Location assign(Location arg) {
      store(arg.load());
      return this;
    }

    @Override
    public Location bitField(int start, int end) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Location concat(Location arg) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String toBinString() {
      return storage.read(index).toBinString();
    }

    @Override
    public BigInteger getValue() {
      return new BigInteger(storage.read(index).toByteArray());
    }
  }

  protected static void checkNotNull(Object o) {
    if (null == o) {
      throw new NullPointerException();
    }
  }
}
