package ru.ispras.microtesk.translator.mmu.errors;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;
import ru.ispras.microtesk.translator.mmu.antlrex.ESymbolKind;

public class SymbolTypeMismatch implements ISemanticError
{
    public static final String FORMAT =
        "The '%s' symbol has a wrong type. It is '%s' while '%s' is expected in this expression.";

    private final String        symbolName;
    private final ru.ispras.microtesk.translator.mmu.antlrex.ESymbolKind         kind;
    private final ESymbolKind expectedKind;

    public SymbolTypeMismatch(String symbolName, ru.ispras.microtesk.translator.mmu.antlrex.ESymbolKind eSymbolKind, ESymbolKind expectedKind2)
    {
        this.symbolName   = symbolName;
        this.kind         = eSymbolKind;
        this.expectedKind = expectedKind2;
    }

    @Override
    public String getMessage()
    {
        return String.format(FORMAT, symbolName, kind, expectedKind);
    }
}
