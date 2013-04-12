/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * LetLabel.java, Apr 11, 2013 4:47:36 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.let;

public final class LetLabel
{
    private final String name;
    private final String memoryName;
    private final int index;

    protected LetLabel(String name, String memoryName)
    {
        this(name, memoryName, 0);
    }

    protected LetLabel(String name, String memoryName, int index)
    {
        this.name = name;
        this.memoryName = memoryName;
        this.index = index;
    }

    public String getName()
    {
        return name;
    }

    public String getMemoryName()
    {
        return memoryName;
    }

    public int getIndex()
    {
        return index;
    }

    @Override
    public String toString()
    {
        return String.format(
           "LetLabel [name=%s, memoryName=%s, index=%d]", name, memoryName, index);
    }
}
