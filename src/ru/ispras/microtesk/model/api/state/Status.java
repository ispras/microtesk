/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Status.java, Apr 19, 2013 5:05:38 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.state;

public final class Status
{
    private final String name;
    private final int defaultValue;
    private int value;

    public Status(String name, int defaultValue)
    {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getName()
    {
        return name;
    }

    public int get()
    {
        return value;
    }

    public void set(int value)
    {
        this.value = value;
    }

    public void reset()
    {
        value = defaultValue;
    }
    
    public int getDefault()
    {
        return defaultValue;
    }
}
