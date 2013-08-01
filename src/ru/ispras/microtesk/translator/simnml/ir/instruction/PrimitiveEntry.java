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
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;

public final class PrimitiveEntry
{
    private final Primitive              primitive;
    private final Map<String, PrimitiveEntry> args;

    public PrimitiveEntry(Primitive primitive)
    {
        this.primitive = primitive;
        this.args = new LinkedHashMap<String, PrimitiveEntry>();
    }

    public PrimitiveEntry(PrimitiveEntry entry)
    {
        this(entry.primitive);

        for (Map.Entry<String, PrimitiveEntry> e : entry.args.entrySet())
            this.args.put(e.getKey(), new PrimitiveEntry(e.getValue()));
    }

    public String getName()
    {
        return primitive.getName();
    }

    public Primitive.Kind getKind()
    {
        return primitive.getKind();
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
