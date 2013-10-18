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

import java.util.Map;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.TreeNodeStream;

import ru.ispras.microtesk.translator.antlrex.IErrorReporter;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.TreeParserEx;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.antlrex.symbols.ISymbol;
import ru.ispras.microtesk.translator.antlrex.symbols.ScopedSymbol;
import ru.ispras.microtesk.translator.antlrex.symbols.Symbol;
import ru.ispras.microtesk.translator.antlrex.symbols.SymbolTable;

import ru.ispras.microtesk.translator.simnml.ESymbolKind;
import ru.ispras.microtesk.translator.antlrex.errors.RedeclaredSymbol;
import ru.ispras.microtesk.translator.antlrex.errors.SymbolTypeMismatch;
import ru.ispras.microtesk.translator.antlrex.errors.UndeclaredSymbol;
import ru.ispras.microtesk.translator.antlrex.errors.UnrecognizedStructure;

import ru.ispras.microtesk.translator.simnml.ir.IR;
import ru.ispras.microtesk.translator.simnml.ir.expression.ExprFactory;
import ru.ispras.microtesk.translator.simnml.ir.expression.LocationFactory;
import ru.ispras.microtesk.translator.simnml.ir.primitive.AttributeFactory;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveFactory;
import ru.ispras.microtesk.translator.simnml.ir.primitive.StatementFactory;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetFactory;
import ru.ispras.microtesk.translator.simnml.ir.shared.TypeFactory;
import ru.ispras.microtesk.translator.simnml.ir.shared.MemoryExprFactory;

public class TreeWalkerBase extends TreeParserEx implements WalkerContext
{
    private SymbolTable<ESymbolKind> symbols;
    private IR ir;

    private Map<String, Primitive> thisArgs;
    private Primitive.Holder thisPrimitive;

    public TreeWalkerBase(TreeNodeStream input, RecognizerSharedState state)
    {
        super(input, state);

        this.symbols       = null;
        this.ir            = null;

        this.thisArgs      = null;
        this.thisPrimitive = null;
    }

    @Override
    public final IErrorReporter getReporter()
    {
        return this;
    }

    public final void assignSymbols(SymbolTable<ESymbolKind> symbols)
    {
        this.symbols = symbols;
    }

    @Override
    public final SymbolTable<ESymbolKind> getSymbols()
    {
        return symbols;
    }

    public final void assignIR(IR ir)
    {
        this.ir = ir;
    }

    @Override
    public final IR getIR()
    {
        return ir;
    }

    protected final void setThisArgs(Map<String, Primitive> value)
    {
        assert null != value;
        this.thisArgs = value;
    }

    protected final void resetThisArgs()
    {
        this.thisArgs = null;
    }

    @Override
    public final Map<String, Primitive> getThisArgs()
    {
        return thisArgs;
    }

    protected final void reserveThis()
    {
        assert null == thisPrimitive;
        thisPrimitive = new Primitive.Holder();
    }

    protected final void finalizeThis(Primitive value)
    {
        assert null != thisPrimitive;
        thisPrimitive.setValue(value);
        thisPrimitive = null;
    }

    @Override
    public final Primitive.Holder getThis()
    {
        return thisPrimitive;
    }

    /*======================================================================================*/
    /* Factories of Semantic Elements that Make Up Intermediate Data to Be Used by          */
    /* code generators (emitters).                                                          */
    /*======================================================================================*/

    private ExprFactory         exprFactory         = null;
    private LetFactory          letFactory          = null; 
    private LocationFactory     locationFactory     = null;
    private TypeFactory         typeFactory         = null;
    private MemoryExprFactory   memoryExprFactory   = null;
    private PrimitiveFactory    primitiveFactory    = null;
    private AttributeFactory    attributeFactory    = null;
    private StatementFactory    statementFactory    = null;

    protected final ExprFactory getExprFactory()
    {
        if (null == exprFactory)
            exprFactory = new ExprFactory(this);
        return exprFactory;
    }

    protected final LetFactory getLetFactory()
    {
        if (null == letFactory)
            letFactory = new LetFactory(this);
        return letFactory;
    }

    protected final LocationFactory getLocationFactory()
    {
        if (null == locationFactory)
            locationFactory = new LocationFactory(this); 
        return locationFactory;
    }

    protected final TypeFactory getTypeFactory()
    {
        if (null == typeFactory)
            typeFactory = new TypeFactory(this);
        return typeFactory;
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
            primitiveFactory = new PrimitiveFactory(this);
        return primitiveFactory;
    }

    protected final AttributeFactory getAttributeFactory()
    {
        if (null == attributeFactory)
            attributeFactory = new AttributeFactory(this);
        return attributeFactory;
    }

    protected final StatementFactory getStatementFactory()
    {
        if (null == statementFactory)
            statementFactory = new StatementFactory(this);
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

    protected final void checkMemberDeclared(CommonTree t, ESymbolKind expectedKind) throws SemanticException
    {
        assert null != symbols;

        final ISymbol<ESymbolKind> symbol = symbols.resolveMember(t.getText());

        if (null == symbol)
            raiseError(where(t), new UndeclaredSymbol(t.getText()));

        if (expectedKind != symbol.getKind())
            raiseError(where(t), new SymbolTypeMismatch<ESymbolKind>(t.getText(), symbol.getKind(), expectedKind));
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
