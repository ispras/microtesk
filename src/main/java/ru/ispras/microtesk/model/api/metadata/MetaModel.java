/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MetaModel.java, Jun 23, 2014 11:25:49 AM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.api.metadata;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
* The MetaModel class stores information on the model and provides methods to
* access it. The information includes the list of instructions, the list of
* memory resources (registers, memory) and the list of test situations
* (behavioral properties of the instructions).   
* 
* @author Andrei Tatarnikov
*/

public final class MetaModel
{
    private final Map<String, MetaInstruction>  instructions;
    private final Map<String, MetaAddressingMode>      modes;
    private final Map<String, MetaOperation>      operations;
    private final Map<String, MetaLocationStore>   registers;
    private final Map<String, MetaLocationStore>      memory;

    public MetaModel(
        Collection<MetaInstruction> instructions,
        Collection<MetaAddressingMode>     modes,
        Collection<MetaOperation>     operations,
        Collection<MetaLocationStore>  registers,
        Collection<MetaLocationStore>     memory
        )
    {
        this.instructions = toMap(instructions);
        this.modes        = toMap(modes);
        this.operations   = toMap(operations);
        this.registers    = toMap(registers);
        this.memory       = toMap(memory);
    }

    private static <T extends MetaData> Map<String, T> toMap(Collection<T> c)
    {
        final Map<String, T> map = new LinkedHashMap<String, T>();

        for (T t : c)
            map.put(t.getName(), t);

        return map;
    }

    /**
     * Returns an iterator for the collection of addressing modes (excluding
     * modes defined as OR rules). 
     * 
     * @return An Iterable object.
     */

    public Iterable<MetaAddressingMode> getAddressingModes()
    {
        return modes.values();
    }

    /**
     * Returns metadata for the specified addressing mode.
     * 
     * @param name Addressing mode name.
     * @return Addressing mode metadata.
     */

    public MetaAddressingMode getAddressingMode(String name)
    {
        return modes.get(name);
    }

    /**
     * Returns an iterator for the collection of operations (excluding
     * operations defined as OR rules). 
     * 
     * @return An Iterable object.
     */

    public Iterable<MetaOperation> getOperations()
    {
        return operations.values();
    }

    /**
     * Returns metadata for the specified operation.
     * 
     * @param name Operation name.
     * @return Operation metadata.
     */

    public MetaOperation getOperation(String name)
    {
        return operations.get(name);
    }

    /**
     * Returns an iterator for the collection of instructions. 
     * 
     * @return An Iterable object.
     */

    public Iterable<MetaInstruction> getInstructions()
    {
        return instructions.values();
    }

    /**
     * Returns metadata for the specified instruction.
     * 
     * @param name Instruction name.
     * @return Instruction metadata.
     */

    public MetaInstruction getInstruction(String name)
    {
        return instructions.get(name);
    }

    /**
     * Returns an iterator for the collection of registers.
     * 
     * @return An Iterable object.
     */

    public Iterable<MetaLocationStore> getRegisters()
    {
        return registers.values();
    }

    /**
     * Returns metadata for the specified register file.
     * 
     * @param name Register file name.
     * @return Register file metadata.
     */

    public MetaLocationStore getRegister(String name)
    {
        return registers.get(name);
    }

    /**
     * Returns an iterator for the collection of memory store locations.
     * 
     * @return An Iterable object.
     */

    public Iterable<MetaLocationStore> getMemoryStores()
    {
        return memory.values();
    }

    /**
     * Returns metadata for the specified memory store location.
     * 
     * @param name Memory store location name.
     * @return Memory store location metadata.
     */

    public MetaLocationStore getMemoryStore(String name)
    {
        return memory.get(name);
    }
}
