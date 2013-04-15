/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ModelStateMonitor.java, Nov 8, 2012 2:03:46 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.monitor;

import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.model.api.ILocationAccessor;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.exception.config.UndeclaredException;
import ru.ispras.microtesk.model.api.memory.Label;
import ru.ispras.microtesk.model.api.memory.MemoryBase;

/**
 * The ModelStateMonitor class implements the IModelStateMonitor interface.
 * 
 * @author Andrei Tatarnikov
 */

public final class ModelStateMonitor implements IModelStateMonitor
{
    private final static String ALREADY_ADDED_ERR_FRMT =
        "The %s item has already been added to the table.";

    private final static String UNDEFINED_ERR_FRMT =
        "The %s resource is not defined in the current model.";

    private final static String BOUNDS_ERR_FRMT =
        "The %d index is invalid for the %s resource.";

    private final Map<String, MemoryBase> memoryMap;
    private final Map<String, Label> labelMap;

    public ModelStateMonitor(
        MemoryBase[] registers,
        MemoryBase[] memory,
        Label[] labels
        )
    {
        assert null != registers;
        assert null != memory;
        assert null != labels;

        memoryMap = new HashMap<String, MemoryBase>();
        for(MemoryBase r : registers)
        {
            final MemoryBase prev = memoryMap.put(r.getName(), r);
            assert null == prev : String.format(ALREADY_ADDED_ERR_FRMT, r.getName());
        }

        for(MemoryBase m : memory)
        {
            final MemoryBase prev = memoryMap.put(m.getName(), m);
            assert null == prev : String.format(ALREADY_ADDED_ERR_FRMT, m.getName());
        }

        labelMap  = new HashMap<String, Label>();
        for(Label l : labels)
        {
            final Label prev = labelMap.put(l.getName(), l);
            assert null == prev : String.format(ALREADY_ADDED_ERR_FRMT, l.getName());
        }
    }

    @Override
    public ILocationAccessor accessLocation(String name) throws ConfigurationException
    {
        return accessLocation(name, 0);
    }

    @Override
    public ILocationAccessor accessLocation(String name, int index) throws ConfigurationException
    {
        if (labelMap.containsKey(name))
        {
            if (0 != index)
                throw new UndeclaredException(String.format(BOUNDS_ERR_FRMT, index, name));

            return labelMap.get(name).access().externalAccess();
        }

        if (!memoryMap.containsKey(name))
            throw new UndeclaredException(String.format(UNDEFINED_ERR_FRMT, name));

        final MemoryBase current = memoryMap.get(name);

        if ((index < 0) || (index >= current.getLength()))
            throw new UndeclaredException(String.format(BOUNDS_ERR_FRMT, index, name));

        return current.access(index).externalAccess(); 
    }
}
