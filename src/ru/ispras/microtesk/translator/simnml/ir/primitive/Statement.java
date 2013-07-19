/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Statement.java, Feb 7, 2013 12:09:14 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.primitive;

public abstract class Statement
{
    public static enum Kind
    {
        CALL,
        ASSIGN,
        COND,
        FORMAT,
        TEXT
    }

    private final Kind kind;

    Statement(Kind kind)
    {
        this.kind = kind;
    }

    public final Kind getKind()
    {
        return kind;
    }
}
