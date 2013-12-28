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

import java.math.BigInteger;

import ru.ispras.formula.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.type.ETypeID;
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
    private final class Accessor implements ILocationAccessor
    {
        @Override
        public int getBitSize()
        {
            return type.getBitSize();
        }

        @Override
        public String toBinString()
        {
            return rawData.toBinString();
        }

        @Override
        public BigInteger getValue()
        {
            return new BigInteger(rawData.toByteArray());
        }

        @Override
        public void setValue(BigInteger value)
        {
            //TODO: Restrictions on value size.

            assert (value.bitLength() <= Long.SIZE) :
                "Restriction: If the input data size exceeds size of long, it is truncated to long.";

            assert (type.getBitSize() <= Long.SIZE) :
                "Restriction: If the location size exceeds 64 bits, input data is truncated.";

            assert !readOnly;
            rawData.assign(BitVector.valueOf(value.longValue(), type.getBitSize()));
        }
    }

    private final Type         type;
    private final BitVector rawData;
    private final boolean  readOnly;

    private IMemoryAccessHandler handler;
    private final ILocationAccessor accessor;

    public Location(Type type)
    {
        this(type, BitVector.createEmpty(type.getBitSize()), false, null);
    }

    public Location(Data data)
    {
        this(data.getType(), data.getRawData(), true, null);
    }

    private Location(Type type, BitVector rawData, boolean readOnly, IMemoryAccessHandler handler)
    {
        this.type     = type;
        this.rawData  = rawData;
        this.readOnly = readOnly;
        this.handler  = handler;
        this.accessor = new Accessor();
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

    public void reset()
    {
        assert !isReadOnly();
        rawData.reset();
    }

    /**
     * Concatenates two locations: a.concat(b) = a(high) :: b(low)
     */

    public Location concat(Location arg)
    {
        return new Location(
           new Type(type.getTypeID(), type.getBitSize() + arg.getType().getBitSize()),
           BitVector.createMapping(arg.rawData /*low*/, rawData /*high*/),
           readOnly || arg.readOnly,
           handler
           );
    }

    public static Location concat(Location ... locations)
    {
        assert locations.length > 0;

        if (1 == locations.length)
            return locations[0];

        final BitVector[] rawDataArray = new BitVector[locations.length];

        boolean readOnly = false;
        int totalBitSize = 0;

        final ETypeID typeID = locations[0].getType().getTypeID();
        for (int index = 0; index < locations.length; ++index)
        {
            readOnly = readOnly || locations[index].readOnly;
            rawDataArray[index] = locations[index].rawData;
            totalBitSize += rawDataArray[index].getBitSize();
        }

        return new Location(
            new Type(typeID, totalBitSize),
            BitVector.createMapping(rawDataArray),
            readOnly,
            null
            );
    }

    public Location bitField(int start, int end)
    {
        assert (start >= 0) && (end >= 0);
        assert start <= end : "Start must be <= end. Reverse order is currently not supported.";

        final int bitSize = end - start + 1;
        return new Location(
            type,
            BitVector.createMapping(rawData, start, bitSize),
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

        return new Data(rawData.createCopy(), type);
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
        return new Data(rawData.createCopy(), type);
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

    public ILocationAccessor externalAccess()
    {
        return accessor;
    }
}
