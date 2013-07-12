/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * LocationInfo.java, Apr 23, 2013 3:52:05 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

import ru.ispras.microtesk.translator.simnml.ESymbolKind;

public final class LocationInfo
{
    private final String name;
    private final ESymbolKind symbolKind;
    private final Expr index;

    public LocationInfo(String name, ESymbolKind symbolKind, Expr index)
    {
        this.name = name;
        this.symbolKind = symbolKind;
        this.index = index;
    }

    public String getName()
    {
        return name;
    }

    public ESymbolKind getSymbolKind()
    {
        return symbolKind;
    }

    public Expr getIndex()
    {
        return index;
    }
}
