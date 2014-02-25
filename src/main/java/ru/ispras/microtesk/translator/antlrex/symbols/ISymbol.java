/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ISymbol.java, Dec 10, 2012 6:11:37 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.antlrex.symbols;

public interface ISymbol<Kind extends Enum<Kind>>
{
    public String getName();
    public Kind   getKind();

    public int getTokenIndex();
    public int getLine();
    public int getPositionInLine();

    public IScope<Kind> getOuterScope();
    public IScope<Kind> getInnerScope();
}
