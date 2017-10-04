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

package ru.ispras.microtesk.mmu.translator.generation.spec;

import java.math.BigInteger;
import java.util.List;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerUtils;
import ru.ispras.microtesk.mmu.translator.ir.Constant;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Var;

public final class Utils {
  private Utils() {}

  public static String toString(final BigInteger value) {
    InvariantChecks.checkNotNull(value);
    return String.format("new BigInteger(\"%d\", 10)", value);
  }

  public static String getVariableName(final String context, final String name) {
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(name);

    final int dotIndex = name.indexOf('.');
    if (dotIndex == -1) {
      return name + ".get()";
    }

    final String prefix = name.substring(0, dotIndex);
    final String suffix = name.substring(dotIndex + 1, name.length());

    if (prefix.equals(context)) {
      return suffix;
    }

    return prefix + ".get()." + suffix;
  }

  public static String getVariableName(
      final Ir ir,
      final String context,
      final Variable variable) {
    InvariantChecks.checkNotNull(ir);
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(variable);

    final String name = variable.getName();
    final Constant constant = ir.getConstants().get(name);

    if (null != constant) {
      final DataType type = constant.getVariable().getDataType();
      if (variable.getType().getSize() == type.getSize()) {
        return name + ".get()";
      } else {
        return String.format("%s.get(%d)", name, variable.getType().getSize());
      }
    }

    return getVariableName(context, name);
  }

  public static String toString(final String context, final Node field) {
    return toString(context, field, true);
  }

  public static String toString(
      final String context,
      final Node field,
      final boolean printAsVariable) {
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(field);

    if (IntegerUtils.getVariable(field).hasValue()) {
      return Utils.toString(IntegerUtils.getVariable(field).getData().getInteger());
    }

    final String name = getVariableName(context, IntegerUtils.getVariable(field).getName());
    if (field.getKind() == Node.Kind.VARIABLE && printAsVariable) {
      return name;
    }

    return String.format(
        "%s.field(%d, %d)", name, IntegerUtils.getLowerBit(field), IntegerUtils.getUpperBit(field));
  }

  public static String toMmuExpressionText(final String context, final List<Node> fields) {
    InvariantChecks.checkNotNull(context);

    if (null == fields) {
      return "null";
    }

    if (fields.isEmpty()) {
      return "MmuExpression.empty()";
    }

    if (fields.size() == 1) {
      return toMmuExpressionText(context, fields.get(0));
    }

    final StringBuilder sb = new StringBuilder();
    sb.append("MmuExpression.rcat(");

    boolean isFirst = true;
    for (final Node field : fields) {
      if (isFirst) {
        isFirst = false;
      } else {
        sb.append(", ");
      }

      final Variable variable = IntegerUtils.getVariable(field);
      final String variableText;

      if (variable.hasValue()) {
        variableText = String.format("new Variable(%d, %s)",
            variable.getType().getSize(), toString(variable.getData().getInteger()));
      } else {
        variableText =
            getVariableName(context, variable.getName());
      }

      final String text = String.format("%s.field(%d, %d)",
          variableText, IntegerUtils.getLowerBit(field), IntegerUtils.getUpperBit(field));

      sb.append(text);
    }

    sb.append(')');
    return sb.toString();
  }

  public static String toMmuExpressionText(final String context, final Node field) {
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(field);

    final StringBuilder sb = new StringBuilder();
    sb.append("MmuExpression.");

    if (IntegerUtils.getVariable(field).hasValue()) {
      sb.append(String.format(
          "val(%s, %d",
          toString(IntegerUtils.getVariable(field).getData().getInteger()),
          IntegerUtils.getBitSize(field)));
    } else {
      final String name = getVariableName(context, IntegerUtils.getVariable(field).getName());
      sb.append(String.format("var(%s", name));

      if (field.getKind() != Node.Kind.VARIABLE) {
        sb.append(String.format(", %d, %d", IntegerUtils.getLowerBit(field), IntegerUtils.getUpperBit(field)));
      }
    }

    sb.append(')');
    return sb.toString();
  }

  @SuppressWarnings("unchecked")
  public static String toString(
      final Ir ir,
      final String context,
      final Atom atom) {
    InvariantChecks.checkNotNull(ir);
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(atom);

    final Object object = atom.getObject();
    switch (atom.getKind()) {
      case VALUE:
        return toString((BigInteger) object);

      case VARIABLE:
        return getVariableName(ir, context, (Variable) object);

      case FIELD: {
        final Node field = (Node) object;
        final Variable variable = IntegerUtils.getVariable(field);
        return String.format("%s.field(%d, %d)",
            getVariableName(ir, context, variable),
            IntegerUtils.getLowerBit(field),
            IntegerUtils.getUpperBit(field)
            );
      }

      case GROUP:
        return getVariableName(context, ((Var) object).getName());

      case CONCAT:
        return toMmuExpressionText(context, (List<Node>) object);

      default:
        throw new IllegalStateException("Unsupported atom kind: " + atom.getKind());
    }
  }
}
