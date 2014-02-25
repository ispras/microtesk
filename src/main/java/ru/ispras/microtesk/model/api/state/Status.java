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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Status
{
    // Prototypes for standard status flags
    public static final Status CTRL_TRANSFER = new Status("__CTRL_TRANSFER", 0);
    public static final Map<String, Status> STANDARD_STATUSES = createStandardStatuses();

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
    
    private static final Map<String, Status> createStandardStatuses()
    {
        final Map<String, Status> result = new HashMap<String, Status>();
        result.put(CTRL_TRANSFER.getName(), CTRL_TRANSFER);
        return Collections.unmodifiableMap(result);
    }
}
