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

package ru.ispras.microtesk.translator.mmu.spec;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.mmu.spec.basis.AddressView;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerField;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerVariable;
import ru.ispras.microtesk.utils.function.Function;

/**
 * {@link MmuDevice} describes an MMU device (buffer).
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public final class MmuDevice {
  /** The device name. */
  private final String name;

  /** The number of ways (associativity). */
  private final long ways;
  /** The number of sets. */
  private final long sets;

  /** The MMU address. */
  private final MmuAddress address;

  /** The entry fields. */
  private final List<IntegerVariable> fields = new ArrayList<>();

  /** The tag calculation function. */
  private final MmuExpression tagExpression;
  /** The index calculation function. */
  private final MmuExpression indexExpression;
  /** The offset calculation function. */
  private final MmuExpression offsetExpression;

  /** The flag indicating whether the device supports data replacement. */
  private final boolean replaceable;

  // TODO: E.g., JTLB for DTLB.
  private final MmuDevice parent;
  // TODO: E.g., DTLB for JTLB.
  private final List<MmuDevice> children = new ArrayList<>();

  /** The auxiliary tag variable (used in the address calculation function). */
  private final IntegerVariable tagVariable;
  /** The auxiliary index variable (used in the address calculation function). */
  private final IntegerVariable indexVariable;
  /** The auxiliary offset variable (used in the address calculation function). */
  private final IntegerVariable offsetVariable;

  /** The address calculation function. */
  private final MmuExpression addressExpression;

  /** The address view. */
  private final AddressView<Long> addressView;

  /**
   * Constructs a MMU device.
   * 
   * @param name the device name.
   * @param ways the number of ways.
   * @param sets the number of sets.
   * @param address the address type.
   * @param tagExpression the tag calculation function.
   * @param indexExpression the index calculation function.
   * @param offsetExpression the offset calculation function.
   * @param replaceable the flag indicating that data stored in the device are replaceable.
   * @param parent TODO
   * 
   * @throws NullPointerException if some parameters are null.
   * @throws IllegalArgumentException of the address calculation function cannot be reconstructed.
   */
  public MmuDevice(
      final String name,
      final long ways,
      final long sets,
      final MmuAddress address,
      final MmuExpression tagExpression,
      final MmuExpression indexExpression,
      final MmuExpression offsetExpression,
      final boolean replaceable,
      final MmuDevice parent) {

    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(address);
    InvariantChecks.checkNotNull(tagExpression);
    InvariantChecks.checkNotNull(indexExpression);
    InvariantChecks.checkNotNull(offsetExpression);

    this.name = name;
    this.ways = ways;
    this.sets = sets;
    this.address = address;
    this.tagExpression = tagExpression;
    this.indexExpression = indexExpression;
    this.offsetExpression = offsetExpression;
    this.replaceable = replaceable;

    // TODO:
    this.parent = parent;
    if (parent != null) {
      parent.children.add(this);
    }

    final int addressWidth = address.getAddress().getWidth();

    // Create the auxiliary variables to represent the address calculation function.
    this.tagVariable = new IntegerVariable(String.format("%s$TAG", name), addressWidth);
    this.indexVariable = new IntegerVariable(String.format("%s$INDEX", name), addressWidth);
    this.offsetVariable = new IntegerVariable(String.format("%s$OFFSET", name), addressWidth);

    // Derive the address reconstruction expression.
    addressExpression = createAddressExpression(
        address.getAddress(),
        tagVariable,
        tagExpression,
        indexVariable,
        indexExpression,
        offsetVariable,
        offsetExpression
        );

    addressView = new AddressView<Long>(new Function<Long, List<Long>>() {
      @Override
      public List<Long> apply(final Long addr) {
        InvariantChecks.checkNotNull(addr);

        final BigInteger addrValue = BigInteger.valueOf(addr);

        final BigInteger tagValue =
            MmuCalculator.eval(tagExpression, address.getAddress(), addrValue);
        final BigInteger indexValue =
            MmuCalculator.eval(indexExpression, address.getAddress(), addrValue);
        final BigInteger offsetValue =
            MmuCalculator.eval(offsetExpression, address.getAddress(), addrValue);

        final List<Long> fields = new ArrayList<Long>();

        fields.add(tagValue.longValue());
        fields.add(indexValue.longValue());
        fields.add(offsetValue.longValue());

        return fields;
      }
    }, new Function<List<Long>, Long>() {
      @Override
      public Long apply(final List<Long> fields) {
        InvariantChecks.checkNotNull(fields);

        final Map<IntegerVariable, BigInteger> values = new LinkedHashMap<>();

        values.put(tagVariable, BigInteger.valueOf(fields.get(0)));
        values.put(indexVariable, BigInteger.valueOf(fields.get(1)));
        values.put(offsetVariable, BigInteger.valueOf(fields.get(2)));

        final BigInteger addressValue = MmuCalculator.eval(addressExpression, values);
        return addressValue.longValue();
      }
    });
  }

  /**
   * Creates an address calculation function (concatenation of tag, index and offset fields).
   * 
   * @param addressVariable the address variable.
   * @param tagVariable the tag variable.
   * @param tagExpression the tag calculation function.
   * @param indexVariable the index variable.
   * @param indexExpression the index calculation function.
   * @param offsetVariable the offset variable.
   * @param offsetExpression the offset calculation function.
   * @return the address calculation function.
   * 
   * @throws IllegalArgumentException if the function cannot be reconstructed.
   */
  private static MmuExpression createAddressExpression(
      final IntegerVariable addressVariable,
      final IntegerVariable tagVariable,
      final MmuExpression tagExpression,
      final IntegerVariable indexVariable,
      final MmuExpression indexExpression,
      final IntegerVariable offsetVariable,
      final MmuExpression offsetExpression) {

    final SortedMap<Integer, IntegerField> fields = new TreeMap<>();

    reverseAssignment(fields, tagVariable, tagExpression);
    reverseAssignment(fields, indexVariable, indexExpression);
    reverseAssignment(fields, offsetVariable, offsetExpression);

    final MmuExpression expression = new MmuExpression();

    int expectedIndex = 0;
    for (final Map.Entry<Integer, IntegerField> entry : fields.entrySet()) {
      final int index = entry.getKey();
      final IntegerField field = entry.getValue();

      if (index != expectedIndex) {
        throw new IllegalArgumentException(String.format(
            "Address function cannot be reconstructed: %d != %d (%s)",
            index, expectedIndex, fields));
      }

      expression.addHiTerm(field);
      expectedIndex += field.getWidth();
    }

    return expression;
  }

  /**
   * Reverses the assignment {@code variable = expression} and fills the {@code fields} map.
   * 
   * @param fields the map to be filled.
   * @param expression the right-hand-side expression.
   * @param variable the left-hand-side variable.
   */
  private static void reverseAssignment(
      final SortedMap<Integer, IntegerField> fields,
      final IntegerVariable variable,
      final MmuExpression expression) {

    int offset = 0;

    for (final IntegerField addressField : expression.getTerms()) {
      final IntegerField field =
          new IntegerField(variable, offset, (offset + addressField.getWidth()) - 1);

      fields.put(addressField.getLoIndex(), field);
      offset += addressField.getWidth();
    }
  }

  /**
   * Returns the name of the device.
   * 
   * @return the device name.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the number of ways (associativity).
   * 
   * @return the number of ways.
   */
  public long getWays() {
    return ways;
  }

  /**
   * Returns the number of sets.
   * 
   * @return the number of sets.
   */
  public long getSets() {
    return sets;
  }

  /**
   * Returns the input parameter.
   * 
   * @return the input parameter.
   */
  public MmuAddress getAddress() {
    return address;
  }

  /**
   * Returns the entry fields.
   * 
   * @return the entry fields.
   */
  public List<IntegerVariable> getFields() {
    return fields;
  }

  /**
   * Registers the entry field.
   * 
   * @param field the entry field to be registered.
   */
  public void addField(final IntegerVariable field) {
    InvariantChecks.checkNotNull(field);
    fields.add(field);
  }

  /**
   * Returns the tag calculation function.
   * 
   * @return the tag calculation function.
   */
  public MmuExpression getTagExpression() {
    return tagExpression;
  }

  /**
   * Returns the index calculation function.
   * 
   * @return the index calculation function.
   */
  public MmuExpression getIndexExpression() {
    return indexExpression;
  }

  /**
   * Returns the offset calculation function.
   * 
   * @return the offset calculation function.
   */
  public MmuExpression getOffsetExpression() {
    return offsetExpression;
  }

  /**
   * Returns the address view.
   * 
   * @return the address view.
   */
  public AddressView<Long> getAddressView() {
    return addressView;
  }

  /**
   * Returns the address tag.
   * 
   * @param address the address.
   * @return the value of the tag.
   */
  public long getTag(long address) {
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
  public long getOffset(long address) {
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
  public long getAddress(long tag, long index, long offset) {
    return addressView.getAddress(tag, index, offset);
  }

  /**
   * Checks whether the device support data replacement.
   * 
   * @return {@code true} if the device supports data replacement; {@code false} otherwise.
   */
  public boolean isReplaceable() {
    return replaceable;
  }

  // TODO:
  public boolean isView() {
    return parent != null;
  }

  // TODO:
  public MmuDevice getParent() {
    return parent;
  }

  // TODO:
  public boolean isParent() {
    return !children.isEmpty();
  }

  // TODO:
  public List<MmuDevice> getChildren() {
    return children;
  }

  @Override
  public String toString() {
    return name;
  }
}
