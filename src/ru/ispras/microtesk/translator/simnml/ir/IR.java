/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IR.java, Dec 11, 2012 1:57:58 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.microtesk.translator.simnml.ir.primitive.Instruction;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetExpr;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetLabel;
import ru.ispras.microtesk.translator.simnml.ir.shared.MemoryExpr;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public final class IR
{
    private final Map<String, LetExpr>      lets;
    private final Map<String, LetLabel>   labels;
    private final Map<String, Type>    types;
    private final Map<String, MemoryExpr> memory;
    private final Map<String, Primitive>   modes;
    private final Map<String, Primitive>     ops;

    private Map<String, Instruction> instructions;

    public IR()
    {
        this.lets   = new LinkedHashMap<String, LetExpr>();
        this.labels = new LinkedHashMap<String, LetLabel>();
        this.types  = new LinkedHashMap<String, Type>();
        this.memory = new LinkedHashMap<String, MemoryExpr>();

        this.modes  = new LinkedHashMap<String, Primitive>();
        this.ops    = new LinkedHashMap<String, Primitive>();

        this.instructions = new LinkedHashMap<String, Instruction>();
    }

    public void add(String name, LetExpr value)
    {
        lets.put(name, value);
    }

    public void add(String name, LetLabel value)
    {
        labels.put(name, value);
    }

    public void add(String name, Type value)
    {
        types.put(name, value);
    }

    public void add(String name, MemoryExpr value)
    {
        memory.put(name, value);
    }

    public void add(String name, Primitive value)
    {
        if (Primitive.Kind.MODE == value.getKind())
            modes.put(name, value);
        else if (Primitive.Kind.OP == value.getKind())
            ops.put(name, value);
        else
            assert false : String.format( "Incorrect primitive kind: %s.", value.getKind());
    }

    public Map<String, LetExpr> getLets()
    {
        return Collections.unmodifiableMap(lets);
    }

    public Map<String, LetLabel> getLabels()
    {
        return Collections.unmodifiableMap(labels);
    }

    public Map<String, Type> getTypes()
    {
        return Collections.unmodifiableMap(types);
    }

    public Map<String, MemoryExpr> getMemory()
    {
        return Collections.unmodifiableMap(memory);
    }

    public Map<String, Primitive> getModes()
    {
        return Collections.unmodifiableMap(modes);
    }

    public Map<String, Primitive> getOps()
    {
        return Collections.unmodifiableMap(ops);
    }

    public Map<String, Instruction> getInstructions()
    {
        return Collections.unmodifiableMap(instructions);
    }

    public void putInstructions(Map<String, Instruction> value)
    {
        assert instructions.isEmpty();
        assert null != value;
        instructions = value;
    }
}
