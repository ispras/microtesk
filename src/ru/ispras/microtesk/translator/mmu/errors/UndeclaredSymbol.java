package ru.ispras.microtesk.translator.mmu.errors;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;

public class UndeclaredSymbol implements ISemanticError
{
    private static final String FORMAT = "The '%s' symbol is not declared.";

    private final String symbolName; 

    public UndeclaredSymbol(String symbolName)
    {
        this.symbolName = symbolName;
    }

    @Override
    public String getMessage()
    {
        return String.format(FORMAT, symbolName);
    }
}
