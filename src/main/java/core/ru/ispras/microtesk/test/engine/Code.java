/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.test.Executor;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.TestSequence;
import ru.ispras.microtesk.test.template.ConcreteCall;

public final class Code implements Executor.ICode {
  private final Map<String, Long> handlerAddresses;
  private final Map<Long, Pair<Block, Integer>> addresses;
  private final Map<Long, Block> blocks;

  public Code() {
    this.handlerAddresses = new HashMap<>();
    this.addresses = new HashMap<>();
    this.blocks = new TreeMap<>();
  }

  public void addTestSequence(final TestSequence sequence) {
    InvariantChecks.checkNotNull(sequence);

    if (!sequence.isEmpty()) {
      final Block block = new Block(sequence);
      registerBlock(block);
    }
  }

  private void registerBlock(final Block newBlock) {
    Block blockToLink = null;

    for(final Block block : blocks.values()) {
      final Pair<Long, Long> overlapping = block.getOverlapping(newBlock);
      if (null != overlapping) {
        throw newOverlappingException(overlapping);
      }

      if (block.endAddress == newBlock.startAddress) {
        blockToLink = block;
      }
    }

    if (null != blockToLink) {
      blockToLink.setNext(newBlock);
    }

    blocks.put(newBlock.startAddress, newBlock);
    registerAddresses(newBlock);
  }

  private GenerationAbortedException newOverlappingException(final Pair<Long, Long> overlapping) {
    final StringBuilder sb = new StringBuilder();

    sb.append("Failed to place code at addresses");
    sb.append(String.format(" [0x%016x..0x%016x]. ", overlapping.first, overlapping.second));
    sb.append("They are already used.");

    return new GenerationAbortedException(sb.toString());
  }

  private void registerAddresses(final Block block) {
    for (int index = 0; index < block.calls.size(); index++) {
      final ConcreteCall call = block.calls.get(index);
      final long address = call.getAddress();

      if (!addresses.containsKey(address)) {
        addresses.put(call.getAddress(), new Pair<>(block, index));
      }
    }
  }

  public boolean hasAddress(final long address) {
    return addresses.containsKey(address);
  }

  public Iterator getIterator(final long address, final boolean lookForBlockStart) {
    if (lookForBlockStart) {
      final Block block = blocks.get(address);
      if (null != block) {
        return new Iterator(block, 0);
      }
    }

    final Pair<Block, Integer> entry = addresses.get(address);
    InvariantChecks.checkNotNull(entry);

    return new Iterator(entry.first, entry.second);
  }

  public void addHandlerAddress(final String id, final long address) {
    InvariantChecks.checkNotNull(id);
    handlerAddresses.put(id, address);
  }

  public boolean hasHandler(final String id) {
    return handlerAddresses.containsKey(id);
  }

  public long getHandlerAddress(final String id) {
    final Long address = handlerAddresses.get(id);
    InvariantChecks.checkNotNull(address);
    return address;
  }

  private static final class Block {
    private final long startAddress;
    private final long endAddress;
    private final List<ConcreteCall> calls;
    private Block next;

    public Block(final TestSequence sequence) {
      InvariantChecks.checkNotNull(sequence);
      InvariantChecks.checkFalse(sequence.isEmpty());

      this.startAddress = sequence.getStartAddress();
      this.endAddress = sequence.getEndAddress();
      this.calls = sequence.getAll();
      this.next = null;
    }

    public Pair<Long, Long> getOverlapping(final Block other) {
      final long start = Math.max(this.startAddress, other.startAddress);
      final long end = Math.min(this.endAddress, other.endAddress);
      return start < end ? new Pair<>(start, end) : null;
    }

    public void setNext(final Block block) {
      InvariantChecks.checkTrue(this.next == null);
      InvariantChecks.checkTrue(this.endAddress == block.startAddress);

      this.next = block;
    }
  }

  public static final class Iterator {
    private Block block;
    private int index;
    private ConcreteCall current;

    private Iterator(final Block block, final int startIndex) {
      init(block, startIndex);
    }

    private void init(final Block block, final int index) {
      InvariantChecks.checkNotEmpty(block.calls);
      InvariantChecks.checkBounds(index, block.calls.size());

      this.block = block;
      this.index = index;
      this.current = block.calls.get(index);
    }

    public ConcreteCall current() {
      return current;
    }

    public void next() {
      InvariantChecks.checkNotNull(current);

      if (++index < block.calls.size()) {
        current = block.calls.get(index);
        return;
      }

      if (null != block.next) {
        init(block.next, 0);
        return;
      }

      current = null;
    }
  }
}
