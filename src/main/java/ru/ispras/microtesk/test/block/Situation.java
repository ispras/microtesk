/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Situation.java, May 14, 2013 4:35:10 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.block;

public final class Situation
{
    private final String name;

    public Situation(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
