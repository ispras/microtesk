/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * InstructionSet.java, Nov 23, 2012 2:46:42 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.instruction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import ru.ispras.microtesk.model.api.metadata.IMetaInstruction;

/**
 * The InstructionSet abstract class provides implementation of a container
 * for storing information on instructions that exist in the model. The
 * constructor is parameterized by an array of instruction objects.
 * 
 * @author Andrei Tatarnikov
 */

public abstract class InstructionSet implements IInstructionSet
{
    private final    Map<String, IInstruction> instructions;
    private final Collection<IMetaInstruction> metaData;
    
    /**
     * Creates a map of instructions and instruction meta data collection
     * based on an array of instruction objects.
     * 
     * @param entries Array of instructions.
     */

    public InstructionSet(IInstruction[] entries)
    {
        this.instructions = createInstructions(entries);
        this.metaData = createMetaData(instructions); 
    }

    private static Map<String, IInstruction> createInstructions(IInstruction[] entries)
    {
        final Map<String, IInstruction> result = new LinkedHashMap<String, IInstruction>();

        for (IInstruction e : entries)
            result.put(e.getName(), e);

        return Collections.unmodifiableMap(result);
    }

    private static Collection<IMetaInstruction> createMetaData(Map<String, IInstruction>  instructions)
    {
        final Collection<IMetaInstruction> result = new ArrayList<IMetaInstruction>();
        
        for(IInstruction i: instructions.values())
            result.add(i.getMetaData());
        
        return Collections.unmodifiableCollection(result);
    }

    @Override
    public final boolean supportsInstruction(String name)
    {
        return instructions.containsKey(name);
    }
    
    @Override
    public final IInstruction getInstruction(String name)
    {
        return instructions.get(name);
    }
    
    @Override 
    public final Collection<IMetaInstruction> getMetaData()
    {
        return metaData;
    }
}
