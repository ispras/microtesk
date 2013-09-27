/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprFactoryCreator.java, Sep 27, 2013 12:27:30 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.errors.ValueParsingFailure;

interface ExprFactoryCreator
{
    public Expr create() throws SemanticException;
}

final class ConstantCreator extends WalkerFactoryBase implements ExprFactoryCreator
{
    private final Where     w;
    private final String text;
    private final int   radix;

    public ConstantCreator(WalkerFactoryBase context, Where w, String text, int radix)
    {
        super(context);

        this.w = w;
        this.text = text;
        this.radix = radix;
    }

    public ExprNodeConst create() throws SemanticException
    {
        try
        {
            final long  value = Long.valueOf(text, radix);
            final int bitSize = getBitSize(value); 

            return new ExprNodeConst(NativeValue.makeNumber(bitSize, value), radix);
        }
        catch (NumberFormatException e)
        {
            getReporter().raiseError(w, new ValueParsingFailure(text, long.class.getSimpleName()));
            return null;
        }
    }

    private int getBitSize(long value)
    {
        final int BIN_RADIX = 2;
        final int HEX_RADIX = 16;
        final int BITS_IN_HEX_CHAR = 4;

        if (BIN_RADIX == radix)
            return text.length();

        if (HEX_RADIX == radix)
            return text.length() * BITS_IN_HEX_CHAR;

        final int bitSize = 
            Long.SIZE - Long.numberOfLeadingZeros(value);

        if (bitSize <= Integer.SIZE)
            return Integer.SIZE;

        return Long.SIZE;
    }
}
