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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.test.sequence.GeneratorSingle;
import ru.ispras.testbase.knowledge.iterator.Iterator;

public final class Block {
  private final BlockId blockId;
  private final Iterator<List<Call>> iterator;
  private final Map<String, Object> attributes;
  private final boolean isEmpty;
  private final boolean isSingle;

  protected Block(
      final BlockId blockId,
      final Iterator<List<Call>> iterator,
      final Map<String, Object> attributes) {
    checkNotNull(blockId);
    checkNotNull(iterator);
    checkNotNull(attributes);

    this.blockId = blockId;
    this.iterator = iterator;
    this.attributes = attributes;

    // A block is considered empty if it does not contain any instruction
    // sequences). Attributes affect only the block itself and are not
    // useful outside the block.

    iterator.init();
    this.isEmpty = !iterator.hasValue();

    // TODO: HACK! BE CAREFUL! PROBABLY, NEED A MORE RELIABLE WAY
    this.isSingle = iterator instanceof GeneratorSingle;
  }

  protected Block(final BlockId blockId, final Iterator<List<Call>> iterator) {
    this(blockId, iterator, Collections.<String, Object>emptyMap());
  }

  public BlockId getBlockId() {
    return blockId;
  }

  public Iterator<List<Call>> getIterator() {
    return iterator;
  }

  public Object getAttribute(final String name) {
    return attributes.get(name);
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public boolean isEmpty() {
    return isEmpty;
  }

  public boolean isSingle() {
    return isSingle;
  }
}
