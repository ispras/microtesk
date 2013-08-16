/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Expression.java, Aug 14, 2013 12:30:28 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

public abstract class Expr
{
    public enum Kind
    {
        CONSTANT,
        VARIABLE,
        OPERATION
    }

    private final Kind kind;

    public Expr(Kind kind)
    {
        assert null != kind;
        this.kind = kind;
    }

    public final Kind getKind()
    {
        return kind;
    }

    public abstract ValueInfo getValueInfo();
}
