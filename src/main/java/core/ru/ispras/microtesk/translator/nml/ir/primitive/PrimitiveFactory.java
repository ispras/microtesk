/*
 * Copyright 2013-2015 ISP RAS (http://www.ispras.ru)
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.symbols.Where;
import ru.ispras.microtesk.translator.antlrex.errors.SymbolTypeMismatch;
import ru.ispras.microtesk.translator.antlrex.errors.UndeclaredSymbol;
import ru.ispras.microtesk.translator.antlrex.symbols.Symbol;
import ru.ispras.microtesk.translator.nml.NmlSymbolKind;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.nml.errors.UndefinedPrimitive;
import ru.ispras.microtesk.translator.nml.errors.UndefinedProductionRuleItem;
import ru.ispras.microtesk.translator.nml.errors.UnsupportedParameterType;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationSourceMemory;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationSourcePrimitive;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;
import ru.ispras.microtesk.translator.nml.ir.expr.TypeCast;
import ru.ispras.microtesk.translator.nml.ir.expr.Location;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationAtom;
import ru.ispras.microtesk.translator.nml.ir.expr.LocationConcat;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

public final class PrimitiveFactory extends WalkerFactoryBase {
  public PrimitiveFactory(final WalkerContext context) {
    super(context);
  }

  public Primitive createMode(
      final Where where,
      final String name,
      final Map<String, Primitive> args,
      final Map<String, Attribute> attrs,
      final Expr retExpr) throws SemanticException {
    //checkAttributeDefined(where, name, Attribute.SYNTAX_NAME, attrs);
    //checkAttributeDefined(where, name, Attribute.IMAGE_NAME, attrs);

    for (final Map.Entry<String, Primitive> e : args.entrySet()) {
      if (Primitive.Kind.IMM != e.getValue().getKind()) {
        raiseError(where, new UnsupportedParameterType(
            e.getKey(), e.getValue().getKind().name(), Primitive.Kind.IMM.name()));
      }
    }

    if (null != retExpr && null == retExpr.getNodeInfo().getType()) {
      raiseError(where, "Return value is untyped. Use casts to enforce a certain type.");
    }

    final MemoryAccessStatus memoryAccessStatus =
        new MemoryAccessDetector(args, attrs).getMemoryAccessStatus(Attribute.ACTION_NAME);

    return new PrimitiveAND(
        name,
        Primitive.Kind.MODE,
        retExpr,
        args,
        attrs,
        canThrowException(attrs),
        MemoryAccessDetector.isMemoryReference(retExpr),
        memoryAccessStatus.isLoad(),
        memoryAccessStatus.isStore(),
        memoryAccessStatus.getBlockSize()
        );
  }

  public Primitive createOp(
      final Where where,
      final String name,
      final Map<String, Primitive> args,
      final Map<String, Attribute> attrs) throws SemanticException {
    //checkAttributeDefined(where, name, Attribute.SYNTAX_NAME, attrs);
    //checkAttributeDefined(where, name, Attribute.IMAGE_NAME, attrs);

    final MemoryAccessStatus memoryAccessStatus = 
        new MemoryAccessDetector(args, attrs).getMemoryAccessStatus(Attribute.ACTION_NAME);

    // if (memoryAccessStatus.isLoad() && memoryAccessStatus.isStore()) {
    //   System.out.printf("%-25s : %s%n", name, memoryAccessStatus);
    // }

    return new PrimitiveAND(
        name,
        Primitive.Kind.OP,
        null,
        args,
        attrs,
        canThrowException(attrs),
        false,
        memoryAccessStatus.isLoad(),
        memoryAccessStatus.isStore(),
        memoryAccessStatus.getBlockSize()
        );
  }

  /*private void checkAttributeDefined(
      final Where where,
      final String primitiveName,
      final String attributeName,
      final Map<String, Attribute> attrs) throws SemanticException {
    if (!attrs.containsKey(attributeName)) {
      raiseError(where, String.format(
          "The '%s' attribute is not defined for the '%s' primitive.",
          attributeName,
          primitiveName
          ));
    }
  }*/

  public Primitive createModeOR(
      final Where where,
      final String name,
      final List<String> orNames)throws SemanticException {
    final List<Primitive> orModes = new ArrayList<>();

    for (final String orName : orNames) {
      if (!getIR().getModes().containsKey(orName)) {
        raiseError(where, new UndefinedProductionRuleItem(orName, name, true, NmlSymbolKind.MODE));
      }

      final Primitive mode = getIR().getModes().get(orName);

      if (!orModes.isEmpty()) {
        new PrimitiveCompatibilityChecker(
            this, where, name, mode, orModes.get(0)).check();
      }

      orModes.add(mode);
    }

    return new PrimitiveOR(name, Primitive.Kind.MODE, orModes);
  }

  public Primitive createOpOR(
      final Where where,
      final String name,
      final List<String> orNames) throws SemanticException {
    final List<Primitive> orOps = new ArrayList<>();

    for (final String orName : orNames) {
      if (!getIR().getOps().containsKey(orName)) {
        raiseError(where, new UndefinedProductionRuleItem(orName, name, true, NmlSymbolKind.OP));
      }

      final Primitive op = getIR().getOps().get(orName);
      if (!orOps.isEmpty()) {
        new PrimitiveCompatibilityChecker(
            this, where, name, op, orOps.get(0)).check();
      }

      orOps.add(op);
    }

    return new PrimitiveOR(name, Primitive.Kind.OP, orOps);
  }

  public Primitive createImm(final Type type) {
    return new Primitive(type.getAlias(), Primitive.Kind.IMM, false, type, null);
  }

  public Primitive getMode(
      final Where where,
      final String modeName) throws SemanticException {
    if (!getIR().getModes().containsKey(modeName)) {
      raiseError(where, new UndefinedPrimitive(modeName, NmlSymbolKind.MODE));
    }

    return getIR().getModes().get(modeName);
  }

  public Primitive getOp(
      final Where where,
      final String opName) throws SemanticException {
    if (!getIR().getOps().containsKey(opName)) {
      raiseError(where, new UndefinedPrimitive(opName, NmlSymbolKind.OP));
    }

    return getIR().getOps().get(opName);
  }

  public Primitive getArgument(
      final Where where,
      final String name) throws SemanticException {
    if (!getThisArgs().containsKey(name)) {
      raiseError(where, new UndefinedPrimitive(name, NmlSymbolKind.ARGUMENT));
    }

    return getThisArgs().get(name);
  }

  public Instance newInstance(
      final Where where,
      final String name,
      final List<InstanceArgument> arguments) throws SemanticException {
    final Symbol symbol = getSymbols().resolve(name);
    if (null == symbol) {
      raiseError(where, new UndeclaredSymbol(name));
    }

    if ((symbol.getKind() != NmlSymbolKind.MODE) && (symbol.getKind() != NmlSymbolKind.OP)) {
      raiseError(where, new SymbolTypeMismatch(name, symbol.getKind(),
          Arrays.<Enum<?>>asList(NmlSymbolKind.MODE, NmlSymbolKind.OP)));
    }

    final Primitive primitive = symbol.getKind() == NmlSymbolKind.MODE ?
        getIR().getModes().get(name) : getIR().getOps().get(name);

    if (null == primitive) {
      raiseError(where, new UndefinedPrimitive(name, (NmlSymbolKind)symbol.getKind()));
    }

    if (primitive.isOrRule()) {
      raiseError(where, String.format("%s is not an AND rule!", name)); 
    }

    final List<InstanceArgument> args = new ArrayList<>(arguments);
    final PrimitiveAND primitiveAND = (PrimitiveAND) primitive;

    final String[] argNames = 
        primitiveAND.getArguments().keySet().toArray(new String[primitiveAND.getArguments().size()]);

    int index = 0;
    for (final InstanceArgument ie : args) {
      if (ie.getKind() == InstanceArgument.Kind.EXPR && ie.getExpr().isConstant()) {
        final Primitive argument = primitiveAND.getArguments().get(argNames[index]);
        final Type argumentType = argument.getReturnType();

        final Expr newExpr = TypeCast.castConstantTo(ie.getExpr(), argumentType);
        final InstanceArgument newIe = InstanceArgument.newExpr(newExpr, ie.getInvolvedArgs());

        args.set(index, newIe);
      }
      
      for(final String involvedArgName : ie.getInvolvedArgs()) {
        /*System.out.println(String.format("%s <- %s.%s [%s]",
            involvedArgName,
            primitiveAND.getName(),
            argNames[index],
            primitiveAND.getArgUsage(argNames[index]))
            );*/

        getThis().setArgsUsage(
            involvedArgName,
            primitiveAND.getArgUsage(argNames[index]));
      }
      index++;
    }

    final Instance result = new Instance(primitiveAND, args);
    return result;
  }
  
  private boolean canThrowException(final Map<String, Attribute> attrs) {
    for (final Attribute attr : attrs.values()) {
      if (attr.canThrowException()) {
        return true;
      }
    }
    return false;
  }

  private static final class MemoryAccessDetector {

    private static boolean isMemoryReference(final Expr expr) {
      if (null == expr) {
        return false;
      }

      final NodeInfo nodeInfo = expr.getNodeInfo();
      if (!nodeInfo.isLocation()) {
        return false;
      }

      return isMemoryReference((Location) nodeInfo.getSource()); 
    }

    private static boolean isMemoryReference(final Location location) {
      if (location instanceof LocationAtom) {
        return isMemoryReference((LocationAtom) location);
      } else {
        return isMemoryReference((LocationConcat) location);
      }
    }

    private static boolean isMemoryReference(final LocationAtom locationAtom) {
      if ((locationAtom.getSource() instanceof LocationSourceMemory)) {
        final LocationSourceMemory source = (LocationSourceMemory) locationAtom.getSource();
        final BigInteger memorySize = source.getMemory().getSize();

        // MEMs of length 1 are often used as global variables.
        // For this reason, there such MEMs are excluded.
        return source.getMemory().getKind() == Memory.Kind.MEM &&
            memorySize.compareTo(BigInteger.ONE) > 0;
      }

      if ((locationAtom.getSource() instanceof LocationSourcePrimitive)) {
        final LocationSourcePrimitive source =
            (LocationSourcePrimitive) locationAtom.getSource();

        if (source.getPrimitive() instanceof PrimitiveAND) {
          return ((PrimitiveAND) source.getPrimitive()).isMemoryReference();
        }
      }

      return false;
    }

    private static boolean isMemoryReference(final LocationConcat locationConcat) { 
      for (final LocationAtom locationAtom : locationConcat.getLocations()) {
        if (isMemoryReference(locationAtom)) {
          return true;
        }
      }
      return false;
    }

    private final Map<String, Primitive> args;
    private final Map<String, Attribute> attrs;
    private final List<Location> loadTargets;

    public MemoryAccessDetector(
        final Map<String, Primitive> args,
        final Map<String, Attribute> attrs) {
      this.args = args;
      this.attrs = attrs;
      this.loadTargets = new ArrayList<>();
    }

    private void addLoadTarget(final Location location) {
      loadTargets.add(location);
    }

    private boolean isLoadTarget(final Expr expr) {
      final NodeInfo nodeInfo = expr.getNodeInfo();
      if (!nodeInfo.isLocation()) {
        return false;
      }

      final Location location = (Location) nodeInfo.getSource();
      for (final Location loadLocation : loadTargets) {
        if (location.equals(loadLocation)) {
          return true;
        }
      }

      return false;
    }

    private MemoryAccessStatus getMemoryAccessStatus(final String attributeName) {
      final Attribute attribute = attrs.get(attributeName);
      if (null == attribute) {
        return MemoryAccessStatus.NO;
      }

      return getMemoryAccessStatus(attribute.getStatements());
    }

    private MemoryAccessStatus getMemoryAccessStatus(final List<Statement> stmts) {
      MemoryAccessStatus result = MemoryAccessStatus.NO;

      for (final Statement stmt : stmts) {
        switch(stmt.getKind()) {
          case ASSIGN:
            final StatementAssignment stmtAssign = (StatementAssignment) stmt;
            final MemoryAccessStatus assignResult = getMemoryAccessStatus(stmtAssign);

            // If the same a variable was used by a load and a store action,
            // we assume that this is store action and the load was performed just
            // make it possible to to write a small portion of data (smaller than the storage unit).

            if (assignResult.isStore() && isLoadTarget(stmtAssign.getRight())) {
              result = assignResult;
            } else {
              result = result.merge(getMemoryAccessStatus((StatementAssignment) stmt));
            }

            break;

          case CALL:
            result = result.merge(getMemoryAccessStatus((StatementAttributeCall) stmt));
            break;

          case COND:
            result = result.merge(getMemoryAccessStatus((StatementCondition) stmt));
            break;

          case FORMAT:  // Ignored
          case STATUS:  // Ignored
          case FUNCALL: // Ignored
            break;

          default:
            throw new IllegalArgumentException("Unknown statement kind: " + stmt.getKind());
        }
      }

      return result;
    }

    private MemoryAccessStatus getMemoryAccessStatus(final StatementAssignment stmt) {
      MemoryAccessStatus result = MemoryAccessStatus.NO;

      final Expr right = stmt.getRight();
      final Location left = stmt.getLeft();

      if (isMemoryReference(right)) {
        // Load action is detected
        final int bitSize = right.getNodeInfo().getType().getBitSize();
        result = new MemoryAccessStatus(true, false, bitSize);
        addLoadTarget(left);
      }

      if (isMemoryReference(left)) {
        // Store action is detected
        final int bitSize = left.getType().getBitSize();
        result = result.merge(new MemoryAccessStatus(false, true, bitSize));
      }

      return result;
    }

    private MemoryAccessStatus getMemoryAccessStatus(final StatementAttributeCall stmt) {
      // Instance Attribute Call
      if (stmt.getCalleeInstance() != null) {
        final PrimitiveAND primitive = stmt.getCalleeInstance().getPrimitive();
        return new MemoryAccessStatus(
            primitive.isLoad(), primitive.isStore(), primitive.getBlockSize());
      }

      // Argument Attribute Call
      if (stmt.getCalleeName() != null) {
        final Primitive callee = args.get(stmt.getCalleeName());

        if (callee.isOrRule()) {
          return MemoryAccessStatus.NO;
        }

        final PrimitiveAND primitive = (PrimitiveAND) callee;
        return new MemoryAccessStatus(
            primitive.isLoad(), primitive.isStore(), primitive.getBlockSize());
      }

      // This Object Attribute Call
      final Attribute attribute = attrs.get(stmt.getAttributeName());
      return getMemoryAccessStatus(attribute.getStatements());
    }

    private MemoryAccessStatus getMemoryAccessStatus(final StatementCondition stmt) {
      MemoryAccessStatus result = MemoryAccessStatus.NO;

      for (int index = 0; index < stmt.getBlockCount(); ++index) {
        final StatementCondition.Block block = stmt.getBlock(index);
        result = result.merge(getMemoryAccessStatus(block.getStatements()));
      }

      return result;
    }
  }
}

final class MemoryAccessStatus {
  public static final MemoryAccessStatus NO =
      new MemoryAccessStatus(false, false, 0);

  private boolean load;
  private boolean store;
  private int blockSize;

  public MemoryAccessStatus(
      final boolean load,
      final boolean store,
      final int blockSize) {
    this.load = load;
    this.store = store;
    this.blockSize = blockSize;
  }

  public boolean isLoad() { return load; }
  public boolean isStore() { return store; }
  public int getBlockSize() { return blockSize; }

  public MemoryAccessStatus merge(final MemoryAccessStatus other) {
    return new MemoryAccessStatus(
        this.load  || other.load,
        this.store || other.store,
        Math.max(this.blockSize, other.blockSize)
        );
  }

  @Override
  public String toString() {
    return String.format(
        "MemoryAccessStatus [isLoad=%s, isStore=%s, blockSize=%s]",
        load,
        store,
        blockSize
        );
  }
}
