/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ProcessorModel.java, Dec 3, 2012 10:10:15 AM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import ru.ispras.microtesk.model.api.memory.Label;
import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.model.api.metadata.*;
import ru.ispras.microtesk.model.api.simnml.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.simnml.instruction.IOperation;
import ru.ispras.microtesk.model.api.state.IModelStateObserver;
import ru.ispras.microtesk.model.api.state.ModelStateObserver;
import ru.ispras.microtesk.model.api.state.Status;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.exception.config.UnsupportedInstructionException;
import ru.ispras.microtesk.model.api.instruction.IInstruction;
import ru.ispras.microtesk.model.api.instruction.IInstructionSet;

/**
 * The ProcessorModel class is base class for all families of microprocessor
 * model classes. It implements all methods of the interfaces that provide
 * services to external users. The responsibility to initialize member data
 * (to create corresponding objects) is delegated to descendant classes. 
 * 
 * @author Andrei Tatarnikov
 */

public abstract class ProcessorModel implements IModel
{
    public static final String SHARED_REGISTERS = "__REGISTERS";
    public static final String SHARED_MEMORY    = "__MEMORY";
    public static final String SHARED_VARIABLES = "__VARIABLES";
    public static final String SHARED_LABELS    = "__LABELS";
    public static final String SHARED_STATUSES  = "__STATUSES";
    public static final String SHARED_RESETTER  = "__RESETTER";

    private final IInstructionSet instructions;
    private final IModelStateObserver observer;
    private final MetaModel           metaData;

    public ProcessorModel(
        IInstructionSet instructions,
        IAddressingMode.IInfo[] modes,
        IOperation.IInfo[] ops,
        Memory[] registers,
        Memory[] memory,
        Label[] labels,
        Status[] statuses
        )
    {
        this.instructions = instructions;
        this.observer = new ModelStateObserver(registers, memory, labels, statuses);
        
        this.metaData = new MetaModel(
            instructions.getMetaData(),
            createAddressingModeMetaData(modes),
            createOperationMetaData(ops),
            createRegisterMetaData(registers),
            createMemoryMetaData(memory)
            );
    }

    private static Collection<MetaOperation> createOperationMetaData(IOperation.IInfo[] ops)
    {
        final Collection<MetaOperation> result =
            new ArrayList<MetaOperation>();

        for(IOperation.IInfo i : ops)
            result.addAll(i.getMetaData());

        return Collections.unmodifiableCollection(result);
    }

    private static Collection<MetaAddressingMode> createAddressingModeMetaData(IAddressingMode.IInfo[] modes)
    {
        final Collection<MetaAddressingMode> result =
            new ArrayList<MetaAddressingMode>();

        for(IAddressingMode.IInfo i : modes)
            result.addAll(i.getMetaData());

        return Collections.unmodifiableCollection(result);
    }

    private static Collection<MetaLocationStore> createRegisterMetaData(Memory[] registers)
    {
        final Collection<MetaLocationStore> result = new ArrayList<MetaLocationStore>();

        for(Memory r : registers)
            result.add(r.getMetaData());

        return Collections.unmodifiableCollection(result);
    }

    private static Collection<MetaLocationStore> createMemoryMetaData(Memory[] memory)
    {
        final Collection<MetaLocationStore> result = new ArrayList<MetaLocationStore>();

        for(Memory m : memory)
            result.add(m.getMetaData());

        return Collections.unmodifiableCollection(result);
    }

    // IModel
    @Override
    public final MetaModel getMetaData()
    {
        return metaData;
    }

    // IModel
    @Override
    public final IModelStateObserver getStateObserver()
    {
        return observer;
    }

    // IModel
    @Override
    public IInstruction getInstruction(String name) throws ConfigurationException
    {
        if (!instructions.supportsInstruction(name))
            throw new UnsupportedInstructionException(name);

        return instructions.getInstruction(name);
    }
} 
