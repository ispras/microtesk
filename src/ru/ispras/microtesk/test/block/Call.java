/*
 * Copyright (c) 2013 ISPRAS
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

public final class Call
{
    private Map<String, Object> attributes;

    public Call(Map<String, Object> attributes)
    {
        this.attributes = attributes;
    }

    public Object getAttribute(String name)
    {
        return attributes.get(name);
    }

    // instruction name
    // arguments 
    // situations
    // other attributes
}
