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

import java.util.Collection;

import ru.ispras.microtesk.model.api.metadata.*;
import ru.ispras.microtesk.model.api.state.IModelStateObserver;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.exception.config.UnsupportedInstructionException;
import ru.ispras.microtesk.model.api.instruction.IInstruction;
import ru.ispras.microtesk.model.api.instruction.IInstructionCallBlockBuilder;
import ru.ispras.microtesk.model.api.instruction.InstructionCallBlockBuilder;
import ru.ispras.microtesk.model.api.instruction.IInstructionSet;

/**
 * The ProcessorModel class is base class for all families of microprocessor
 * model classes. It implements all methods of the interfaces that provide
 * services to external users. The responsibility to initialize member data
 * (to create corresponding objects) is delegated to descendant classes. 
 * 
 * @author Andrei Tatarnikov
 */

public abstract class ProcessorModel implements IModel, ISimulator
{
    private final IInstructionSet instructions;
    private final MetaModel           metaData;
    private final IModelStateObserver observer;

    public ProcessorModel(
        IInstructionSet instructions,
        Collection<MetaAddressingMode> modesMetaData,
        Collection<MetaLocationStore> registerMetaData,
        Collection<MetaLocationStore> memoryMetaData,
        IModelStateObserver observer
        )
    {
        this.instructions = instructions;
        this.metaData     = new MetaModel(
            instructions.getMetaData(), modesMetaData, registerMetaData, memoryMetaData);
        this.observer     = observer;
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

    // IModel
    @Deprecated
    @Override
    public final ISimulator getSimulator()
    {
        return this;
    }

    // ISimulator
    @Deprecated
    @Override
    public final IInstructionCallBlockBuilder createCallBlock()
    {
        return new InstructionCallBlockBuilder(instructions);
    }
} 
