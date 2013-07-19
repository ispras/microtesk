/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * TreeWalkerBase.java, Oct 22, 2012 2:15:42 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.antlrex;

import java.util.EnumMap;
import java.util.Map;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.TreeNodeStream;

import ru.ispras.microtesk.translator.antlrex.TreeParserEx;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.antlrex.symbols.ISymbol;
import ru.ispras.microtesk.translator.antlrex.symbols.ScopedSymbol;
import ru.ispras.microtesk.translator.antlrex.symbols.Symbol;
import ru.ispras.microtesk.translator.antlrex.symbols.SymbolTable;

import ru.ispras.microtesk.translator.simnml.ESymbolKind;
import ru.ispras.microtesk.translator.antlrex.errors.RedeclaredSymbol;
import ru.ispras.microtesk.translator.antlrex.errors.UnrecognizedStructure;

import ru.ispras.microtesk.translator.simnml.ir.IR;
import ru.ispras.microtesk.translator.simnml.ir.expression.EExprKind;
import ru.ispras.microtesk.translator.simnml.ir.expression.ExprFactory;
import ru.ispras.microtesk.translator.simnml.ir.expression.ExprFactoryClass;
import ru.ispras.microtesk.translator.simnml.ir.expression.LocationExprFactory;
import ru.ispras.microtesk.translator.simnml.ir.expression.LocationExprFactoryClass;
import ru.ispras.microtesk.translator.simnml.ir.primitive.AttributeFactory;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveFactory;
import ru.ispras.microtesk.translator.simnml.ir.primitive.StatementFactory;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetFactory;
import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExprFactory;
import ru.ispras.microtesk.translator.simnml.ir.shared.MemoryExprFactory;

public class TreeWalkerBase extends TreeParserEx
{
    private SymbolTable<ESymbolKind> symbols = null;
    private IR ir = null;

    public TreeWalkerBase(TreeNodeStream input, RecognizerSharedState state)
    {
        super(input, state);
    }

    public final void assignSymbols(SymbolTable<ESymbolKind> symbols)
    {
        this.symbols = symbols;
    }

    protected final SymbolTable<ESymbolKind> getSymbols()
    {
        return symbols;
    }

    public final void assignIR(IR ir)
    {
        this.ir = ir;
    }

    protected final IR getIR()
    {
        return ir;
    }

    /*======================================================================================*/
    /* Factories of Semantic Elements that Make Up Intermediate Data to Be Used by          */
    /* code generators (emitters).                                                          */
    /*======================================================================================*/

    private Map<EExprKind, ExprFactory> exprFactories =
        new EnumMap<EExprKind, ExprFactory>(EExprKind.class);

    private LetFactory          letFactory          = null; 
    private LocationExprFactory locationExprFactory = null;
    private TypeExprFactory     typeExprFactory     = null;
    private MemoryExprFactory   memoryExprFactory   = null;
    private PrimitiveFactory    primitiveFactory    = null;
    private AttributeFactory    attributeFactory    = null;
    private StatementFactory    statementFactory    = null;

    protected final ExprFactory getExprFactory(EExprKind targetKind)
    {
        if (exprFactories.containsKey(targetKind))
            return exprFactories.get(targetKind);

        final ExprFactory factory = ExprFactoryClass.createFactory(targetKind, this, ir);
        exprFactories.put(targetKind, factory);

        return factory;
    }
    
    protected final LetFactory getLetFactory()
    {
        if (null == letFactory)
            letFactory = new LetFactory(symbols);
        return letFactory;
    }

    protected final LocationExprFactory getLocationExprFactory()
    {
        if (null == locationExprFactory)
            locationExprFactory = LocationExprFactoryClass.createFactory(this, symbols, ir); 
        return locationExprFactory;
    }

    protected final TypeExprFactory getTypeExprFactory()
    {
        if (null == typeExprFactory)
            typeExprFactory = new TypeExprFactory(ir.getTypes(), this);
        return typeExprFactory;
    }

    protected final MemoryExprFactory getMemoryExprFactory()
    {
        if (null == memoryExprFactory)
            memoryExprFactory = new MemoryExprFactory(this);
        return memoryExprFactory;
    }

    protected final PrimitiveFactory getPrimitiveFactory()
    {
        if (null == primitiveFactory)
            primitiveFactory = new PrimitiveFactory(ir, this);
        return primitiveFactory;
    }

    protected final AttributeFactory getAttributeFactory()
    {
        if (null == attributeFactory)
            attributeFactory = new AttributeFactory(this, symbols, ir);
        return attributeFactory;
    }

    protected final StatementFactory getStatementFactory()
    {
        if (null == statementFactory)
            statementFactory = new StatementFactory();
        return statementFactory;
    }

    /*======================================================================================*/

    protected final void checkRedeclared(CommonTree current) throws RecognitionException
    {
        final ISymbol<ESymbolKind> symbol = 
            symbols.resolve(current.getText());

        if (null != symbol)
            raiseError(where(current), new RedeclaredSymbol(symbol));		
    }

    protected final boolean isDeclaredAs(CommonTree t, ESymbolKind expectedKind)
    {
        assert null != symbols;

        final ISymbol<ESymbolKind> symbol =
            symbols.resolve(t.getText());

        if (null == symbol)
            return false;

        if (expectedKind != symbol.getKind())
            return false;

        return true;
    }

    protected final void declare(CommonTree t, ESymbolKind kind, boolean scoped) throws RecognitionException
    {
        assert null != symbols;

        checkRedeclared(t);

        final ISymbol<ESymbolKind> symbol = scoped ?
            new ScopedSymbol<ESymbolKind>(t.getToken(), kind, symbols.peek()) :
            new Symbol<ESymbolKind>(t.getToken(), kind, symbols.peek());

        symbols.define(symbol);
    }

    protected void checkNotNull(CommonTree current, Object obj, String text) throws RecognitionException
    {
        if (null == obj)
            raiseError(where(current), new UnrecognizedStructure(text));		
    }

    protected void checkNotNull(Where w, Object obj, String text) throws RecognitionException
    {
        if (null == obj)
            raiseError(w, new UnrecognizedStructure(text));        
    }

    protected void pushSymbolScope(CommonTree scopeID)
    {
        assert null != symbols;

        final ISymbol<ESymbolKind> scopeSymbol =
            symbols.resolve(scopeID.getText());

        assert (null != scopeSymbol) :
            String.format("The %s symbol must be registered in the symbol table.", scopeID.getText());

        assert (null != scopeSymbol.getInnerScope()) :
            String.format("The %s symbol must be a scoped symbol.", scopeID.getText());

        symbols.push(scopeSymbol.getInnerScope());
    }

    protected void popSymbolScope()
    {
        symbols.pop();
    }
}
