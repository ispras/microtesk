/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * $Id: Int.java, Oct 17, 2012 12:20:43 PM Andrei Tatarnikov Exp $
 */

package ru.ispras.microtesk.model.api.data_old;

import static ru.ispras.microtesk.model.api.rawdata.RawDataAlgorithm.*;
import ru.ispras.microtesk.model.api.rawdata.RawData;

/**
 * The Int class implements logic responsible for operations with signed integers.
 * Signed integers are stored in binary form in bit arrays. The Int class high-level
 * methods for manipulating with its contents that encapsulates bit-level operations.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class Int extends Data
{  
    /**
     * Type information for Int objects.
     */

    public final static DataType<Int> DATA_TYPE = new DataType<Int>()
    {
        @Override
        public Int valueOf(RawData rawData) { return new Int(rawData); }

        @Override
        public EDataTypeID typeId() { return EDataTypeID.INT; }

        @Override
        public Int newInstance(int bitSize) { return new Int(bitSize); }
    };

    /**
     * {@inheritDoc}
     */

    @Override
    public DataType<Int> getDataType()
    {
        return DATA_TYPE;
    }
    
    /**
     * Creates an Int object initialized with a copy of the specified binary data. 
     * 
     * @param src Source raw data object.
     */

    private Int(RawData src)
    {
        super(src);
    }
    
    /**
     * Creates an Int object of the specified size.
     * 
     * @param bitSize Data size in bits.
     */

    public Int(int bitSize)
    {
        super(bitSize); 
    }
    
    /**
     * Creates an Int object of the specified size that holds the 
     * specified Java integer value (int). If bit size is greater than the size
     * of a Java integer, the rest high part of the raw data array is filled 
     * with zeros. If the Java integer size is greater, the source value is 
     * truncated (high bits are cut off).  
     * 
     * @param value Java integer value (int).
     * @param bitSize Data size in bits.
     */

    public Int(int value, int bitSize)
    {
        this(bitSize);
        assign(value);
    }
    
    /**
     * Creates an Int object of the specified size that holds the 
     * specified Java integer value (long). If bit size is greater than the size
     * of a Java integer, the rest high part of the raw data array is filled 
     * with zeros. If the Java integer size is greater, the source value is 
     * truncated (high bits are cut off)
     * 
     * @param value Java integer value (long).
     * @param bitSize Data size in bits.
     */

    public Int(long value, int bitSize)
    {
        this(bitSize);
        assign(value);
    }
    
    /**
     * Assigns a Java integer value (int) to the current Int object.
     * 
     * @param value Java integer value (int).
     */

    public void assign(int value)
    {
        assign(((long)value) & 0xFFFFFFFFL);
    }
    
    /**
     * Assigns a Java integer value (long) to the current Int object.
     * 
     * @param value Java integer value (long).
     */

    public void assign(final long value)
    {
        final IOperation op = new IOperation()
        {
            private long v = value; 

            @Override
            public char run()
            {
                if (0 == v) return 0;

                final char result = (char)(v & 0xFFL);
                v = v >>> RawData.BITS_IN_BYTE;

                return result;
            }
        };

        generate(this.getRawData(), op);
    }
    
    /**
     * Converts the contents of the Int object to a Java integer value.
     *
     * @return Integer value.
     */
    
    public int intValue()
    {
        class Result { public int value = 0; }
        final Result result = new Result();

        final IAction op = new IAction()
        {
            private int bitCount  = 0;

            @Override
            public void run(char v)
            {
                if (bitCount < Integer.SIZE)
                {
                    result.value = result.value | ((v & RawData.DEF_BYTE_MASK) << bitCount);
                    bitCount += RawData.BITS_IN_BYTE;
                }
                else
                {
                    // TODO: Throw an exception here
                    assert false : "Data will be truncated";
                }
            }
        };

        for_each(getRawData(), op);
        return result.value;
    }

    /**
     * Returns a result of negation of the value stored in the current Int object.
     * Negation is performed by inverting bits in the source value and by 
     * adding one (0x1) to it.  
     * 
     * @return An Int object that holds a negated value of the current Int object. 
     */

    public Int negate()
    {
        final Int inv = (Int) bitNOT();
        return inv.add(new Int(0x1, inv.getRawData().getBitSize()));
    }

    /**
     * Adds the current object value and the parameter value and returns
     * an Int object that holds the addition result.  
     * 
     * @param rhs An Int object to be added to the current object.
     * @return An Int object holding the addition result.
     */

    public Int add(Int rhs)
    {
        assert null != rhs;
        assert getRawData().getBitSize() == rhs.getRawData().getBitSize();

        final IBinaryOperation op = new IBinaryOperation()
        {
            private char remainder = 0;

            @Override
            public char run(char lhs, char rhs)
            { 
                final int retvalue = lhs + rhs + remainder;
                remainder = (char)(retvalue >>> RawData.BITS_IN_BYTE);
                return (char) retvalue;
            }
        };

        final Int result = getDataType().newInstance(getRawData().getBitSize());
        transform(getRawData(), rhs.getRawData(), result.getRawData(), op);

        return result;
    }

    /**
     * Subtracts the parameter value from the current object value and 
     * returns an Int object that holds the subtraction result.  
     * 
     * @param rhs An Int object to be subtracted from the current object.
     * @return An Int object holding the subtraction result.
     */
    
    public Int sub(Int rhs)
    {
        assert null != rhs;
        assert getRawData().getBitSize() == rhs.getRawData().getBitSize();

        return add(rhs.negate());
    }
}
