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

package ru.ispras.microtesk.test.template;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.test.sequence.Generator;
import ru.ispras.microtesk.test.sequence.GeneratorBuilder;
import ru.ispras.microtesk.test.sequence.GeneratorPrologueEpilogue;
import ru.ispras.testbase.knowledge.iterator.Iterator;
import ru.ispras.testbase.knowledge.iterator.SingleValueIterator;

public final class BlockBuilder {
  private final BlockId blockId;

  private List<Block> nestedBlocks;
  private Map<String, Object> attributes;

  private String compositorName;
  private String combinatorName;

  private boolean isAtomic;

  private List<Call> prologue;
  private List<Call> epilogue;

  protected BlockBuilder() {
    this(new BlockId());
  }

  protected BlockBuilder(final BlockBuilder parent) {
    this(parent.getBlockId().nextChildId());
  }

  private BlockBuilder(final BlockId blockId) {
    this.blockId = blockId;

    this.nestedBlocks = new ArrayList<>();
    this.attributes = new HashMap<>();

    this.compositorName = null;
    this.combinatorName = null;

    this.isAtomic = false;

    this.prologue = Collections.emptyList();
    this.epilogue = Collections.emptyList();
  }

  public BlockId getBlockId() {
    return blockId;
  }

  public boolean isEmpty() {
    // A block is considered empty if it contains no nested blocks or calls
    // (that is it does not produce any instruction sequences). Attributes 
    // affect only the block itself and are not useful outside the block.

    return nestedBlocks.isEmpty();
  }

  public void setCompositor(final String name) {
    assert null == compositorName;
    compositorName = name;
  }

  public void setCombinator(final String name) {
    assert null == combinatorName;
    combinatorName = name;
  }

  public void setAtomic(final boolean value) {
    this.isAtomic = value;
  }

  public void setAttribute(final String name, final Object value) {
    assert !attributes.containsKey(name);
    attributes.put(name, value);
  }

  public void addBlock(final Block block) {
    checkNotNull(block);

    if (block.isEmpty()) {
      return;
    }

    nestedBlocks.add(block);
  }

  public void addCall(final Call call) {
    checkNotNull(call);

    if (call.isEmpty()) {
      return;
    }

    final List<Call> sequence = new ArrayList<>();
    sequence.add(call);

    final Iterator<List<Call>> iterator = new SingleValueIterator<>(sequence);
    nestedBlocks.add(new Block(blockId, iterator));
  }

  public void setPrologue(final List<Call> value) {
    checkNotNull(value);
    this.prologue = value;
  }

  public void setEpilogue(final List<Call> value) {
    checkNotNull(value);
    this.epilogue = value;
  }

  public Block build() {
    final GeneratorBuilder<Call> generatorBuilder = new GeneratorBuilder<>();
    generatorBuilder.setSingle(isAtomic);

    if (null != combinatorName) {
      generatorBuilder.setCombinator(combinatorName);
    }

    if (null != compositorName) {
      generatorBuilder.setCompositor(compositorName);
    }

    for (final Block block : nestedBlocks) {
      generatorBuilder.addIterator(block.getIterator());
    }

    final Generator<Call> generator = generatorBuilder.getGenerator();

    if (prologue.isEmpty() && epilogue.isEmpty()) {
      return new Block(blockId, generator, attributes);
    }

    final Generator<Call> generatorPrologueEpilogue =
        new GeneratorPrologueEpilogue<>(generator, prologue, epilogue);

    return new Block(blockId, generatorPrologueEpilogue, attributes);
  }
}
