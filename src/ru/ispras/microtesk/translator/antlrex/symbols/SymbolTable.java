/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * SymbolTable.java, Dec 10, 2012 6:47:46 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.antlrex.symbols;

public final class SymbolTable<Kind extends Enum<Kind>> implements IScope<Kind>
{
    private final IScope<Kind> globalScope = new Scope<Kind>(null);
    private IScope<Kind> scope;

    public SymbolTable()
    {
        this.scope = globalScope;
    }

    public void defineReserved(Kind kind, String[] names)
    {
        for (String s : names)
        {
            globalScope.define(
                new BuiltInSymbol<Kind>(s, kind, globalScope)
            );
        }
    }

    public void push()
    {
        this.scope = new Scope<Kind>(scope);
    }

    public void push(IScope<Kind> scope)
    {
        assert null != scope;
        assert globalScope != scope;

        this.scope = scope;
    }

    public void pop()
    {
        assert null != scope;
        assert globalScope != scope;

        if (null != scope && globalScope != scope)
            scope = scope.getOuterScope();
    }

    public IScope<Kind> peek()
    {
        return scope;
    }

    @Override
    public void define(ISymbol<Kind> symbol)
    {
        peek().define(symbol);
    }

    @Override
    public ISymbol<Kind> resolve(String name)
    {
        return peek().resolve(name);
    }

    @Override
    public ISymbol<Kind> resolveMember(String name)
    {
        return peek().resolveMember(name);
    }

    @Override
    public ISymbol<Kind> resolveNested(String ... names)
    {
        return peek().resolveNested(names);
    }

    @Override
    public IScope<Kind> getOuterScope()
    {
        return peek().getOuterScope();
    }
    
    @Override
    public ISymbol<Kind> getAssociatedSymbol()
    {
        return peek().getAssociatedSymbol();
    }
}
