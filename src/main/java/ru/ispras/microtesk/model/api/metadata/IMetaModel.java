/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IMetaModel.java, Nov 2, 2012 3:14:27 PM Andrei Tatarnikov
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

/**
 * The IMetaModel interface provides methods to query information
 * on the ISA. This includes the list of instructions, the list of memory
 * resources (registers, memory) and the list of test situations (behavioral
 * properties of the instructions).   
 * 
 * @author Andrei Tatarnikov
 */

public interface IMetaModel
{
    /**
     * Returns an iterator for the collection of addressing modes (excluding
     * modes defined as OR rules). 
     * 
     * @return An Iterable object.
     */

    public Iterable<IMetaAddressingMode> getAddressingModes();

    /**
     * Returns an iterator for the collection of instructions. 
     * 
     * @return An Iterable object.
     */

    public Iterable<IMetaInstruction> getInstructions();

    /**
     * Returns an iterator for the collection of registers.
     * 
     * @return An Iterable object.
     */

    public Iterable<IMetaLocationStore> getRegisters();

    /**
     * Returns an iterator for the collection of memory store locations.
     * 
     * @return An Iterable object.
     */

    public Iterable<IMetaLocationStore> getMemoryStores();
}
