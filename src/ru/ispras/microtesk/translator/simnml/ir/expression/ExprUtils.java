/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprUtils.java, Oct 17, 2013 4:28:29 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

public final class ExprUtils
{
    private ExprUtils() {} 

    public static int integerValue(Expr expr)
    {
        assert null != expr;
        final ValueInfo vi = expr.getValueInfo();

        if (vi.isConstant() && Integer.class == vi.getNativeType())
            return ((Number) vi.getNativeValue()).intValue();

        assert false : "Not a constant integer value";
        return 0;
    }

    public static Expr createConstant(int value)
    {
        return new ExprNodeConst(value, 10);
    }
}
