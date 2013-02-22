/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * $Id: Card.java, Oct 17, 2012 12:20:04 PM Andrei Tatarnikov Exp $
 */

package ru.ispras.microtesk.model.api.data_old;

import static ru.ispras.microtesk.model.api.rawdata.RawDataAlgorithm.*;
import ru.ispras.microtesk.model.api.rawdata.RawData;

/**
 * The Card class implements logic responsible for operations with unsigned integers.
 * Unsigned integers are stored in binary form in bit arrays. The Card class high-level
 * methods for manipulating with its contents that encapsulates bit-level operations.
 *
 * @author Andrei Tatarnikov
 */

public class Card extends Data
{
    /**
     * Type information for Int objects.
     */
    
    public final static DataType<Card> DATA_TYPE = new DataType<Card>()
    {
        @Override
        public Card valueOf(RawData rawData) { return new Card(rawData); }

        @Override
        public EDataTypeID typeId() { return EDataTypeID.CARD; }

        @Override
        public Card newInstance(int bitSize) { return new Card(bitSize); }
    };

    /**
     * {@inheritDoc}
     */

    @Override
    public DataType<Card> getDataType()
    {
        return DATA_TYPE;
    }
    
    /**
     * Creates a Card object initialized with a copy of the specified binary data. 
     * 
     * @param src Source raw data object.
     */

    private Card(RawData src)
    {
        super(src);
    }

    /**
     * Creates a Card object of the specified size.
     * 
     * @param bitSize Data size in bits.
     */

    public Card(int bitSize)
    {
        super(bitSize); 
    }
    
    public Card(int value, int bitSize)
    {
        this(bitSize);
        assign(value);
    }

    public Card(long value, int bitSize)
    {
        this(bitSize);
        assign(value);
    }
    
    public void assign(int value)
    {
        assign(((long)value) & 0xFFFFFFFFL);
    }
    
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

        generate(getRawData(), op);
    }

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

    public Card negate()
    {
        final Card inv = (Card) bitNOT();
        return inv.add(new Card(0x1, inv.getRawData().getBitSize()));
    }

    public Card add(Card rhs)
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

        final Card result = getDataType().newInstance(getRawData().getBitSize());
        transform(getRawData(), rhs.getRawData(), result.getRawData(), op);

        return result;
    }

    public Card sub(Card rhs)
    {
        assert null != rhs;
        assert getRawData().getBitSize() == rhs.getRawData().getBitSize();

        return add(rhs.negate());
    }
}
