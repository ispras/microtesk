/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * PrimitiveFactory.java, Jul 9, 2013 3:53:11 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.simnml.ESymbolKind;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.errors.UndefinedPrimitive;
import ru.ispras.microtesk.translator.simnml.errors.UndefinedProductionRuleItem;
import ru.ispras.microtesk.translator.simnml.errors.UnsupportedParameterType;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public final class PrimitiveFactory extends WalkerFactoryBase
{
    public PrimitiveFactory(WalkerContext context)
    {
        super(context);
    }

    public Primitive createMode(
        Where where,
        String name,
        Map<String, Primitive> args,
        Map<String, Attribute> attrs,
        Expr retExpr) throws SemanticException
    {
        for (Map.Entry<String, Primitive> e : args.entrySet())
        {
            if (Primitive.Kind.IMM != e.getValue().getKind())
            {
                raiseError(
                    where, new UnsupportedParameterType(e.getKey(), e.getValue().getKind().name(), Primitive.Kind.IMM.name()));
            }
        }

        return new PrimitiveAND(name, Primitive.Kind.MODE, retExpr, args, attrs);
    }

    public Primitive createOp(
        Where where,
        String name,
        Map<String, Primitive> args,
        Map<String, Attribute> attrs) throws SemanticException
    {
        return new PrimitiveAND(name, Primitive.Kind.OP, null, args, attrs);
    }

    public Primitive createModeOR(
        Where where,
        String name,
        List<String> orNames) throws SemanticException
    {
        final List<Primitive> orModes = new ArrayList<Primitive>();

        for (String orName : orNames)
        {
            if (!getIR().getModes().containsKey(orName))
            {
                raiseError(
                    where, new UndefinedProductionRuleItem(orName, name, true, ESymbolKind.MODE));
            }

            final Primitive mode = getIR().getModes().get(orName);

            if (!orModes.isEmpty())
                new CompatibilityChecker(this, where, name, mode, orModes.get(0)).check();

            orModes.add(mode);
        }

        return new PrimitiveOR(name, Primitive.Kind.MODE, orModes);
    }

    public Primitive createOpOR(
        Where where,
        String name,
        List<String> orNames) throws SemanticException
    {
        final List<Primitive> orOps = new ArrayList<Primitive>();

        for (String orName : orNames)
        {
            if (!getIR().getOps().containsKey(orName))
                raiseError(where, new UndefinedProductionRuleItem(orName, name, true, ESymbolKind.OP));

            orOps.add(getIR().getOps().get(orName));
        }

        return new PrimitiveOR(name, Primitive.Kind.OP, orOps);
    }

    public Primitive createImm(Type type)
    {
        return new Primitive(type.getRefName(), Primitive.Kind.IMM, false, type, null);
    }

    public Primitive getMode(Where where, String modeName) throws SemanticException
    {
        if (!getIR().getModes().containsKey(modeName))
            raiseError(where, new UndefinedPrimitive(modeName, ESymbolKind.MODE));

        return getIR().getModes().get(modeName);
    }

    public Primitive getOp(Where where, String opName) throws SemanticException
    {
        if (!getIR().getOps().containsKey(opName))
            raiseError(where, new UndefinedPrimitive(opName, ESymbolKind.OP));

        return getIR().getOps().get(opName);
    }
}

final class CompatibilityChecker extends WalkerFactoryBase
{
    private static final String COMMON_ERROR =
        "The %s primitive cannot be a part of the %s OR-rule.";

    private static final String TYPE_MISMATCH_ERROR =
         COMMON_ERROR + " Reason: return type mismatch.";

    private static final String SIZE_MISMATCH_ERROR =
         COMMON_ERROR + " Reason: return type size mismatch.";

    private static final String ATTRIBUTE_MISMATCH_ERROR =
         COMMON_ERROR + " Reason: sets of attributes do not match (expected: %s, current: %s)."; 

    private final Where        where;
    private final String        name;
    private final Primitive  current;
    private final Primitive expected;

    public CompatibilityChecker(
        WalkerContext context,
        Where where,
        String name,
        Primitive current,
        Primitive expected
        )
    {
        super(context);

        this.where    = where;
        this.name     = name;
        this.current  = current;
        this.expected = expected;
    }

    public void check() throws SemanticException
    {
        checkReturnTypes();
        checkAttributes();
    }

    private void checkReturnTypes() throws SemanticException
    {
        final Type  currentType = current.getReturnType();
        final Type expectedType = expected.getReturnType();

        if (currentType == expectedType)
            return;

        checkType(currentType, expectedType);
        checkSize(currentType, expectedType);
    }

    private void checkType(final Type currentType, final Type expectedType) throws SemanticException
    {
        if ((null != expectedType) && (null != currentType))
        {
            if (expectedType.getTypeId() == currentType.getTypeId())
                return;

            if (isInteger(currentType.getTypeId()) && isInteger(expectedType.getTypeId()))
                return;
        }

        raiseError(
            where, String.format(TYPE_MISMATCH_ERROR, current.getName(), name));
    }

    private void checkSize(final Type currentType, final Type expectedType) throws SemanticException
    {
        if ((null != expectedType) && (null != currentType))
        {
            if (currentType.getBitSize().getValue().equals(expectedType.getBitSize().getValue()))
                return;
        }

        raiseError(
            where, String.format(SIZE_MISMATCH_ERROR, current.getName(), name));
    }

    private boolean isInteger(final ETypeID typeID)
    {
        return (typeID == ETypeID.CARD) || (typeID == ETypeID.INT);
    }

    private void checkAttributes() throws SemanticException
    {
        final Set<String> expectedAttrs = expected.getAttrNames();
        final Set<String>  currentAttrs = current.getAttrNames();

        if (expectedAttrs == currentAttrs)
            return;

        if ((null != expectedAttrs) && (null != currentAttrs))
        {
            if (expectedAttrs.equals(currentAttrs))
                return;
        }

        raiseError(
            where, String.format(ATTRIBUTE_MISMATCH_ERROR, current.getName(), name, expectedAttrs, currentAttrs));
    }
}
