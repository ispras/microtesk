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

package ru.ispras.microtesk.translator.nml.ir.expr;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.symbols.Where;
import ru.ispras.microtesk.translator.antlrex.errors.SymbolTypeMismatch;
import ru.ispras.microtesk.translator.antlrex.errors.UndeclaredSymbol;
import ru.ispras.microtesk.translator.antlrex.symbols.Symbol;
import ru.ispras.microtesk.translator.nml.NmlSymbolKind;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.nml.errors.UndefinedPrimitive;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryExpr;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

public final class LocationFactory extends WalkerFactoryBase {
  private static final String OUT_OF_BOUNDS =
      "The bitfield expression tries to access bit %d which is beyond location bounds (%d bits).";

  private static final String FAILED_TO_CALCULATE_SIZE =
      "Unable to calculate bitfield size. The given bitfield expressions cannot be reduced to constant value.";

  public LocationFactory(WalkerContext context) {
    super(context);
  }

  public Expr location(Where where, String name) throws SemanticException {
    final Symbol symbol = findSymbol(where, name);
    final Enum<?> kind = symbol.getKind();

    if ((NmlSymbolKind.MEMORY != kind) && (NmlSymbolKind.ARGUMENT != kind)) {
      raiseError(
        where,
        new SymbolTypeMismatch(
          name, kind, Arrays.<Enum<?>>asList(NmlSymbolKind.MEMORY, NmlSymbolKind.ARGUMENT))
      );
    }

    final LocationCreator creator = (NmlSymbolKind.MEMORY == kind) ?
        new MemoryBasedLocationCreator(this, where, name, null) :
        new ArgumentBasedLocationCreator(this, where, name);

    final LocationAtom result = creator.create();
    return newLocationExpr(result);
  }

  public Expr location(Where where, String name, Expr index) throws SemanticException {
    checkNotNull(index);

    final Symbol symbol = findSymbol(where, name);
    final Enum<?> kind = symbol.getKind();

    if (NmlSymbolKind.MEMORY != kind) {
      raiseError(where, new SymbolTypeMismatch(name, kind, NmlSymbolKind.MEMORY));
    }

    final LocationCreator creator = new MemoryBasedLocationCreator(this, where, name, index);
    final LocationAtom result = creator.create();
    return newLocationExpr(result);
  }

  public Expr location(Where where, String name, List<String> argNames) throws SemanticException {
    // TODO
    raiseError(where, "Unsupported construct.");
    return null;
  }

  public Expr bitfield(
      final Where where, final Expr variable, final Expr pos) throws SemanticException {
    checkNotNull(variable);
    checkNotNull(pos);

    final LocationAtom location = extractLocationAtom(variable);
    if (pos.isConstant()) {
      checkBitfieldBounds(where, pos.integerValue(), location.getType().getBitSize());
    }

    final Type bitfieldType = location.getType().resize(1);
    final Location result = LocationAtom.createBitfield(location, pos, pos, bitfieldType);

    return newLocationExpr(result);
  }

  public Expr bitfield(
      final Where where, final Expr variable, final Expr from, final Expr to)
      throws SemanticException {
    checkNotNull(variable);
    checkNotNull(from);
    checkNotNull(to);

    final LocationAtom location = extractLocationAtom(variable);
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

      return newLocationExpr(LocationAtom.createBitfield(location, from, to, bitfieldType));
    }

    final Expr.Reduced reducedFrom = from.reduce();
    final Expr.Reduced reducedTo = to.reduce();

    if (null == reducedFrom || null == reducedTo) {
      raiseError(where, FAILED_TO_CALCULATE_SIZE);
    }

    checkNotNull(reducedFrom.polynomial); // Cannot be reduced to constant at this point
    checkNotNull(reducedTo.polynomial); // Cannot be reduced to constant at this point

    if (reducedFrom.polynomial.equals(reducedTo.polynomial)) {
      final int bitfieldSize = Math.abs(reducedTo.constant - reducedFrom.constant) + 1;
      final Type bitfieldType = location.getType().resize(bitfieldSize);

      return newLocationExpr(LocationAtom.createBitfield(location, from, to, bitfieldType));
    }

    raiseError(where, FAILED_TO_CALCULATE_SIZE);
    return null;
  }

  private void checkBitfieldBounds(Where w, int position, int size) throws SemanticException {
    if (!(0 <= position && position < size)) {
      raiseError(w, String.format(OUT_OF_BOUNDS, position, size));
    }
  }

  public Expr concat(final Where w, final Expr leftExpr, final Expr rightExpr) {
    checkNotNull(leftExpr);
    checkNotNull(rightExpr);

    final LocationAtom left = extractLocationAtom(leftExpr);
    final Location right = extractLocation(rightExpr);

    final int leftSize = left.getType().getBitSize();
    final int rightSize = right.getType().getBitSize();
    final int concatSize = leftSize + rightSize;

    final Type concatType = left.getType().resize(concatSize);

    if (right instanceof LocationAtom) {
      return newLocationExpr(new LocationConcat(concatType, Arrays.asList((LocationAtom) right, left)));
    }

    final List<LocationAtom> concatenated = new ArrayList<>(((LocationConcat) right).getLocations());
    concatenated.add(left);

    return newLocationExpr(new LocationConcat(concatType, concatenated));
  }

  private Symbol findSymbol(Where where, String name) throws SemanticException {
    final Symbol symbol = getSymbols().resolve(name);

    if (null == symbol) {
      raiseError(where, new UndeclaredSymbol(name));
    }

    return symbol;
  }

  private static Expr newLocationExpr(final Location source) {
    InvariantChecks.checkNotNull(source);

    final NodeInfo nodeInfo = NodeInfo.newLocation(source);

    final String name = source.toString();
    final DataType dataType = TypeCast.getFortressDataType(nodeInfo.getType());

    final Node node = new NodeVariable(name, dataType);
    node.setUserData(nodeInfo);

    return new Expr(node);
  }

  private static LocationAtom extractLocationAtom(final Expr expr) {
    InvariantChecks.checkTrue(expr.getNodeInfo().isLocation());
    return (LocationAtom) expr.getNodeInfo().getSource();
  }

  private static Location extractLocation(final Expr expr) {
    InvariantChecks.checkTrue(expr.getNodeInfo().isLocation());
    return (Location) expr.getNodeInfo().getSource();
  }
}


interface LocationCreator {
  public LocationAtom create() throws SemanticException;
}


final class MemoryBasedLocationCreator extends WalkerFactoryBase implements LocationCreator {
  private final Where where;
  private final String name;
  private final Expr index;

  public MemoryBasedLocationCreator(WalkerContext context, Where where, String name, Expr index) {
    super(context);

    this.where = where;
    this.name = name;
    this.index = index;
  }

  @Override
  public LocationAtom create() throws SemanticException {
    final MemoryExpr memory = findMemory();

    // Checking bounds for constant values.
    if (index != null && index.isConstant()) {
      final BigInteger indexValue = index.bigIntegerValue();
      if (indexValue.compareTo(BigInteger.ZERO) < 0 || 
          indexValue.compareTo(memory.getSize()) >= 0) {
        raiseError(where, String.format("Index is out of bounds: %d. It must be in [0..%d)",
            indexValue, memory.getSize()));
      }
    }

    return LocationAtom.createMemoryBased(name, memory, index);
  }

  private MemoryExpr findMemory() throws SemanticException {
    if (!getIR().getMemory().containsKey(name)) {
      raiseError(where, new UndefinedPrimitive(name, NmlSymbolKind.MEMORY));
    }

    return getIR().getMemory().get(name);
  }
}


final class ArgumentBasedLocationCreator extends WalkerFactoryBase implements LocationCreator {
  private static final String UNEXPECTED_PRIMITIVE =
    "The %s argument refers to a %s primitive that cannot be used as a location.";

  private final Where where;
  private final String name;

  public ArgumentBasedLocationCreator(WalkerContext context, Where where, String name) {
    super(context);

    this.where = where;
    this.name = name;
  }

  @Override
  public LocationAtom create() throws SemanticException {
    final Primitive primitive = findArgument();

    if ((Primitive.Kind.MODE != primitive.getKind()) && (Primitive.Kind.IMM != primitive.getKind())) {
      raiseError(where, String.format(UNEXPECTED_PRIMITIVE, name, primitive.getKind()));
    }

    return LocationAtom.createPrimitiveBased(name, primitive);
  }

  private Primitive findArgument() throws SemanticException {
    if (!getThisArgs().containsKey(name)) {
      raiseError(where, new UndefinedPrimitive(name, NmlSymbolKind.ARGUMENT));
    }

    return getThisArgs().get(name);
  }
}
