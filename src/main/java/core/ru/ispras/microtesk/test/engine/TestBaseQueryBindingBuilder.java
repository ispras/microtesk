/*
 * Copyright 2013-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.engine;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.ArgumentMode;
import ru.ispras.microtesk.model.ConfigurationException;
import ru.ispras.microtesk.model.IsaPrimitive;
import ru.ispras.microtesk.model.Model;
import ru.ispras.microtesk.model.memory.Location;
import ru.ispras.microtesk.test.engine.EngineUtils;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.LabelValue;
import ru.ispras.microtesk.test.template.LazyValue;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.RandomValue;
import ru.ispras.microtesk.test.template.UnknownImmediateValue;
import ru.ispras.testbase.TestBaseQueryBuilder;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

final class TestBaseQueryBindingBuilder {
  private EngineContext engineContext;
  private final TestBaseQueryBuilder queryBuilder;
  private final Map<String, Argument> unknownValues;
  private final Map<String, Argument> modes;

  public TestBaseQueryBindingBuilder(
      final EngineContext engineContext,
      final TestBaseQueryBuilder queryBuilder,
      final Primitive primitive) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(queryBuilder);
    InvariantChecks.checkNotNull(primitive);

    this.engineContext = engineContext;
    this.queryBuilder = queryBuilder;
    this.unknownValues = new HashMap<>();
    this.modes = new HashMap<>();

    visit(primitive.getName(), primitive);
  }

  public Map<String, Argument> getUnknownValues() {
    return unknownValues;
  }

  public Map<String, Argument> getModes() {
    return modes;
  }

  private void visit(final String prefix, final Primitive p) {
    for (final Argument arg : p.getArguments().values()) {
      final String argName = prefix.isEmpty() ?
          arg.getName() : String.format("%s.%s", prefix, arg.getName());

      switch (arg.getKind()) {
        case IMM:
          queryBuilder.setBinding(
              argName,
              NodeValue.newBitVector(
                  (BigInteger) arg.getValue(), arg.getType().getBitSize())
              );
          break;

        case IMM_RANDOM:
          queryBuilder.setBinding(
              argName, 
              NodeValue.newBitVector(
                  ((RandomValue) arg.getValue()).getValue(), arg.getType().getBitSize())
              );
          break;

        case IMM_LAZY:
          queryBuilder.setBinding(
              argName, 
              NodeValue.newBitVector(
                  ((LazyValue) arg.getValue()).getValue(), arg.getType().getBitSize())
              );
          break;

        case LABEL:
          queryBuilder.setBinding(
              argName, 
              NodeValue.newBitVector(
                  ((LabelValue) arg.getValue()).getValue(), arg.getType().getBitSize())
              );
          break;

        case IMM_UNKNOWN:
          final UnknownImmediateValue unknownValue = (UnknownImmediateValue) arg.getValue();

          if (!unknownValue.isValueSet()) {
            queryBuilder.setBinding(
                argName,
                new NodeVariable(argName, DataType.bitVector(arg.getType().getBitSize()))
                );

            unknownValues.put(argName, arg);
          } else {
            queryBuilder.setBinding(
                argName,
                NodeValue.newBitVector(
                    unknownValue.getValue(), arg.getType().getBitSize())
                );
          }
          break;

        case MODE: {
          // The mode's arguments should be processed before processing the mode.
          // Otherwise, if there are unknown values, the mode cannot be instantiated.
          visit(argName, (Primitive) arg.getValue());

          // If a MODE has no return expression it is treated as OP and
          // it is NOT added to bindings and mode list
          if (arg.getMode() != ArgumentMode.NA) {
            final DataType dataType = DataType.bitVector(arg.getType().getBitSize());
            Node bindingValue = null;

            try {
                final IsaPrimitive mode = EngineUtils.makeMode(engineContext, arg);
                final Model model = engineContext.getModel();
                final Location location = mode.access(model.getPE(), model.getTempVars());

                if (location.isInitialized()) {
                  bindingValue = NodeValue.newBitVector(location.getValue(), location.getBitSize());
                } else {
                  bindingValue = new NodeVariable(argName, dataType);
                }
            } catch (final ConfigurationException e) {
              Logger.error("Failed to read data from %s. Reason: %s",
                  arg.getTypeName(), e.getMessage());

              bindingValue = new NodeVariable(argName, dataType);
            }

            queryBuilder.setBinding(argName, bindingValue);
            modes.put(argName, arg);
          }

          break;
        }

        case OP:
          visit(argName, (Primitive) arg.getValue());
          break;

        default:
          throw new IllegalArgumentException(String.format(
            "Illegal kind of argument %s: %s.", argName, arg.getKind()));
      }
    }
  }
}
