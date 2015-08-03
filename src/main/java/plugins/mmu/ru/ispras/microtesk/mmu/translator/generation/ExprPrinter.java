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

package ru.ispras.microtesk.mmu.translator.generation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.expression.printer.MapBasedPrinter;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.Variable;

final class ExprPrinter extends MapBasedPrinter {
  private static ExprPrinter instance = null;

  public static ExprPrinter get() {
    if (null == instance) {
      instance = new ExprPrinter();
    }
    return instance;
  }

  private final Deque<Map<String, Object>> variableMappings = new ArrayDeque<>();

  private ExprPrinter() {
    addMapping(
        StandardOperation.BVEXTRACT, "", new String[] {".field(", ", "}, ")", new int[] {2, 0, 1});
    addMapping(StandardOperation.EQ, "", ".equals(", ")");
    addMapping(StandardOperation.AND, "", " && ", "");
    
    

    setVisitor(new Visitor());
    pushVariableScope(); // Global scope
  }

  public void pushVariableScope() {
    variableMappings.push(new HashMap<String, Object>());
  }

  public void popVariableScope() {
    InvariantChecks.checkTrue(variableMappings.size() > 1, "Global scope must stay on stack.");
    variableMappings.pop();
  }

  public void addVariableMapping(final String variableName, final Object text) {
    InvariantChecks.checkNotNull(variableName);
    InvariantChecks.checkNotNull(text);
    variableMappings.peek().put(variableName, text);
  }

  private String getVariableMapping(final String variableName) {
    for (final Map<String, Object> scope : variableMappings) {
      final Object mapping = scope.get(variableName);
      if (mapping != null) {
        return mapping.toString();
      }
    }

    return variableName;
  }

  public void addVariableMappings(final Variable variable, final String mapping) {
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(mapping);

    addVariableMapping(variable.getName(), mapping);
    addVariableFieldMappings(variable.getName(), variable.getType(), mapping, "");
  }

  private void addVariableFieldMappings(
      final String name,
      final Type type,
      final String mapping,
      final String field) {
    for (final Map.Entry<String, Type> e : type.getFields().entrySet()) {
      final String variableName = name + "." + e.getKey();
      final String fieldName = field.isEmpty() ? e.getKey() : field + "." + e.getKey();
      final String mappingName = String.format("%s.getField(\"%s\")", mapping, fieldName);

      ExprPrinter.get().addVariableMapping(variableName, mappingName);
      addVariableFieldMappings(variableName, e.getValue(), fieldName, mapping);
    }
  }

  public static String bitVectorToString(final BitVector value) {
    InvariantChecks.checkNotNull(value);

    final int bitSize = value.getBitSize();
    final String hexValue = value.toHexString();

    final String text;
    if (value.getBitSize() <= Integer.SIZE) {
      text = String.format("BitVector.valueOf(0x%s, %d)", hexValue, bitSize);
    } else if (bitSize <= Long.SIZE) {
      text = String.format("BitVector.valueOf(0x%sL, %d)", hexValue, bitSize);
    } else {
      text = String.format("BitVector.valueOf(\"%s\", 16, %d)", hexValue, bitSize);
    }

    return text;
  }

  private final class Visitor extends ExprTreeVisitor {
    @Override
    public void onValue(final NodeValue value) {
      if (value.isType(DataTypeId.BIT_VECTOR)) {
        final String text = bitVectorToString(value.getBitVector());
        appendText(text);
      } else {
        appendText(value.toString());
      }
    }

    @Override
    public void onVariable(final NodeVariable variable) {
      final String text = getVariableMapping(variable.getName());
      appendText(text);
    }
  }
}
