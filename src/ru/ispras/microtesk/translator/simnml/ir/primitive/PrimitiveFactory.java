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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.RecognitionException;

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
import ru.ispras.microtesk.translator.simnml.ir.modeop.Argument;
import ru.ispras.microtesk.translator.simnml.ir.modeop.ArgumentTypeExpr;
import ru.ispras.microtesk.translator.simnml.ir.modeop.Attribute;
import ru.ispras.microtesk.translator.simnml.ir.modeop.EArgumentKind;
import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;

public final class PrimitiveFactory
{
    public final Map<String, Primitive> modes;
    public final Map<String, Primitive>   ops;
    public final IErrorReporter      reporter;

    public PrimitiveFactory(IR ir, IErrorReporter reporter)
    {
        this.modes    = ir.getModes();
        this.ops      = ir.getOps();
        this.reporter = reporter;
    }

    public Primitive createMode(
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

            modeArgs.put(
                e.getKey(),
                new Argument(e.getKey(), e.getValue().getTypeExpr())
            );
        }

        return Primitive.createMode(name, retExpr, modeArgs, attrs);
    }

    public Primitive createOp(
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

                    final Primitive argTypeMode = modes.get(argType.getTypeName());
                    opArgs.put(argName, new Argument(argName, argTypeMode));
                }
                else if (EArgumentKind.OP == argType.getKind())
                {
                    if (!ops.containsKey(argType.getTypeName()))
                        reporter.raiseError(
                            where,
                            new UndefinedPrimitive(argType.getTypeName(), ESymbolKind.OP)
                        );

                    final Primitive argTypeOp = ops.get(argType.getTypeName());
                    opArgs.put(argName, new Argument(argName, argTypeOp));
                }
                else // -> if (EArgumentKind.TYPE == argType.getKind())
                {
                    opArgs.put(argName, new Argument(argName, argType.getTypeExpr()));
                }
            }

        return Primitive.createOp(name, opArgs, attrs);
    }

    public Primitive createModeOR(
        Where where,
        String name,
        List<String> orNames) throws RecognitionException
    {
        final List<Primitive> orModes = new ArrayList<Primitive>();

        for (String orName : orNames)
        {
            if (!modes.containsKey(orName))
                reporter.raiseError(
                    where,
                    new UndefinedProductionRuleItem(orName, name, true, ESymbolKind.MODE)
                );

            final Primitive mode = modes.get(orName);

            if (!orModes.isEmpty())
                new CompatibilityChecker(where, name, mode, orModes.get(0)).check();

            orModes.add(mode);
        }

        return Primitive.createModeOR(name, orModes);
    }
    
    public Primitive createOpOR(
        Where where,
        String name,
        List<String> orNames) throws RecognitionException
    {
        final List<Primitive> orOps = new ArrayList<Primitive>();

        for (String orName : orNames)
        {
            if (!ops.containsKey(orName))
                reporter.raiseError(
                    where,
                    new UndefinedProductionRuleItem(orName, name, true, ESymbolKind.OP)
                );

            orOps.add(ops.get(orName));
        }

        return Primitive.createOpOR(name, orOps);
    }
    
    private final class CompatibilityChecker
    {
        private static final String TYPE_MISMATCH_ERROR =
            "The %s mode cannot be a part of the %s mode OR-rule. Reason: return type mismatch.";

        private static final String SIZE_MISMATCH_ERROR =
            "The %s mode cannot be a part of the %s mode OR-rule. Reason: return type size mismatch.";

        private final Where where;
        private final String name;
        private final Primitive current;
        private final Primitive expected;

        public CompatibilityChecker(
            Where where,
            String name,
            Primitive current,
            Primitive expected
            )
        {
            this.where    = where;
            this.name     = name;
            this.current  = current;
            this.expected = expected;
        }
        
        public void check() throws RecognitionException
        {
            final TypeExpr currentType = current.getReturnType();
            final TypeExpr expectedType = expected.getReturnType();

            checkType(currentType, expectedType);
            checkSize(currentType, expectedType);
        }

        private void checkType(final TypeExpr currentType, final TypeExpr expectedType) throws RecognitionException
        {
            if (expectedType == currentType)
                return;

            if (expectedType.getTypeId() == currentType.getTypeId())
                return;

            if (isInteger(currentType.getTypeId()) && isInteger(expectedType.getTypeId()))
                return;

            raiseError(
                where,
                String.format(TYPE_MISMATCH_ERROR, current.getName(), name)
                );
        }

        private void checkSize(final TypeExpr currentType, final TypeExpr expectedType) throws RecognitionException
        {
            if (currentType.getBitSize().getValue().equals(expectedType.getBitSize().getValue()))
                return;

            raiseError(
                where,
                String.format(SIZE_MISMATCH_ERROR, current.getName(), name)
                );
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
}
