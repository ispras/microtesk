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
 * This class describes a MMU device (buffer).
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class MmuDevice {
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
   * @throws NullPointerException if some parameters are null.
   * @throws IllegalArgumentException of the address calculation function cannot be reconstructed.
   */
  public MmuDevice(final String name, final long ways, final long sets, final MmuAddress address,
      final MmuExpression tagExpression, final MmuExpression indexExpression,
      final MmuExpression offsetExpression, boolean replaceable) {

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

    final int addressWidth = address.getAddress().getWidth();

    // Create the auxiliary variables to represent the address calculation function.
    tagVariable = new IntegerVariable(String.format("%s$TAG", name), addressWidth);
    indexVariable = new IntegerVariable(String.format("%s$INDEX", name), addressWidth);
    offsetVariable = new IntegerVariable(String.format("%s$OFFSET", name), addressWidth);

    // Derive the address reconstruction expression.
    addressExpression =
        createAddressExpression(address.getAddress(), tagVariable, tagExpression, indexVariable,
            indexExpression, offsetVariable, offsetExpression);

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
   * @throws IllegalArgumentException if the function cannot be reconstructed.
   */
  private MmuExpression createAddressExpression(final IntegerVariable addressVariable,
      final IntegerVariable tagVariable, final MmuExpression tagExpression,
      final IntegerVariable indexVariable, final MmuExpression indexExpression,
      final IntegerVariable offsetVariable, final MmuExpression offsetExpression) {
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
        throw new IllegalArgumentException(
            String.format("Address function cannot be reconstructed: %d != %d (%s)", index,
                expectedIndex, fields));
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
  private void reverseAssignment(final SortedMap<Integer, IntegerField> fields,
      final IntegerVariable variable, final MmuExpression expression) {
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
  public long getTag(final long address) {
    return addressView.getTag(address);
  }

  /**
   * Returns the address index.
   * 
   * @param address the address.
   * @return the value of the index.
   */
  public long getIndex(final long address) {
    return addressView.getIndex(address);
  }

  /**
   * Returns the address offset.
   * 
   * @param address the address.
   * @return the value of the offset.
   */
  public long getOffset(final long address) {
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
  public long getAddress(final long tag, final long index, final long offset) {
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

  /**
   * Returns the list of the device conflicts.
   * 
   * The device conflicts can be one of the following categories: 1. indexes aren't equal; 2.
   * indexes are equal, tags are equal; 3. indexes are equal, tags aren't equal; 4. indexes are
   * equal, tags aren't equal, tag1 isn't equal to Replaced2; 5. indexes are equal, tags aren't
   * equal, tag1 is equal to Replaced2.
   * 
   * @return the conflicts list.
   */
  public List<MmuConflict> getConflicts() {
    final List<MmuConflict> conflictList = new ArrayList<>();

    if (sets > 1 && indexExpression != null) {
      final MmuCondition conditionIndexNoEqual = new MmuCondition();

      final MmuEquality equalityIndexNoEqual =
          new MmuEquality(MmuEquality.Type.NOT_EQUAL, indexExpression);
      conditionIndexNoEqual.addEquality(equalityIndexNoEqual);

      // Index1 != Index2.
      final MmuConflict conflictIndexNoEqual =
          new MmuConflict(MmuConflict.Type.INDEX_NOT_EQUAL, this, conditionIndexNoEqual);
      conflictList.add(conflictIndexNoEqual);
    }

    if (tagExpression != null) {
      final MmuCondition conditionTagNoEqual = new MmuCondition();

      if (sets > 1 && indexExpression != null) {
        final MmuEquality equalityIndexEqual =
            new MmuEquality(MmuEquality.Type.EQUAL, indexExpression);
        conditionTagNoEqual.addEquality(equalityIndexEqual);
      }

      final MmuEquality equalityTagNoEqual =
          new MmuEquality(MmuEquality.Type.NOT_EQUAL, tagExpression);
      conditionTagNoEqual.addEquality(equalityTagNoEqual);

      if (!replaceable) {
        // Index1 == Index2 && Tag1 != Tag2.
        final MmuConflict conflictTagNoEqual =
            new MmuConflict(MmuConflict.Type.TAG_NOT_EQUAL, this, conditionTagNoEqual);
        conflictList.add(conflictTagNoEqual);
      } else {
        final MmuCondition conditionTagNoReplaced = new MmuCondition(conditionTagNoEqual);

        final MmuEquality equalityTagNoReplaced =
            new MmuEquality(MmuEquality.Type.NOT_EQUAL_REPLACED, tagExpression);

        conditionTagNoReplaced.addEquality(equalityTagNoReplaced);

        // Index1 == Index2 && Tag1 != Tag2 && Tag1 != Replaced2.
        final MmuConflict conflictTagNoReplaced =
            new MmuConflict(MmuConflict.Type.TAG_NOT_REPLACED, this, conditionTagNoReplaced);
        conflictList.add(conflictTagNoReplaced);

        final MmuCondition conditionTagReplaced = new MmuCondition(conditionTagNoEqual);

        final MmuEquality equalityTagReplaced =
            new MmuEquality(MmuEquality.Type.EQUAL_REPLACED, tagExpression);

        conditionTagReplaced.addEquality(equalityTagReplaced);

        // Index1 == Index2 && Tag1 != Tag2 && Tag1 == Replaced2.
        final MmuConflict conflictTagReplaced =
            new MmuConflict(MmuConflict.Type.TAG_REPLACED, this, conditionTagReplaced);
        conflictList.add(conflictTagReplaced);
      }

      final MmuCondition conditionTagEqual = new MmuCondition();

      if (sets > 1 && indexExpression != null) {
        final MmuEquality equalityIndexEqual =
            new MmuEquality(MmuEquality.Type.EQUAL, indexExpression);
        conditionTagEqual.addEquality(equalityIndexEqual);
      }

      final MmuEquality equalityTagEqual = new MmuEquality(MmuEquality.Type.EQUAL, tagExpression);
      conditionTagEqual.addEquality(equalityTagEqual);

      // Index1 == Index2 && Tag1 == Tag2.
      final MmuConflict conflictTagEqual =
          new MmuConflict(MmuConflict.Type.TAG_EQUAL, this, conditionTagEqual);
      conflictList.add(conflictTagEqual);
    }

    return conflictList;
  }

  @Override
  public String toString() {
    return name;
  }
}
