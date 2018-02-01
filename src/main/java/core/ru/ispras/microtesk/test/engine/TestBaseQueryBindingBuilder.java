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
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.ArgumentMode;
import ru.ispras.microtesk.model.ConfigurationException;
import ru.ispras.microtesk.model.IsaPrimitive;
import ru.ispras.microtesk.model.Model;
import ru.ispras.microtesk.model.data.Type;
import ru.ispras.microtesk.model.memory.Location;
import ru.ispras.microtesk.model.metadata.MetaAddressingMode;
import ru.ispras.microtesk.test.engine.EngineUtils;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.UnknownImmediateValue;
import ru.ispras.microtesk.test.template.Value;
import ru.ispras.microtesk.utils.StringUtils;
import ru.ispras.testbase.TestBaseQueryBuilder;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

final class TestBaseQueryBindingBuilder {
  private final EngineContext engineContext;
  private final TestBaseQueryBuilder queryBuilder;
  private final Map<String, Argument> unknownValues;
  private final Map<String, Primitive> modes;

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

    if (primitive.getKind() == Primitive.Kind.MODE) {
      visitMode(primitive);
    }

    visit(primitive.getName(), primitive);
  }

  public Map<String, Argument> getUnknownValues() {
    return unknownValues;
  }

  public Map<String, Primitive> getTargetModes() {
    return modes;
  }

  private void visit(final String prefix, final Primitive primitive) {
    for (final Argument arg : primitive.getArguments().values()) {
      final String argName = StringUtils.dotConc(prefix, arg.getName());

      switch (arg.getKind()) {
        case IMM:
          setBindingValue(argName, (BigInteger) arg.getValue(), arg.getType().getBitSize());
          break;

        case IMM_RANDOM:
        case IMM_LAZY:
        case LABEL:
          setBindingValue(argName, (Value) arg.getValue(), arg.getType().getBitSize());
          break;

        case IMM_UNKNOWN: {
          final UnknownImmediateValue unknownValue = (UnknownImmediateValue) arg.getValue();
          if (unknownValue.isValueSet()) {
            setBindingValue(argName, unknownValue, arg.getType().getBitSize());
          } else {
            setBindingVariable(argName, arg.getType().getBitSize());
            unknownValues.put(argName, arg);
          }
          break;
        }

        case MODE: {
          // The mode's arguments should be processed before processing the mode.
          // Otherwise, if there are unknown values, the mode cannot be instantiated.
          final Primitive modePrimitive = (Primitive) arg.getValue();
          visit(argName, modePrimitive);

          // If a MODE has no return expression it is treated as OP and
          // it is NOT added to bindings and mode list
          if (arg.getMode() != ArgumentMode.NA) {
            setBindingMode(argName, modePrimitive);
            if (arg.getMode().isIn()) {
              modes.put(argName, (Primitive) arg.getValue());
            }
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

  private void visitMode(final Primitive modePrimitive) {
    final String name = modePrimitive.getName();
    final Model model = engineContext.getModel();

    final MetaAddressingMode metaMode = model.getMetaData().getAddressingMode(name);
    InvariantChecks.checkNotNull(metaMode);

    final Type type = metaMode.getDataType();
    if (null == type) {
      // No return expression.
      return;
    }

    setBindingMode(name, modePrimitive);
    modes.put(name, modePrimitive);
  }

  private void setBindingValue(final String name, final BigInteger value, int bitSize) {
    queryBuilder.setBinding(name, NodeValue.newBitVector(value, bitSize));
  }

  private void setBindingValue(final String name, final Value value, int bitSize) {
    setBindingValue(name, value.getValue(), bitSize);
  }

  private void setBindingVariable(final String name, final int bitSize) {
    queryBuilder.setBinding(name, new NodeVariable(name, DataType.bitVector(bitSize)));
  }

  private void setBindingMode(final String name, final Primitive modePrimitive) {
    final Model model = engineContext.getModel();
    try {
      final IsaPrimitive mode = EngineUtils.makeMode(engineContext, modePrimitive);
      final Location location = mode.access(model.getPE(), model.getTempVars());

      if (location.isInitialized()) {
        setBindingValue(name, location.getValue(), location.getBitSize());
      } else {
        setBindingVariable(name, location.getBitSize());
      }
    } catch (final ConfigurationException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
