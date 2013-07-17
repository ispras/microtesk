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

import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.translator.antlrex.IErrorReporter;
import ru.ispras.microtesk.translator.antlrex.ISemanticError;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.simnml.ESymbolKind;
import ru.ispras.microtesk.translator.simnml.errors.UndefinedPrimitive;
import ru.ispras.microtesk.translator.simnml.errors.UndefinedProductionRuleItem;
import ru.ispras.microtesk.translator.simnml.errors.UnsupportedParameterType;
import ru.ispras.microtesk.translator.simnml.ir.IR;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;

public final class PrimitiveFactory
{
    private final IR ir;
    private final IErrorReporter reporter;

    public PrimitiveFactory(IR ir, IErrorReporter reporter)
    {
        this.ir = ir;
        this.reporter = reporter;
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
                reporter.raiseError(
                    where, new UnsupportedParameterType(e.getKey(), e.getValue().getKind().name(), Primitive.Kind.IMM.name()));
            }
        }

        return Primitive.createMode(name, retExpr, args, attrs);
    }

    public Primitive createOp(
        Where where,
        String name,
        Map<String, Primitive> args,
        Map<String, Attribute> attrs) throws SemanticException
    {
        return Primitive.createOp(name, args, attrs);
    }

    public Primitive createModeOR(
        Where where,
        String name,
        List<String> orNames) throws SemanticException
    {
        final List<Primitive> orModes = new ArrayList<Primitive>();

        for (String orName : orNames)
        {
            if (!ir.getModes().containsKey(orName))
            {
                reporter.raiseError(
                    where, new UndefinedProductionRuleItem(orName, name, true, ESymbolKind.MODE));
            }

            final Primitive mode = ir.getModes().get(orName);

            if (!orModes.isEmpty())
                new CompatibilityChecker(reporter, where, name, mode, orModes.get(0)).check();

            orModes.add(mode);
        }

        return Primitive.createModeOR(name, orModes);
    }
    
    public Primitive createOpOR(
        Where where,
        String name,
        List<String> orNames) throws SemanticException
    {
        final List<Primitive> orOps = new ArrayList<Primitive>();

        for (String orName : orNames)
        {
            if (!ir.getOps().containsKey(orName))
            {
                reporter.raiseError(
                    where, new UndefinedProductionRuleItem(orName, name, true, ESymbolKind.OP));
            }

            orOps.add(ir.getOps().get(orName));
        }

        return Primitive.createOpOR(name, orOps);
    }

    public Primitive createImm(TypeExpr type)
    {
        return Primitive.createImm(type);
    }

    public Primitive getMode(Where where, String modeName) throws SemanticException
    {
        if (!ir.getModes().containsKey(modeName))
        {
            reporter.raiseError(
                where, new UndefinedPrimitive(modeName, ESymbolKind.MODE));
        }

        return ir.getModes().get(modeName);
    }

    public Primitive getOp(Where where, String opName) throws SemanticException
    {
        if (!ir.getOps().containsKey(opName))
        {
            reporter.raiseError(
                where, new UndefinedPrimitive(opName, ESymbolKind.OP));
        }

        return ir.getOps().get(opName);
    }
}

final class CompatibilityChecker
{
    private static final String TYPE_MISMATCH_ERROR =
        "The %s mode cannot be a part of the %s mode OR-rule. Reason: return type mismatch.";

    private static final String SIZE_MISMATCH_ERROR =
        "The %s mode cannot be a part of the %s mode OR-rule. Reason: return type size mismatch.";

    private final IErrorReporter reporter;
    private final Where where;
    private final String name;
    private final Primitive current;
    private final Primitive expected;

    public CompatibilityChecker(
        IErrorReporter reporter,
        Where where,
        String name,
        Primitive current,
        Primitive expected
        )
    {
        this.reporter = reporter;
        this.where    = where;
        this.name     = name;
        this.current  = current;
        this.expected = expected;
    }
    
    public void check() throws SemanticException
    {
        final TypeExpr currentType = current.getReturnType();
        final TypeExpr expectedType = expected.getReturnType();
        
        if (currentType == expectedType)
            return;

        checkType(currentType, expectedType);
        checkSize(currentType, expectedType);
    }

    private void checkType(final TypeExpr currentType, final TypeExpr expectedType) throws SemanticException
    {
        if (expectedType == currentType)
            return;

        if (expectedType.getTypeId() == currentType.getTypeId())
            return;

        if (isInteger(currentType.getTypeId()) && isInteger(expectedType.getTypeId()))
            return;

        raiseError(
            where, String.format(TYPE_MISMATCH_ERROR, current.getName(), name));
    }

    private void checkSize(final TypeExpr currentType, final TypeExpr expectedType) throws SemanticException
    {
        if (currentType.getBitSize().getValue().equals(expectedType.getBitSize().getValue()))
            return;

        raiseError(
            where, String.format(SIZE_MISMATCH_ERROR, current.getName(), name));
    }

    private boolean isInteger(final ETypeID typeID)
    {
        return (typeID == ETypeID.CARD) || (typeID == ETypeID.INT);
    }

    private void raiseError(final Where where, final String what) throws SemanticException
    {
        reporter.raiseError(where, new ISemanticError()
        {
            @Override
            public String getMessage() { return what; }
        });
    }
}
