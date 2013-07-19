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

    public abstract String getText();
}

final class FormatStatement extends Statement
{
    public FormatStatement()
    {
        super(Kind.FORMAT);
    }

    @Override
    public String getText()
    {
        return null;
    }
}

class TextStatement extends Statement
{
    private final String text;

    TextStatement()
    {
        super(Kind.TEXT);
        
        this.text = "//Default code\r\nreturn null";
    }

    public TextStatement(String text)
    {
        super(Kind.TEXT);
        
        this.text = text;
    }

    public String getText()
    {
        return text;
    }
}
