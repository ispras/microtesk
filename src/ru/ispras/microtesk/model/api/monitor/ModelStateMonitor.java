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

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.DataEngine;
import ru.ispras.microtesk.model.api.memory.Location;
import ru.ispras.microtesk.model.api.memory.MemoryBase;
import ru.ispras.microtesk.model.api.rawdata.RawData;
import ru.ispras.microtesk.model.api.rawdata.RawDataStore;
import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.model.api.type.Type;

/**
 * The ModelStateMonitor class implements the IModelStateMonitor interface.
 * 
 * @author Andrei Tatarnikov
 */

public class ModelStateMonitor implements IModelStateMonitor
{
    private final Map<String, MemoryBase> memoryMap;

    public ModelStateMonitor()
    {
        memoryMap = new HashMap<String, MemoryBase>();
    }
    
    public void addMemoryLine(MemoryBase value)
    {
        final String ERROR_FORMAT =
            "The %s memory line has already been added to the table of monitored resources.";

        final MemoryBase prev = memoryMap.put(value.getName(), value);
        assert null == prev : String.format(ERROR_FORMAT, value.getName());
    }

    @Override
    public IStoredValue getPC()
    {
        // TODO NOT IMPLEMENTED YET
        //assert false : "NOT IMPLEMENTED";
        //return null;
        return this.readLocationValue("GPR", 15); //FAKE_PC;
    }

    @Override
    public void setPC(long value)
    {
        //setPC(BigInteger.valueOf(value));
    	this.memoryMap.get("GPR").access(15).store(DataEngine.valueOf(new Type(ETypeID.CARD, 32), value));
    }

    @Override
    public void setPC(BigInteger value)
    {
        // TODO NOT IMPLEMENTED YET
        //assert false : "NOT IMPLEMENTED";
        //FAKE_PC.setValue(value);
    	this.memoryMap.get("GPR").access(15).store(DataEngine.valueOf(new Type(ETypeID.CARD, 32), value.longValue()));
    }

    @Override
    public IStoredValue readLocationValue(String name)
    {
        return readLocationValue(name, 0);
    }

    @Override
    public IStoredValue readLocationValue(String name, int index)
    {
        final MemoryBase current = memoryMap.get(name);

        if (null == current)
            return null;

        return new StoredValue(current.access(index)); 
    }
}

/**
 * The StoredValue class implements the IStoredValue interface.
 * 
 * @author Andrei Tatarnikov
 */

class StoredValue implements IStoredValue
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

    public void setValue(BigInteger value)
    {
        final RawData newRawData = new RawDataStore(location.getType().getBitSize());
        final byte[] bytes = value.toByteArray();

        for (int index = 0; index < Math.min(bytes.length, newRawData.getByteSize()); ++index)
            newRawData.setByte(index, (char)bytes[index]);

        final Data newData = new Data(newRawData, location.getType());
        location.store(newData);
    }

    @Override
    public String toBinString()
    {
        return location.getDataCopy().getRawData().toBinString();
    }
}
