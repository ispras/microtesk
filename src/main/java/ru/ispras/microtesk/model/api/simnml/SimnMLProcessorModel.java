/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * SimnMLProcessorModel.java, Dec 3, 2012 11:18:44 AM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.api.simnml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import ru.ispras.microtesk.model.api.ProcessorModel;
import ru.ispras.microtesk.model.api.instruction.IInstructionSet;
import ru.ispras.microtesk.model.api.memory.Label;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.model.api.metadata.IMetaAddressingMode;
import ru.ispras.microtesk.model.api.metadata.IMetaLocationStore;
import ru.ispras.microtesk.model.api.simnml.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.simnml.instruction.IOperation;
import ru.ispras.microtesk.model.api.state.ModelStateObserver;
import ru.ispras.microtesk.model.api.state.Status;

/**
 * The SimnMLProcessorModel class is a base class for all microprocessor models
 * described in the Sim-nML ADL. It is responsible for setting up the processor's 
 * memory and register meta data and the model state monitor (basing on information
 * about memory resources).
 * 
 * @author Andrei Tatarnikov
 */

public abstract class SimnMLProcessorModel extends ProcessorModel
{
    public static final String SHARED_REGISTERS = "__REGISTERS";
    public static final String SHARED_MEMORY    = "__MEMORY";
    public static final String SHARED_VARIABLES = "__VARIABLES";
    public static final String SHARED_LABELS    = "__LABELS";
    public static final String SHARED_STATUSES  = "__STATUSES";
    public static final String SHARED_RESETTER  = "__RESETTER";

    public SimnMLProcessorModel(
        IInstructionSet instructions,
        IAddressingMode.IInfo[] modes,
        IOperation.IInfo[] ops,
        Memory[] registers,
        Memory[] memory,
        Label[] labels,
        Status[] statuses
        )
    {
        super(
            instructions,
            createAddressingModeMetaData(modes),
            createRegisterMetaData(registers),
            createMemoryMetaData(memory),
            new ModelStateObserver(registers, memory, labels, statuses)
            );
    }

    private static Collection<IMetaAddressingMode> createAddressingModeMetaData(IAddressingMode.IInfo[] modes)
    {
        final Collection<IMetaAddressingMode> result =
            new ArrayList<IMetaAddressingMode>();

        for(IAddressingMode.IInfo i : modes)
            result.addAll(i.getMetaData());

        return Collections.unmodifiableCollection(result);
    }

    private static Collection<IMetaLocationStore> createRegisterMetaData(Memory[] registers)
    {
        final Collection<IMetaLocationStore> result = new ArrayList<IMetaLocationStore>();

        for(Memory r : registers)
            result.add(r.getMetaData());

        return Collections.unmodifiableCollection(result);
    }

    private static Collection<IMetaLocationStore> createMemoryMetaData(Memory[] memory)
    {
        final Collection<IMetaLocationStore> result = new ArrayList<IMetaLocationStore>();

        for(Memory m : memory)
            result.add(m.getMetaData());

        return Collections.unmodifiableCollection(result);
    }
}
