package ru.ispras.microtesk.translator.mmu.errors;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;

public final class UnrecognizedStructure implements ISemanticError
{
    private static final String FORMAT =
        "Failed to recognize the grammar structure. It will be ignored: '%s'.";

    private final String what;

    public UnrecognizedStructure(String what)
    {
        this.what = what;
    }

    @Override
    public String getMessage()
    {
        return String.format(FORMAT, what);
    }
}
