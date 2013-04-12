/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ScopedSymbol.java, Dec 10, 2012 6:37:38 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.antlrex.symbols;

import org.antlr.runtime.Token;

public final class ScopedSymbol <Kind extends Enum<Kind>> extends Symbol<Kind>
{
    private final IScope<Kind> innerScope;

    public ScopedSymbol(Token token, Kind kind, IScope<Kind> scope)
    {
        super(token, kind, scope);
        this.innerScope = new Scope<Kind>(scope, this);
    }

    @Override
    public final IScope<Kind> getInnerScope()
    {
        return innerScope;
    }
}
