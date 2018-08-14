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
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.expression.printer.MapBasedPrinter;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.translator.nml.NmlSymbolKind;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.Location;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;

final class ExprPrinter extends MapBasedPrinter {
  public static String toString(final Importer importer, final Expr expr) {
    return new ExprPrinter(importer).toString(expr.getNode());
  }

  private final Importer importer;

  private ExprPrinter(final Importer importer) {
    this.importer = importer;
    setVisitor(new Visitor());

    addMapping(StandardOperation.EQ,     "", ".equals(", ")");
    addMapping(StandardOperation.NOTEQ, "!", ".equals(", ")");

    addMapping(StandardOperation.AND, "", " && ", "");
    addMapping(StandardOperation.OR,  "(", " || ", ")");
    addMapping(StandardOperation.NOT, "!(", "", ")");

    addMapping(StandardOperation.ITE, "(", new String[] {" ? ", " : "}, ")");

    addMapping(StandardOperation.LESS,      "(", ".compareTo(", ") < 0)");
    addMapping(StandardOperation.LESSEQ,    "(", ".compareTo(", ") <= 0)");
    addMapping(StandardOperation.GREATER,   "(", ".compareTo(", ") > 0)");
    addMapping(StandardOperation.GREATEREQ, "(", ".compareTo(", ") >= 0)");

    addMapping(StandardOperation.MINUS,  "", "", ".negate()");
    addMapping(StandardOperation.PLUS,   "", "", "");

    addMapping(StandardOperation.ADD,    "add", " ", "");
    addMapping(StandardOperation.SUB,    "sub", " ", "");
    addMapping(StandardOperation.MUL,    "", ".multiply(", ")");
    addMapping(StandardOperation.DIV,    "", ".divide(", ")");
    addMapping(StandardOperation.MOD,    "", ".mod(", ")");
    addMapping(StandardOperation.POWER,  "", ".pow(", ")");

    //<<=========== WORK FINE. TESTED. ===========
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
    //===========================================>>

    //<<=== TODO: NOT IMPLEMENTED in bvgen.why ===
    addMapping(StandardOperation.BVSDIV, "sdiv ", " ", "");
    addMapping(StandardOperation.BVSREM, "srem ", " ", "");
    addMapping(StandardOperation.BVSMOD, "smod ", " ", "");
    //===========================================>>

    //<<=========== WORK FINE. TESTED. ===========
    addMapping(StandardOperation.BVLSHL, "lsl_bv ", " ", "");
    addMapping(StandardOperation.BVASHL, "lsl_bv ", " ", "");
    addMapping(StandardOperation.BVLSHR, "lsr_bv ", " ", "");
    addMapping(StandardOperation.BVASHR, "asr_bv ", " ", "");

    addMapping(StandardOperation.BVROL,  "rotate_left_bv ",  " ", "");
    addMapping(StandardOperation.BVROR,  "rotate_right_bv ", " ", "");
    //===========================================>>

    addMapping(StandardOperation.BVULE, "(", ".compareTo(", ") <= 0)");
    addMapping(StandardOperation.BVULT, "(", ".compareTo(", ") < 0)");
    addMapping(StandardOperation.BVUGE, "(", ".compareTo(", ") >= 0)");
    addMapping(StandardOperation.BVUGT, "(", ".compareTo(", ") > 0)");
    addMapping(StandardOperation.BVSLE, "(", ".compareTo(", ") <= 0)");
    addMapping(StandardOperation.BVSLT, "(", ".compareTo(", ") < 0)");
    addMapping(StandardOperation.BVSGE, "(", ".compareTo(", ") >= 0)");
    addMapping(StandardOperation.BVSGT, "(", ".compareTo(", ") > 0)");

    addMapping(StandardOperation.BVREPEAT,
        "", new String[] {".repeat("}, ")", new int[] {1, 0});

    addMapping(StandardOperation.BVEXTRACT,
        "", new String[] {".bitField(", ", "}, ")", new int[] {2, 0, 1});

    addMapping(StandardOperation.BVCONCAT,
        "Location.concat(", ", ", ")");

    addMapping(StandardOperation.BVSIGNEXT, "signExtend", "", "");
    addMapping(StandardOperation.BVZEROEXT, "zeroExtend", "", "");
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

      if (expr.isType(DataTypeId.BIT_VECTOR)) {
        appendText(String.format("BV%d.", expr.getDataType().getSize()));
      }

      super.onOperationBegin(expr);
    }

    @Override
    public void onOperationEnd(final NodeOperation expr) {
      super.onOperationEnd(expr);
      appendText(")");
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
}
