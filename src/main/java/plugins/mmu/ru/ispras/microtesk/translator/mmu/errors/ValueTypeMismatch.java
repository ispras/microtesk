package ru.ispras.microtesk.translator.mmu.errors;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;

public class ValueTypeMismatch implements ISemanticError
{
    private static final String FORMAT = "Types %s and %s are incompatible.";

    private final Class<?> type1;
    private final Class<?> type2;

    public ValueTypeMismatch(Class<?> type1, Class<?> type2)
    {
        this.type1 = type1;
        this.type2 = type2;
    }

    @Override
    public String getMessage()
    {
        return String.format(FORMAT, type1.getSimpleName(), type2.getSimpleName());
    }
}
