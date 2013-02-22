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
 */

package ru.ispras.microtesk.model.api.simnml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import ru.ispras.microtesk.model.api.ProcessorModel;
import ru.ispras.microtesk.model.api.instruction.IInstructionSet;
import ru.ispras.microtesk.model.api.memory.MemoryBase;
import ru.ispras.microtesk.model.api.metadata.IMetaLocationStore;
import ru.ispras.microtesk.model.api.monitor.IModelStateMonitor;
import ru.ispras.microtesk.model.api.monitor.ModelStateMonitor;

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
    /**
     * The name of the collection of all registers provided by the model.
     */

    public static final String SHARED_REGISTERS = "__REGISTERS";
    
    /**
     * The name of the collection of all memory lines provided by the model.
     */

    public static final String SHARED_MEMORY = "__MEMORY";
    
    public SimnMLProcessorModel(
        IInstructionSet instructions,
        MemoryBase[] registers,
        MemoryBase[] memory
        )
    {
        super(
            instructions,
            createRegisterMetaData(registers),
            createMemoryMetaData(memory),
            createStateMonitor(registers, memory)
            );
    }

    private static Collection<IMetaLocationStore> createRegisterMetaData(MemoryBase[] registers)
    {
        final Collection<IMetaLocationStore> result = new ArrayList<IMetaLocationStore>();

        for(MemoryBase r : registers)
            result.add(r.getMetaData());

        return Collections.unmodifiableCollection(result);
    }

    private static Collection<IMetaLocationStore> createMemoryMetaData(MemoryBase[] memory)
    {
        final Collection<IMetaLocationStore> result = new ArrayList<IMetaLocationStore>();

        for(MemoryBase m : memory)
            result.add(m.getMetaData());

        return Collections.unmodifiableCollection(result);
    }

    private static IModelStateMonitor createStateMonitor(MemoryBase[] registers, MemoryBase[] memory)
    {
        final ModelStateMonitor result = new ModelStateMonitor();

        for(MemoryBase r : registers)
            result.addRegisterLine(r.getName(), r);

        for(MemoryBase m : memory)
            result.addMemoryLine(m.getName(), m);

        return result;
    }
}
