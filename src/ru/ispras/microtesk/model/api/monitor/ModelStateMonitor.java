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

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.model.api.memory.Location;
import ru.ispras.microtesk.model.api.memory.MemoryBase;

/**
 * The ModelStateMonitor class implements the IModelStateMonitor interface.
 * 
 * @author Andrei Tatarnikov
 */

public class ModelStateMonitor implements IModelStateMonitor
{
    /**
     * The StoredValue class implements the IStoredValue interface.
     * 
     * @author Andrei Tatarnikov
     */
    
    private static class StoredValue implements IStoredValue
    {
        public final Location location;

        public StoredValue(Location location)
        {
            this.location = location;
        }

        @Override
        public int getBitSize()
        {
            return location.getType().getBitSize();
        }

        @Override
        public BigInteger getValue()
        {
            return new BigInteger(location.getDataCopy().getRawData().toByteArray());
        }

        @Override
        public String toBinString()
        {
            return location.getDataCopy().getRawData().toBinString();
        }
    }
    
    private final Map<String, MemoryBase>   memoryMap;
    private final Map<String, MemoryBase> registerMap;

    public ModelStateMonitor()
    {
        memoryMap   = new HashMap<String, MemoryBase>();
        registerMap = new HashMap<String, MemoryBase>();
    }
    
    public void addMemoryLine(String name, MemoryBase value)
    {
        final String ERROR_FORMAT =
            "The %s memory line has already been added to the table of minitored resources. The reference has been reassigned.";

        final MemoryBase prev = memoryMap.put(name, value);
        assert null == prev : String.format(ERROR_FORMAT, name);
    }
    
    public void addRegisterLine(String name, MemoryBase value)
    {
        final String ERROR_FORMAT =
            "The %s register line has already been added to the table of minitored resources. The reference has been reassigned.";

        final MemoryBase prev = registerMap.put(name, value);
        assert null == prev : String.format(ERROR_FORMAT, name);
    }
    
    @Override
    public IStoredValue getPC()
    {
        // TODO NOT IMPLEMENTED YET
    	assert false : "NOT IMPLEMENTED";
    	return null;
    }
    
    @Override
    public IStoredValue readRegisterValue(String name)
    {
        return readRegisterValue(name, 0);
    }

    @Override
    public IStoredValue readRegisterValue(String name, int index)
    {
        final MemoryBase current = registerMap.get(name);

        if (null == current)
            return null;

        return new StoredValue(current.access(index)); 
    }

    @Override
    public IStoredValue readMemoryValue(String name)
    {
        return readMemoryValue(name, 0);
    }

    @Override
    public IStoredValue readMemoryValue(String name, int index)
    {
        final MemoryBase current = memoryMap.get(name);

        if (null == current)
            return null;

        return new StoredValue(current.access(index)); 
    }
}
