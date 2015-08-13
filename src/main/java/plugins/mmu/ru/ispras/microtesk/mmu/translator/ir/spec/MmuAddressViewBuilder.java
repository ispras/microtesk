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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.basis.AddressView;
import ru.ispras.microtesk.utils.function.Function;

/**
 * {@link MmuAddressViewBuilder} implements an address view builder.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuAddressViewBuilder {
  /**
   * Creates an address calculation function (concatenation of fields).
   * 
   * @param addressVariable the address variable.
   * @param variables the list of field variables.
   * @param expressions the list of field expressions.
   * @return the address calculation function.
   * @throws IllegalArgumentException if the function cannot be reconstructed.
   */
  private static MmuExpression createAddressExpression(
      final IntegerVariable addressVariable,
      final List<IntegerVariable> variables,
      final List<MmuExpression> expressions) {
    InvariantChecks.checkNotNull(addressVariable);
    InvariantChecks.checkNotNull(variables);
    InvariantChecks.checkNotNull(expressions);
    InvariantChecks.checkTrue(variables.size() == expressions.size());

    final SortedMap<Integer, IntegerField> fields = new TreeMap<>();

    for (int i = 0; i < variables.size(); i++) {
      final IntegerVariable variable = variables.get(i);
      final MmuExpression expression = expressions.get(i);

      reverseAssignment(fields, variable, expression);
    }

    final List<IntegerField> concatenation = new ArrayList<>(fields.size());

    int expectedIndex = 0;
    for (final Map.Entry<Integer, IntegerField> entry : fields.entrySet()) {
      final int index = entry.getKey();
      final IntegerField field = entry.getValue();

      if (index != expectedIndex) {
        throw new IllegalArgumentException(String.format(
            "Address function cannot be reconstructed: %d != %d (%s)",
            index, expectedIndex, fields));
      }

      concatenation.add(field);
      expectedIndex += field.getWidth();
    }

    return MmuExpression.cat(concatenation);
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

  private final MmuAddressType addressType;
  private final List<MmuExpression> expressions = new ArrayList<>();

  /** Auxiliary variables that represent values of the fields. */
  private final List<IntegerVariable> variables = new ArrayList<>();

  public MmuAddressViewBuilder(final MmuAddressType addressType, final MmuExpression ... fields) {
    InvariantChecks.checkNotNull(addressType);
    InvariantChecks.checkNotNull(fields);

    this.addressType = addressType;

    for (final MmuExpression expression : fields) {
      this.expressions.add(expression);
    }
  }

  public AddressView<Long> build() {
    final int addressWidth = addressType.getWidth();
    final IntegerVariable addressVariable = addressType.getVariable();

    // Create the auxiliary variables to represent the address calculation function.
    final String variableNamePrefix = "var";

    for (int i = 0; i < expressions.size(); i++) {
      final IntegerVariable variable =
          new IntegerVariable(String.format("%s$%d", variableNamePrefix, i), addressWidth);

      variables.add(variable);
    }

    final MmuExpression addressExpression =
        createAddressExpression(addressVariable, variables, expressions);

    final AddressView<Long> addressView = new AddressView<Long>(new Function<Long, List<Long>>() {
      @Override
      public List<Long> apply(final Long address) {
        InvariantChecks.checkNotNull(address);

        final List<Long> fields = new ArrayList<Long>();
        final BigInteger addressValue = BigInteger.valueOf(address);

        for (final MmuExpression expression : expressions) {
          final BigInteger value = MmuCalculator.eval(expression, addressVariable, addressValue);
          fields.add(value.longValue());
        }

        return fields;
      }
    }, new Function<List<Long>, Long>() {
      @Override
      public Long apply(final List<Long> fields) {
        InvariantChecks.checkNotNull(fields);

        final Map<IntegerVariable, BigInteger> values = new LinkedHashMap<>();

        for (final IntegerVariable variable : variables) {
          values.put(variable, BigInteger.valueOf(fields.get(0)));
        }

        final BigInteger addressValue = MmuCalculator.eval(addressExpression, values);
        return addressValue.longValue();
      }
    });

    return addressView;
  }
}
