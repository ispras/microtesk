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
import ru.ispras.fortress.util.InvariantChecks;
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
      final IntegerVariable variable) {
    InvariantChecks.checkNotNull(ir);
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(variable);

    final String name = variable.getName();
    final Constant constant = ir.getConstants().get(name);

    if (null != constant) {
      final DataType type = constant.getVariable().getDataType();
      if (variable.getWidth() == type.getSize()) {
        return name + ".get()";
      } else {
        return String.format("%s.get(%d)", name, variable.getWidth());
      }
    }

    return getVariableName(context, name);
  }

  public static String toString(final String context, final IntegerField field) {
    return toString(context, field, true);
  }

  public static String toString(
      final String context,
      final IntegerField field,
      final boolean printAsVariable) {
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(field);

    if (field.getVariable().isDefined()) {
      return Utils.toString(field.getVariable().getValue());
    }

    final String name = getVariableName(context, field.getVariable().getName());
    if (field.isVariable() && printAsVariable) {
      return name;
    }

    return String.format(
        "%s.field(%d, %d)", name, field.getLoIndex(), field.getHiIndex());
  }

  public static String toMmuExpressionText(final String context, final List<IntegerField> fields) {
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
    for (final IntegerField field : fields) {
      if (isFirst) {
        isFirst = false;
      } else {
        sb.append(", ");
      }

      final IntegerVariable variable = field.getVariable();
      final String variableText;

      if (variable.isDefined()) {
        variableText = String.format("new IntegerVariable(%d, %s)",
            variable.getWidth(), toString(variable.getValue()));
      } else {
        variableText =
            getVariableName(context, variable.getName());
      }

      final String text = String.format(
          "%s.field(%d, %d)", variableText, field.getLoIndex(), field.getHiIndex());

      sb.append(text);
    }

    sb.append(')');
    return sb.toString();
  }

  public static String toMmuExpressionText(final String context, final IntegerField field) {
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(field);

    final StringBuilder sb = new StringBuilder();
    sb.append("MmuExpression.");

    if (field.getVariable().isDefined()) {
      sb.append(String.format(
          "val(%s, %d", toString(field.getVariable().getValue()), field.getWidth()));
    } else {
      final String name = getVariableName(context, field.getVariable().getName());
      sb.append(String.format("var(%s", name));

      if (!field.isVariable()) {
        sb.append(String.format(", %d, %d", field.getLoIndex(), field.getHiIndex()));
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
        return getVariableName(ir, context, (IntegerVariable) object);

      case FIELD: {
        final IntegerField field = (IntegerField) object;
        final IntegerVariable variable = field.getVariable();
        return String.format("%s.field(%d, %d)",
            getVariableName(ir, context, variable),
            field.getLoIndex(),
            field.getHiIndex()
            );
      }

      case GROUP:
        return getVariableName(context, ((Var) object).getName());

      case CONCAT:
        return toMmuExpressionText(context, (List<IntegerField>) object);

      default:
        throw new IllegalStateException("Unsupported atom kind: " + atom.getKind());
    }
  }
}
