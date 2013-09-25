/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprFactory.java, Aug 14, 2013 12:00:36 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.errors.UndefinedConstant;
import ru.ispras.microtesk.translator.simnml.errors.ValueParsingFailure;
import ru.ispras.microtesk.translator.simnml.ir.expression.Location;

import ru.ispras.microtesk.translator.simnml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public class ExprFactory extends WalkerFactoryBase
{
    public ExprFactory(WalkerContext context)
    {
        super(context);
    }

    public final ExprNodeNamedConst namedConstant(Where w, String name) throws SemanticException
    {
        if (!getIR().getConstants().containsKey(name))
            getReporter().raiseError(w, new UndefinedConstant(name));

        final LetConstant constant = getIR().getConstants().get(name);
        return new ExprNodeNamedConst(constant);
    }

    public ExprNodeConst constant(Where w, String text, int radix) throws SemanticException
    {
        final ExprNodeConstCreator creator = new ExprNodeConstCreator(this, w, text, radix); 
        return creator.create();
    }

    public final ExprNodeLocation location(Location location)
    {
        return new ExprNodeLocation(location);
    }

    public Expr binary(Where w, String opID, Expr arg1, Expr arg2) throws SemanticException
    {
        // TODO
        return null;        
    }

    public Expr unary(Where w, String opID, Expr arg) throws SemanticException
    {
        // TODO
        return null;
    }

    public Expr coerce(Where w, Expr src, Type type) throws SemanticException
    {
        // TODO
        return null;
    }
}

final class ExprNodeConstCreator extends WalkerFactoryBase
{
    private final Where     w;
    private final String text;
    private final int   radix;

    public ExprNodeConstCreator(
        WalkerFactoryBase context,
        Where w,
        String text,
        int radix
        )
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

            return new ExprNodeConst(value, radix, bitSize);
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

        final int bitSize = Long.SIZE - Long.numberOfLeadingZeros(value);
        
        if (bitSize <= Integer.SIZE)
            return Integer.SIZE;

        return Long.SIZE;
    }
}


interface IOperator
{
    ValueInfo execute(ValueInfo ... args) throws SemanticException; 
}

class OperatorClass implements IOperator
{
    @Override
    public ValueInfo execute(ValueInfo ... args) throws SemanticException
    {
        if (args.length == 0)
            assert false; // Raise error
        
        // TODO Auto-generated method stub
        return new ValueInfo() {
            
            @Override
            public Type locationType() {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public boolean isConstant() {
                // TODO Auto-generated method stub
                return false;
            }
            
            @Override
            public long integerValue() {
                // TODO Auto-generated method stub
                return 0;
            }
            
            @Override
            public ValueKind getValueKind() {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public int getBitSize() {
                // TODO Auto-generated method stub
                return 0;
            }
            
            @Override
            public boolean booleanValue() {
                // TODO Auto-generated method stub
                return false;
            }
        };
    }
    
    
}
