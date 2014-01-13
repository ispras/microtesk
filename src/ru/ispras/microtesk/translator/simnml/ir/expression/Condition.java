/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Condition.java, Jan 13, 2014 4:01:54 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

/**
 * Helper class to temporarily represent conditional expressions
 * based on the if-elif-else operator:
 * <pre>
 * if cond1 then 
 *    expr1
 * elif cond2 then
 *    expr2
 * ...
 * else
 *    exprN
 * endif
 * </pre>
 * 
 * @author Andrei Tatarnikov
 */

public final class Condition
{
    private final Expr cond;
    private final Expr expr;

    private Condition(Expr cond, Expr expr)
    {
        if (null == expr)
            throw new NullPointerException();

        this.cond = cond;
        this.expr = expr;
    }

    public static Condition newIf(Expr cond, Expr expr)
    {
        if (null == cond)
            throw new NullPointerException();

        return new Condition(cond, expr);
    }

    public static Condition newElse(Expr expr)
    {
        return new Condition(null, expr);
    }

    public Expr getCondition()
    {
        return cond;
    }

    public boolean isElse()
    {
        return null == cond;
    }

    public Expr getExpression()
    {
        return expr;
    }
}
