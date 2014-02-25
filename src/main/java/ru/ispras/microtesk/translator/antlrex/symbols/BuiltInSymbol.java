/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * BuiltInSymbol.java, Dec 10, 2012 6:36:17 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.antlrex.symbols;

public final class BuiltInSymbol <Kind extends Enum<Kind>> implements ISymbol<Kind> 
{
    private final String name;
    private final   Kind kind;
    private final IScope<Kind> scope;

    public BuiltInSymbol(String name, Kind kind, IScope<Kind> scope)
    {
        this.name  = name;
        this.kind  = kind;
        this.scope = scope;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Kind getKind()
    {
        return kind;
    }

    @Override
    public int getTokenIndex()
    {
        return -1;
    }

    @Override
    public int getLine()
    {
        return -1;
    }

    @Override
    public int getPositionInLine()
    {
        return -1;
    }

    @Override
    public IScope<Kind> getOuterScope()
    {
        return scope;
    }

    @Override
    public IScope<Kind> getInnerScope()
    {
        return null;
    }
}
