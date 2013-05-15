/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Situation.java, May 15, 2013 2:36:11 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.situation;

public abstract class Situation implements ISituation
{
    private final String name;

    public Situation(String name)
    {
        this.name = name;
    }

    @Override
    public final String getName()
    {
        return name;
    }
}
