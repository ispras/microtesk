package ru.ispras.microtesk.translator.mmu.antlrex;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.TreeNodeStream;

import ru.ispras.microtesk.translator.antlrex.TreeParserEx;
import ru.ispras.microtesk.translator.antlrex.symbols.ISymbol;
import ru.ispras.microtesk.translator.antlrex.symbols.SymbolTable;
import ru.ispras.microtesk.translator.antlrex.errors.RedeclaredSymbol;
import ru.ispras.microtesk.translator.antlrex.errors.UnrecognizedStructure;
import ru.ispras.microtesk.translator.mmu.ESymbolKind;
import ru.ispras.microtesk.translator.mmu.ir.IR;
import ru.ispras.microtesk.translator.mmu.ir.expression.ConstExprFactory;

public class TreeWalkerBase extends TreeParserEx //implements ISemanticChecks
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

    private ConstExprFactory     constExprFactory = null;

    protected final ConstExprFactory getConstExprFactory()
    {
        if (null == constExprFactory)
            constExprFactory = new ConstExprFactory(ir.getLets(), this);
        return constExprFactory;
    }

    /*======================================================================================*/

    protected final void checkRedeclared(CommonTree current) throws RecognitionException
    {
        final ISymbol<?> symbol = symbols.resolve(current.getText());
        if (null != symbol)
            raiseError(where(current), new RedeclaredSymbol(symbol));		
    }

    protected void checkNotNull(CommonTree current, Object obj, String text) throws RecognitionException
    {
        if (null == obj)
            raiseError(where(current), new UnrecognizedStructure(text));		
    }
    
    /*======================================================================================*/
    
    protected final boolean isDeclaredAs(CommonTree t, ESymbolKind expectedKind)
    {
        assert null != symbols;

        final ISymbol<ESymbolKind> symbol = symbols.resolve(t.getText());

        if (null == symbol)
            return false;

        if (expectedKind != symbol.getKind())
            return false;

        return true;
    }
}
