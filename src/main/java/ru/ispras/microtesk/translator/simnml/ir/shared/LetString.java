/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * LetString.java, Aug 20, 2013 6:41:25 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.shared;

public final class LetString
{
    private final String name;
    private final String text;

    LetString(String name, String text)
    {
        assert null != name;
        assert null != text;

        this.name = name;
        this.text = text;
    }

    public String getName()
    {
        return name;
    }

    public String getText()
    {
        return text;
    }

    @Override
    public String toString()
    {
        return String.format(
            "LetString [name=%s, text=%s]", name, text);
    }
}
