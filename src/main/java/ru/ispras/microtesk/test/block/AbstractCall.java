/*
+ * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Call.java, Apr 30, 2013 1:12:59 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.block;

import java.util.Map;

public final class AbstractCall
{
    private final String name;
    private final Map<String, Argument> arguments;
    private final Map<String, Object>  attributes;
    private final Situation situation;

    protected AbstractCall(
        String name,
        Map<String, Argument> arguments,
        Map<String, Object> attributes,
        Situation situation
        )
    {
        this.name = name;
        this.arguments = arguments;
        this.attributes = attributes;
        this.situation = situation;
    }

    public String getName()
    {
        return name;
    }

    public Argument getArgument(String name)
    {
        return arguments.get(name);
    }

    public Map<String, Argument> getArguments()
    {
        return arguments;
    }

    public Object getAttribute(String name)
    {
        return attributes.get(name);
    }

    public Map<String, Object> getAttributes()
    {
        return attributes;
    }
    
    public Situation getSituation()
    {
        return situation;
    }
}
