/*
 * Copyright 2013-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.sequence.engine.utils;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.microtesk.test.sequence.engine.utils.EngineUtils.makeMode;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.ArgumentMode;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.memory.Location;
import ru.ispras.microtesk.test.sequence.engine.EngineContext;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.LazyValue;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.RandomValue;
import ru.ispras.microtesk.test.template.UnknownImmediateValue;
import ru.ispras.testbase.TestBaseQueryBuilder;

public final class BindingBuilder {
  private EngineContext engineContext;
  private final TestBaseQueryBuilder queryBuilder;
  private final Map<String, Argument> unknownValues;
  private final Map<String, Argument> modes;

  public BindingBuilder(
      final EngineContext engineContext,
      final TestBaseQueryBuilder queryBuilder,
      final Primitive primitive) {
    checkNotNull(engineContext);
    checkNotNull(queryBuilder);
    checkNotNull(primitive);

    this.engineContext = engineContext;
    this.queryBuilder = queryBuilder;
    this.unknownValues = new HashMap<String, Argument>();
    this.modes = new HashMap<String, Argument>();

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
              new NodeValue(Data.newBitVector(BitVector.valueOf(
                  (BigInteger) arg.getValue(), arg.getType().getBitSize())))
              );
          break;

        case IMM_RANDOM:
          queryBuilder.setBinding(
              argName, 
              new NodeValue(Data.newBitVector(BitVector.valueOf(
                  ((RandomValue) arg.getValue()).getValue(), arg.getType().getBitSize())))
              );
          break;

        case IMM_LAZY:
          queryBuilder.setBinding(
              argName, 
              new NodeValue(Data.newBitVector(BitVector.valueOf(
                  ((LazyValue) arg.getValue()).getValue(), arg.getType().getBitSize())))
              );
          break;

        case IMM_UNKNOWN:
          final UnknownImmediateValue unknownValue = (UnknownImmediateValue) arg.getValue();

          if (!unknownValue.isValueSet()) {
            queryBuilder.setBinding(
                argName,
                new NodeVariable(argName, DataType.BIT_VECTOR(arg.getType().getBitSize()))
                );

            unknownValues.put(argName, arg);
          } else {
            queryBuilder.setBinding(
                argName,
                new NodeValue(Data.newBitVector(BitVector.valueOf(
                    unknownValue.getValue(), arg.getType().getBitSize())))
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
            final DataType dataType = DataType.BIT_VECTOR(arg.getType().getBitSize());
            Node bindingValue = null;

            try {
              if (arg.getMode() != ArgumentMode.NA) {
                final IAddressingMode mode = makeMode(engineContext, arg);
                final Location location = mode.access();

                if (location.isInitialized()) {
                  bindingValue = NodeValue.newBitVector(
                      BitVector.valueOf(location.getValue(), location.getBitSize()));
                } else {
                  bindingValue = new NodeVariable(argName, dataType);
                }
              } else {
                bindingValue = new NodeVariable(argName, dataType);
              }
            } catch (ConfigurationException e) {
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
