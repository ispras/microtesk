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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.transformer.ValueProvider;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerUtils;
import ru.ispras.microtesk.mmu.basis.AddressView;
import ru.ispras.microtesk.utils.function.Function;

/**
 * {@link MmuAddressViewBuilder} implements an address view builder.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuAddressViewBuilder {
  private static Node createAddressExpression(
      final Variable addressVariable,
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
      expectedIndex += IntegerUtils.getBitSize(field);
    }

    return IntegerUtils.makeNodeConcat(operands);
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
      final Node node) {

    if (node.getKind() != Node.Kind.OPERATION) {
      return;
    }

    final NodeOperation operation = (NodeOperation) node;
    InvariantChecks.checkTrue(operation.getOperationId() == StandardOperation.BVCONCAT);

    int offset = 0;
    for (final Node addressField : operation.getOperands()) {
      final Node field = IntegerUtils.makeNodeExtract(
          variable, offset, (offset + IntegerUtils.getBitSize(addressField)) - 1);

      fields.put(IntegerUtils.getLowerBit(addressField), field);
      offset += IntegerUtils.getBitSize(addressField);
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

  public AddressView<BigInteger> build() {
    final int addressWidth = addressType.getWidth();
    final Variable addressVariable = addressType.getVariable();

    // Create the auxiliary variables to represent the address calculation function.
    final String variableNamePrefix = "var";

    for (int i = 0; i < expressions.size(); i++) {
      final Variable variable = new Variable(
          String.format("%s$%d", variableNamePrefix, i), DataType.BIT_VECTOR(addressWidth));

      variables.add(variable);
    }

    final Node addressExpression =
        createAddressExpression(addressVariable, variables, expressions);

    final AddressView<BigInteger> addressView = new AddressView<BigInteger>(
        new Function<BigInteger, List<BigInteger>>() {
          @Override
          public List<BigInteger> apply(final BigInteger addressValue) {
            InvariantChecks.checkNotNull(addressValue);

            final List<BigInteger> fields = new ArrayList<BigInteger>();

            for (final Node expression : expressions) {
              final BigInteger value = IntegerUtils.evaluate(
                  expression,
                  new ValueProvider() {
                    @Override
                    public Data getVariableValue(final Variable variable) {
                      return Data.newBitVector(addressValue, variable.getType().getSize());
                    }
                  });

              fields.add(value);
            }

            return fields;
          }
        },
        new Function<List<BigInteger>, BigInteger>() {
          @Override
          public BigInteger apply(final List<BigInteger> fields) {
            InvariantChecks.checkNotNull(fields);

            final Map<Variable, BigInteger> values = new LinkedHashMap<>();

            for (int i = 0; i < variables.size(); i++) {
              final Variable variable = variables.get(i);
              values.put(variable, fields.get(i));
            }

            final BigInteger addressValue = IntegerUtils.evaluate(
                addressExpression,
                new ValueProvider() {
                  @Override
                  public Data getVariableValue(final Variable variable) {
                    return Data.newBitVector(values.get(variable), variable.getType().getSize());
                  }
                });

            return addressValue;
          }
        });

    return addressView;
  }
}
