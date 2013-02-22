/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Location.java, Nov 9, 2012 8:52:26 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.memory;

import ru.ispras.microtesk.model.api.rawdata.RawData;
import ru.ispras.microtesk.model.api.rawdata.RawDataMapping;
import ru.ispras.microtesk.model.api.rawdata.RawDataMultiMapping;
import ru.ispras.microtesk.model.api.rawdata.RawDataStore;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.type.Type;

/**
 * The Location class represents memory location of the specified size that store
 * data of the specified data type. A location represents a bit array that stores
 * a piece of data (like a register or an address in the main memory). 
 * 
 * @author Andrei Tatarnikov
 */

public final class Location
{
    private final Type        type;
    private final RawData  rawData;
    private final boolean readOnly;

    private IMemoryAccessHandler handler;
    
    public Location(Type type)
    {
        this(type, new RawDataStore(type.getBitSize()), false, null);
    }
    
    public Location(Data data)
    {
        this(data.getType(), data.getRawData(), true, null);
    }

    private Location(Type type, RawData rawData, boolean readOnly, IMemoryAccessHandler handler)
    {
        this.type     = type;
        this.rawData  = rawData;
        this.readOnly = readOnly;
        this.handler  = handler;
    }

    public final Type getType()
    {
        return type;
    }

    public final boolean isReadOnly()
    {
        return readOnly;
    }

    public Location assign(Location arg)
    {
        store(arg.load());
        return this;
    }

    public Location concat(Location arg)
    {
        return new Location(
           type,
           new RawDataMultiMapping(new RawData[] { rawData, arg.rawData } ),
           readOnly || arg.readOnly,
           handler
           );
    }

    public Location bitField(int start, int end)
    {
        assert (start >= 0) && (start < end); // TODO: exception

        return new Location(
            type,
            new RawDataMapping(rawData, start, end - start),
            readOnly,
            handler
            );
    }

    public Data load()
    {
        // TODO: Multiple handlers (in the case of concatenation)
        
        /*// TODO: NOT SUPPORTED IN THE CURRENT VERSION. 
          
        if (null != handler)
        {
            final RawData cachedRawData = handler.onLoad();
            if (null != cachedRawData)
                return new Data(new RawDataStore(cachedRawData), type);
        }
        */

        return new Data(new RawDataStore(rawData), type);
    }

    public void store(Data data)
    {
        // TODO: Multiple handlers (in the case of concatenation)
        
        assert !readOnly; // TODO: Throw exception            
        
        /*// TODO: NOT SUPPORTED IN THE CURRENT VERSION.
        
        if (null != handler)
        {
            if (handler.onStore(data.getRawData()))
                return;            
        }
        
        */

        rawData.assign(data.getRawData());
    }
    
    /**
     * Returns a copy of stored data. This method is needed to monitor the state
     * of a data location. It provide access without simulation of storage operations. 
     * 
     * @return A data object.
     */

    public Data getDataCopy()
    {
        return new Data(new RawDataStore(rawData), type);
    }
    
    /* TODO: NOT SUPPORTED IN THE CURRENT VERSION.

    public void advise(IMemoryAccessHandler handler)
    {
        assert null == this.handler;
        this.handler = handler;
    }

    public void unadvise()
    {
        assert null != this.handler;
        this.handler = null;
    }
    */
}
