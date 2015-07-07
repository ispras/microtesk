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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.model.api.type.TypeId;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.antlrex.errors.SymbolTypeMismatch;
import ru.ispras.microtesk.translator.antlrex.errors.UndeclaredSymbol;
import ru.ispras.microtesk.translator.antlrex.symbols.ISymbol;
import ru.ispras.microtesk.translator.nml.NmlSymbolKind;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.nml.errors.UndefinedPrimitive;
import ru.ispras.microtesk.translator.nml.errors.UndefinedProductionRuleItem;
import ru.ispras.microtesk.translator.nml.errors.UnsupportedParameterType;
import ru.ispras.microtesk.translator.nml.ir.expression.Expr;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

public final class PrimitiveFactory extends WalkerFactoryBase {
  public PrimitiveFactory(WalkerContext context) {
    super(context);
  }

  public Primitive createMode(
      final Where where,
      final String name,
      final Map<String, Primitive> args,
      final Map<String, Attribute> attrs,
      final Expr retExpr) throws SemanticException {

    for (final Map.Entry<String, Primitive> e : args.entrySet()) {
      if (Primitive.Kind.IMM != e.getValue().getKind()) {
        raiseError(where, new UnsupportedParameterType(e.getKey(), e.getValue().getKind().name(),
            Primitive.Kind.IMM.name()));
      }
    }

    return new PrimitiveAND(
        name,
        Primitive.Kind.MODE,
        retExpr,
        args,
        attrs,
        canThrowException(attrs)
        );
  }

  public Primitive createOp(
      final Where where,
      final String name,
      final Map<String, Primitive> args,
      final Map<String, Attribute> attrs) throws SemanticException {
    return new PrimitiveAND(
        name,
        Primitive.Kind.OP,
        null,
        args,
        attrs,
        canThrowException(attrs)
        );
  }

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
        new CompatibilityChecker(this, where, name, mode, orModes.get(0)).check();
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

      orOps.add(getIR().getOps().get(orName));
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
      final List<InstanceArgument> args) throws SemanticException {
    final ISymbol symbol = getSymbols().resolve(name);
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

    final PrimitiveAND primitiveAND = (PrimitiveAND) primitive;
    final Instance result = new Instance(primitiveAND, args);

    final String[] argNames = 
        primitiveAND.getArguments().keySet().toArray(new String[primitiveAND.getArguments().size()]);

    int index = 0;
    for (final InstanceArgument ie : args) {
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
}

final class CompatibilityChecker extends WalkerFactoryBase {
  private static final String COMMON_ERROR =
      "The %s primitive cannot be a part of the %s OR-rule.";

  private static final String TYPE_MISMATCH_ERROR =
      COMMON_ERROR + " Reason: return type mismatch.";

  private static final String SIZE_MISMATCH_ERROR =
      COMMON_ERROR + " Reason: return type size mismatch.";

  private static final String ATTRIBUTE_MISMATCH_ERROR =
      COMMON_ERROR + " Reason: sets of attributes do not match (expected: %s, current: %s).";

  private final Where where;
  private final String name;
  private final Primitive current;
  private final Primitive expected;

  public CompatibilityChecker(
      final WalkerContext context,
      final Where where,
      final String name,
      final Primitive current,
      final Primitive expected) {
    super(context);

    this.where = where;
    this.name = name;
    this.current = current;
    this.expected = expected;
  }

  public void check() throws SemanticException {
    checkReturnTypes();
    checkAttributes();
  }

  private void checkReturnTypes() throws SemanticException {
    final Type currentType = current.getReturnType();
    final Type expectedType = expected.getReturnType();

    if (currentType == expectedType) {
      return;
    }

    checkType(currentType, expectedType);
    checkSize(currentType, expectedType);
  }

  private void checkType(final Type currentType, final Type expectedType) throws SemanticException {
    if ((null != expectedType) && (null != currentType)) {
      if (expectedType.getTypeId() == currentType.getTypeId()) {
        return;
      }

      if (isInteger(currentType.getTypeId()) && isInteger(expectedType.getTypeId())) {
        return;
      }
    }

    raiseError(where, String.format(TYPE_MISMATCH_ERROR, current.getName(), name));
  }

  private void checkSize(final Type currentType, final Type expectedType) throws SemanticException {
    if ((null != expectedType) && (null != currentType)) {
      if (currentType.getBitSize() == expectedType.getBitSize()) {
        return;
      }
    }

    raiseError(where, String.format(SIZE_MISMATCH_ERROR, current.getName(), name));
  }

  private boolean isInteger(final TypeId typeID) {
    return (typeID == TypeId.CARD) || (typeID == TypeId.INT);
  }

  private void checkAttributes() throws SemanticException {
    final Set<String> expectedAttrs = expected.getAttrNames();
    final Set<String> currentAttrs = current.getAttrNames();

    if (expectedAttrs == currentAttrs) {
      return;
    }

    if ((null != expectedAttrs) && (null != currentAttrs)) {
      if (expectedAttrs.equals(currentAttrs)) {
        return;
      }
    }

    raiseError(where, String.format(
        ATTRIBUTE_MISMATCH_ERROR, current.getName(), name, expectedAttrs, currentAttrs));
  }
}
