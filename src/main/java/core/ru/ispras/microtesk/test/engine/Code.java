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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.TestSequence;
import ru.ispras.microtesk.test.template.ConcreteCall;

public final class Code {
  private final Map<String, Long> handlerAddresses;
  private final Map<Long, Pair<Block, Integer>> addresses;
  private final List<Block> blocks;

  public Code() {
    this.handlerAddresses = new HashMap<>();
    this.addresses = new HashMap<>();
    this.blocks = new ArrayList<>();
  }

  public void addTestSequence(final TestSequence sequence) {
    InvariantChecks.checkNotNull(sequence);

    final Block block = new Block(
        sequence.getAll(), sequence.getStartAddress(), sequence.getEndAddress());

    final Block registeredBlock = registerBlock(block);
    registerAddresses(registeredBlock);
  }

  private Block registerBlock(final Block newBlock) {
    Block blockToMerge = null;
    for(final Block block : blocks) {
      final Pair<Long, Long> overlapping = block.getOverlapping(newBlock);
      if (null != overlapping) {
        throw newOverlappingException(overlapping);
      }

      if (block.endAddress == newBlock.startAddress) {
        blockToMerge = block;
      }
    }

    if (null != blockToMerge) {
      blockToMerge.merge(newBlock);
      return blockToMerge;
    }

    blocks.add(newBlock);
    return newBlock;
  }

  private GenerationAbortedException newOverlappingException(final Pair<Long, Long> overlapping) {
    final StringBuilder sb = new StringBuilder();

    sb.append("Failed to place code at addresses");
    sb.append(String.format(" [0x%s..0x%s]. ", overlapping.first, overlapping.second));
    sb.append("They are already used.");

    return new GenerationAbortedException(sb.toString());
  }

  private void registerAddresses(final Block block) {
    for (int index = 0; index < block.calls.size(); index++) {
      final ConcreteCall call = block.calls.get(index);
      addresses.put(call.getAddress(), new Pair<>(block, index));
    }
  }

  public boolean hasAddress(final long address) {
    return addresses.containsKey(address);
  }

  public Iterator<ConcreteCall> getCallIterator(final long address) {
    final Pair<Block, Integer> entry = addresses.get(address);
    InvariantChecks.checkNotNull(entry);

    final Block block = entry.first;
    final int index = entry.second;

    return new CallIterator(block.calls, index);
  }

  public void addHanderAddress(final String id, final long address) {
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
    private long endAddress;
    private final List<ConcreteCall> calls;

    public Block(
        final List<ConcreteCall> calls,
        final long startAddress,
        final long endAddress) {
      InvariantChecks.checkNotEmpty(calls);

      this.startAddress = startAddress;
      this.endAddress = endAddress;
      this.calls = calls;
    }

    public Pair<Long, Long> getOverlapping(final Block other) {
      final long start = Math.max(this.startAddress, other.startAddress);
      final long end = Math.min(this.endAddress, other.endAddress);
      return start < end ? new Pair<>(start, end) : null;
    }

    public void merge(final Block other) {
      InvariantChecks.checkNotNull(other);
      InvariantChecks.checkTrue(this.endAddress == other.startAddress);

      this.calls.addAll(other.calls);
      this.endAddress = other.endAddress;
    }
  }

  private static final class CallIterator implements Iterator<ConcreteCall> {
    private final List<ConcreteCall> calls;
    private int index;

    public CallIterator(final List<ConcreteCall> calls, final int startIndex) {
      InvariantChecks.checkNotEmpty(calls);
      InvariantChecks.checkBounds(startIndex, calls.size());

      this.calls = calls;
      this.index = startIndex;
    }

    @Override
    public boolean hasNext() {
      return index < calls.size();
    }

    @Override
    public ConcreteCall next() {
      return calls.get(index++);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
