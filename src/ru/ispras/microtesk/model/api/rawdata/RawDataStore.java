/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * $Id: RawDataStore.java, Oct 11, 2012 12:46:54 PM Andrei Tatarnikov Exp $
 */

package ru.ispras.microtesk.model.api.rawdata;

/**
 * The RawDataStore class represents a data array that stores "raw" binary data (a bit vector).
 * Data can be accessed by bytes. If the number of bits is not multiple of 8 (number of bits in a byte)
 * the highest byte is truncated (its highest bits are excluded).  
 *
 * <pre>
 * Example:
 *
 * Data representation for a 29-bit long data array. The highest 3 bits are "cut off" by a bit mask.
 * 
 * Byte:
 * 4        3        2        1        0
 *
 * Bit:
 * 32  29!  24       16       8        0
 * _____________________________________
 * |   !    |        |        |        |
 * |%%%!    |        |        |        |
 * |%%%!    |        |        |        |
 * |___!____|________|________|________|
 *
 * Bit size:       29
 * Byte size:      4
 * High byte mask: 00011111 (binary)
 * </pre>
 *
 * @author Andrei Tatarnikov
 */

public final class RawDataStore extends RawData 
{  
    private final char[] dataBytes; // Array that stores binary data. 
    private final int      bitSize; // Number of used bits.

    /**
     * Allocates a new data array. 
     *
     * @param bitSize Data size in bits.
     */

    public RawDataStore(int bitSize)
    {
        final int byteSize = bitSize / BITS_IN_BYTE + (0 == (bitSize % BITS_IN_BYTE) ? 0 : 1);

        this.bitSize   = bitSize;
        this.dataBytes = new char[byteSize];

        reset();
    }

    /**
     * Creates a copy of existing an data store.
     *
     * @param src An existing data array to be copied.
     */

    public RawDataStore(RawData src)
    {
        assert null != src; 

        this.dataBytes = new char[src.getByteSize()];
        this.bitSize   = src.getBitSize();

        assign(src);
    }
    
    /**
     * {@inheritDoc}
     */

    @Override
    public int getBitSize()
    {
        return bitSize;
    }

    /**
     * {@inheritDoc}
     */

    @Override 
    public int getByteSize()
    {
        return dataBytes.length;
    }
    
    /**
     * {@inheritDoc}
     */

    @Override 
    public char getByte(int index)
    {
        assert (index >= 0) && (index < getByteSize());

        return (char)(dataBytes[index] & getByteBitMask(index));
    }

    /**
     * {@inheritDoc}
     */

    @Override 
    public void setByte(int index, char value)
    {
        assert (index >= 0) && (index < getByteSize());

        //Expected situation: value contains more bits than can be stored in highest (incomplete) byte.  
        //assert (0 == (value & (~getByteBitMask(index) & RawData.DEF_BYTE_MASK)));

        final char mask = getByteBitMask(index);
        final char old  = dataBytes[index];

        // Bits beyond the range <bitCount-1..0> are preserved.
        dataBytes[index] = (char)(old & (char)~mask);
        dataBytes[index] = (char)(dataBytes[index] | (value & mask));

        // To make sure that bits beyond the bound have not been changed.
        assert ((char)(old & (char)~mask)) ==
               ((char)(dataBytes[index] & (char)~mask));
    }
}
