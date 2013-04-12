/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ParserBase.java
 */

package ru.ispras.microtesk.translator.mmu.parser;

import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

import ru.ispras.microtesk.translator.antlrex.ParserEx;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.symbols.ISymbol;
import ru.ispras.microtesk.translator.antlrex.symbols.ScopedSymbol;
import ru.ispras.microtesk.translator.antlrex.symbols.Symbol;
import ru.ispras.microtesk.translator.antlrex.symbols.SymbolTable;
import ru.ispras.microtesk.translator.mmu.errors.SymbolTypeMismatch;
import ru.ispras.microtesk.translator.mmu.errors.UndeclaredSymbol;
import ru.ispras.microtesk.translator.mmu.translator.ESymbolKind;
import ru.ispras.microtesk.translator.simnml.errors.RedeclaredSymbol;

public class ParserBase extends ParserEx
{   
    private SymbolTable<ESymbolKind> symbols = null;

    public ParserBase(TokenStream input, RecognizerSharedState state)
    {
        super(input, state);      
    }

    public final void assignSymbols(SymbolTable<ESymbolKind> symbols)
    {
        this.symbols = symbols;
    }

    protected final void declare(Token t, ESymbolKind kind, boolean scoped) throws SemanticException
    {
        assert null != symbols;
        
        checkRedeclared(t);

        final ISymbol<ESymbolKind> symbol = scoped ?
            new ScopedSymbol<ESymbolKind>(t, kind, symbols.peek()) :
            new Symbol<ESymbolKind>(t, kind, symbols.peek());

        symbols.define(symbol);
    }

    private final void checkRedeclared(final Token t) throws SemanticException
    {
        assert null != symbols;
        
        final ISymbol<?> symbol = symbols.resolve(t.getText());

        if (null == symbol) // OK
            return;

        raiseError(where(t), new RedeclaredSymbol(symbol));  
    }

    protected final void checkDeclaration(Token t, ESymbolKind expectedKind) throws SemanticException
    {
        assert null != symbols;
        
        final ISymbol<?> symbol = symbols.resolve(t.getText());
        
        if (null == symbol)
            raiseError(new UndeclaredSymbol(t.getText()));
        
        if (expectedKind != symbol.getKind())
            raiseError(new SymbolTypeMismatch(t.getText(), (ESymbolKind) symbol.getKind(), expectedKind));
    }

    protected final boolean isDeclaredAs(Token t, ESymbolKind expectedKind)
    {
        assert null != symbols;

        final ISymbol<?> symbol = symbols.resolve(t.getText());

        if (null == symbol)
            return false;

        if (expectedKind != symbol.getKind())
            return false;

        return true;
    }
}