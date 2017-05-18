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

import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.sequence.GeneratorPrologueEpilogue;
import ru.ispras.testbase.knowledge.iterator.Iterator;

public final class Block {
  private final BlockId blockId;
  private final Where where;

  private final boolean isAtomic;
  private final boolean isExternal;
  private final Map<String, Object> attributes;

  private final Iterator<List<AbstractCall>> iterator;
  private final boolean isEmpty;

  private final List<AbstractCall> prologue;
  private final List<AbstractCall> epilogue;
  private final Iterator<List<AbstractCall>> wrappedIterator;

  // Number of times the block was nested into other blocks.
  private int refCount;

  protected Block(
      final BlockId blockId,
      final Where where,
      final boolean isAtomic,
      final boolean isExternal,
      final Map<String, Object> attributes,
      final Iterator<List<AbstractCall>> iterator,
      final List<AbstractCall> prologue,
      final List<AbstractCall> epilogue) {
    InvariantChecks.checkNotNull(blockId);
    InvariantChecks.checkNotNull(attributes);
    InvariantChecks.checkNotNull(iterator);
    InvariantChecks.checkNotNull(prologue);
    InvariantChecks.checkNotNull(epilogue);

    // External code has no prologue and epilogue, it prologue and epilogue for all root blocks.
    InvariantChecks.checkTrue(isExternal ? epilogue.isEmpty() && prologue.isEmpty() : true);

    this.blockId = blockId;
    this.where = where;

    this.isAtomic = isAtomic;
    this.isExternal = isExternal;
    this.attributes = attributes;

    // A block is considered empty if it does not contain any instruction
    // sequences). Attributes affect only the block itself and are not
    // useful outside the block.

    iterator.init();
    this.iterator = iterator;
    this.isEmpty = !iterator.hasValue();

    this.prologue = prologue;
    this.epilogue = epilogue;
    this.wrappedIterator = prologue.isEmpty() && epilogue.isEmpty() ?
        iterator :
        new GeneratorPrologueEpilogue<AbstractCall>(iterator, prologue, epilogue);

    this.refCount = 0;
  }

  public BlockId getBlockId() {
    return blockId;
  }

  public Where getWhere() {
    return where;
  }

  public boolean isAtomic() {
    return isAtomic;
  }

  public boolean isExternal() {
    return isExternal;
  }

  public Object getAttribute(final String name) {
    return attributes.get(name);
  }

  public String getAttribute(final String name, final String fallback) {
    final Object value = getAttribute(name);
    return null == value ? fallback : value.toString();
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public Iterator<List<AbstractCall>> getIterator() {
    return getIterator(true);
  }

  public Iterator<List<AbstractCall>> getIterator(final boolean isPrologueEpilogue) {
    return isPrologueEpilogue ? wrappedIterator : iterator;
  }

  public boolean isEmpty() {
    return isEmpty;
  }

  public List<AbstractCall> getPrologue() {
    return prologue;
  }

  public List<AbstractCall> getEpilogue() {
    return epilogue;
  }

  protected int getRefCount() {
    return refCount;
  }

  protected void incRefCount() {
    refCount++;
  }
}
