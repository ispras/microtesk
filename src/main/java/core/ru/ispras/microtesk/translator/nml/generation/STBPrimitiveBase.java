/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.generation;

import java.util.EnumMap;
import java.util.Map;

import org.stringtemplate.v4.ST;

import ru.ispras.microtesk.translator.generation.STBuilder;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Format;
import ru.ispras.microtesk.translator.nml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAssignment;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAttributeCall;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementCondition;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementFormat;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementFunctionCall;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementStatus;
import ru.ispras.microtesk.utils.FormatMarker;

abstract class STBPrimitiveBase implements STBuilder {
  private static final Map<Attribute.Kind, String> RET_TYPE_MAP =
    new EnumMap<>(Attribute.Kind.class);

  static {
    RET_TYPE_MAP.put(Attribute.Kind.ACTION, "void");
    RET_TYPE_MAP.put(Attribute.Kind.EXPRESSION, "String");
  }

  protected final String getRetTypeName(Attribute.Kind kind) {
    return RET_TYPE_MAP.get(kind);
  }

  protected final boolean isStandardAttribute(String name) {
    return Attribute.STANDARD_NAMES.contains(name);
  }

  protected static void addStatement(ST attrST, Statement stmt, boolean isReturn) {
    new StatementBuilder(attrST, isReturn).build(stmt);
  }
}


final class StatementBuilder {
  private static final String SINDENT = "  ";

  private final ST sequenceST;
  private final boolean isReturn;
  private int indent;

  StatementBuilder(ST sequenceST, boolean isReturn) {
    this.sequenceST = sequenceST;
    this.isReturn = isReturn;
    this.indent = 0;
  }

  private void increaseIndent() {
    indent++;
  }

  private void decreaseIndent() {
    assert indent > 0;
    if (indent > 0)
      indent--;
  }

  public void build(Statement stmt) {
    addStatement(stmt);
  }

  private void addStatement(Statement stmt) {
    if (null == stmt) {
      if (isReturn)
        addStatement("null;");

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

      case STATUS:
        addStatement((StatementStatus) stmt);
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

  private void addStatement(String stmt) {
    final StringBuilder sb = new StringBuilder();

    if (isReturn)
      sb.append("return ");


    for (int index = 0; index < indent; ++index)
      sb.append(SINDENT);
    sb.append(stmt);

    sequenceST.add("stmts", sb.toString());
  }

  private void addStatement(StatementAssignment stmt) {
    addStatement(String.format("%s.store(%s);", PrinterLocation.toString(stmt.getLeft()),
        new PrinterExpr(stmt.getRight())));
  }

  private void addStatement(StatementCondition stmt) {
    final int FIRST = 0;
    final int LAST = stmt.getBlockCount() - 1;

    for (int index = FIRST; index <= LAST; ++index) {
      final StatementCondition.Block block = stmt.getBlock(index);

      if (FIRST == index) {
        addStatement(String.format("if (%s) {", new PrinterExpr(block.getCondition())));
      } else if (LAST == index && block.isElseBlock()) {
        addStatement("} else {");
      } else {
        addStatement(String.format("} else if (%s) {", new PrinterExpr(block.getCondition())));
      }

      increaseIndent();
      for (final Statement blockStmt : block.getStatements()) {
        addStatement(blockStmt);
      }
      decreaseIndent();
    }

    addStatement("}");
  }

  private void addStatement(StatementAttributeCall stmt) {
    final String attrName = stmt.getAttributeName();
    if (null != stmt.getCalleeName()) {
      addStatement(String.format("%s.%s();", stmt.getCalleeName(), attrName));
    } else if(null != stmt.getCalleeInstance()) {
      addStatement(String.format("%s.%s();", PrinterInstance.toString(stmt.getCalleeInstance()), attrName));
    } else {
      addStatement(String.format("%s();", attrName));
    }
  }

  private void addStatement(StatementFormat stmt) {
    if (null == stmt.getArguments()) {
      if (null == stmt.getFunction()) {
        addStatement(String.format("\"%s\";", stmt.getFormat()));
      } else {
        addStatement(String.format("%s(\"%s\");", stmt.getFunction(), stmt.getFormat()));
      }
      return;
    }

    final StringBuffer sb = new StringBuffer();
    for (int index = 0; index < stmt.getArguments().size(); ++index) {
      final Format.Argument argument = stmt.getArguments().get(index);
      final FormatMarker marker = stmt.getMarkers().get(index);
      
      if (null == argument || null == marker) {
        continue;
      }

      sb.append(", ");
      sb.append(argument.convertTo(marker));
    }

    if (null == stmt.getFunction()) {
      addStatement(String.format("String.format(\"%s\"%s);", stmt.getFormat(), sb.toString()));
    } else {
      addStatement(String.format("%s(\"%s\"%s);", stmt.getFunction(), stmt.getFormat(), sb.toString()));
    }
  }

  private void addStatement(StatementFunctionCall stmt) {
    final StringBuffer sb = new StringBuffer();
    for (int index = 0; index < stmt.getArgumentCount(); ++index) {
      if (sb.length() > 0) {
        sb.append(", ");
      }

      final Object arg = stmt.getArgument(index);
      final boolean isString = arg instanceof String;

      if (isString) sb.append('"');
      sb.append(arg);
      if (isString) sb.append('"');
    }

    addStatement(String.format("%s(%s);", stmt.getName(), sb.toString()));
  }

  private void addStatement(StatementStatus stmt) {
    addStatement(String.format("%s.set(%d);", stmt.getStatus().getName(), stmt.getNewValue()));
  }
}
