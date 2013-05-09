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
    // instruction name
    // arguments
    // situations
    // other attributes

    private final String name;
    private final Map<String, Argument> arguments;
    private final Map<String, Object>  attributes;

    protected AbstractCall(
        String name,
        Map<String, Argument> arguments,
        Map<String, Object> attributes
        )
    {
        this.name = name;
        this.arguments = arguments;
        this.attributes = attributes;
    }

    public String getName()
    {
        return name;
    }

    public Object getArgument(String name)
    {
        return arguments.get(name);
    }

    public Object getAttribute(String name)
    {
        return attributes.get(name);
    }
}
