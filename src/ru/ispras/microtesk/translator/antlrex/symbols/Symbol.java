/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Symbol.java, Dec 7, 2012 4:37:03 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.antlrex.symbols;

import org.antlr.runtime.Token;

public class Symbol <Kind extends Enum<Kind>> implements ISymbol<Kind>
{
    private final  Token token;
    private final   Kind kind;
    private final IScope<Kind> scope;    

    public Symbol(Token token, Kind kind, IScope<Kind> scope)
    {
        this.token = token;
        this.kind  = kind;
        this.scope = scope;
    }
    
    @Override
    public String toString()
    {
        return String.format(
            "Symbol [name=%s, kind=%s, scope=%s, innerScope=%s]",
            getName(),
            getKind(),
            getOuterScope(),
            getInnerScope()
            );
    }
    
    @Override
    public final String getName()
    {
        return token.getText();
    }

    @Override
    public final Kind getKind()
    {
        return kind;
    }

    @Override
    public final int getTokenIndex()
    {
        return token.getTokenIndex();
    }

    @Override
    public final int getLine()
    {
        return token.getLine();
    }

    @Override
    public final int getPositionInLine()
    {
        return token.getCharPositionInLine();
    }

    @Override
    public final IScope<Kind> getOuterScope()
    {
        return scope;
    }

    @Override
    public IScope<Kind> getInnerScope()
    {
        return null;
    }
}
