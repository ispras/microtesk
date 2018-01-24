/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.Nodes;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.transformer.ValueProvider;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.AddressView;
import ru.ispras.microtesk.utils.FortressUtils;
import ru.ispras.microtesk.utils.function.Function;

/**
 * {@link MmuAddressViewBuilder} implements an address view builder.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuAddressViewBuilder {
  private static Node createAddressExpression(
      final NodeVariable addressVariable,
      final List<Variable> variables,
      final List<Node> expressions) {
    InvariantChecks.checkNotNull(addressVariable);
    InvariantChecks.checkNotNull(variables);
    InvariantChecks.checkNotNull(expressions);
    InvariantChecks.checkTrue(variables.size() == expressions.size());

    final SortedMap<Integer, Node> fields = new TreeMap<>();

    for (int i = 0; i < variables.size(); i++) {
      final Variable variable = variables.get(i);
      final Node expression = expressions.get(i);

      reverseAssignment(fields, variable, expression);
    }

    final List<Node> operands = new ArrayList<>(fields.size());

    int expectedIndex = 0;
    for (final Map.Entry<Integer, Node> entry : fields.entrySet()) {
      final int index = entry.getKey();
      InvariantChecks.checkTrue(index == expectedIndex,
          String.format("Address function cannot be reconstructed: %d != %d (%s)",
              index, expectedIndex, fields));

      final Node field = entry.getValue();

      operands.add(field);
      expectedIndex += FortressUtils.getBitSize(field);
    }

    return Nodes.reverseBvconcat(operands);
  }

  /**
   * Reverses the assignment {@code variable = expression} and fills the {@code fields} map.
   *
   * @param fields the map to be filled.
   * @param expression the right-hand-side expression.
   * @param variable the left-hand-side variable.
   */
  private static void reverseAssignment(
      final SortedMap<Integer, Node> fields,
      final Variable variable,
      final Node expression) {
    if (expression.getKind() == Node.Kind.VALUE) {
      return;
    }

    final List<Node> addressFields;

    if (expression.getKind() == Node.Kind.OPERATION) {
      final NodeOperation operation = (NodeOperation) expression;

      if (operation.getOperationId() == StandardOperation.BVCONCAT) {
        addressFields = operation.getOperands();
      } else {
        InvariantChecks.checkTrue(operation.getOperationId() == StandardOperation.BVEXTRACT);
        addressFields = Collections.<Node>singletonList(expression);
      }
    } else {
      addressFields = Collections.<Node>singletonList(expression);
    }

    int offset = 0;
    for (final Node addressField : addressFields) {
      final Node field = Nodes.bvextract(
          (offset + FortressUtils.getBitSize(addressField)) - 1, offset, variable);

      fields.put(FortressUtils.getLowerBit(addressField), field);
      offset += FortressUtils.getBitSize(addressField);
    }
  }

  private final MmuAddressInstance addressType;
  private final List<Node> expressions = new ArrayList<>();

  /** Auxiliary variables that represent values of the fields. */
  private final List<Variable> variables = new ArrayList<>();

  public MmuAddressViewBuilder(final MmuAddressInstance addressType, final Node ... fields) {
    InvariantChecks.checkNotNull(addressType);
    InvariantChecks.checkNotNull(fields);

    this.addressType = addressType;

    for (final Node expression : fields) {
      this.expressions.add(expression);
    }
  }

  public AddressView<BitVector> build() {
    final int addressWidth = addressType.getWidth();
    final NodeVariable addressVariable = addressType.getVariable();

    // Create the auxiliary variables to represent the address calculation function.
    final String variableNamePrefix = "var";

    for (int i = 0; i < expressions.size(); i++) {
      final Variable variable = new Variable(
          String.format("%s$%d", variableNamePrefix, i), DataType.bitVector(addressWidth));

      variables.add(variable);
    }

    final Node addressExpression =
        createAddressExpression(addressVariable, variables, expressions);

    final AddressView<BitVector> addressView = new AddressView<BitVector>(
        new Function<BitVector, List<BitVector>>() {
          @Override
          public List<BitVector> apply(final BitVector addressValue) {
            InvariantChecks.checkNotNull(addressValue);
            InvariantChecks.checkTrue(addressValue.getBitSize() == addressWidth);

            final List<BitVector> fields = new ArrayList<BitVector>();

            for (final Node expression : expressions) {
              final BitVector value = FortressUtils.evaluateBitVector(
                  expression,
                  new ValueProvider() {
                    @Override
                    public Data getVariableValue(final Variable variable) {
                      return Data.newBitVector(addressValue);
                    }
                  });

              InvariantChecks.checkNotNull(value, String.format("Cannot evaluate: %s", expression));
              fields.add(value);
            }

            return fields;
          }
        },
        new Function<List<BitVector>, BitVector>() {
          @Override
          public BitVector apply(final List<BitVector> fields) {
            InvariantChecks.checkNotNull(fields);

            final Map<Variable, BitVector> values = new LinkedHashMap<>();

            for (int i = 0; i < variables.size(); i++) {
              final Variable variable = variables.get(i);
              values.put(variable, fields.get(i));
              System.out.println("Value: " + fields.get(i).toHexString());
            }

            System.out.println(addressExpression);
            final BitVector addressValue = FortressUtils.evaluateBitVector(
                addressExpression,
                new ValueProvider() {
                  @Override
                  public Data getVariableValue(final Variable variable) {
                    return Data.newBitVector(values.get(variable));
                  }
                });

            return addressValue;
          }
        });

    return addressView;
  }
}
