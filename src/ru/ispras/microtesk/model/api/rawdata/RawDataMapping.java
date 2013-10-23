/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * RawDataMapping.java, Oct 11, 2012 12:49:59 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.rawdata;

/**
 * The RawDataMapping class implements logic that provides the possibility to map
 * an array of raw data to another data array. Mapping can start at an arbitrary position
 * and can have an arbitrary length (bonded by the size of the target data array).
 * 
 * <pre>
 * The scheme below demonstrates how the class works:
 *
 * Target data array (a 29-bit vector):
 *
 * Real data:
 *
 * Bits:
 * 32  29!  24       16 14    8    3   0
 * _____________________________________
 * |   !    |        |  |     |    |   |
 * |%%%!    |        |HH|XXXXX|XXXX|LLL|
 * |%%%!    |        |HH|XXXXX|XXXX|LLL|
 * |___!____|________|__|_____|____|___|
 * 
 * Mapped view:
 *  
 * Bits: 
 * 16    11 8       0
 * _________________
 * |     |  |       |
 * |%%%%%|XX|XXXXXXX|
 * |%%%%%|XX|XXXXXXX|
 * |_____|__|_______|
 * 
 * (%) - excluded area
 * (X) - mapped area
 * (H) - high byte mask area. The mask excludes the marked bits (mask bits are set to zero). 
 * (L) - low byte mask area. The mask excludes the marked bits (mask bits are set to zero).
 * 
 * When we work with data via a mapping, the methods of our RawDataMapping class split bytes into parts 
 * and perform the needed bit operations to align the data in a proper way.  
 * </pre>
 * 
 * @author Andrei Tatarnikov
 */

public class RawDataMapping extends RawData
{
    private final RawData data;

    private final int  beginBitPos;
    private final int  bitSize;
    private final int  excludedLowBits;
    private final byte lowByteMask;
    private final byte highByteMask;

    /**
     * Creates a mapping for the specified data array. 
     *  
     * @param src The source data array.
     * @param beginBitPos The starting position of the mapping.
     * @param bitSize The length of the mapping in bits.
     */

    public RawDataMapping(RawData src, int beginBitPos, int bitSize)
    {
        assert null != src;
        assert 0 != bitSize;

        assert (0 <= beginBitPos) && (beginBitPos < src.getBitSize());
        assert (0 <= beginBitPos + bitSize) && (beginBitPos + bitSize <= src.getBitSize());

        this.data            = src;
        this.beginBitPos     = beginBitPos;
        this.bitSize         = bitSize;

        this.excludedLowBits = beginBitPos % BITS_IN_BYTE;
        this.lowByteMask     = (0 == excludedLowBits) ? 0 : (byte)(0xFF << excludedLowBits);

        this.highByteMask    = (byte) (0xFF >>> (BITS_IN_BYTE - (beginBitPos + bitSize) % BITS_IN_BYTE));
    }

    /**
     * Creates a reference to the specified data array (a mapping that matches
     * the original data array). 
     * 
     * @param src The source data array.
     */

    public RawDataMapping(RawData src)
    {
        this(src, 0, src.getBitSize());
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
        return bitSize / BITS_IN_BYTE + ((0 == bitSize % BITS_IN_BYTE) ? 0 : 1);
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public byte getByte(int index)
    {
        final int beginBytePos = getBeginBytePos();
        final int endBytePos   = getEndBytePos();

        final int byteIndex    = beginBytePos + index;

        assert ((0 <= index) && (index < getByteSize()));
        assert ((0 <= byteIndex) && (byteIndex < data.getByteSize())); 

        // If there is no mask for the lowest bytes this means that data
        // is aligned by bytes and no data transformation is needed. Also,
        // in case if there is an incomplete high byte we apply a bit mask that
        // corresponds to the specified byte.

        if (0 == lowByteMask)
            return (byte) (data.getByte(byteIndex) & getByteBitMask(index));

        // Takes needed bits (the higher part) of the low byte (specified by byteIndex) and
        // shifts them to the beginning of the byte (towards the least significant part).

        final byte lowByte =
            (byte)((data.getByte(byteIndex) & (lowByteMask & 0xFF)) >>> excludedLowBits);

        // If there is not bytes left in the data array, we don't go further.        
        if (byteIndex == endBytePos)
            return (byte) (lowByte & getByteBitMask(index));

        // Takes the needed bits (the lower part) of the high byte (following after the low byte)
        // and shifts them to the end of the byte (towards the most significant part).

        final byte highByte =
            (byte)((data.getByte(byteIndex + 1) & (~lowByteMask & 0xFF)) << (BITS_IN_BYTE - excludedLowBits));

        // Unites the low and high parts and cuts bits to be excluded with help of a mask
        // (in case if we are addressing an incomplete high byte).

        return (byte) ((highByte | lowByte) & getByteBitMask(index));
    }

    /**
     * {@inheritDoc}
     */
    
    @Override
    public void setByte(int index, byte value)
    {
        final int beginBytePos = getBeginBytePos();
        final int endBytePos   = getEndBytePos();

        final int byteIndex    = beginBytePos + index;

        assert ((0 <= index) && (index < getByteSize()));
        assert ((0 <= byteIndex) && (byteIndex < data.getByteSize()));

        final boolean isHighByteMaskApplied = (byteIndex == endBytePos) && (0 != highByteMask);

        // If there is no mask for the low bytes this means that data
        // is aligned by bytes (start position is multiple of 8) and no byte split is needed.

        if (0 == lowByteMask)
        {
            // If this is the highest byte of the mapping, it might be incomplete. In this case,
            // we need to preserve the excluded part of the target byte from overwriting.  

            final byte prevValue = (byte)(isHighByteMaskApplied ? (data.getByte(byteIndex) & ~highByteMask) : 0);

            // Excludes the redundant bits from the value and unites it with the initial value
            // part to be preserved.

            data.setByte(byteIndex, (byte)(prevValue | (value & getByteBitMask(index))) );
            return;
        }

        // Forms the mask to preserve previous values of bits that are not affected by 
        // the modification (in incomplete low and high bytes).

        final byte prevValueMask =
            (byte)(isHighByteMaskApplied ? (~lowByteMask | ~highByteMask) & 0xFF : ~lowByteMask & 0xFF);

        // Moves the low part of the specified byte to the high border of the byte
        // and unites the result with the old part of the target byte that should be preserved.
        // Also, we reset all redundant bits that go beyond the border of the high incomplete byte. 

        final byte    prevValue = (byte)(data.getByte(byteIndex) & prevValueMask);
        final byte alignedValue = (byte)((value << excludedLowBits) & 0xFF);

        final byte lowByte =
            (byte)((alignedValue & (isHighByteMaskApplied ? highByteMask : 0xFF)) | prevValue);

        data.setByte(byteIndex, lowByte);

        // If there is not bytes left in the data array
        // (the highest is the current), we don't go further.

        if (byteIndex == endBytePos)
            return;

        // Moves the high part of the parameter byte to the low border (beginning) of the byte and unites
        // it with the high part of the target byte that we want to preserve. Also, in case when the high
        // part of the target byte is limited with the high border of the mask, we reset all excluded bits
        // with a high byte mask. 

        final byte highByte = (byte)
        (
            ((value >>> (BITS_IN_BYTE-excludedLowBits)) & ((byteIndex+1 == endBytePos) ? highByteMask : 0xFF))
            |
            data.getByte(byteIndex+1) & lowByteMask
        );

        data.setByte(byteIndex + 1, highByte);
    }

    private int getBeginBytePos()
    {
        return beginBitPos / BITS_IN_BYTE;
    }

    private int getEndBytePos()
    {
        return (beginBitPos + bitSize - 1) / BITS_IN_BYTE; // Highest bit position / bits in byte

        // Another version of the above code: 
        // return (beginBitPos + bitSize) / BITS_IN_BYTE -
        //     ((0 == ((beginBitPos + bitSize) % BITS_IN_BYTE)) ? 1 : 0);
    }
}
