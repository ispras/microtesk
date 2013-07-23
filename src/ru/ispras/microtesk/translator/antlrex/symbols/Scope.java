/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Scope.java, Dec 10, 2012 6:39:01 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.antlrex.symbols;

import java.util.HashMap;
import java.util.Map;

public final class Scope<Kind extends Enum<Kind>> implements IScope<Kind>
{
    private final IScope<Kind>                  outerScope;
    private final Map<String, ISymbol<Kind>> memberSymbols;
    private final ISymbol<Kind>           associatedSymbol;

    public Scope(IScope<Kind> scope, ISymbol<Kind> associatedSymbol)
    {
        this.outerScope       = scope;
        this.memberSymbols    = new HashMap<String, ISymbol<Kind>>();
        this.associatedSymbol = associatedSymbol;
    }

    @Override
    public String toString()
    {
        return String.format(
            "Scope [symbol=%s, outerScope=%s, members=%d]",
            null != associatedSymbol ? associatedSymbol.getName() : "null",
            null != outerScope ? "YES": "NO",
            memberSymbols.size()
            );
    }

    public Scope(IScope<Kind> scope)
    {
        this(scope, null);
    }

    @Override
    public void define(ISymbol<Kind> symbol)
    {
        assert null != symbol;
        assert !memberSymbols.containsKey(symbol.getName());

        if (!memberSymbols.containsKey(symbol.getName()))
            memberSymbols.put(symbol.getName(), symbol);
    }

    @Override
    public ISymbol<Kind> resolve(String name)
    {
        if (memberSymbols.containsKey(name))
            return memberSymbols.get(name);

        if (null != outerScope)
            return outerScope.resolve(name);

        return null;
    }

    @Override
    public ISymbol<Kind> resolveMember(String name)
    {
        return memberSymbols.get(name);
    }

    @Override
    public ISymbol<Kind> resolveNested(String ... names)
    {
        assert names.length > 0;

        ISymbol<Kind> symbol = resolve(names[0]);
        for(int index = 1; index < names.length; ++index)
        {
            if (null == symbol)
                return null;

            final IScope<Kind> scope = symbol.getInnerScope();

            if (null == scope)
                return null;

            symbol = scope.resolveMember(names[index]);
        }

        return symbol;
    }

    @Override
    public IScope<Kind> getOuterScope()
    {
        return outerScope;
    }
    
    @Override
    public ISymbol<Kind> getAssociatedSymbol()
    {
        return associatedSymbol; 
    }
}
