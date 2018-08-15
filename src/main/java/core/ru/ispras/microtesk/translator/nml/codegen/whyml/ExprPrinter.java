/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.codegen.whyml;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.expression.printer.MapBasedPrinter;
import ru.ispras.fortress.expression.printer.OperationDescription;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.translator.nml.NmlSymbolKind;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.Location;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;
import ru.ispras.microtesk.translator.nml.ir.expr.Operator;

import java.util.EnumMap;
import java.util.Map;

final class ExprPrinter extends MapBasedPrinter {
  public static String toString(final Importer importer, final Expr expr) {
    return new ExprPrinter(importer).toString(expr.getNode());
  }

  private static final OperationDescription BVEQ =
      new OperationDescription("eq ", " ", "");

  private static final OperationDescription BVNEQ =
      new OperationDescription("neq ", " ", "");

  private final Importer importer;
  private final Map<Operator, OperationDescription> operatorMap;

  private ExprPrinter(final Importer importer) {
    this.importer = importer;
    this.operatorMap = new EnumMap<>(Operator.class);

    setVisitor(new Visitor());

    addMapping(StandardOperation.EQ,    "", " = ", "");
    addMapping(StandardOperation.NOTEQ, "", " <> ", ")");

    addMapping(StandardOperation.AND, "", " && ", "");
    addMapping(StandardOperation.OR,  "", " || ", "");
    addMapping(StandardOperation.NOT, "not ", " ", "");

    addMapping(StandardOperation.ITE, "ite ", " ", "");

    addMapping(StandardOperation.LESS,      "", " < ",  "");
    addMapping(StandardOperation.LESSEQ,    "", " <= ", "");
    addMapping(StandardOperation.GREATER,   "", " > ",  "");
    addMapping(StandardOperation.GREATEREQ, "", " >= ", "");

    addMapping(StandardOperation.MINUS,  "- ", "",    "");
    addMapping(StandardOperation.PLUS,   "",   "",    "");
    addMapping(StandardOperation.ADD,    "",   " + ", "");
    addMapping(StandardOperation.SUB,    "",   " - ", "");
    addMapping(StandardOperation.MUL,    "",   " * ", "");

    //<<=== TODO: DID NOT FIND IN WHY3 THEORIES ==
    addMapping(StandardOperation.DIV,    "", " / ", "");
    addMapping(StandardOperation.MOD,    "", " % ", "");
    //===========================================>>

    addMapping(StandardOperation.POWER,  "power ", " ", " ");

    addMapping(StandardOperation.BVNOT,  "bw_not ", " ", "");
    addMapping(StandardOperation.BVNEG,  "neg ",    " ", "");

    addMapping(StandardOperation.BVOR,   "bw_or ",  " ", "");
    addMapping(StandardOperation.BVXOR,  "bw_xor ", " ", "");
    addMapping(StandardOperation.BVAND,  "bw_and ", " ", "");

    addMapping(StandardOperation.BVADD,  "add ", " ", "");
    addMapping(StandardOperation.BVSUB,  "sub ", " ", "");
    addMapping(StandardOperation.BVMUL,  "mul ", " ", "");

    addMapping(StandardOperation.BVUDIV, "udiv ", " ", "");
    addMapping(StandardOperation.BVUREM, "urem ", " ", "");

    //<<=== TODO: NOT IMPLEMENTED in bvgen.why ====
    addMapping(StandardOperation.BVSDIV, "sdiv ", " ", "");
    addMapping(StandardOperation.BVSREM, "srem ", " ", "");
    addMapping(StandardOperation.BVSMOD, "smod ", " ", "");
    //===========================================>>

    addMapping(StandardOperation.BVLSHL, "lsl_bv ", " ", "");
    addMapping(StandardOperation.BVASHL, "lsl_bv ", " ", "");
    addMapping(StandardOperation.BVLSHR, "lsr_bv ", " ", "");
    addMapping(StandardOperation.BVASHR, "asr_bv ", " ", "");

    addMapping(StandardOperation.BVROL,  "rotate_left_bv ",  " ", "");
    addMapping(StandardOperation.BVROR,  "rotate_right_bv ", " ", "");

    addMapping(StandardOperation.BVULE, "ule ", " ", "");
    addMapping(StandardOperation.BVULT, "ult ", " ", "");
    addMapping(StandardOperation.BVUGE, "uge ", " ", "");
    addMapping(StandardOperation.BVUGT, "ugt ", " ", "");
    addMapping(StandardOperation.BVSLE, "sle ", " ", "");
    addMapping(StandardOperation.BVSLT, "slt ", " ", "");
    addMapping(StandardOperation.BVSGE, "sge ", " ", "");
    addMapping(StandardOperation.BVSGT, "sgt ", " ", "");

    //<<=== TODO ==================================
    addMapping(StandardOperation.BVREPEAT,
        "", new String[] {".repeat("}, ")", new int[] {1, 0});

    addMapping(StandardOperation.BVEXTRACT,
        "", new String[] {".bitField(", ", "}, ")", new int[] {2, 0, 1});

    //===========================================>>

    addMapping(StandardOperation.BVCONCAT, "concat ", " ", "");
    addMapping(StandardOperation.BVSIGNEXT, "signExtend ", "", "");
    addMapping(StandardOperation.BVZEROEXT, "zeroExtend ", "", "");

    //<<== TODO: Unsupported FP operators. Stub code is generated to hide errors. ===
    addMapping(Operator.SQRT,           "sqrt ",           "", "");
    addMapping(Operator.ROUND,          "round ",          "", "");
    addMapping(Operator.IS_NAN,         "isNan ",          "", "");
    addMapping(Operator.IS_SIGN_NAN,    "isSignalingNan ", "", "");
    addMapping(Operator.INT_TO_FLOAT,   "intToFloat ",     "", "");
    addMapping(Operator.FLOAT_TO_INT,   "floatToInt ",     "", "");
    addMapping(Operator.FLOAT_TO_FLOAT, "floatToFloat ",   "", "");

    // TODO
    // addMapping(Operator.COERCE, "coerce");
  }

  protected final void addMapping(
      final Operator op,
      final String prefix,
      final String infix,
      final String suffix) {
    operatorMap.put(op, new OperationDescription(prefix, infix, suffix));
  }

  @Override
  protected OperationDescription getOperationDescription(final NodeOperation expr) {
    final Enum<?> operator = expr.getOperationId();

    if (operator instanceof Operator) {
      return operatorMap.get(operator);
    }

    if (operator == StandardOperation.EQ || operator == StandardOperation.NOTEQ) {
      if (expr.getOperand(0).isType(DataTypeId.BIT_VECTOR)) {
        return operator == StandardOperation.EQ ? BVEQ : BVNEQ;
      }
    }

    return super.getOperationDescription(expr);
  }

  private final class Visitor extends ExprTreeVisitor {
    @Override
    public void onVariable(final NodeVariable variable) {
      InvariantChecks.checkTrue(variable.getUserData() instanceof NodeInfo);
      final NodeInfo nodeInfo = (NodeInfo) variable.getUserData();

      InvariantChecks.checkTrue(nodeInfo.getSource() instanceof Location);
      appendText(ExprPrinter.this.toString((Location) nodeInfo.getSource()));
    }

    @Override
    public void onOperationBegin(final NodeOperation expr) {
      appendText("(");

      if (expr.getOperationId() == StandardOperation.ITE) {
        onIte();
      } else if (expr.getOperationId() == StandardOperation.POWER) {
        onPower();
      } else if (isCastOperation(expr)) {
        onCast(expr);
      } else if (expr.getOperationId() == StandardOperation.BVCONCAT) {
        onConcat(expr);
      } else if (expr.isType(DataTypeId.BIT_VECTOR)) {
        onBvOperation(expr);
      } else if (expr.isType(DataTypeId.LOGIC_BOOLEAN)) {
        onBoolOperation(expr);
      }

      super.onOperationBegin(expr);
    }

    private void onIte() {
      importer.addImport("bool.Ite");
      appendText("Ite.");
    }

    private void onPower() {
      importer.addImport("int.Power");
    }

    private void onCast(final NodeOperation expr) {
      final int sourceSize = expr.getOperand(1).getDataType().getSize();
      final int targetSize = expr.getDataType().getSize();

      importer.addImport(WhymlUtils.getCastTheoryFullName(sourceSize, targetSize));
      BvCastTheoryGenerator.get().generate(sourceSize, targetSize);

      appendText(WhymlUtils.getCastTheoryName(sourceSize, targetSize));
      appendText(".");
    }

    private void onConcat(final NodeOperation expr) {
      final int firstSize = expr.getOperand(0).getDataType().getSize();
      final int secondSize = expr.getOperand(1).getDataType().getSize();

      importer.addImport(WhymlUtils.getConcatTheoryFullName(firstSize, secondSize));
      BvConcatTheoryGenerator.get().generate(firstSize, secondSize);

      appendText(WhymlUtils.getConcatTheoryName(firstSize, secondSize));
      appendText(".");
    }

    private void onBvOperation(final NodeOperation expr) {
      appendText(String.format("BV%d.", expr.getDataType().getSize()));
    }

    private void onBoolOperation(final NodeOperation expr) {
      if (expr.getOperandCount() > 0 && expr.getOperand(0).isType(DataTypeId.BIT_VECTOR)) {
        final int size = expr.getOperand(0).getDataType().getSize();
        appendText(String.format("BV%d.", size));
      }
    }

    @Override
    public void onOperationEnd(final NodeOperation expr) {
      super.onOperationEnd(expr);
      appendText(")");
    }

    @Override
    public void onOperandBegin(final NodeOperation expr, final Node operand, final int index) {
      if (isCastOperation(expr) && index == 0) {
        setStatus(Status.SKIP);
      } else {
        super.onOperandBegin(expr, operand, index);
      }
    }

    @Override
    public void onOperandEnd(final NodeOperation expr, final Node operand, final int index) {
      if (isCastOperation(expr) && index == 0) {
        setStatus(Status.OK);
      } else {
        super.onOperandEnd(expr, operand, index);
      }
    }

    @Override
    public void onValue(final NodeValue value) {
      if (value.isType(DataTypeId.BIT_VECTOR)) {
        appendText(WhymlUtils.getBitVectorText(value.getBitVector()));
      } else {
        super.onValue(value);
      }
    }
  }

  private String toString(final Location location) {
    InvariantChecks.checkNotNull(location);
    String text = getLocationName(location);

    if (location.getIndex() != null) {
      final Expr index = location.getIndex();
      final String indexText = toStringAsUint(index);
      text = String.format("(get %s %s)", text, indexText);
    }

    if (location.getBitfield() != null) {
      final Location.Bitfield bitfield = location.getBitfield();

      final String fromText = toStringAsUint(bitfield.getFrom());
      final String toText = toStringAsUint(bitfield.getTo());

      final int sourceSize = location.getSource().getType().getBitSize();
      final int fieldSize = bitfield.getType().getBitSize();

      BvExtractTheoryGenerator.get().generate(sourceSize, fieldSize);
      importer.addImport(WhymlUtils.getExtractTheoryFullName(sourceSize, fieldSize));

      text = String.format(
          "(%s.extract %s %s %s)",
          WhymlUtils.getExtractTheoryName(sourceSize, fieldSize),
          text,
          fromText,
          toText
      );
    }

    return text;
  }

  private static String getLocationName(final Location location) {
    final String name = location.getName().toLowerCase();
    return location.getSource().getSymbolKind() == NmlSymbolKind.MEMORY
        ? WhymlUtils.getStateFieldName(name) : name;
  }

  private String toStringAsUint(final Expr expr) {
    final String text = toString(importer, expr);
    final boolean isBitVector = expr.getNodeInfo().getType() != null;
    return isBitVector ? String.format("(to_uint %s)", text) : text;
  }

  private static boolean isCastOperation(final NodeOperation expr) {
    return expr.getOperationId() == StandardOperation.BVSIGNEXT ||
           expr.getOperationId() == StandardOperation.BVZEROEXT;
  }
}
