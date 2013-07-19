/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * StatementText.java, Jul 19, 2013 3:30:58 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.primitive;

public class StatementText extends Statement
{
    private static final String DEFAULT_CODE =
       "//Default code\r\nreturn null;";

    private final String text;

    StatementText()
    {
        this(DEFAULT_CODE);
    }

    public StatementText(String text)
    {
        super(Kind.TEXT);

        assert null != text;
        this.text = text;
    }

    public String getText()
    {
        return text;
    }
}
