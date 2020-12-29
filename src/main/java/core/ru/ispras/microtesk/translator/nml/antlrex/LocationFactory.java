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

package ru.ispras.microtesk.translator.nml.antlrex;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.transformer.Reducer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.errors.SymbolTypeMismatch;
import ru.ispras.microtesk.translator.antlrex.errors.UndeclaredSymbol;
import ru.ispras.microtesk.translator.antlrex.symbols.Symbol;
import ru.ispras.microtesk.translator.antlrex.symbols.Where;
import ru.ispras.microtesk.translator.nml.NmlSymbolKind;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.Location;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;
import ru.ispras.microtesk.translator.nml.ir.expr.Operator;
import ru.ispras.microtesk.translator.nml.ir.primitive.Instance;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAnd;
import ru.ispras.microtesk.translator.nml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryResource;
import ru.ispras.microtesk.translator.nml.ir.shared.Struct;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;
import ru.ispras.microtesk.utils.StringUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public final class LocationFactory extends WalkerFactoryBase {
  private static final String OUT_OF_BOUNDS =
      "The bitfield expression tries to access bit %d which is beyond location bounds (%d bits).";

  private static final String FAILED_TO_CALCULATE_SIZE =
      "Unable to calculate bitfield size."
          + " The given bitfield expressions cannot be reduced to constant value.";

  public LocationFactory(WalkerContext context) {
    super(context);
  }

  public Expr location(
      final Where where,
      final String name) throws SemanticException {
    final Symbol symbol = findSymbol(where, name);
    final Enum<?> kind = symbol.getKind();

    // Hack to deal with internal variables described by string constants.
    if (NmlSymbolKind.LET_CONST == kind) {
      final LetConstant constant = getIr().getConstants().get(name);
      if (null == constant) {
        // Forward definition.
        raiseError(where, String.format(
            "Constant %s is not found. Forward definitions are not allowed.", name));
      }

      final Expr expr = constant.getExpr();
      if (expr.isInternalVariable()) {
        return new Expr(expr);
      }
    }

    if (NmlSymbolKind.MEMORY != kind && NmlSymbolKind.ARGUMENT != kind) {
      raiseError(
          where,
          new SymbolTypeMismatch(
              name, kind, Arrays.<Enum<?>>asList(NmlSymbolKind.MEMORY, NmlSymbolKind.ARGUMENT))
      );
    }

    final Location location = NmlSymbolKind.MEMORY == kind
        ? newLocationMemory(where, name, null)
        : newLocationArgument(where, name);

    return newLocationExpr(where, location);
  }

  public Expr location(
      final Where where,
      final String name,
      final Expr index,
      final List<String> fields) throws SemanticException {
    InvariantChecks.checkNotNull(index);

    final Symbol symbol = findSymbol(where, name);
    final Enum<?> kind = symbol.getKind();

    if (NmlSymbolKind.MEMORY != kind) {
      raiseError(where, new SymbolTypeMismatch(name, kind, NmlSymbolKind.MEMORY));
    }

    final Location location = newLocationMemory(where, name, index);
    final Location locationField = namedField(where, location, fields);

    return newLocationExpr(where, locationField);
  }

  public Expr location(
      final Where where,
      final String name,
      final List<String> fields) throws SemanticException {

    final Symbol symbol = findSymbol(where, name);
    final Enum<?> kind = symbol.getKind();

    if (NmlSymbolKind.MEMORY != kind && NmlSymbolKind.ARGUMENT != kind) {
      raiseError(
          where,
          new SymbolTypeMismatch(
              name, kind, Arrays.<Enum<?>>asList(NmlSymbolKind.MEMORY, NmlSymbolKind.ARGUMENT))
      );
    }

    if (NmlSymbolKind.MEMORY == kind) {
      final Location location = newLocationMemory(where, name, null);
      final Location locationField = namedField(where, location, fields);
      return newLocationExpr(where, locationField);
    }

    final Primitive argument = getThisArgs().get(name);
    if (null == argument) {
      raiseError(where, new UndefinedPrimitive(name, NmlSymbolKind.ARGUMENT));
    }

    final Location location = argumentField(where, argument, name, fields);
    return newLocationExpr(where, location);
  }

  public Expr location(
      final Where where,
      final Instance instance) throws SemanticException {
    final Primitive primitive = instance.getPrimitive();
    if (primitive.getKind() != Primitive.Kind.MODE) {
      raiseError(where,
          instance.getPrimitive().getName() + " is not an addressing mode.");
    }

    if (primitive.getReturnType() == null) {
      raiseError(where,
          instance.getPrimitive().getName() + " does not have a return expression.");
    }

    final Type type = primitive.getReturnType();
    final Location location = null;

    raiseError(where, "Unsupported construct.");
    return newLocationExpr(where, location);
  }

  private Location namedField(
      final Where where,
      final Location location,
      final List<String> fields) throws SemanticException {
    if (fields.isEmpty()) {
      return location;
    }

    final Type type = location.getType();
    final Struct struct = type.getStruct();

    if (null == struct) {
      raiseError(where, String.format(
          "%s does not have named fields.", location.toString()));
    }

    final Struct.Field field = struct.getField(fields);
    if (null == field) {
      raiseError(where, String.format(
          "%s does not have field named %s",
          location.toString(),
          StringUtils.toString(fields, ".")));
    }

    final Expr from = new Expr(NodeValue.newInteger(field.getMin()));
    from.setNodeInfo(NodeInfo.newConst(null));

    final Expr to = new Expr(NodeValue.newInteger(field.getMax()));
    to.setNodeInfo(NodeInfo.newConst(null));

    return createBitfield(where, location, from, to, field.getType());
  }

  private Location argumentField(
      final Where where,
      final Primitive argument,
      final String argumentName,
      final List<String> fields) throws SemanticException {
    if (argument.getKind() == Primitive.Kind.IMM) {
      final Location location = Location.createPrimitiveBased(argumentName, argument);
      return namedField(where, location, fields);
    }

    if (fields.isEmpty()) {
      raiseError(where, String.format(
          "%s cannot be uses as a value. Only immediate arguments are allowed.",
          argumentName)
      );
    }

    final String nestedArgumentName = fields.get(0);
    final String nestedArgrumentFullName = argumentName + "." + nestedArgumentName;

    if (!(argument instanceof PrimitiveAnd)) {
      raiseError(where, String.format(
          "Cannot access arguments of %s (%s). It must be an AND-rule.",
          argument.getName(),
          nestedArgrumentFullName
          ));
    }

    final Primitive nestedArgument =
        ((PrimitiveAnd) argument).getArguments().get(nestedArgumentName);

    if (null == nestedArgument) {
      raiseError(where, new UndefinedPrimitive(nestedArgrumentFullName, NmlSymbolKind.ARGUMENT));
    }

    return argumentField(
        where, nestedArgument, nestedArgrumentFullName, fields.subList(1, fields.size()));
  }

  public Expr bitfield(
      final Where where, final Expr variable, final Expr pos) throws SemanticException {
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(pos);

    final Location location = extractLocation(variable);
    final Type type = location.getType();

    if (pos.isConstant()) {
      checkBitfieldBounds(where, pos.integerValue(), type.getBitSize());
    }

    final Type bitfieldType = type.resize(1);
    final Location result = createBitfield(where, location, pos, pos, bitfieldType);

    return newLocationExpr(where, result);
  }

  public Expr bitfield(
      final Where where, final Expr variable, final Expr from, final Expr to)
      throws SemanticException {
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(from);
    InvariantChecks.checkNotNull(to);

    final Location location = extractLocation(variable);
    if (from.isConstant() != to.isConstant()) {
      raiseError(where, FAILED_TO_CALCULATE_SIZE);
    }

    if (from.isConstant()) {
      final int fromPos = from.integerValue();
      final int toPos = to.integerValue();
      final int locationSize = location.getType().getBitSize();

      checkBitfieldBounds(where, fromPos, locationSize);
      checkBitfieldBounds(where, toPos, locationSize);

      final int bitfieldSize = Math.abs(toPos - fromPos) + 1;
      final Type bitfieldType = location.getType().resize(bitfieldSize);

      return newLocationExpr(where, createBitfield(
          where,
          location,
          fromPos < toPos ? from : to,
          fromPos > toPos ? from : to,
          bitfieldType
          ));
    }

    final ExprReducer.Reduced reducedFrom = ExprReducer.reduce(from);
    final ExprReducer.Reduced reducedTo = ExprReducer.reduce(to);

    if (null == reducedFrom || null == reducedTo) {
      raiseError(where, FAILED_TO_CALCULATE_SIZE);
    }

    InvariantChecks.checkNotNull(reducedFrom.polynomial); // Can't be reduced to const at this point
    InvariantChecks.checkNotNull(reducedTo.polynomial); // Can't be reduced to const at this point

    if (reducedFrom.polynomial.equals(reducedTo.polynomial)) {
      final int bitfieldSize = Math.abs(reducedTo.constant - reducedFrom.constant) + 1;
      final Type bitfieldType = location.getType().resize(bitfieldSize);

      return newLocationExpr(where, createBitfield(
          where,
          location,
          reducedFrom.constant < reducedTo.constant ? from : to,
          reducedFrom.constant > reducedTo.constant ? from : to,
          bitfieldType
          ));
    }

    raiseError(where, FAILED_TO_CALCULATE_SIZE);
    return null;
  }

  private Location createBitfield(
      final Where where,
      final Location location,
      final Expr low,
      final Expr high,
      final Type type) throws SemanticException {
    final Location.Bitfield bitfield = location.getBitfield();

    if (null == bitfield) {
      return Location.createBitfield(location, high, low, type);
    }

    if (!(bitfield.getFrom().isConstant() && bitfield.getTo().isConstant())) {
      // Currently, an existing bitfield means a field of a structure, which has fixed boundaries.
      // Other cases are unexpected.
      raiseError(where, "Fixed bitfield boundaries are required for " + location.toString());
    }

    final Node oldNodeLow = bitfield.getTo().getNode();
    final Node newNodeLow = new NodeOperation(StandardOperation.ADD, low.getNode(), oldNodeLow);
    final Node newNodeHigh = new NodeOperation(StandardOperation.ADD, high.getNode(), oldNodeLow);

    final Expr newLow = new Expr(Reducer.reduce(newNodeLow));
    newLow.setNodeInfo(newLow.isConstant()
        ? NodeInfo.newConst(null)
        : NodeInfo.newOperator(Operator.PLUS, low.getNodeInfo().getType()));

    final Expr newHigh = new Expr(Reducer.reduce(newNodeHigh));
    newHigh.setNodeInfo(newHigh.isConstant()
        ? NodeInfo.newConst(null)
        : NodeInfo.newOperator(Operator.PLUS, high.getNodeInfo().getType()));

    return Location.createBitfield(location, newHigh, newLow, type);
  }

  private void checkBitfieldBounds(Where w, int position, int size) throws SemanticException {
    if (!(0 <= position && position < size)) {
      raiseError(w, String.format(OUT_OF_BOUNDS, position, size));
    }
  }

  private Symbol findSymbol(Where where, String name) throws SemanticException {
    final Symbol symbol = getSymbols().resolve(name);

    if (null == symbol) {
      raiseError(where, new UndeclaredSymbol(name));
    }

    return symbol;
  }

  private Expr newLocationExpr(final Where where, final Location source) throws SemanticException {
    InvariantChecks.checkNotNull(source);

    final NodeInfo nodeInfo = NodeInfo.newLocation(source);
    final String name = source.toString();
    final Type type = nodeInfo.getType();

    if (null == type) {
      raiseError(where, String.format("Location %s is of unknown type", name));
    }

    final DataType dataType = TypeCast.getFortressDataType(type);
    final Node node = new NodeVariable(name, dataType);
    node.setUserData(nodeInfo);

    return new Expr(node);
  }

  private static Location extractLocation(final Expr expr) {
    InvariantChecks.checkTrue(expr.getNodeInfo().isLocation());
    return (Location) expr.getNodeInfo().getSource();
  }

  private Location newLocationMemory(
      final Where where,
      final String name,
      final Expr index) throws SemanticException {
    final MemoryResource memory = findMemory(where, name);

    // Checking bounds for constant values.
    if (index != null && index.isConstant()) {
      final BigInteger indexValue = index.bigIntegerValue();
      if (indexValue.compareTo(BigInteger.ZERO) < 0
          || indexValue.compareTo(memory.getSize()) >= 0) {
        raiseError(where, String.format("Index is out of bounds: %d. It must be in [0..%d)",
            indexValue, memory.getSize()));
      }
    }

    return Location.createMemoryBased(name, memory, index);
  }

  private Location newLocationArgument(
      final Where where,
      final String name) throws SemanticException {
    final Primitive primitive = findArgument(where, name);

    if ((Primitive.Kind.MODE != primitive.getKind())
        && (Primitive.Kind.IMM != primitive.getKind())) {
      raiseError(where, String.format(
          "The %s argument refers to a %s primitive that cannot be used as a location.",
          name,
          primitive.getKind())
      );
    }

    return Location.createPrimitiveBased(name, primitive);
  }

  private MemoryResource findMemory(
      final Where where,
      final String name) throws SemanticException {
    if (!getIr().getMemory().containsKey(name)) {
      raiseError(where, new UndefinedPrimitive(name, NmlSymbolKind.MEMORY));
    }

    return getIr().getMemory().get(name);
  }

  private Primitive findArgument(
      final Where where,
      final String name) throws SemanticException {
    if (!getThisArgs().containsKey(name)) {
      raiseError(where, new UndefinedPrimitive(name, NmlSymbolKind.ARGUMENT));
    }

    return getThisArgs().get(name);
  }
}
