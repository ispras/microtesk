/*
 * Copyright 2012-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.codegen.simc;

import org.stringtemplate.v4.ST;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;

import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAssignment;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAttributeCall;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementCondition;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementFormat;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementFunctionCall;
import ru.ispras.microtesk.utils.FormatMarker;

import java.util.List;

public final class StatementBuilder {
  private static final String SINDENT = "  ";

  private final ST sequenceST;
  private final boolean isReturn;
  private int indent;
  private Boolean isPrint;

  public StatementBuilder(final ST sequenceST, final boolean isReturn) {
    this.sequenceST = sequenceST;
    this.isReturn = isReturn;
    this.indent = 0;
    this.isPrint = false;
  }

  private void increaseIndent() {
    indent++;
  }

  private void decreaseIndent() {
    assert indent > 0;
    if (indent > 0) {
      indent--;
    }
  }

  public void build(final Statement stmt) {
    addStatement(stmt);
  }

  private void addStatement(final Statement stmt) {
    if (null == stmt) {
      if (isReturn) {
        addStatement("NULL;");
      }

      return;
    }

    switch (stmt.getKind()) {
      case ASSIGN:
        addStatement((StatementAssignment) stmt);
        break;

      case COND:
        addStatement((StatementCondition) stmt);
        break;

      case CALL:
        addStatement((StatementAttributeCall) stmt);
        break;

      case FORMAT:
        addStatement((StatementFormat) stmt);
        break;

      case FUNCALL:
        addStatement((StatementFunctionCall) stmt);
        break;

      default:
        assert false : String.format("Unsupported statement type: %s.", stmt.getKind());
        addStatement("// Error! Unknown statement!");
        break;
    }
  }

  private void addStatement(final String stmt) {
    final StringBuilder sb = new StringBuilder();

    if (isReturn && isPrint) {
      sb.append(stmt + "return str_tmp;" );
      sequenceST.add("stmts", sb.toString());
      return;
    }

    if (isReturn) {
      sb.append("return ");
    }

    for (int index = 0; index < indent; ++index) {
      sb.append(SINDENT);
    }
    sb.append(stmt);

    sequenceST.add("stmts", sb.toString());
  }

  private void addStatement(final StatementAssignment stmt) {
    addStatement(
            String.format("store(%s, %s);",
                    ExprPrinter.toString(stmt.getLeft(), true),
                    ExprPrinter.toString(stmt.getRight()))
    );
  }

  private void addStatement(final StatementCondition stmt) {
    final int FIRST = 0;
    final int LAST = stmt.getBlockCount() - 1;

    for (int index = FIRST; index <= LAST; ++index) {
      final Pair<Expr, List<Statement>> block = stmt.getBlock(index);

      if (FIRST == index) {
        addStatement(String.format("if (%s) {", ExprPrinter.toString(block.first)));
      } else if (LAST == index && null == block.first) {
        addStatement("} else {");
      } else {
        addStatement(String.format("} else if (%s) {", ExprPrinter.toString(block.first)));
      }

      increaseIndent();
      for (final Statement blockStmt : block.second) {
        addStatement(blockStmt);
      }
      decreaseIndent();
    }

    addStatement("}");
  }

  private void addStatement(final StatementAttributeCall stmt) {
    String attrName = stmt.getAttributeName();
    final boolean usePE = !attrName.equals(Attribute.INIT_NAME)
            && !attrName.equals(Attribute.IMAGE_NAME)
            && !attrName.equals(Attribute.SYNTAX_NAME);

    final boolean isAction = attrName.equals(Attribute.ACTION_NAME);
    final boolean isSyntax = attrName.equals(Attribute.SYNTAX_NAME);
    if (attrName.equals(Attribute.INIT_NAME)) {
      attrName = "INIT";
    }
    final String methodName;
    if (null != stmt.getCalleeName()) {
      methodName = String.format("%s(%s",
              isAction ? "execute" : (isSyntax ? "text" : attrName),
              stmt.getCalleeName());
    } else if (null != stmt.getCalleeInstance()) {
      methodName = String.format("%s(%s",
              isAction ? "execute" : (isSyntax ? "text" : attrName),
              PrinterInstance.toString(stmt.getCalleeInstance())
              );
    } else {
      methodName = attrName;
    }

    final String arguments = usePE ? "pe__, vars__" : "vars__";
    addStatement(String.format(methodName + (methodName.indexOf("(") == -1 ? "(%s);" : ", %s);"), arguments));
  }

  private void addStatement(final StatementFormat stmt) {
    if (null == stmt.getArguments()) {
      if (null == stmt.getFunction()) {
        addStatement(String.format("\"%s\";", stmt.getFormat()));
      } else {
        addStatement(String.format("//Execution.%s(\"%s\");", stmt.getFunction(), stmt.getFormat()));
      }
      return;
    }

    final StringBuffer sb = new StringBuffer();
    for (int index = 0; index < stmt.getArguments().size(); ++index) {
      final Node argument = stmt.getArguments().get(index);
      final FormatMarker marker = stmt.getMarkers().get(index);

      if (null == argument || null == marker) {
        continue;
      }

      sb.append(", ");
      sb.append(convertTo(argument, marker));
    }

    if (null == stmt.getFunction()) {
      isPrint = true;
      String text = "char* str_tmp = (char*) malloc(64);\n" + String.format("sprintf(%s,\"%s\"%s);\n", "str_tmp", stmt.getFormat(), sb.toString());
      addStatement(text);
    } else {
      addStatement(String.format("//Execution.%s(\"%s\"%s);",
              stmt.getFunction(), stmt.getFormat(), sb.toString()));
    }
  }

  private void addStatement(final StatementFunctionCall stmt) {
    final StringBuffer sb = new StringBuffer();
    for (int index = 0; index < stmt.getArgumentCount(); ++index) {
      if (sb.length() > 0) {
        sb.append(", ");
      }

      final Object arg = stmt.getArgument(index);
      if (arg instanceof Expr) {
        sb.append(ExprPrinter.toString((Expr) arg));
      } else if (arg instanceof String) {
        sb.append('"');
        sb.append(arg);
        sb.append('"');
      } else {
        sb.append(arg);
      }
    }

    addStatement(String.format(
            "//Execution.%s(%s);", stmt.getName(), sb.toString()));
  }

  private static String convertTo(final Node argument, final FormatMarker marker) {
    if (argument.isType(DataTypeId.LOGIC_INTEGER)) {
      return ExprPrinter.toString(new Expr(argument));
    }

    if (argument.isType(DataTypeId.LOGIC_STRING)) {
      InvariantChecks.checkTrue(marker.isKind(FormatMarker.Kind.STR)
              || marker.isKind(FormatMarker.Kind.BIN));
      return ExprPrinter.toString(new Expr(argument));
    }

    // Bit vector
    final String methodName;
    switch (marker.getKind()) {
      case BIN:
        methodName = "toBinString(%s)";
        break;
      case STR:
        methodName = "toString(%s)";
        break;
      case HEX:
        methodName = "bigIntegerValue(%s, false)";
        break;
      case DEC:
        methodName = "bigIntegerValue(%s)";
        break;
      default:
        throw new IllegalArgumentException("Unsupported marker kind: " + marker.getKind());
    }

    return String.format(methodName, ExprPrinter.toString(new Expr(argument)));
  }
}
