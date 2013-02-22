/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Data.java, Oct 8, 2012 12:31:17 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data_old;

import static ru.ispras.microtesk.model.api.rawdata.RawDataAlgorithm.*;
import ru.ispras.microtesk.model.api.rawdata.RawData;
import ru.ispras.microtesk.model.api.rawdata.RawDataStore;

/**
 * The Data class is a storage of untyped "raw" binary data of fixed size.
 * The class provides basic conversion and comparison methods and implements
 * basic bit level operations with stored data.
 * 
 * <pre>
 * Conversion:
 *    to binary string.
 *
 * Operations:
 *
 *    &, |, ^ and ~ (bit-wise and, or, xor and complement respectively),
 *    << and >>     (left shift and right shift),
 *    <<< and >>>   (left and right rotate operations).
 * </pre>
 * 
 * @author Andrei Tatarnikov
 */

public abstract class Data
{
    private final RawData rawData;

    /**
     * Creates a data storage of the specified size.
     * 
     * @param bitSize Data size in bits.
     */
    
    public Data(int bitSize)
    {
        assert 0 != bitSize;
        this.rawData = new RawDataStore(bitSize);
    }
    
    /**
     * Creates a copy of the specified data storage.
     *
     * @param src Source data object.
     */

    public Data(Data src)
    {
        this.rawData = new RawDataStore(src.getRawData());
    }
    
    /**
     * Creates a data storage that stores a copy of the specified "raw" binary data array.  
     * 
     * @param src Source "raw" binary data array.
     */

    public Data(RawData src)
    {
        this.rawData = new RawDataStore(src);
    }
    
    /**
     * Initializes the current data storage with data stored in the 
     * specified data storage. If the source data array is longer, it is
     * truncated. If it is shorter, the rest of the target data array is 
     * filled with zeros.  
     * 
     * @param src Source data storage.
     */

    public final void assign(Data src)
    {
        this.rawData.assign(src.getRawData());
    }
    
    /**
     * Returns a reference to the "raw" binary data array. 
     * 
     * @return A reference to the "raw" binary data array.
     */
    
    public final RawData getRawData()
    {
        return rawData;
    }
    
    /**
     * Returns information on the type of the stored data.
     * 
     * @return Type information
     */
    
    public abstract DataType<?> getDataType();
      
    /**
     * Converts the stored data to textual representation in binary format.
     * 
     * @return Binary string.
     */

    public final String toBinString()
    {
        return rawData.toBinString();
    }
    
    public Data bitAND(Data rhs)
    {
        assert null != rhs;
        assert getRawData().getBitSize() == rhs.getRawData().getBitSize();

        final Data result = getDataType().newInstance(getRawData().getBitSize());

        final IBinaryOperation op = new IBinaryOperation()
        {
            @Override
            public char run(char lhs, char rhs) { return (char) (lhs & rhs); }
        };

        transform(getRawData(), rhs.getRawData(), result.getRawData(), op);
        return result;        
    }
    
    public Data bitOR(Data rhs)
    {
        assert null != rhs;
        assert getRawData().getBitSize() == rhs.getRawData().getBitSize();
        
        final Data result = getDataType().newInstance(getRawData().getBitSize());

        final IBinaryOperation op = new IBinaryOperation()
        {
            @Override
            public char run(char lhs, char rhs) { return (char) (lhs | rhs); }
        };

        transform(getRawData(), rhs.getRawData(), result.getRawData(), op);
        return result; 
    }
    
    public Data bitXOR(Data rhs)
    {
        assert null != rhs;
        assert getRawData().getBitSize() == rhs.getRawData().getBitSize();
        
        final Data result = getDataType().newInstance(getRawData().getBitSize());

        final IBinaryOperation op = new IBinaryOperation()
        {
            @Override
            public char run(char lhs, char rhs) { return (char) (lhs ^ rhs); }
        };

        transform(getRawData(), rhs.getRawData(), result.getRawData(), op);
        return result;
    }
    
    public Data bitNOT()
    {
        final Data result = getDataType().newInstance(getRawData().getBitSize());

        final IUnaryOperation op = new IUnaryOperation()
        {
            @Override
            public char run(char v) { return (char)~v; }
        };

        transform(getRawData(), result.getRawData(), op);
        return result;
    }

    public Data shiftLeft(int to)
    {
        final Data result = getDataType().newInstance(getRawData().getBitSize());
        
        if (to < getRawData().getBitSize())
            copy(getRawData(), 0, result.getRawData(), to, result.getRawData().getBitSize() - to);
        
        return result;
    }

    public Data shiftRight(int to)
    {
        final Data result = getDataType().newInstance(getRawData().getBitSize());

        if (to < getRawData().getBitSize())
            copy(getRawData(), to, result.getRawData(), 0, result.getRawData().getBitSize() - to);

        return result;
    }

    public Data rotateLeft(int to)
    {
        final Data result = getDataType().newInstance(getRawData().getBitSize());

        final int realTo = to % result.getRawData().getBitSize();
        
        copy(getRawData(), 0, result.getRawData(), realTo, result.getRawData().getBitSize() - realTo);
        copy(getRawData(), getRawData().getBitSize()-realTo, result.getRawData(), 0, realTo);

        return result;
    }

    public Data rotateRight(int to)
    {
        final Data result = getDataType().newInstance(getRawData().getBitSize());

        final int realTo = to % result.getRawData().getBitSize();
        
        copy(getRawData(), 0, result.getRawData(), result.getRawData().getBitSize() - realTo, realTo);
        copy(getRawData(), realTo, result.getRawData(), 0, result.getRawData().getBitSize()-realTo);

        return result;
    }
}
