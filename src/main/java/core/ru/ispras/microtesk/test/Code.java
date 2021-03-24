/*
 * Copyright 2016-2021 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.test.template.ConcreteCall;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The {@link Code} class describes the organization of code sections to be simulated.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class Code {
  private final Map<Long, CodeBlock> blocks;
  private final Map<Long, Pair<CodeBlock, Integer>> addresses;
  private final Map<String, Long> handlerAddresses;
  private final Set<Long> breakAddresses;

  public Code() {
    this.blocks = new TreeMap<>();
    this.addresses = new HashMap<>();
    this.handlerAddresses = new HashMap<>();
    this.breakAddresses = new HashSet<>();
  }

  public void registerBlock(final CodeBlock newBlock) {
    InvariantChecks.checkNotNull(newBlock);

    CodeBlock blockToLink = null;
    for (final CodeBlock block : blocks.values()) {
      final Pair<Long, Long> overlapping = block.getOverlapping(newBlock);
      if (null != overlapping) {
        throw newOverlappingException(newBlock, overlapping);
      }

      if (block.getEndAddress() == newBlock.getStartAddress()) {
        blockToLink = block;
      }
    }

    if (null != blockToLink) {
      blockToLink.setNext(newBlock);
    }

    blocks.put(newBlock.getStartAddress(), newBlock);
    registerAddresses(newBlock);
  }

  private GenerationAbortedException newOverlappingException(
          final CodeBlock newBlock, final Pair<Long, Long> overlapping) {
    final StringBuilder sb = new StringBuilder();

    sb.append("Failed to place code at addresses ");
    sb.append(String.format("[0x%016x..0x%016x]. ",
        newBlock.getStartAddress(), newBlock.getEndAddress()));
    sb.append(String.format("Addresses [0x%016x..0x%016x] are already used by:",
        overlapping.first, overlapping.second));

    final Iterator iterator = getIterator(overlapping.first, false);
    for (int index = 0; index < 5 && iterator.current() != null; index++, iterator.next()) {
      final ConcreteCall call = iterator.current();
      sb.append(System.lineSeparator());
      sb.append(String.format("0x%016x %s", call.getAddress(), call.getText()));
    }

    if (null != iterator.current()) {
      sb.append(System.lineSeparator());
      sb.append("...");
    }

    return new GenerationAbortedException(sb.toString());
  }

  private void registerAddresses(final CodeBlock block) {
    final List<ConcreteCall> calls = block.getCalls();
    for (int index = 0; index < calls.size(); index++) {
      final ConcreteCall call = calls.get(index);
      final long address = call.getAddress().longValue();

      if (!addresses.containsKey(address)) {
        addresses.put(address, new Pair<>(block, index));
      }
    }
  }

  public boolean hasAddress(final long address) {
    return addresses.containsKey(address);
  }

  public boolean hasBlockStartAt(final long address) {
    return blocks.containsKey(address);
  }

  public Iterator getIterator(final long address, final boolean fromBlockStart) {
    if (fromBlockStart) {
      final CodeBlock block = blocks.get(address);
      if (null != block) {
        return new Iterator(block, 0);
      }
    }

    final Pair<CodeBlock, Integer> entry = addresses.get(address);
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

  public Map<String, Long> getHandlerAddresses() {
    return handlerAddresses;
  }

  public boolean isBreakAddress(final long address) {
    return breakAddresses.contains(address);
  }

  public void addBreakAddress(final long address) {
    breakAddresses.add(address);
  }

  public static final class Iterator {
    private CodeBlock block;
    private int index;
    private ConcreteCall current;

    private Iterator(final CodeBlock block, final int startIndex) {
      init(block, startIndex);
    }

    private void init(final CodeBlock block, final int index) {
      InvariantChecks.checkNotNull(block);
      InvariantChecks.checkBounds(index, block.getCalls().size());

      this.block = block;
      this.index = index;
      this.current = block.getCalls().get(index);
    }

    public ConcreteCall current() {
      return current;
    }

    public void next() {
      InvariantChecks.checkNotNull(current);

      if (++index < block.getCalls().size()) {
        current = block.getCalls().get(index);
        return;
      }

      if (null != block.getNext()) {
        init(block.getNext(), 0);
        return;
      }

      current = null;
    }
  }
}
