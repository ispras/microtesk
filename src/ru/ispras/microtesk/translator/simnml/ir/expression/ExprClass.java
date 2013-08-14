/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprClass.java, Jan 22, 2013 3:06:58 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public final class ExprClass implements Expr
{
    private final EExprKind kind;
    private final String text;

    private final Class<?> javaType;
    private final Type modelType;

    private final ExprOperator operator;
    private final Object value;
    private final Location location;

    public ExprClass(
        EExprKind    kind,
        String       text,
        Class<?>     javaType,
        Type     modelType,
        ExprOperator operator,
        Object       value,
        Location     location
        )
    {
        this.kind      = kind;
        this.text      = text;
        this.javaType  = javaType;
        this.modelType = modelType;
        this.operator  = operator;
        this.value     = value;
        this.location  = location;
    }

    public static Expr createConstant(int value, String text)
    {
        return new ExprClass(
            EExprKind.JAVA_STATIC,
            text,
            int.class,
            null,
            null,
            value,
            null
            );
    }

    public static Expr createConstant(int value)
    {
        return createConstant(value, Integer.toString(value));
    }

    @Override
    public EExprKind getKind()
    {
        return kind;
    }

    @Override
    public String getText()
    {
        return text;
    }

    @Override
    public Class<?> getJavaType()
    {
        return javaType;
    }

    @Override
    public Type getModelType()
    {
        return modelType;
    }

    @Override
    public ExprOperator getOperator()
    {
        return operator;
    }

    @Override
    public Object getValue()
    {
        return value;
    }

    @Override
    public Location getLocation()
    {
        return location;
    }
}
