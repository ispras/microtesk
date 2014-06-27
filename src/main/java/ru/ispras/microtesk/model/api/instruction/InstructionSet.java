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
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.model.api.instruction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import ru.ispras.microtesk.model.api.metadata.MetaInstruction;

/**
 * The InstructionSet abstract class provides implementation of a container
 * for storing information on instructions that exist in the model. The
 * constructor is parameterized by an array of instruction objects.
 * 
 * @author Andrei Tatarnikov
 */

public abstract class InstructionSet implements IInstructionSet
{
    private final  Map<String, IInstructionEx> instructions;
    private final Collection<MetaInstruction> metaData;
    
    /**
     * Creates a map of instructions and instruction meta data collection
     * based on an array of instruction objects.
     * 
     * @param entries Array of instructions.
     */

    public InstructionSet(IInstructionEx[] entries)
    {
        this.instructions = createInstructions(entries);
        this.metaData = createMetaData(instructions); 
    }

    private static Map<String, IInstructionEx> createInstructions(IInstructionEx[] entries)
    {
        final Map<String, IInstructionEx> result = new LinkedHashMap<String, IInstructionEx>();

        for (IInstructionEx e : entries)
            result.put(e.getName(), e);

        return Collections.unmodifiableMap(result);
    }

    private static Collection<MetaInstruction> createMetaData(Map<String, IInstructionEx>  instructions)
    {
        final Collection<MetaInstruction> result = new ArrayList<MetaInstruction>();
        
        for(IInstructionEx i: instructions.values())
            result.add(i.getMetaData());
        
        return Collections.unmodifiableCollection(result);
    }

    @Override
    public final boolean supportsInstruction(String name)
    {
        return instructions.containsKey(name);
    }
    
    @Override
    public final IInstructionEx getInstruction(String name)
    {
        return instructions.get(name);
    }
    
    @Override 
    public final Collection<MetaInstruction> getMetaData()
    {
        return metaData;
    }
}
