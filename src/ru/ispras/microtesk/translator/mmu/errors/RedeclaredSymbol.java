package ru.ispras.microtesk.translator.mmu.errors;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;
import ru.ispras.microtesk.translator.antlrex.symbols.ISymbol;
import ru.ispras.microtesk.translator.mmu.translator.ESymbolKind;

public final class RedeclaredSymbol implements ISemanticError
{
    private static final String FORMAT =
        "The '%s' name is already used to declare another symbol of type %s (position %d:%d).";
    private final String FORMAT_KEYWORD =
        "The '%s' name is already used a reserved keyword.";

    private final ISymbol<?> symbol;

    public RedeclaredSymbol(ISymbol<?> symbol)
    {
        this.symbol = symbol;
    }

    @Override
    public String getMessage()
    {
        if (symbol.getKind() == ESymbolKind.KEYWORD)
            return String.format(FORMAT_KEYWORD, symbol.getName());

        return String.format(
            FORMAT,
            symbol.getName(),
            symbol.getKind().toString(),
            symbol.getLine(),
            symbol.getPositionInLine()
            );
    }
}
