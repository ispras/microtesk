/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ArgumentFactory.java, Jul 17, 2013 10:12:37 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.modeop;

import ru.ispras.microtesk.translator.antlrex.IErrorReporter;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.simnml.ESymbolKind;
import ru.ispras.microtesk.translator.simnml.errors.UndefinedPrimitive;
import ru.ispras.microtesk.translator.simnml.ir.IR;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;

public final class ArgumentFactory
{
    private final IR ir;
    private final IErrorReporter reporter;

    public ArgumentFactory(IR ir, IErrorReporter reporter)
    {
        this.ir = ir;
        this.reporter = reporter;
    }

    public Argument createType(String argName, TypeExpr argType)
    {
        return new Argument(argName, argType);
    }
    
    public Argument createOp(Where where, String argName, String opName) throws SemanticException
    {
        if (!ir.getOps().containsKey(opName))
        {
            reporter.raiseError(
                where,
                new UndefinedPrimitive(opName, ESymbolKind.OP)
            );
        }

        final Primitive op = ir.getOps().get(opName);
        return new Argument(argName, op);
    }
    
    public Argument createMode(Where where, String argName, String modeName) throws SemanticException
    {
        if (!ir.getModes().containsKey(modeName))
        {
            reporter.raiseError(
                where,
                new UndefinedPrimitive(modeName, ESymbolKind.MODE)
            );
        }

        final Primitive mode = ir.getModes().get(modeName);
        return new Argument(argName, mode);
    }
}
