/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IScope.java, Dec 10, 2012 6:12:59 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.antlrex.symbols;

public interface IScope<Kind extends Enum<Kind>>
{
    public void define(ISymbol<Kind> symbol);

    public ISymbol<Kind> resolve(String name);
    public ISymbol<Kind> resolveMember(String name);
    public ISymbol<Kind> resolveNested(String ... names);

    public IScope<Kind> getOuterScope();

    public ISymbol<Kind> getAssociatedSymbol();
}
