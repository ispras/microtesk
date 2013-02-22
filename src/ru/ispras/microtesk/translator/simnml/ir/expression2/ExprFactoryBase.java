/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprFactoryBase.java, Feb 1, 2013 11:36:09 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.microtesk.translator.antlrex.IErrorReporter;
import ru.ispras.microtesk.translator.simnml.ir.IR;

abstract class ExprFactoryBase
{
    private final IErrorReporter reporter;
    private final IR ir;

    public ExprFactoryBase(IErrorReporter reporter, IR ir)
    {
        assert null != reporter;
        this.reporter = reporter;

        assert null != ir;
        this.ir = ir;
    }

    public ExprFactoryBase(ExprFactoryBase context)
    {
        this.reporter = context.reporter;
        this.ir = context.ir;
    }

    protected final IErrorReporter getReporter()
    {
        return reporter;
    }

    protected final IR getIR()
    {
        return ir;
    }
}
