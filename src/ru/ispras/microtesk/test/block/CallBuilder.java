/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * CallBuilder.java, May 6, 2013 5:22:10 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.block;

import java.util.HashMap;
import java.util.Map;

public final class CallBuilder
{
    private Map<String, Object> attributes;

    public CallBuilder()
    {
        this.attributes = new HashMap<String, Object>();
    }

    public void setAttribute(String name, Object value)
    {
        assert !attributes.containsKey(name);
        attributes.put(name, value);
    }

    public Call createCall()
    {
        return new Call(attributes);
    }
}
