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

package ru.ispras.microtesk.mmu.translator.generation.sim;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.ExprTreeWalker;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.expression.printer.MapBasedPrinter;
import ru.ispras.fortress.expression.printer.OperationDescription;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.mmu.translator.MmuSymbolKind;
import ru.ispras.microtesk.mmu.translator.ir.AbstractStorage;
import ru.ispras.microtesk.mmu.translator.ir.AttributeRef;
import ru.ispras.microtesk.mmu.translator.ir.Callable;
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

  private final Deque<Map<String, String>> variableMappings = new ArrayDeque<>();

  private ExprPrinter() {
    addMapping(StandardOperation.EQ, "", ".equals(", ")");
    addMapping(StandardOperation.NOTEQ, "!", ".equals(", ")");
    addMapping(StandardOperation.AND, "", " && ", "");
    addMapping(StandardOperation.OR, "", " || ", "");

    addBitVectorMathMapping(StandardOperation.BVADD,  "add");
    addBitVectorMathMapping(StandardOperation.BVSUB,  "sub");
    addBitVectorMathMapping(StandardOperation.BVNEG,  "neg");
    addBitVectorMathMapping(StandardOperation.BVMUL,  "mul");
    addBitVectorMathMapping(StandardOperation.BVUDIV, "udiv");
    addBitVectorMathMapping(StandardOperation.BVSDIV, "sdiv");
    addBitVectorMathMapping(StandardOperation.BVUREM, "urem");
    addBitVectorMathMapping(StandardOperation.BVSREM, "srem");
    addBitVectorMathMapping(StandardOperation.BVSMOD, "smod");
    addBitVectorMathMapping(StandardOperation.BVLSHL, "shl");
    addBitVectorMathMapping(StandardOperation.BVASHL, "slh");
    addBitVectorMathMapping(StandardOperation.BVLSHR, "lshr");
    addBitVectorMathMapping(StandardOperation.BVASHR, "ashr");
    addMapping(StandardOperation.BVCONCAT, "BitVector.newMapping(", ", ", ")");
    // StandardOperation.BVREPEAT // TODO
    addBitVectorMathMapping(StandardOperation.BVROL, "rotl");
    addBitVectorMathMapping(StandardOperation.BVROR, "rotr");

    addMapping(StandardOperation.BVZEROEXT,
        "", new String[] {".resize("}, ", false)", new int[] {1, 0});
    addMapping(StandardOperation.BVSIGNEXT,
        "", new String[] {".resize("}, ", true)", new int[] {1, 0});
    addMapping(StandardOperation.BVEXTRACT,
        "", new String[] {".field(", ", "}, ")", new int[] {2, 0, 1});

    addBitVectorMathMapping(StandardOperation.BVOR,   "or");
    addBitVectorMathMapping(StandardOperation.BVXOR,  "xor");
    addBitVectorMathMapping(StandardOperation.BVAND,  "and");
    addBitVectorMathMapping(StandardOperation.BVNOT,  "not");
    addBitVectorMathMapping(StandardOperation.BVNAND, "nand");
    addBitVectorMathMapping(StandardOperation.BVNOR,  "nor");
    addBitVectorMathMapping(StandardOperation.BVXNOR, "xnor");

    addBitVectorMathMappingBool(StandardOperation.BVULE, "ule");
    addBitVectorMathMappingBool(StandardOperation.BVULT, "ult");
    addBitVectorMathMappingBool(StandardOperation.BVUGE, "uge");
    addBitVectorMathMappingBool(StandardOperation.BVUGT, "ugt");
    addBitVectorMathMappingBool(StandardOperation.BVSLE, "sle");
    addBitVectorMathMappingBool(StandardOperation.BVSLT, "slt");
    addBitVectorMathMappingBool(StandardOperation.BVSGE, "sge");
    addBitVectorMathMappingBool(StandardOperation.BVSGT, "sgt");

    addBitVectorMathMappingBool(StandardOperation.BVANDR,  "andr");
    addBitVectorMathMappingBool(StandardOperation.BVNANDR, "!andr");
    addBitVectorMathMappingBool(StandardOperation.BVORR,   "orr");
    addBitVectorMathMappingBool(StandardOperation.BVNORR,  "!orr");
    addBitVectorMathMappingBool(StandardOperation.BVXORR,  "xorr");
    addBitVectorMathMappingBool(StandardOperation.BVXNORR, "!xorr");

    // StandardOperation.BV2BOOL // TODO

    setVisitor(new Visitor());
    pushVariableScope(); // Global scope
  }

  @Override
  protected OperationDescription getOperationDescription(final NodeOperation expr) {
    if (expr.getOperationId() != MmuSymbolKind.FUNCTION) {
      return super.getOperationDescription(expr);
    }

    final Callable function = (Callable) expr.getUserData();
    return new OperationDescription(
        String.format("%s.call(", function.getName()),
        ", ",
        ")"
        );
  }

  private void addBitVectorMathMapping(final StandardOperation op, final String opMapping) {
    addMapping(
        op,
        String.format("BitVectorMath.%s(", opMapping),
        ", ",
        ")"
        );
  }

  private void addBitVectorMathMappingBool(final StandardOperation op, final String opMapping) {
    addMapping(
        op,
        String.format("BitVectorMath.%s(", opMapping),
        ", ",
        ").equals(BitVector.TRUE)"
        );
  }

  public void pushVariableScope() {
    variableMappings.push(new HashMap<String, String>());
  }

  public void popVariableScope() {
    InvariantChecks.checkTrue(variableMappings.size() > 1, "Global scope must stay on stack.");
    variableMappings.pop();
  }

  public void addVariableMappings(final Variable variable, final String mapping) {
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(mapping);

    addVariableMapping(variable.getName(), mapping);
    addVariableFieldMappings(variable.getType(), variable.getName(), mapping);
  }

  private void addVariableMapping(final String key, final String mapping) {
    InvariantChecks.checkNotNull(key);
    InvariantChecks.checkNotNull(mapping);
    variableMappings.peek().put(key, mapping);
  }

  private void addVariableFieldMappings(
      final Type type,
      final String key,
      final String mapping) {
    for (final Map.Entry<String, Type> e : type.getFields().entrySet()) {
      final String newKey = key + "." + e.getKey();
      final String newMapping = mapping.isEmpty() ? e.getKey() : mapping + "." + e.getKey();

      addVariableMapping(newKey, newMapping);
      addVariableFieldMappings(e.getValue(), newKey, newMapping);
    }
  }

  private String getVariableMapping(final String variableName) {
    for (final Map<String, String> scope : variableMappings) {
      final String mapping = scope.get(variableName);
      if (mapping != null) {
        return mapping;
      }
    }

    throw new IllegalStateException(
        "No mapping for variable " + variableName);
  }

  public static String bitVectorToString(final BitVector value) {
    InvariantChecks.checkNotNull(value);

    final int bitSize = value.getBitSize();
    final String hexValue = value.toHexString();

    final String text;
    if (value.getBitSize() <= Integer.SIZE) {
      text = String.format("%s.valueOf(0x%s, %d)",
          BitVector.class.getSimpleName(), hexValue, bitSize);
    } else if (bitSize <= Long.SIZE) {
      text = String.format("%s.valueOf(0x%sL, %d)",
          BitVector.class.getSimpleName(), hexValue, bitSize);
    } else {
      text = String.format("%s.valueOf(\"%s\", 16, %d)",
          BitVector.class.getSimpleName(), hexValue, bitSize);
    }

    return text;
  }

  private final class Visitor extends ExprTreeVisitor {
    private final Map<String, String> attrMap;

    public Visitor() {
      attrMap = new HashMap<>();
      attrMap.put(AbstractStorage.HIT_ATTR_NAME,   "isHit");
      attrMap.put(AbstractStorage.READ_ATTR_NAME,  "getData");
      attrMap.put(AbstractStorage.WRITE_ATTR_NAME, "setData");
    }

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
      if (variable.getUserData() instanceof AttributeRef) {
        final AttributeRef attrRef = (AttributeRef) variable.getUserData();

        appendText(attrRef.getTarget().getId());
        appendText(".get().");

        appendText(attrMap.get(attrRef.getAttribute().getId()));
        appendText("(");

        final ExprTreeWalker walker = new ExprTreeWalker(this);
        walker.visit(attrRef.getAddressArgValue());

        appendText(")");
      } else {
        final String text = getVariableMapping(variable.getName());
        appendText(text);
      }
    }
  }
}
