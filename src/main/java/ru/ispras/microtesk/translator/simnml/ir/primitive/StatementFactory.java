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

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import java.util.List;

import ru.ispras.microtesk.model.api.state.Status;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.antlrex.symbols.ISymbol;
import ru.ispras.microtesk.translator.simnml.ESymbolKind;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.errors.UndefinedPrimitive;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.location.Location;
import ru.ispras.microtesk.utils.FormatMarker;

public final class StatementFactory extends WalkerFactoryBase {
  private static final String UNDEFINED_ARG =
    "The %s argument is not defined.";

  private static final String IMM_HAVE_NO_ATTR =
    "The immediate value %s does not provide any callable attributes.";

  private static final String ONLY_STANDARD_ATTR =
    "Only standard attributes can be called for the %s object.";

  private static final String WRONG_FORMAT_ARG_SPEC =
    "Incorrect format specification. The number of arguments specified in the format string (%d) " +
    "does not match to the number of provided argumens (%d).";

  private static final String UNDEFINED_ATTR =
    "The %s arrtibute is not defined for the %s primitive.";

  public StatementFactory(WalkerContext context) {
    super(context);
  }

  public Statement createAssignment(Location left, Expr right) {
    return new StatementAssignment(left, right);
  }

  public Statement createCondition(List<StatementCondition.Block> blocks) {
    return new StatementCondition(blocks);
  }

  public Statement createAttributeCall(Where where, String attributeName) throws SemanticException {
    if (null == attributeName) {
      throw new NullPointerException();
    }

    final ISymbol<ESymbolKind> symbol = getSymbols().resolveMember(attributeName);
    if ((null == symbol) || (symbol.getKind() != ESymbolKind.ATTRIBUTE)) {
      raiseError(where, new UndefinedPrimitive(attributeName, ESymbolKind.ATTRIBUTE));
    }

    return new StatementAttributeCall(null, attributeName);
  }

  public Statement createAttributeCall(Where where, String calleeName, String attributeName)
      throws SemanticException {
    if (null == attributeName) {
      throw new NullPointerException();
    }

    if (null == calleeName) {
      throw new NullPointerException();
    }

    if (!getThisArgs().containsKey(calleeName)) {
      raiseError(where, String.format(UNDEFINED_ARG, calleeName));
    }

    final Primitive callee = getThisArgs().get(calleeName);
    if (Primitive.Kind.IMM == callee.getKind()) {
      raiseError(where, String.format(IMM_HAVE_NO_ATTR, calleeName));
    }

    if (!Attribute.STANDARD_NAMES.contains(attributeName)) {
      raiseError(where, String.format(ONLY_STANDARD_ATTR, calleeName));
    }

    if (!callee.getAttrNames().contains(attributeName)) {
      raiseError(where, String.format(UNDEFINED_ATTR, attributeName, callee.getName()));
    }

    return new StatementAttributeCall(calleeName, attributeName);
  }

  public Statement createControlTransfer(int index) {
    return new StatementStatus(Status.CTRL_TRANSFER, index);
  }

  public Statement createFormat(Where where, String format, List<Format.Argument> args)
      throws SemanticException {
    if (null == args) {
      return new StatementFormat(format, null, null);
    }

    final List<FormatMarker> markers = FormatMarker.extractMarkers(format);
    if (markers.size() != args.size()) {
      raiseError(where, String.format(WRONG_FORMAT_ARG_SPEC, markers.size(), args.size()));
    }

    return new StatementFormat(format, markers, args);
  }

  public Statement createExceptionCall(Where where, String text){
    return new StatementFunctionCall("exception", String.format("\"%s\"", text));
  }
}
