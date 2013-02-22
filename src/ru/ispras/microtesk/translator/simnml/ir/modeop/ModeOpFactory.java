/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ModeFactory.java, Dec 25, 2012 12:53:38 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.modeop;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.translator.simnml.ESymbolKind;

import org.antlr.runtime.RecognitionException;
import ru.ispras.microtesk.translator.antlrex.IErrorReporter;
import ru.ispras.microtesk.translator.antlrex.ISemanticError;
import ru.ispras.microtesk.translator.antlrex.Where;

import ru.ispras.microtesk.translator.simnml.errors.UndefinedPrimitive;
import ru.ispras.microtesk.translator.simnml.errors.UndefinedProductionRuleItem;
import ru.ispras.microtesk.translator.simnml.errors.UnsupportedParameterType;
import ru.ispras.microtesk.translator.simnml.ir.expression2.Expr;
import ru.ispras.microtesk.translator.simnml.ir.type.TypeExpr;

public final class ModeOpFactory
{
    public final Map<String, Mode> modes;
    public final Map<String, Op> ops;

    public final IErrorReporter reporter;

    public ModeOpFactory(Map<String, Mode> modes, Map<String, Op> ops, IErrorReporter reporter)
    {
        this.modes    = modes;
        this.ops      = ops;
        this.reporter = reporter;
    }

    public Mode createMode(
        Where where,
        String name,
        Map<String, ArgumentTypeExpr> args,
        Map<String, Attribute> attrs,
        Expr retExpr) throws RecognitionException
    {
        final Map<String, Argument> modeArgs = new LinkedHashMap<String, Argument>();

        for (Map.Entry<String, ArgumentTypeExpr> e : args.entrySet())
        {
            if (EArgumentKind.TYPE != e.getValue().getKind())
                reporter.raiseError(
                    where,
                    new UnsupportedParameterType(
                        e.getKey(),
                        e.getValue().getKind().name(),
                        EArgumentKind.TYPE.name()
                    )
                );

            modeArgs.put(e.getKey(), new Argument(e.getKey(), e.getValue().getTypeExpr()));
        }

        return new Mode(name, modeArgs, attrs, retExpr);
    }

    public Mode createModeOr(
        Where where,
        String name,
        List<String> orNames) throws RecognitionException
    {
        final List<Mode> orModes = new ArrayList<Mode>();

        for (String orName : orNames)
        {
            if (!modes.containsKey(orName))
                reporter.raiseError(
                    where,
                    new UndefinedProductionRuleItem(
                        orName,
                        name,
                        true,
                        ESymbolKind.MODE
                        )
                    );

            final Mode mode = modes.get(orName);

            if (!orModes.isEmpty())
                checkTypeCompatibility(where, name, mode, orModes.get(0));

            orModes.add(mode);
        }

        return new Mode(name, orModes);
    }
    
    private void checkTypeCompatibility(
        Where where,
        final String name,
        final Mode current,
        final Mode expected) throws RecognitionException
    {
        final TypeExpr currentType = current.getReturnType();
        final TypeExpr expectedType = expected.getReturnType();
        
        if (expectedType == currentType)
            return;

        final boolean isTypeMismatch =
            currentType.getTypeId() != expectedType.getTypeId();

        if (isTypeMismatch)
        {
            final boolean isCurrentInteger =
                (currentType.getTypeId() == ETypeID.CARD) || (currentType.getTypeId() == ETypeID.INT);

            final boolean isExpectedInteger =
                (expectedType.getTypeId() == ETypeID.CARD) || (expectedType.getTypeId() == ETypeID.INT);

            final boolean compatibleTypes = isCurrentInteger && isExpectedInteger;
            if (!compatibleTypes)
            {
                reporter.raiseError(where, new ISemanticError()
                {
                    @Override
                    public String getMessage()
                    {
                        return String.format(
                            "The %s mode cannot be a part of the %s mode OR-rule. Reason: return type mismatch.",
                            current.getName(), name);
                    }
                });
            }
        }
        
        final boolean isEqualSize = currentType.getBitSize().getValue().equals(expectedType.getBitSize().getValue());
        if (!isEqualSize)
        {
            reporter.raiseError(where, new ISemanticError()
            {
                @Override
                public String getMessage()
                {
                    return String.format(
                        "The %s mode cannot be a part of the %s mode OR-rule. Reason: return type size mismatch.",
                        current.getName(), name);
                }
            });
        }
    }

    public Op createOp(
        Where where,
        String name,
        Map<String, ArgumentTypeExpr> args,
        Map<String, Attribute> attrs) throws RecognitionException
    {
        final Map<String, Argument> opArgs = new LinkedHashMap<String, Argument>();

        for (Map.Entry<String, ArgumentTypeExpr> e : args.entrySet())
        {
            final String argName = e.getKey();
            final ArgumentTypeExpr argType = e.getValue();
           
            if (EArgumentKind.MODE == argType.getKind())
            {
                if (!modes.containsKey(argType.getTypeName()))
                    reporter.raiseError(
                        where,
                        new UndefinedPrimitive(argType.getTypeName(), ESymbolKind.MODE)
                    );

                final Mode argTypeMode = modes.get(argType.getTypeName());
                opArgs.put(argName, new Argument(argName, argTypeMode));
            }
            else if (EArgumentKind.OP == argType.getKind())
            {
                if (!ops.containsKey(argType.getTypeName()))
                    reporter.raiseError(
                        where,
                        new UndefinedPrimitive(argType.getTypeName(), ESymbolKind.OP)
                    );

                final Op argTypeOp = ops.get(argType.getTypeName());
                opArgs.put(argName, new Argument(argName, argTypeOp));
            }
            else // -> if (EArgumentKind.TYPE == argType.getKind())
            {
                opArgs.put(argName, new Argument(argName, argType.getTypeExpr()));
            }
        }

        return new Op(name, opArgs, attrs);
    }

    public Op createOpOr(
        Where where,
        String name,
        List<String> orNames) throws RecognitionException
    {
        final List<Op> orOps = new ArrayList<Op>();

        for (String orName : orNames)
        {
            if (!ops.containsKey(orName))
                reporter.raiseError(
                    where,
                    new UndefinedProductionRuleItem(
                        orName,
                        name,
                        true,
                        ESymbolKind.OP
                        )
                    );

            orOps.add(ops.get(orName));
        }

        return new Op(name, orOps);
    }
}
