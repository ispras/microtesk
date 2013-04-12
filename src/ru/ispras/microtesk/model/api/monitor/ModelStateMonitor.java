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
import ru.ispras.microtesk.model.api.memory.Label;
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

public final class ModelStateMonitor implements IModelStateMonitor
{
    private final static String ALREADY_ADDED_ERR_FRMT =
        "The %s item has already been added to the table.";

    private final Map<String, MemoryBase> memoryMap;
    private final Map<String, Label> labelMap;

    public ModelStateMonitor()
    {
        memoryMap = new HashMap<String, MemoryBase>();
        labelMap  = new HashMap<String, Label>(); 
    }

    public void addMemoryLine(MemoryBase value)
    {
        final MemoryBase prev = memoryMap.put(value.getName(), value);
        assert null == prev : String.format(ALREADY_ADDED_ERR_FRMT, value.getName());
    }
    
    public void addLabel(Label value)
    {
        final Label prev = labelMap.put(value.getName(), value);
        assert null == prev : String.format(ALREADY_ADDED_ERR_FRMT, value.getName());
    }

    @Override
    public IStoredValue getPC()
    {
        return readLocationValue("PC");
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
        if (labelMap.containsKey(name))
            return new StoredValue(labelMap.get(name).access());

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
