/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * LetFactory.java, Apr 11, 2013 4:46:47 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.let;

import ru.ispras.microtesk.translator.antlrex.IErrorReporter;
import ru.ispras.microtesk.translator.antlrex.symbols.SymbolTable;
import ru.ispras.microtesk.translator.simnml.ESymbolKind;
import ru.ispras.microtesk.translator.simnml.ir.expression.ConstExpr;

public final class LetFactory
{
    private final IErrorReporter reporter;
    private final SymbolTable<ESymbolKind> symbols;

    public LetFactory(
        IErrorReporter reporter,
        SymbolTable<ESymbolKind> symbols
        ) 
    {
        this.reporter = reporter;
        this.symbols = symbols;
    }

    public LetExpr createConstString(String name, String text)
    {
        return new LetExpr(
             name,
             String.format("\"%s\"", text),
             String.class,
             text
             );
    }

    public LetExpr createConstValue(String name, ConstExpr value)
    {
        return new LetExpr(
            name,
            value.getText(),
            value.getJavaType(),
            value.getValue()
            );
    }

    public LetLabel createLabel(String name, String text)
    {
        System.out.println(String.format("createLabel %s : %s", name, text));
        return null;
    }
}
