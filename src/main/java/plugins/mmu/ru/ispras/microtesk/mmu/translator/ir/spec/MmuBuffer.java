/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.ir.spec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.AddressView;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.utils.function.Predicate;

/**
 * {@link MmuBuffer} represents an MMU buffer.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public class MmuBuffer extends MmuStruct {
  /** The number of ways (associativity). */
  private final long ways;
  /** The number of sets. */
  private final long sets;

  /** The MMU address. */
  private final MmuAddressType address;

  /** The tag calculation function. */
  private final MmuExpression tagExpression;
  /** The index calculation function. */
  private final MmuExpression indexExpression;
  /** The offset calculation function. */
  private final MmuExpression offsetExpression;

  private final Collection<MmuBinding> matchBindings;

  /** Guard condition (only for views). */
  private final MmuCondition guardCondition;
  // TODO: Temporal solution.
  private final Predicate<MemoryAccess> guard;

  /** The flag indicating whether the device supports data replacement. */
  private final boolean replaceable;

  // TODO: E.g., JTLB for DTLB.
  private final MmuBuffer parent;
  // TODO: E.g., DTLB for JTLB.
  private final List<MmuBuffer> children = new ArrayList<>();

  /** The address view. */
  private final AddressView<Long> addressView;

  public MmuBuffer(
      final String name,
      final long ways,
      final long sets,
      final MmuAddressType address,
      final MmuExpression tagExpression,
      final MmuExpression indexExpression,
      final MmuExpression offsetExpression,
      final Collection<MmuBinding> matchBindings,
      final MmuCondition guardCondition,
      final Predicate<MemoryAccess> guard,
      final boolean replaceable,
      final MmuBuffer parent) {
    super(name);
    setDevice(this);

    InvariantChecks.checkNotNull(address);
    InvariantChecks.checkNotNull(tagExpression);
    InvariantChecks.checkNotNull(indexExpression);
    InvariantChecks.checkNotNull(offsetExpression);
    InvariantChecks.checkNotNull(matchBindings);

    this.ways = ways;
    this.sets = sets;

    this.address = address;

    this.tagExpression = tagExpression;
    this.indexExpression = indexExpression;
    this.offsetExpression = offsetExpression;
    this.matchBindings = matchBindings;

    this.guardCondition = guardCondition;
    this.guard = guard;

    this.replaceable = replaceable;

    // TODO:
    this.parent = parent;
    if (parent != null) {
      parent.children.add(this);
    }

    this.addressView = new MmuAddressViewBuilder(address,
        tagExpression, indexExpression, offsetExpression).build();
  }

  /**
   * Returns the number of ways (associativity).
   * 
   * @return the number of ways.
   */
  public final long getWays() {
    return ways;
  }

  /**
   * Returns the number of sets.
   * 
   * @return the number of sets.
   */
  public final long getSets() {
    return sets;
  }

  /**
   * Returns the input parameter.
   * 
   * @return the input parameter.
   */
  public final MmuAddressType getAddress() {
    return address;
  }

  /**
   * Returns the tag calculation function.
   * 
   * @return the tag calculation function.
   */
  public final MmuExpression getTagExpression() {
    return tagExpression;
  }

  /**
   * Returns the index calculation function.
   * 
   * @return the index calculation function.
   */
  public final MmuExpression getIndexExpression() {
    return indexExpression;
  }

  /**
   * Returns the offset calculation function.
   * 
   * @return the offset calculation function.
   */
  public final MmuExpression getOffsetExpression() {
    return offsetExpression;
  }

  public final Collection<MmuBinding> getMatchBindings() {
    return matchBindings;
  }

  /**
   * Returns the address view.
   * 
   * @return the address view.
   */
  public final AddressView<Long> getAddressView() {
    return addressView;
  }

  /**
   * Returns the address tag.
   * 
   * @param address the address.
   * @return the value of the tag.
   */
  public final long getTag(long address) {
    return addressView.getTag(address);
  }

  /**
   * Returns the address index.
   * 
   * @param address the address.
   * @return the value of the index.
   */
  public long getIndex(long address) {
    return addressView.getIndex(address);
  }

  /**
   * Returns the address offset.
   * 
   * @param address the address.
   * @return the value of the offset.
   */
  public final long getOffset(long address) {
    return addressView.getOffset(address);
  }

  /**
   * Returns the address for the given tag, index and offset.
   * 
   * @param tag the tag.
   * @param index the index.
   * @param offset the offset.
   * @return the value of the address.
   */
  public final long getAddress(long tag, long index, long offset) {
    return addressView.getAddress(tag, index, offset);
  }

  public final long getTagMask() {
    return getAddress(getTag(-1L), 0, 0);
  }

  public final long getIndexMask() {
    return getAddress(0, getIndex(-1L), 0);
  }

  public final long getOffsetMask() {
    return getAddress(0, 0, getOffset(-1L));
  }

  // TODO:
  public final boolean checkGuard(final MemoryAccess access) {
    InvariantChecks.checkNotNull(access);
    InvariantChecks.checkTrue((guardCondition == null) == (guard == null));

    return guard != null ? guard.test(access) : true;
  }

  /**
   * Checks whether the buffer support data replacement.
   * 
   * @return {@code true} if the buffer supports data replacement; {@code false} otherwise.
   */
  public final boolean isReplaceable() {
    return replaceable;
  }

  // TODO:
  public final boolean isView() {
    return parent != null;
  }

  // TODO:
  public final MmuBuffer getParent() {
    return parent;
  }

  // TODO:
  public final boolean isParent() {
    return !children.isEmpty();
  }

  // TODO:
  public final List<MmuBuffer> getChildren() {
    return children;
  }
}
