/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * AttributeFactory.java, Feb 7, 2013 1:00:30 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import java.util.Collections;
import java.util.List;

import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;

public final class AttributeFactory extends WalkerFactoryBase
{
    public AttributeFactory(WalkerContext context)
    {
        super(context);
    }

    public Attribute createAction(String name, List<Statement> stmts)
    {
        return new Attribute(
             name,
             Attribute.Kind.ACTION,
             stmts
             );
    }

    public Attribute createExpression(String name, Statement stmt)
    {
        return new Attribute(
            name,
            Attribute.Kind.EXPRESSION,
            Collections.singletonList(stmt)
            );
    }
}
