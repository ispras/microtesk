package ru.ispras.microtesk.translator.mmu.errors;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;

public class UnsupportedConstOperation implements ISemanticError
{
    private static final String FORMAT = "The %s operation is not supported for the following operand types: %s (\"%s\").";

    private final String   op;
    private final String   text;
    private final Class<?> type1;
    private final Class<?> type2;

    public UnsupportedConstOperation(String op, String text, Class<?> type1, Class<?> type2)
    {
        this.op    = op;
        this.text  = text;
        this.type1 = type1;
        this.type2 = type2;
    }
    
    public UnsupportedConstOperation(String op, String text, Class<?> type)
    {
        this(op, text, type, null);
    }

    @Override
    public String getMessage()
    {
        final String types = (null == type2) ?
            type1.getSimpleName() :
            type1.getSimpleName() + " and " + type2.getSimpleName();

        return String.format(FORMAT, op, types, text);
    }
}
