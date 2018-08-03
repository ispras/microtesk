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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.errors.SymbolTypeMismatch;
import ru.ispras.microtesk.translator.antlrex.errors.UndeclaredSymbol;
import ru.ispras.microtesk.translator.antlrex.symbols.Symbol;
import ru.ispras.microtesk.translator.antlrex.symbols.Where;
import ru.ispras.microtesk.translator.nml.NmlSymbolKind;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.nml.errors.UndefinedPrimitive;
import ru.ispras.microtesk.translator.nml.errors.UndefinedProductionRuleItem;
import ru.ispras.microtesk.translator.nml.errors.UnsupportedParameterType;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.TypeCast;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class PrimitiveFactory extends WalkerFactoryBase {
  public PrimitiveFactory(final WalkerContext context) {
    super(context);
  }

  public Primitive createMode(
      final Where where,
      final String name,
      final boolean isLabel,
      final Map<String, Primitive> args,
      final Map<String, Attribute> attrs,
      final Expr retExpr) throws SemanticException {
    if (isLabel && args.size() != 1 && args.size() != 2) {
      raiseError(where, String.format(
          "Label-based addressing mode %s must have 1 or 2 arguments.", name));
    }

    for (final Map.Entry<String, Primitive> e : args.entrySet()) {
      if (Primitive.Kind.IMM != e.getValue().getKind()) {
        raiseError(where, new UnsupportedParameterType(
            e.getKey(), e.getValue().getKind().name(), Primitive.Kind.IMM.name()));
      }
    }

    if (null != retExpr && null == retExpr.getNodeInfo().getType()) {
      raiseError(where, "Return value is untyped. Use casts to enforce a certain type.");
    }

    insertInitCalls(attrs);
    return new PrimitiveAND(
        name,
        Primitive.Kind.MODE,
        isLabel ? Primitive.Modifier.LABEL : Primitive.Modifier.NORMAL,
        retExpr,
        args,
        attrs
        );
  }

  public Primitive createOp(
      final Where where,
      final String name,
      final String modifierName,
      final Map<String, Primitive> args,
      final Map<String, Attribute> attrs) throws SemanticException {

    final Primitive.Modifier modifier = null == modifierName
        ? Primitive.Modifier.NORMAL
        : Primitive.Modifier.valueOf(modifierName.toUpperCase());

    for (final Map.Entry<String, Primitive> entry : args.entrySet()) {
      final Primitive.Modifier argModifier = entry.getValue().getModifier();
      if (modifier != Primitive.Modifier.INTERNAL && argModifier == Primitive.Modifier.INTERNAL) {
        final String argName = entry.getKey();
        raiseError(where, String.format(
            "Argument %s of %s is not allowed to be internal.", argName, name));
      }

      if (modifier != Primitive.Modifier.PSEUDO
          && (argModifier == Primitive.Modifier.PSEUDO
          || argModifier == Primitive.Modifier.LABEL)) {
        final String argName = entry.getKey();
        raiseError(where, String.format(
            "Argument %s of %s is not allowed to be a %s. "
                + "Although it can be an OR-rule containing a %3$s.",
            argName, name, argModifier.name().toLowerCase()));
      }
    }

    insertInitCalls(attrs);
    return new PrimitiveAND(
        name,
        Primitive.Kind.OP,
        modifier,
        null,
        args,
        attrs
        );
  }

  private static void insertInitCalls(final Map<String, Attribute> attrs) {
    if (attrs.containsKey(Attribute.INIT_NAME)) {
      final Statement initCall = StatementAttributeCall.newThisCall(Attribute.INIT_NAME);
      for (final Attribute attribute : attrs.values()) {
        if (!attribute.getName().equals(Attribute.INIT_NAME)) {
          attribute.insertStatement(initCall);
        }
      }
    }
  }

  public Primitive createModeOR(
      final Where where,
      final String name,
      final List<String> orNames)throws SemanticException {
    final List<Primitive> orModes = new ArrayList<>();

    for (final String orName : orNames) {
      if (!getIr().getModes().containsKey(orName)) {
        raiseError(where, new UndefinedProductionRuleItem(orName, name, true, NmlSymbolKind.MODE));
      }

      final Primitive mode = getIr().getModes().get(orName);

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
      if (!getIr().getOps().containsKey(orName)) {
        raiseError(where, new UndefinedProductionRuleItem(orName, name, true, NmlSymbolKind.OP));
      }

      final Primitive op = getIr().getOps().get(orName);
      if (!orOps.isEmpty()) {
        new PrimitiveCompatibilityChecker(
            this, where, name, op, orOps.get(0)).check();
      }

      orOps.add(op);
    }

    return new PrimitiveOR(name, Primitive.Kind.OP, orOps);
  }

  public Primitive createImm(final Type type) {
    return new Primitive(
        type.getAlias(),
        Primitive.Kind.IMM,
        Primitive.Modifier.NORMAL,
        false,
        type,
        null
        );
  }

  public Primitive getMode(
      final Where where,
      final String modeName) throws SemanticException {
    if (!getIr().getModes().containsKey(modeName)) {
      raiseError(where, new UndefinedPrimitive(modeName, NmlSymbolKind.MODE));
    }

    return getIr().getModes().get(modeName);
  }

  public Primitive getOp(
      final Where where,
      final String opName) throws SemanticException {
    if (!getIr().getOps().containsKey(opName)) {
      raiseError(where, new UndefinedPrimitive(opName, NmlSymbolKind.OP));
    }

    return getIr().getOps().get(opName);
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

    final Primitive primitive = symbol.getKind() == NmlSymbolKind.MODE
        ? getIr().getModes().get(name) : getIr().getOps().get(name);

    if (null == primitive) {
      raiseError(where, new UndefinedPrimitive(name, (NmlSymbolKind)symbol.getKind()));
    }

    if (primitive.isOrRule()) {
      raiseError(where, String.format("%s is not an AND rule!", name));
    }

    final PrimitiveAND primitiveAND = (PrimitiveAND) primitive;
    if (primitiveAND.getArguments().size() != arguments.size()) {
      raiseError(where, String.format(
          "Wrong number of arguments: %s is passed %d arguments while %d is expected.",
          name,
          arguments.size(),
          primitiveAND.getArguments().size()
          ));
    }

    final List<InstanceArgument> args = new ArrayList<>(arguments);
    final String[] argNames =
        primitiveAND.getArguments().keySet().toArray(
            new String[primitiveAND.getArguments().size()]);

    int index = 0;
    for (final InstanceArgument instanceArg : args) {
      final String argName = argNames[index];
      final Primitive arg = primitiveAND.getArguments().get(argName);

      if (!checkType(arg, instanceArg)) {
        raiseError(where, String.format(
            "The %s argument of %s has invalid type %s while %s is expected.",
            argName,
            name,
            instanceArg.getTypeName(),
            arg.getName())
        );
      }

      if (instanceArg.getKind() == InstanceArgument.Kind.EXPR
          && instanceArg.getExpr().isConstant()) {
        final Type argType = arg.getReturnType();
        final Expr newExpr = TypeCast.castConstantTo(instanceArg.getExpr(), argType);
        final InstanceArgument newIe = InstanceArgument.newExpr(newExpr);
        args.set(index, newIe);
      }

      index++;
    }

    return new Instance(primitiveAND, args);
  }

  private static boolean checkType(final Primitive argType, final InstanceArgument arg) {
    switch (arg.getKind()) {
      case EXPR:
        return checkType(argType, arg.getExpr());
      case PRIMITIVE:
        return checkType(argType, arg.getPrimitive());
      case INSTANCE:
        return checkType(argType, arg.getInstance().getPrimitive());
    }

    InvariantChecks.checkTrue(false);
    return false;
  }

  private static boolean checkType(final Primitive argType, final Primitive arg) {
    if (argType.getKind() == Primitive.Kind.IMM) {
      return argType.getReturnType().equals(arg.getReturnType());
    }

    if (argType.getKind() != arg.getKind()) {
      return false;
    }

    if (!argType.isOrRule()) {
      return argType.getName().equals(arg.getName());
    }

    final PrimitiveOR argTypeOr = (PrimitiveOR) argType;
    return argTypeOr.getNames().contains(arg.getName());
  }

  private static boolean checkType(final Primitive argType, final Expr arg) {
    return argType.getKind() == Primitive.Kind.IMM
        && (arg.isConstant() || arg.isTypeOf(argType.getReturnType()));
  }

  public Attribute createAction(final String name, final List<Statement> stmts) {
    return new Attribute(
        name,
        Attribute.Kind.ACTION,
        stmts
        );
  }

  public Attribute createExpression(final String name, final Statement stmt) {
    return new Attribute(
        name,
        Attribute.Kind.EXPRESSION,
        Collections.singletonList(stmt)
        );
  }
}
