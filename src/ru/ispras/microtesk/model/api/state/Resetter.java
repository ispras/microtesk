/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Resetter.java, Apr 22, 2013 3:25:13 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.state;

import ru.ispras.microtesk.model.api.memory.MemoryBase;

public final class Resetter implements IStateResetter
{
    final MemoryBase[] variables;
    final Status[] statuses;

    public Resetter(MemoryBase[] variables, Status[] statuses)
    {
        this.variables = variables;
        this.statuses = statuses;
    }

    public void reset()
    {
        for (MemoryBase variable : variables)
            variable.reset();

        for (Status status : statuses)
            status.reset();
    }
}
