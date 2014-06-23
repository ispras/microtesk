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
    private final Collection<MetaInstruction>  instructions;
    private final Collection<MetaAddressingMode>      modes;
    private final Collection<MetaOperation>      operations;
    private final Collection<MetaLocationStore>   registers;
    private final Collection<MetaLocationStore>      memory;

    public MetaModel(
        Collection<MetaInstruction> instructions,
        Collection<MetaAddressingMode>     modes,
        Collection<MetaOperation>     operations,
        Collection<MetaLocationStore>  registers,
        Collection<MetaLocationStore>     memory
        )
    {
        this.instructions = instructions;
        this.modes        = modes;
        this.operations   = operations;
        this.registers    = registers;
        this.memory       = memory;
    }

    /**
     * Returns an iterator for the collection of addressing modes (excluding
     * modes defined as OR rules). 
     * 
     * @return An Iterable object.
     */

    public Iterable<MetaAddressingMode> getAddressingModes()
    {
        return modes;
    }

    /**
     * Returns an iterator for the collection of operations (excluding
     * operations defined as OR rules). 
     * 
     * @return An Iterable object.
     */

    public Iterable<MetaOperation> getOperations()
    {
        return operations;
    }

    /**
     * Returns an iterator for the collection of instructions. 
     * 
     * @return An Iterable object.
     */

    public Iterable<MetaInstruction> getInstructions()
    {
        return instructions;
    }

    /**
     * Returns an iterator for the collection of registers.
     * 
     * @return An Iterable object.
     */

    public Iterable<MetaLocationStore> getRegisters()
    {
        return registers;
    }

    /**
     * Returns an iterator for the collection of memory store locations.
     * 
     * @return An Iterable object.
     */

    public Iterable<MetaLocationStore> getMemoryStores()
    {
        return memory;
    }
}
