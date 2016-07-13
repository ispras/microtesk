/*
 * Copyright 2014-2016 ISP RAS (http://www.ispras.ru)
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.GenerationAbortedException;

import ru.ispras.microtesk.test.sequence.Generator;
import ru.ispras.microtesk.test.sequence.GeneratorBuilder;
import ru.ispras.microtesk.test.sequence.GeneratorPrologueEpilogue;
import ru.ispras.microtesk.test.sequence.GeneratorUtils;

import ru.ispras.testbase.knowledge.iterator.Iterator;
import ru.ispras.testbase.knowledge.iterator.SingleValueIterator;

public final class BlockBuilder {
  private final BlockId blockId;
  private final boolean isExternal;
  private Where where;

  private List<Block> nestedBlocks;
  private Map<String, Object> attributes;

  private String combinatorName;
  private String permutatorName;
  private String compositorName;
  private String rearrangerName;
  private String obfuscatorName;

  private boolean isAtomic;
  private boolean isSequence;
  private boolean isIterate;

  private List<Call> prologue;
  private List<Call> epilogue;

  protected BlockBuilder(final boolean isExternal) {
    this(new BlockId(), isExternal);
  }

  protected BlockBuilder(final BlockBuilder parent) {
    this(parent.getBlockId().nextChildId(), true);
  }

  private BlockBuilder(final BlockId blockId, final boolean isExternal) {
    this.blockId = blockId;
    this.isExternal = isExternal;
    this.where = null;

    this.nestedBlocks = new ArrayList<>();
    this.attributes = new HashMap<>();

    this.combinatorName = null;
    this.permutatorName = null;
    this.compositorName = null;
    this.rearrangerName = null;
    this.obfuscatorName = null;

    this.isAtomic = false;
    this.isSequence = false;
    this.isIterate = false;

    this.prologue = null;
    this.epilogue = null;
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

  public void setWhere(final Where where) {
    InvariantChecks.checkNotNull(where);
    this.where = where;
  }

  public void setCompositor(final String name) {
    InvariantChecks.checkTrue(null == compositorName);
    compositorName = name;
  }

  public void setPermutator(final String name) {
    InvariantChecks.checkTrue(null == permutatorName);
    permutatorName = name;
  }

  public void setCombinator(final String name) {
    InvariantChecks.checkTrue(null == combinatorName);
    combinatorName = name;
  }

  public void setRearranger(final String name) {
    InvariantChecks.checkTrue(null == rearrangerName);
    rearrangerName = name;
  }

  public void setObfuscator(final String name) {
    InvariantChecks.checkTrue(null == obfuscatorName);
    obfuscatorName = name;
  }

  public void setAtomic(final boolean value) {
    InvariantChecks.checkFalse(value && isIterate);
    InvariantChecks.checkFalse(value && isSequence);
    isAtomic = value;
  }

  public void setSequence(final boolean value) {
    InvariantChecks.checkFalse(value && isAtomic);
    InvariantChecks.checkFalse(value && isIterate);
    isSequence = value;
  }

  public void setIterate(final boolean value) {
    InvariantChecks.checkFalse(value && isAtomic);
    InvariantChecks.checkFalse(value && isSequence);
    isIterate = value;
  }

  public void setAttribute(final String name, final Object value) {
    InvariantChecks.checkFalse(attributes.containsKey(name));
    attributes.put(name, value);
  }

  public void addBlock(final Block block) {
    InvariantChecks.checkNotNull(block);

    if (isAtomic || isSequence) {
      throw new GenerationAbortedException(String.format(
          "Nested blocks are not allowed in '%s' structures. At: %s", 
          isAtomic ? "atomic" : "sequence",
          block.getWhere()
          ));
    }

    if (block.isEmpty()) {
      return;
    }

    if (block.isAtomic()) {
      final List<Call> sequence = GeneratorUtils.expand(block.getIterator());
      addCall(Call.newAtomicSequence(sequence));
    } else {
      nestedBlocks.add(block);
    }

    block.incRefCount();
  }

  public void addCall(final Call call) {
    InvariantChecks.checkNotNull(call);

    if (call.isEmpty()) {
      return;
    }

    final Iterator<List<Call>> iterator =
        new SingleValueIterator<>(Collections.singletonList(call));

    nestedBlocks.add(new Block(blockId, where, true, false, iterator));
  }

  public void setPrologue(final List<Call> value) {
    InvariantChecks.checkNotNull(value);
    InvariantChecks.checkFalse(hasPrologue());

    this.prologue = value;
  }

  public boolean hasPrologue() {
    return prologue != null;
  }

  public void setEpilogue(final List<Call> value) {
    InvariantChecks.checkNotNull(value);
    InvariantChecks.checkFalse(hasEpilogue());

    this.epilogue = value;
  }

  public boolean hasEpilogue() {
    return epilogue != null;
  }

  public Block build() {
    return build(null, null);
  }

  public Block build(final List<Call> globalPrologue, final List<Call> globalEpilogue) {
    if (!isAtomic && !isSequence && !isIterate &&
        combinatorName == null &&
        permutatorName == null &&
        compositorName == null &&
        rearrangerName == null &&
        obfuscatorName == null) {
      throw new GenerationAbortedException(String.format(
          "Using blocks with no arguments is currently forbidden " + 
          "due to compatibility issues. At: %s", where)
          );
    }

    final GeneratorBuilder<Call> generatorBuilder =
        new GeneratorBuilder<>(isAtomic || isSequence, isIterate);

    if (null != combinatorName) {
      generatorBuilder.setCombinator(combinatorName);
    }

    if (null != permutatorName) {
      generatorBuilder.setPermutator(permutatorName);
    }

    if (null != compositorName) {
      generatorBuilder.setCompositor(compositorName);
    }

    if (null != rearrangerName) {
      generatorBuilder.setRearranger(rearrangerName);
    }

    if (null != obfuscatorName) {
      generatorBuilder.setObfuscator(obfuscatorName);
    }

    for (final Block block : nestedBlocks) {
      final Iterator<List<Call>> iterator = block.getIterator();
      if (block.getRefCount() > 1) {
        generatorBuilder.addIterator(iterator.clone());
      } else {
        generatorBuilder.addIterator(iterator);
      }
    }

    // For an empty sequence block (non-external, explicitly specified),
    // a single empty sequence is inserted.
    if (isEmpty() && !isExternal && (isAtomic || isSequence)) {
      generatorBuilder.addIterator(new SingleValueIterator<>(Collections.<Call>emptyList()));
    }

    final Generator<Call> generator =
        generatorBuilder.getGenerator();

    final Generator<Call> generatorPrologueEpilogue =
        wrapWithPrologueAndEpilogue(generator, prologue, epilogue);

    final Generator<Call> generatorGlobalPrologueEpilogue =
        wrapWithPrologueAndEpilogue(generatorPrologueEpilogue, globalPrologue, globalEpilogue);

    return new Block(
        blockId,
        where,
        isAtomic,
        isExternal,
        generatorGlobalPrologueEpilogue,
        attributes
        );
  }

  private static Generator<Call> wrapWithPrologueAndEpilogue(
      final Generator<Call> generator,
      final List<Call> prologue,
      final List<Call> epilogue) {

    if (prologue == null && epilogue == null) {
      return generator;
    }

    return new GeneratorPrologueEpilogue<>(
        generator,
        prologue != null ? prologue : Collections.<Call>emptyList(),
        epilogue != null ? epilogue : Collections.<Call>emptyList()
        );
  }
}
