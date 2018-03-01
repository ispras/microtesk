/*
 * Copyright 2013-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.expression.ExprTreeVisitorDefault;
import ru.ispras.fortress.expression.ExprTreeWalker;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.data.TypeId;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.symbols.Symbol;
import ru.ispras.microtesk.translator.antlrex.symbols.Where;
import ru.ispras.microtesk.translator.nml.NmlSymbolKind;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.nml.errors.UndefinedPrimitive;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.Location;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationSourcePrimitive;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;
import ru.ispras.microtesk.translator.nml.ir.expr.TypeCast;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;
import ru.ispras.microtesk.utils.FormatMarker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StatementFactory extends WalkerFactoryBase {
  private static final String UNDEFINED_ARG =
      "The %s argument is not defined.";

  private static final String IMM_HAVE_NO_ATTR =
      "The immediate value %s does not provide any callable attributes.";

  private static final String WRONG_FORMAT_ARG_SPEC =
      "Incorrect format specification. The number of arguments specified in the format string "
          + "(%d) does not match to the number of provided argumens (%d).";

  private static final String UNDEFINED_ATTR =
      "The %s attribute of the %s object is not defined or is not accessible in this context.";

  private static final String INVALID_LHS =
      "The left side of an expression must be a variable or a concatenation of variables.";

  public StatementFactory(final WalkerContext context) {
    super(context);
  }

  public Statement createAssignment(
      final Where where,
      final Expr leftExpr,
      final Expr right) throws SemanticException {
    // Hack to deal with internal variables described by string constants.
    if (leftExpr.isInternalVariable()) {
      return new StatementAssignment(leftExpr, right);
    }

    checkAssignableLocation(where, leftExpr);

    final NodeInfo leftInfo = leftExpr.getNodeInfo();
    final Type leftType = leftInfo.getType();

    // Hack to deal with internal variables described by string constants.
    if (right.isInternalVariable()) {
      final NodeInfo newNodeInfo =
          right.getNodeInfo().coerceTo(leftType, NodeInfo.Coercion.IMPLICIT);
      right.setNodeInfo(newNodeInfo);
      return new StatementAssignment(leftExpr, right);
    }

    if (right.isConstant()) {
      final Expr castRight = TypeCast.castConstantTo(right, leftType);
      return new StatementAssignment(leftExpr, castRight);
    }

    if (right.getNodeInfo().getType() == null) {
      final NodeInfo newNodeInfo =
          right.getNodeInfo().coerceTo(leftType, NodeInfo.Coercion.IMPLICIT);
      right.setNodeInfo(newNodeInfo);
    } else {
      final Type rightType = right.getNodeInfo().getType();
      if (leftType.getBitSize() != rightType.getBitSize()) {
        raiseError(where, String.format(
            "Assigning %s to %s is not allowed.", rightType.getTypeName(), leftType.getTypeName()));
      }

      if (right.isTypeOf(TypeId.BOOL)) {
        final NodeInfo newNodeInfo =
            right.getNodeInfo().coerceTo(leftType, NodeInfo.Coercion.IMPLICIT);
        right.setNodeInfo(newNodeInfo);
      }
    }

    return new StatementAssignment(leftExpr, right);
  }

  private void checkAssignableLocation(
      final Where where, final Expr expr) throws SemanticException {
    final LocationChecker visitor = new LocationChecker();
    final ExprTreeWalker walker = new ExprTreeWalker(visitor);

    walker.visit(expr.getNode());
    if (!visitor.isValid()) {
      raiseError(where, visitor.getErrorMessage());
    }
  }

  private static final class LocationChecker extends ExprTreeVisitorDefault {
    private String errorMessage = "";

    public boolean isValid() {
      return getStatus() != Status.ABORT;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    @Override
    public void onVariable(final NodeVariable variable) {
      final Expr node = new Expr(variable);
      final NodeInfo nodeInfo = node.getNodeInfo();

      InvariantChecks.checkTrue(nodeInfo.isLocation());
      final Location location = (Location) nodeInfo.getSource();

      if (isImmediate(location)) {
        errorMessage = String.format(
            "'%s' is an input argument and it cannot be assigned a value.", location.getName());
        setStatus(Status.ABORT);
      }
    }

    private boolean isImmediate(final Location location) {
      if (!(location.getSource() instanceof LocationSourcePrimitive)) {
        return false;
      }

      final LocationSourcePrimitive source = (LocationSourcePrimitive) location.getSource();
      return source.getPrimitive().getKind() == Primitive.Kind.IMM;
    }

    @Override
    public void onOperationBegin(final NodeOperation node) {
      if (node.getOperationId() != StandardOperation.BVCONCAT) {
        errorMessage = INVALID_LHS;
        setStatus(Status.ABORT);
      }
    }

    @Override
    public void onValue(final NodeValue value) {
      errorMessage = INVALID_LHS;
      setStatus(Status.ABORT);
    }
  }

  public List<Statement> createCondition(final List<StatementCondition.Block> blocks) {
    final List<StatementCondition.Block> updatedBlocks = new ArrayList<>();
    for (final StatementCondition.Block block : blocks) {
      final Expr condition = block.getCondition();
      if (null != condition && ExprUtils.isValue(condition.getNode())) { // Condition is true.
        if (NodeValue.newBoolean(true).equals(condition.getNode())) {
          // This block becomes an else block, the rest are thrown away.
          updatedBlocks.add(StatementCondition.Block.newElseBlock(block.getStatements()));
          break;
        } else { // Condition is false.
          // This block is thrown away.
        }
      } else {
        updatedBlocks.add(block);
      }
    }

    if (updatedBlocks.isEmpty()) {
      return Collections.emptyList();
    }

    if (updatedBlocks.size() == 1
        && (updatedBlocks.get(0).getCondition() == null
        || updatedBlocks.get(0).getCondition().getNode().equals(NodeValue.newBoolean(true)))) {
      return updatedBlocks.get(0).getStatements();
    }

    return Collections.<Statement>singletonList(new StatementCondition(updatedBlocks));
  }

  public Statement createAttributeCall(
      final Where where,
      final String attributeName) throws SemanticException {
    InvariantChecks.checkNotNull(attributeName);

    final Symbol symbol = getSymbols().resolveMember(attributeName);
    if ((null == symbol) || (symbol.getKind() != NmlSymbolKind.ATTRIBUTE)) {
      raiseError(where, new UndefinedPrimitive(attributeName, NmlSymbolKind.ATTRIBUTE));
    }

    return StatementAttributeCall.newThisCall(attributeName);
  }

  public Statement createAttributeCall(
      final Where where,
      final String calleeName,
      final String attributeName) throws SemanticException {
    InvariantChecks.checkNotNull(attributeName);
    InvariantChecks.checkNotNull(calleeName);

    if (!getThisArgs().containsKey(calleeName)) {
      raiseError(where, String.format(UNDEFINED_ARG, calleeName));
    }

    final Primitive callee = getThisArgs().get(calleeName);
    if (Primitive.Kind.IMM == callee.getKind()) {
      raiseError(where, String.format(IMM_HAVE_NO_ATTR, calleeName));
    }

    if (!callee.getAttrNames().contains(attributeName)) {
      raiseError(where, String.format(UNDEFINED_ATTR, attributeName, callee.getName()));
    }

    return StatementAttributeCall.newArgumentCall(calleeName, attributeName);
  }

  public Statement createAttributeCall(
      final Where where,
      final Instance calleeInstance,
      final String attributeName) throws SemanticException {

    final Primitive callee = calleeInstance.getPrimitive();
    if (!callee.getAttrNames().contains(attributeName)) {
      raiseError(where, String.format(UNDEFINED_ATTR, attributeName, callee.getName()));
    }

    return StatementAttributeCall.newInstanceCall(calleeInstance, attributeName);
  }

  public Node createCallNode(final StatementAttributeCall call) {
    return StatementAttributeCall.createCallNode(call);
  }

  public Statement createFormat(
      final Where where,
      final String format,
      final List<Node> args) throws SemanticException {

    if (null == args) {
      return new StatementFormat(
          format,
          Collections.<FormatMarker>emptyList(),
          Collections.<Node>emptyList()
          );
    }

    final List<FormatMarker> markers = FormatMarker.extractMarkers(format);
    if (markers.size() != args.size()) {
      raiseError(where, String.format(WRONG_FORMAT_ARG_SPEC, markers.size(), args.size()));
    }

    for (int index = 0; index < markers.size(); ++index) {
      final FormatMarker marker = markers.get(index);
      final Node argument = args.get(index);

      if (argument.isType(DataTypeId.LOGIC_STRING)
          && !marker.isKind(FormatMarker.Kind.STR)
          && !marker.isKind(FormatMarker.Kind.BIN)) {
        raiseError(where, String.format(
            "String %s cannot be converted to the %%%s format.",
            argument,
            marker.getKind().getLetter()));
      }

      if (marker.isKind(FormatMarker.Kind.BIN) || marker.isKind(FormatMarker.Kind.STR)) {
        if (argument.isType(DataTypeId.LOGIC_INTEGER)) {
          raiseError(where, String.format("%s must be explicitly typed.", argument));
        }

        final int markerLength = marker.getLength();
        final int argumentLength =
            argument.isType(DataTypeId.BIT_VECTOR) ? argument.getDataType().getSize() : 0;

        if (markerLength != 0 && argumentLength != 0 && markerLength != argumentLength) {
          raiseError(where, String.format(
              "Length specified by the %s format marker mismatches the actual data type. "
                  + "Expected length is %d.",
              format.substring(marker.getStart(), marker.getEnd()),
              argumentLength
              ));
        }
      }
    }

    return new StatementFormat(format, markers, args);
  }

  public Statement createTrace(
      final Where where,
      final String format,
      final List<Node> args) throws SemanticException {

    final String FUNCTION_NAME = "trace";
    if (null == args) {
      return new StatementFormat(
          FUNCTION_NAME,
          format,
          Collections.<FormatMarker>emptyList(),
          Collections.<Node>emptyList());
    }

    final List<FormatMarker> markers = FormatMarker.extractMarkers(format);
    if (markers.size() != args.size()) {
      raiseError(where, String.format(WRONG_FORMAT_ARG_SPEC, markers.size(), args.size()));
    }

    return new StatementFormat(FUNCTION_NAME, format, markers, args);
  }

  public Statement createExceptionCall(final Where where, final String text) {
    return new StatementFunctionCall("exception", text);
  }

  public Statement createMark(final Where where, final String text) {
    return new StatementFunctionCall("mark", text);
  }

  public Statement createUnpredicted() {
    return new StatementFunctionCall("unpredicted");
  }

  public Statement createUndefined() {
    return new StatementFunctionCall("undefined");
  }

  public Statement createAssert(
      final Where where,
      final Expr condition,
      final String message) {
    final String name = "assertion";
    return null != message
        ? new StatementFunctionCall(name, condition, message)
        : new StatementFunctionCall(name, condition);
  }
}
