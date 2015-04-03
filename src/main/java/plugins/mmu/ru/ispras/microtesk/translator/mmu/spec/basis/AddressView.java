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

package ru.ispras.microtesk.translator.mmu.spec.basis;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.utils.function.Function;

/**
 * This class implements an address view, i.e. a set of methods for composing/decomposing address
 * into fields.
 * 
 * @param <A> address type.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AddressView<A> {

  /** The address split functor. */
  private final Function<A, List<A>> split;
  /** The address merge functor. */
  private final Function<List<A>, A> merge;

  /**
   * Constructs an address view.
   * 
   * @param split the address split functor (it splits an address into the fields).
   * @param merge the address merge functor (it merges fields to produce the address).
   * @throws NullPointerException if {@code split} or {@code merge} is null.
   */
  public AddressView(final Function<A, List<A>> split, final Function<List<A>, A> merge) {
    InvariantChecks.checkNotNull(split);
    InvariantChecks.checkNotNull(merge);

    this.split = split;
    this.merge = merge;
  }

  /**
   * Returns the fields of the address.
   * 
   * @param address the address.
   * @return the list of fields.
   * @throws NullPointerException if {@code address} is null.
   */
  public List<A> getFields(final A address) {
    InvariantChecks.checkNotNull(address);

    return split.apply(address);
  }

  /**
   * Returns the field of the address.
   * 
   * @param address the address.
   * @param i the field index.
   * @return the list of fields.
   * @throws NullPointerException if {@code address} is null.
   * @throws IndexOutOfBoundsException if {@code i} is out of bounds.
   */
  public A getField(final A address, final int i) {
    InvariantChecks.checkNotNull(address);

    final List<A> fields = split.apply(address);
    InvariantChecks.checkBounds(i, fields.size());

    return fields.get(i);
  }

  /**
   * Returns the address for the given fields.
   * 
   * @param fields the fields.
   * @return the address.
   * @throws NullPointerException if {@code fields} is null.
   */
  public A getAddress(final List<A> fields) {
    InvariantChecks.checkNotNull(fields);

    return merge.apply(fields);
  }

  /**
   * Returns the tag of the given address ({@code fields[0]}).
   * 
   * @param address the address.
   * @return the tag.
   * @throws NullPointerException if {@code address} is null.
   */
  public A getTag(final A address) {
    return getField(address, 0);
  }

  /**
   * Returns the index of the given address ({@code fields[1]}).
   * 
   * @param address the address.
   * @return the index.
   * @throws NullPointerException if {@code address} is null.
   */
  public A getIndex(final A address) {
    return getField(address, 1);
  }

  /**
   * Returns the offset of the given address ({@code fields[2]}).
   * 
   * @param address the address.
   * @return the offset.
   * @throws NullPointerException if {@code address} is null.
   */
  public A getOffset(final A address) {
    return getField(address, 2);
  }

  /**
   * Returns the address to the given tag, index and offset.
   * 
   * @param tag the tag.
   * @param index the index.
   * @param offset the offset.
   * @return the address.
   * @throws NullPointerException if {@code index}, {@code tag} or {@code offset} is null.
   */
  public A getAddress(final A tag, final A index, final A offset) {
    InvariantChecks.checkNotNull(tag);
    InvariantChecks.checkNotNull(index);
    InvariantChecks.checkNotNull(offset);

    final List<A> fields = new ArrayList<>();
    fields.add(tag);
    fields.add(index);
    fields.add(offset);

    return merge.apply(fields);
  }
}
