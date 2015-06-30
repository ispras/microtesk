/*
 * Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.primitive;

import java.util.Collections;
import java.util.List;

import ru.ispras.microtesk.translator.nml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerFactoryBase;

public final class AttributeFactory extends WalkerFactoryBase {
  public AttributeFactory(final WalkerContext context) {
    super(context);
  }

  public Attribute createAction(
      final String name,
      final List<Statement> stmts) {
    return new Attribute(
        name, 
        Attribute.Kind.ACTION,
        stmts,
        isException(stmts)
        );
  }

  public Attribute createExpression(
      final String name,
      final Statement stmt) {

    return new Attribute(
        name,
        Attribute.Kind.EXPRESSION,
        Collections.singletonList(stmt),
        isException(stmt)
        );
  }

  private static boolean isException(final List<Statement> stmts) {
    for (final Statement stmt : stmts) {
      if (isException(stmt)) {
        return true;
      }
    }

    return false;
  }

  private static boolean isException(final Statement stmt) {
    if (stmt.getKind() == Statement.Kind.FUNCALL) {
      final StatementFunctionCall funCallStmt = (StatementFunctionCall) stmt;
      return "exception".equals(funCallStmt.getName());
    }

    if (stmt.getKind() == Statement.Kind.COND) {
      final StatementCondition condStmt = (StatementCondition) stmt;
      for (int index = 0; index < condStmt.getBlockCount(); index++) {
        if (isException(condStmt.getBlock(index).getStatements())) {
          return true;
        }
      }
      return false;
    }

    if (stmt.getKind() == Statement.Kind.CALL) {
      final StatementAttributeCall attrCallStmt = (StatementAttributeCall) stmt;

      if (null != attrCallStmt.getCalleeInstance()) {
        final PrimitiveAND calleeObject =
            attrCallStmt.getCalleeInstance().getPrimitive();

        final Attribute calleeAttribute =
            calleeObject.getAttributes().get(attrCallStmt.getAttributeName());

        if (calleeAttribute.canThrowException()) {
          return true;
        }
      }

      return false;
    }

    return false;
  }
}
