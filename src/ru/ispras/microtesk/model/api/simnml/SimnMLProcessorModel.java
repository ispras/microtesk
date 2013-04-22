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
import ru.ispras.microtesk.model.api.memory.Label;
import ru.ispras.microtesk.model.api.memory.MemoryBase;
import ru.ispras.microtesk.model.api.metadata.IMetaLocationStore;
import ru.ispras.microtesk.model.api.state.ModelStateObserver;

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

    public SimnMLProcessorModel(
        IInstructionSet instructions,
        MemoryBase[] registers,
        MemoryBase[] memory,
        Label[] labels
        )
    {
        super(
            instructions,
            createRegisterMetaData(registers),
            createMemoryMetaData(memory),
            new ModelStateObserver(registers, memory, labels)
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
}
