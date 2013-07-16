/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Instruction.java, Jan 9, 2013 6:01:52 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.instruction;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Instruction
{
    private static final String CLASS_NAME_FORMAT = "Instruction%s";

    private final String name;
    private final PrimitiveEntry rootPrimitive;
    private final Map<String, PrimitiveEntry> args;

    public Instruction(
        String name,
        PrimitiveEntry rootPrimitive,
        Map<String, PrimitiveEntry> args
        )
    {
        this.name = name;
        this.rootPrimitive = new PrimitiveEntry(rootPrimitive);
        this.args = Collections.unmodifiableMap(new LinkedHashMap<String, PrimitiveEntry>(args));
    }

    public String getName()
    {
        return name;
    }

    public String getClassName()
    {
        return String.format(CLASS_NAME_FORMAT, getName());
    }

    public PrimitiveEntry getRootPrimitive()
    {
        return rootPrimitive;
    }

    public Map<String, PrimitiveEntry> getArguments()
    {
        return args;
    }
}
