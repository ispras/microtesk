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
import java.util.Collections;

import ru.ispras.microtesk.model.api.metadata.*;
import ru.ispras.microtesk.model.api.instruction.IInstructionCallBlockBuilder;
import ru.ispras.microtesk.model.api.instruction.IInstructionSet;
import ru.ispras.microtesk.model.api.instruction.InstructionCallBlockBuilder;
import ru.ispras.microtesk.model.api.monitor.IModelStateMonitor;

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
    private final IModelStateMonitor                      monitor;

    public ProcessorModel(
        IInstructionSet instructions,
        Collection<IMetaLocationStore> registerMetaData,
        Collection<IMetaLocationStore> memoryMetaData,
        IModelStateMonitor monitor)
    {
        this.instructions     = instructions;
        this.registerMetaData = registerMetaData;
        this.memoryMetaData   = memoryMetaData;
        this.monitor          = monitor;
    }

    // IModel
    @Override
    public final IMetaModel getMetaData()
    {
        return this;
    }

    // IModel
    @Override
    public final IModelStateMonitor getStateMonitor()
    {
        return monitor;
    }

    // IModel
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

    // IMetaModel
    @Override
    public final Iterable<IMetaSituation> getSituations()
    {
        // TODO NOT IMPLEMENTED YET
        return Collections.emptyList();
    }

    // ISimulator
    @Override
    public final IInstructionCallBlockBuilder createCallBlock()
    {
        return new InstructionCallBlockBuilder(instructions);
    }
} 
