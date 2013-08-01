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

import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveAND;

public final class Instruction
{
    private static final String CLASS_NAME_FORMAT = "Instruction%s";

    private final String                  name;
    private final PrimitiveAND            root;
    private final Map<String, Primitive>  args;

    public Instruction(
        String name,
        PrimitiveAND root,
        Map<String, Primitive> args
        )
    {
        this.name = name;
        this.root = root;
        this.args = Collections.unmodifiableMap(new LinkedHashMap<String, Primitive>(args));
    }

    public String getName()
    {
        return name;
    }

    public String getClassName()
    {
        return String.format(CLASS_NAME_FORMAT, getName());
    }

    public PrimitiveAND getRootPrimitive()
    {
        return root;
    }

    public Map<String, Primitive> getArguments()
    {
        return args;
    }
}
