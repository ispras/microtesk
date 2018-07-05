/*
 * Copyright 2014-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.tools.microft;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.ExprUtils;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementAttributeCall;
import ru.ispras.microtesk.translator.nml.ir.primitive.StatementFormat;
import ru.ispras.microtesk.utils.FormatMarker;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.json.JsonValue;

class PassFormat extends IrPass<PassFormat.Info> {
  private final Map<String, Primitive> parameters = new java.util.HashMap<>();

  public PassFormat(final String name) {
    super(name);
  }

  @Override
  public Info run(final List<PrimitiveAND> insn, final PassContext ctx) {
    parameters.clear();
    final String line =
      evaluateAttribute(getName().toLowerCase(), Entity.create(insn));
    return new Info(line, parameters);
  }

  public static class Info {
    public final String formatLine;
    public final Map<String, Primitive> parameters;

    public Info(final String line, final Map<String, Primitive> params) {
      this.formatLine = line;
      this.parameters = params;
    }
  }

  private String evaluateAttribute(final String attr, final Entity e) {
    final StatementFormat fmt =
      findStatement(e.getType(), attr, StatementFormat.class);

    final StatementAttributeCall call =
      findStatement(e.getType(), attr, StatementAttributeCall.class);

    if (fmt != null) {
      return evaluateFormat(fmt, e);
    } else if (call != null) {
      return evaluateCall(call, e);
    }
    return null;
  }

  private String evaluateFormat(final StatementFormat fmt, final Entity e) {
    final String origin = fmt.getFormat();
    final StringBuilder builder = new StringBuilder();

    final IterablePair<FormatMarker, Node> args =
      IterablePair.create(fmt.getMarkers(), fmt.getArguments());

    int offset = 0;
    for (final Pair<FormatMarker, Node> token : args) {
      final String value = evaluateNodeFormat(token.first, token.second, e);
      builder.append(origin.substring(offset, token.first.getStart()));
      builder.append(value);

      offset = token.first.getEnd();
    }
    builder.append(origin.substring(offset, origin.length()));

    return builder.toString();
  }

  private String evaluateCall(final StatementAttributeCall call, final Entity e) {
    final String hostName = call.getCalleeName();

    final Entity host;
    if (call.isArgumentCall()) {
      host = e.getTypeArguments().get(hostName);
      if (host == null) {
        final Primitive p = e.getTypeParameters().get(hostName);
        return substitution(hostName, p);
      } else if (isUnboundMode(host)) {
        final Pair<Entity, String> parent = host.getParent();
        return substitution(parent.second, host.getType());
      }
    } else if (call.isInstanceCall()) {
      host = Entity.create(call.getCalleeInstance(), e);
    } else {
      throw new IllegalStateException();
    }
    return evaluateAttribute(call.getAttributeName(), host);
  }

  private static boolean isUnboundMode(final Entity e) {
    return e.isTerminal() &&
      e.hasUnbound() &&
      e.getType().getKind() == Primitive.Kind.MODE ;
  }

  private String evaluateNodeFormat(
      final FormatMarker fmt,
      final Node node,
      final Entity e) {
    if (node.getUserData() instanceof StatementAttributeCall) {
      final StatementAttributeCall call =
        (StatementAttributeCall) node.getUserData();
      return evaluateCall(call, e);
    }
    /*
    if (ExprUtils.isOperation(node, StandardOperation.ITE)) {
      final Iterator<Node> it = valuesOf((NodeOperation) node).iterator();

      final StringBuilder builder = new StringBuilder("${");
      builder.append(it.next().toString());
      while (it.hasNext()) {
        builder.append("|").append(it.next().toString());
      }
      builder.append("}");

      return NodeValue.newString(builder.toString());
    }
    //*/
    if (ExprUtils.isVariable(node)) {
      final NodeVariable var = (NodeVariable) node;
      final Expr binding = e.getBindings().get(var.getName());
      if (binding != null && binding.isConstant()) {
        return evaluateValueFormat(fmt, (NodeValue) binding.getNode());
      }

      final Map<String, Primitive> params = e.getTypeParameters();
      if (params.containsKey(var.getName())) {
        return substitution(var.getName(), params.get(var.getName()));
      }
    }
    return node.toString();
  }

  private static String evaluateValueFormat(final FormatMarker fmt, final NodeValue value) {
    BigInteger ival = null;
    BitVector bv = null;

    switch (value.getDataType().getTypeId()) {
    case LOGIC_INTEGER:
      ival = value.getInteger();
      break;

    case BIT_VECTOR:
      bv = value.getBitVector();
      ival = bv.bigIntegerValue();
      break;
    }
    switch (fmt.getKind()) {
    case DEC:
      return ival.toString();

    case BIN:
      return bv.toBinString();

    case HEX:
      return bv.toHexString();

    case STR:
      return value.toString();
    }
    throw new IllegalStateException("Unreachable state");
  }

  private String substitution(final String name, final Primitive p) {
    parameters.put(name, p);

    return String.format("${%s}", name);
  }

  private static List<Node> valuesOf(final NodeOperation ite) {
    final List<Node> values = new java.util.ArrayList<>();
    collectValues(ite, values);

    return values;
  }

  private static void collectValues(final NodeOperation ite, final List<Node> values) {
    final List<Node> operands = ite.getOperands();
    dispatchNode(operands.get(1), values);
    dispatchNode(operands.get(2), values);
  }

  private static void dispatchNode(final Node node, final List<Node> values) {
    if (ExprUtils.isOperation(node, StandardOperation.ITE)) {
      collectValues((NodeOperation) node, values);
    } else {
      values.add(node);
    }
  }

  private static <T extends Statement> T findStatement(final PrimitiveAND p, final String name, final Class<T> c) {
    if (p.getAttributes().containsKey(name)) {
      final List<Statement> code = p.getAttributes().get(name).getStatements();
      final ListIterator<Statement> it = code.listIterator(code.size());
      while (it.hasPrevious()) {
        final Statement s = it.previous();
        if (c.isInstance(s)) {
          return c.cast(s);
        }
      }
    }
    return null;
  }
}
