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

public abstract class ProcessorModel implements IModel, IMetaModel, ISimulator
{
    private final IInstructionSet                    instructions;
    private final Collection<IMetaLocationStore> registerMetaData;
    private final Collection<IMetaLocationStore>   memoryMetaData;
    private final IModelStateObserver                    observer;

    public ProcessorModel(
        IInstructionSet instructions,
        Collection<IMetaLocationStore> registerMetaData,
        Collection<IMetaLocationStore> memoryMetaData,
        IModelStateObserver observer
        )
    {
        this.instructions     = instructions;
        this.registerMetaData = registerMetaData;
        this.memoryMetaData   = memoryMetaData;
        this.observer         = observer;
    }

    // IModel
    @Override
    public final IMetaModel getMetaData()
    {
        return this;
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

    // IMetaModel
    @Override
    public final Iterable<IMetaInstruction> getInstructions()
    {
        return instructions.getMetaData();
    }

    // IMetaModel
    @Override
    public final Iterable<IMetaLocationStore> getRegisters()
    {
        return registerMetaData;
    }

    // IMetaModel
    @Override
    public final Iterable<IMetaLocationStore> getMemoryStores()
    {
        return memoryMetaData;
    }

    // ISimulator
    @Deprecated
    @Override
    public final IInstructionCallBlockBuilder createCallBlock()
    {
        return new InstructionCallBlockBuilder(instructions);
    }
} 
