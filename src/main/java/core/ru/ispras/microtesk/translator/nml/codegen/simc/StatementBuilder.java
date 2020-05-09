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

import org.jruby.RubyProcess;
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
import java.util.ArrayList;

public final class StatementBuilder {
  private static final String SINDENT = "  ";

  private final ST sequenceST;
  private final boolean isReturn;
  private int indent;
  private Integer tempCount;
  private List<String> blocks;

  public StatementBuilder(final ST sequenceST, final boolean isReturn) {
    this.sequenceST = sequenceST;
    this.isReturn = isReturn;
    this.indent = 0;
    this.tempCount = 0;
    this.blocks = new ArrayList<String>();
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
    String left = ExprPrinter.toString(stmt.getLeft(), true, tempCount);
    String[] dataLeft = left.split("\\|\\|\\|");
    if (dataLeft.length > 1) {
      tempCount += dataLeft[1].split(";\n").length;
    }
    String right = ExprPrinter.toString(stmt.getLeft(), false, tempCount);
    String[] dataRight = right.split("\\|\\|\\|");
    if (dataRight.length > 1) {
      tempCount += dataRight[1].split(";\n").length;
    }

    addStatement(dataLeft[1] + dataRight[1]);
    addStatement(
        String.format("%s.store(%s, %s);",
            dataLeft[0], dataLeft[0], dataRight[0])
    );
  }

  private void addStatement(final StatementCondition stmt) {
    final int FIRST = 0;
    final int LAST = stmt.getBlockCount() - 1;
    List<String> blocks = new ArrayList<String>();

    for (int index = FIRST; index <= LAST; ++index) {
      final Pair<Expr, List<Statement>> block = stmt.getBlock(index);

      if (FIRST == index) {
        String text = ExprPrinter.toString(block.first, false, tempCount);
        String[] dataText = text.split("\\|\\|\\|");
        if (dataText.length > 1) {
          tempCount += dataText[1].split(";\n").length;
        }
        addStatement(dataText[1]);
        blocks.add(String.format("if (%s) {", dataText[0]));
      } else if (LAST == index && null == block.first) {
        addStatement("} else {");
      } else {
        String text = ExprPrinter.toString(block.first, false, tempCount);
        String[] dataText = text.split("\\|\\|\\|");
        if (dataText.length > 1) {
          tempCount += dataText[1].split(";\n").length;
        }
        addStatement(dataText[1]);
        blocks.add(String.format("} else if (%s) {", dataText[0]));
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
    final String attrName = stmt.getAttributeName();
    final boolean usePE = !attrName.equals(Attribute.INIT_NAME)
        && !attrName.equals(Attribute.IMAGE_NAME)
        && !attrName.equals(Attribute.SYNTAX_NAME);

    final boolean isAction = attrName.equals(Attribute.ACTION_NAME);
    final boolean isSyntax = attrName.equals(Attribute.SYNTAX_NAME);

    final String methodName;
    if (null != stmt.getCalleeName()) {
      methodName = String.format("%s.%s(%s",
          stmt.getCalleeName(),
          isAction ? "execute" : (isSyntax ? "text" : attrName),
          stmt.getCalleeName());
    } else if (null != stmt.getCalleeInstance()) {
      methodName = String.format("%s.%s(%s",
          PrinterInstance.toString(stmt.getCalleeInstance()),
          isAction ? "execute" : (isSyntax ? "text" : attrName),
          PrinterInstance.toString(stmt.getCalleeInstance()));
    } else {
      methodName = attrName;
    }

    final String arguments = usePE ? "pe__, vars__" : "vars__";
    addStatement(String.format("%s, %s);", methodName, arguments));
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
      addStatement(String.format("sprintf(\"%s\"%s);", stmt.getFormat(), sb.toString()));
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
        methodName = "toBinString()";
        break;
      case STR:
        methodName = "toString()";
        break;
      case HEX:
        methodName = "bigIntegerValue(false)";
        break;
      case DEC:
        methodName = "bigIntegerValue()";
        break;
      default:
        throw new IllegalArgumentException("Unsupported marker kind: " + marker.getKind());
    }

    return String.format("%s.%s", ExprPrinter.toString(new Expr(argument)), methodName);
  }
}
