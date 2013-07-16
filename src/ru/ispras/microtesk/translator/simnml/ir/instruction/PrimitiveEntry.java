/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * PrimitiveEntry.java, Jan 11, 2013 4:56:38 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.instruction;

import java.util.Collections;
import java.util.Map;
import java.util.LinkedHashMap;

import ru.ispras.microtesk.translator.simnml.ir.modeop.EArgumentKind;

public final class PrimitiveEntry
{
    private final String name;
    private final EArgumentKind kind;
    private final Map<String, PrimitiveEntry> args;

    public PrimitiveEntry(String name, EArgumentKind kind)
    {
        this.name = name;
        this.kind = kind;
        this.args = new LinkedHashMap<String, PrimitiveEntry>();
    }

    public PrimitiveEntry(PrimitiveEntry primitive)
    {
        this(primitive.name, primitive.kind);

        for (Map.Entry<String, PrimitiveEntry> e : primitive.args.entrySet())
            this.args.put(e.getKey(), new PrimitiveEntry(e.getValue()));
    }

    public String getName()
    {
        return name;
    }

    public EArgumentKind getKind()
    {
        return kind;
    }

    public Map<String, PrimitiveEntry> getArguments()
    {
        return Collections.unmodifiableMap(args);
    }

    public void addArgument(String argName, PrimitiveEntry argType)
    {
        assert !args.containsKey(argName);
        args.put(argName, argType);
    }

    public void resetArgument(String argName, PrimitiveEntry argType)
    {
        assert args.containsKey(argName);
        args.put(argName, argType);
    }
}
