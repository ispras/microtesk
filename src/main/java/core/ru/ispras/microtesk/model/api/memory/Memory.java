/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

import java.util.ArrayList;
import java.util.List;

import ru.ispras.microtesk.model.api.metadata.MetaLocationStore;
import ru.ispras.microtesk.model.api.type.Type;

public abstract class Memory {

  public enum Kind {
    REG, MEM, VAR
  }
  
  public static MemoryStore REG(String name, Type type, int length) {
    return new MemoryStore(Kind.REG, name, type, length);
  }

  public static MemoryStore REG(String name, Type type) {
    return new MemoryStore(Kind.REG, name, type);
  }

  public static MemoryStore MEM(String name, Type type, int length) {
    return new MemoryStore(Kind.MEM, name, type, length);
  }

  public static MemoryStore MEM(String name, Type type) {
    return new MemoryStore(Kind.MEM, name, type);
  }

  public static MemoryStore VAR(String name, Type type, int length) {
    return new MemoryStore(Kind.VAR, name, type, length);
  }

  public static MemoryStore VAR(String name, Type type) {
    return new MemoryStore(Kind.VAR, name, type);
  }

  private final Kind kind;
  private final String name;
  private final Type type;
  private final int length;

  public Memory(Kind kind, String name, Type type) {
    this(kind, name, type, 1);
  }

  public Memory(Kind kind, String name, Type type, int length) {
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
  }

  public MetaLocationStore getMetaData() {
    return new MetaLocationStore(name, getLength());
  }

  public final Kind getMemoryKind() {
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

  public abstract Location access(int index);
  public abstract Location access();
  public abstract void reset();

  protected static void checkNotNull(Object o) {
    if (null == o) {
      throw new NullPointerException();
    }
  }
}

final class MemoryStore extends Memory {

  public static int BLOCK_SIZE = 4096;

  private final List<Block> blocks;

  private static final class Block {
    private final List<Location> locations;

    Block(Type type, int length) {
      final List<Location> locationArray = new ArrayList<Location>(length);
      for (int index = 0; index < length; ++index) {
        locationArray.add(new Location(type));
      }

      this.locations = locationArray;
    }

    void reset() {
      for (Location location : locations) {
        location.reset();
      }
    }

    public Location getLocation(int index) {
      return locations.get(index);
    }
  }

  public MemoryStore(Kind kind, String name, Type type) {
    this(kind, name, type, 1);
  }

  public MemoryStore(Kind kind, String name, Type type, int length) {
    super(kind, name, type, length);
    this.blocks = allocateBlocks(type, length);
  }

  private static List<Block> allocateBlocks(Type type, int length) {
    final int count = length / BLOCK_SIZE + (0 == length % BLOCK_SIZE ? 0 : 1);

    final List<Block> result = new ArrayList<Block>(count);
    for (int index = 0; index < count; ++index) {
      result.add(null);
    }

    return result;
  }
  
  private Location getLocation(int index) {
    if (index < 0 || index >= getLength()) {
      throw new IndexOutOfBoundsException(String.format(
        "Index=%s, Length=%s", index, getLength()));
    }

    final int blockIndex = index / BLOCK_SIZE;
    final int locationIndex = index - blockIndex * BLOCK_SIZE;

    // DEBUG CODE:
    // System.out.printf("index = %d, blockIndex = %d, locationIndex = %d%n",
    // index, blockIndex, locationIndex);

    Block block = blocks.get(blockIndex);
    if (null == block) {
      final int blockLength = (blockIndex < getBlockCount() - 1) ?
        BLOCK_SIZE : getLength() - BLOCK_SIZE * (getBlockCount() - 1);

      block = new Block(getType(), blockLength);
      blocks.set(blockIndex, block);

      // DEBUG CODE:
      // System.out.printf("New block: blockIndex = %d, blockLength = %d%n",
      // blockIndex, blockLength);
    }

    return block.getLocation(locationIndex);
  }

  int getBlockCount() {
    return blocks.size();
  }

  @Override
  public void reset() {
    for (Block block : blocks) {
      if (null != block) {
        block.reset();
      }
    }
  }

  @Override
  public Location access(int index) {
    return getLocation(index);
  }

  @Override
  public Location access() {
    return access(0);
  }
}
