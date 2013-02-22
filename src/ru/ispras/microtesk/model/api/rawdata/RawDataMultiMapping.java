/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * RawDataMultiMapping.java, Oct 25, 2012 6:34:33 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.rawdata;

import java.util.ArrayList;

/**
 * The RawDataMultiMapping class implements logic that allows concatenating several data
 * objects together (allows accessing a group of data objects as a single data object).  
 * 
 * <pre>
 * The scheme blow demonstrates mapping of two 27-bit data arrays:
 * 
 * Initial data:
 * 
 * Byte:
 * 4        3        2        1        0  4       3        2         1        0
 *
 * Bit:
 * 32 28!   24       16        8       0  32 28!  24       16        8        0
 * _____________________________________  ____________________________________
 * |    !    |        |        |        | |    !   |        |        |        |
 * |%%%%!    |        |        |        | |%%%%!   |        |        |        |
 * |%%%%!    |        |        |        | |%%%%!   |        |        |        |
 * |____!____|________|________|________| |____!___|________|________|________|
 * 
 * <---------------------------------->  <----------------------------------->
 *                Raw Data 2                         Raw Data 1
 *                
 * Mapped view:
 * 
 * Byte:
 * 7         6        5        4         3        2        1        0
 *
 * Bit:
 * 56  54!   48       40       32   28!  24       16       8        0
 * _________________________________________________________________
 * |   !     |        |        |     !   |        |        |        |
 * |%%%!     |        |        |     !   |        |        |        |
 * |%%%!     |        |        |     !   |        |        |        |
 * |___!_____|________|________|_____!___|________|________|________|
 * 
 *  <-------------------------------><------------------------------>
 *          Raw Data 2                       Raw Data 1
 *     <----><-----------------><----><--><------------------------->
 *      Tail        Head          Cut Tail          Head 
 *                             <--------->  
 *                             Linking Byte
 * </pre>
 * 
 * @author Andrei Tatarnikov
 */

public class RawDataMultiMapping extends RawData
{
    /**
     * The LinkingByteMapping class is a special mapping that helps concatenate 
     * two data object. It is needed to concatenate the incomplete high byte of one
     * data array with the complementary low part of the another. Both part together 
     * should make up a complete single byte.  
     * 
     * @author Andrei Tatarnikov
     */
    
    private static final class LinkingByteMapping extends RawData
    {
        private final RawData lowPart;
        private final RawData highPart;
        
        /**
         * Creates a LinkingByteMapping object from two parts. 
         * 
         * @param lowPart A low part of the linking byte (a raw data object, which size is less that 8). 
         * @param highPart A high part of the linking byte (a raw data object, which size is less that 8).
         */

        public LinkingByteMapping(RawData lowPart, RawData highPart)
        {
            assert ((0 < lowPart.getBitSize()) && (lowPart.getBitSize() < BITS_IN_BYTE));
            assert ((0 < highPart.getBitSize()) && (highPart.getBitSize() < BITS_IN_BYTE));
            assert ((lowPart.getBitSize() + highPart.getBitSize()) <= BITS_IN_BYTE); 

            this.lowPart = lowPart;
            this.highPart = highPart;
        }
        
        /**
         * {@inheritDoc}
         */

        @Override
        public int getBitSize()
        { 
            return lowPart.getBitSize() + highPart.getBitSize();
        }
        
        /**
         * {@inheritDoc}
         * NOTE: The number of bytes a LinkingByteMapping object can store always equals to 1.  
         */

        @Override
        public int getByteSize()
        { 
            return 1;
        }

        /**
         * {@inheritDoc}
         * NOTE: A LinkingByteMapping object always stores 1-byte data and, consequently,
         * accepts only 0 as the value of the index parameter.
         */
        
        @Override
        public char getByte(int index)
        {
            assert (0 == index) : "ONE-BYTE DATA ARRAY!";

            final char  lowValue = lowPart.getByte(0);
            final char highValue = highPart.getByte(0);
            
            final char result =
                (char)(((highValue << lowPart.getBitSize()) | lowValue) & getByteBitMask(0)); 

            return result;
        }
        
        /**
         * {@inheritDoc}
         * NOTE: A LinkingByteMapping object always stores 1-byte data and, consequently,
         * accepts only 0 as the value of the index parameter.
         */

        @Override
        public void setByte(int index, char value)
        {
            assert (0 == index) : "ONE-BYTE DATA ARRAY!";

            final char lowValue =
                (char)((value & ~(DEF_BYTE_MASK << lowPart.getBitSize())));
            
            final char highValue =
                (char)((value >> lowPart.getBitSize()) & ~(DEF_BYTE_MASK << highPart.getBitSize()));

            lowPart.setByte(0, lowValue);
            highPart.setByte(0, highValue);            
        }
    }
    
    /**
     * The ByteAccessor class is aimed to provide access to an arbitrary byte
     * of the mapping. The class encapsulates the real data source
     * (part of the mapping that actually contains the needed byte) and
     * the relative index of the byte in that data source.   
     * 
     * @author Andrei Tatarnikov
     */
    
    private static final class ByteAccessor
    {
        public final RawData data;
        public final int     index;
        
        /**
         * Creates an instance of the ByteAccessor class. 
         * 
         * @param data Data array that hold the specified byte.
         * @param index The relative index of the byte in the data array.
         */

        public ByteAccessor(RawData data, int index)
        {
            this.data  = data;
            this.index = index;
        }
        
        /**
         * Returns the value of the byte the accessor refers to. 
         * @return A target byte value. 
         */

        public char getByte()
        {
            return data.getByte(index);
        }
        
        /**
         * Sets the value of the byte the accessor refers to. 
         * @param value The value to be assign to the target byte.
         */

        public void setByte(char value)
        {
            data.setByte(index, value);
        }
    }

    final ArrayList<ByteAccessor> byteAccessors;
    final int bitSize;

    /**
     * Adds byte accessors for the specified data array to the vector of byte
     * accessors that build up the mapping. Returns the size of processed data in bits. 
     * 
     * @param data The data array to be mapped to the vector of byte accessors. 
     * @return The size of processed data in bits (number of bits in the source data array).
     */
    
    private int addByteAcessors(RawData data)
    {
        for (int index = 0; index < data.getByteSize(); ++index)
            byteAccessors.add(new ByteAccessor(data, index));

        return data.getBitSize();
    }
    
    public RawDataMultiMapping(RawData[] dataArray)
    {
        byteAccessors = new ArrayList<ByteAccessor>();

        RawData unusedPrevPart = null;
        int   processedBitSize = 0;

        for (RawData data : dataArray)
        {
            int offset = 0;
            if (null != unusedPrevPart)
            {
                final int bitsToCompleteByte = BITS_IN_BYTE - unusedPrevPart.getBitSize();
                assert bitsToCompleteByte > 0;

                final RawData currentCutPart = (data.getBitSize() <= bitsToCompleteByte) ?
                    data : new RawDataMapping(data, 0, bitsToCompleteByte);                    

                final RawData linkingBlock = new LinkingByteMapping(unusedPrevPart, currentCutPart);
                if (linkingBlock.getBitSize() < BITS_IN_BYTE)
                {
                    unusedPrevPart = linkingBlock;
                }
                else
                {
                    processedBitSize += addByteAcessors(linkingBlock);
                    unusedPrevPart = null;
                }

                offset = currentCutPart.getBitSize();
            }

            final int dataSize = data.getBitSize() - offset;
            if (0 == dataSize) continue;

            /*
             * We split data in the current data object in to parts (excluding the part that was taken
             * to create a linking byte with the unused part of the previous data object): head and tail.
             * The head size is proportional to the number of bits in a byte. We create for it corresponding
             * entries in the byte accessor vector. Tail contains the rest of the data. Its size is ALLWAYS
             * LESS than the number of bits in a byte. This is the unused part of the current data object to
             * be used to build a linking byte with the lowest part of the next data object. 
             */

            final int headBitSize = (dataSize / BITS_IN_BYTE) * BITS_IN_BYTE;
            final int tailBitSize = dataSize % BITS_IN_BYTE;

            if (0 != headBitSize)
            {
                final boolean headTakesAllData = (0 == offset) && (0 == tailBitSize);
                final RawData currentBlock = headTakesAllData ? data : new RawDataMapping(data, offset, headBitSize);

                processedBitSize += addByteAcessors(currentBlock);
            }

            if (0 != tailBitSize)
            {
                final boolean tailTakesAllData = (0 == offset) && (0 == headBitSize);
                unusedPrevPart = tailTakesAllData ? data : new RawDataMapping(data, offset + headBitSize, tailBitSize);
            }
        }
        
        // If any unused data is left, we process it.
        if (null != unusedPrevPart)
            processedBitSize += addByteAcessors(unusedPrevPart);

        this.bitSize = processedBitSize;
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
        return byteAccessors.size();
    }
    
    /**
     * {@inheritDoc}
     */

    @Override
    public char getByte(int index)
    {
        assert ((0 <= index) && (index < byteAccessors.size()));

        final ByteAccessor accessors = byteAccessors.get(index);
        return accessors.getByte();
    }
    
    /**
     * {@inheritDoc}
     */

    @Override
    public void setByte(int index, char value)
    {
        assert ((0 <= index) && (index < getByteSize()));
        
        final ByteAccessor accessors = byteAccessors.get(index);
        accessors.setByte(value);
    }
}
