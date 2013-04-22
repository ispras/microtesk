/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ModelStateObserver.java, Nov 8, 2012 2:03:46 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.state;

import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.exception.config.UndeclaredException;
import ru.ispras.microtesk.model.api.memory.ILocationAccessor;
import ru.ispras.microtesk.model.api.memory.Label;
import ru.ispras.microtesk.model.api.memory.MemoryBase;

public final class ModelStateObserver implements IModelStateObserver
{
    public static final String  CTRL_TRANSFER_NAME = "__CTRL_TRANSFER";
    public static final Status[] STANDARD_STATUSES = { new Status(CTRL_TRANSFER_NAME, 0) };

    private final static String ALREADY_ADDED_ERR_FRMT =
        "The %s item has already been added to the table.";

    private final static String UNDEFINED_ERR_FRMT =
        "The %s resource is not defined in the current model.";

    private final static String BOUNDS_ERR_FRMT =
        "The %d index is invalid for the %s resource.";

    private final Map<String, MemoryBase> memoryMap;
    private final Map<String, Label> labelMap;

    private final Status controlTransfer;

    public ModelStateObserver(
        MemoryBase[] registers,
        MemoryBase[] memory,
        Label[] labels,
        Status[] statuses
        )
    {
        assert null != registers;
        assert null != memory;
        assert null != labels;
        assert null != statuses;

        memoryMap = new HashMap<String, MemoryBase>();
        addToMemoryMap(memoryMap, registers);
        addToMemoryMap(memoryMap, memory);

        labelMap  = new HashMap<String, Label>();
        addToLabelMap(labelMap, labels);
        
        controlTransfer = findStatus(CTRL_TRANSFER_NAME, statuses);
    }

    private static void addToMemoryMap(Map<String, MemoryBase> map, MemoryBase[] items)
    {
        for(MemoryBase m : items)
        {
            final MemoryBase prev = map.put(m.getName(), m);
            assert null == prev : String.format(ALREADY_ADDED_ERR_FRMT, m.getName());
        }
    }

    private static void addToLabelMap(Map<String, Label> map, Label[] items)
    {
        for(Label l : items)
        {
            final Label prev = map.put(l.getName(), l);
            assert null == prev : String.format(ALREADY_ADDED_ERR_FRMT, l.getName());
        }
    }

    private static Status findStatus(String name, Status[] statuses)
    {
        for (Status status : statuses)
        {
            if (name.equals(status.getName()))
                return status;
        }

        assert false : String.format("The %s status is not defined in the model.", name);
        return null;
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

    @Override
    public int getControlTransferStatus()
    {
        return controlTransfer.get();
    }
}
