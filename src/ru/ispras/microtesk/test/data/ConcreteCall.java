/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ConcreteCall.java, May 9, 2013, 11:00:07 PM PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.data;

import java.util.Map;
import ru.ispras.microtesk.model.api.instruction.IInstructionCall;

public final class ConcreteCall
{
    private final String name;
    private final Map<String, Object> attributes;
    private final IInstructionCall executable;

    public ConcreteCall(
        String name,
        Map<String, Object> attributes,
        IInstructionCall executable
        )
    {
        this.name = name;
        this.attributes = attributes;
        this.executable = executable;
    }

    public String getName()
    {
        return name;
    }

    public Object getAttribute(String name)
    {
        if (null == attributes)
            return null;

        return attributes.get(name);
    }

    public IInstructionCall getExecutable()
    {
        return executable;
    }
}
