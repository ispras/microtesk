/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * RawDataAlgorithm.java, Oct 17, 2012 4:44:59 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.rawdata;

public final class RawDataAlgorithm
{
    private RawDataAlgorithm() {}

    public static interface IUnaryOperation
    {
        public char run(char v);
    }
    
    public static interface IBinaryOperation
    {
        public char run(char lhs, char rhs);
    }
    
    public static interface IOperation
    {
        public char run();
    }
    
    public static interface IAction
    {
        public void run(char v);
    }
    
    public static void fill(RawData dest, char value)
    {
        assert null != dest;

        for (int index = 0; index < dest.getByteSize(); ++index)
        {
            dest.setByte(index, value);
        }
    }
    
    public static void generate(RawData dest, IOperation op)
    {
        assert null != dest;
        
        for (int index = 0; index < dest.getByteSize(); ++index)
        {
            dest.setByte(index, op.run());
        }        
    }
    
    public static void copy(RawData src, RawData dest)
    {
        assert null != src;
        assert null != dest;
        assert src != dest;
        assert src.getBitSize() == dest.getBitSize();

        for (int index = 0; index < dest.getByteSize(); ++index)
        {
            dest.setByte(index, src.getByte(index));
        }
    }
    
    public static void copy(RawData src, int srcPos, RawData dest, int destPos, int bitSize)
    {
        assert null != src;
        assert null != dest;
        
        final RawData srcMapping  = new RawDataMapping(src, srcPos, bitSize);
        final RawData destMapping = new RawDataMapping(dest, destPos, bitSize);
        
        copy(srcMapping, destMapping);
    }
    
    public static void for_each(RawData src, IAction op)
    {
        assert null != src;
        
        for (int index = 0; index < src.getByteSize(); ++index)
        {
            op.run(src.getByte(index));         
        }        
    }
    
    public static void for_each_reverse(RawData src, IAction op)
    {
        assert null != src;

        for (int index = src.getByteSize() - 1; index >= 0; --index)
        {
            op.run(src.getByte(index));         
        }        
    }
    
    public static int mismatch_reverse(RawData src1, RawData src2)
    {
        assert null != src1;
        assert null != src2;
        assert src1 != src2;
        assert src1.getBitSize() == src2.getBitSize();

        for (int index = src1.getByteSize() - 1; index >= 0; --index)
        {
            if (src1.getByte(index) != src2.getByte(index))
                return index;
        }

        return -1;
    }
    
    public static void transform(RawData src, RawData dest, IUnaryOperation op)
    {
        assert null != src;
        assert null != dest;
        assert null != op;
        assert src.getBitSize() == dest.getBitSize();

        for (int index = 0; index < dest.getByteSize(); ++index)
        {
            dest.setByte(index, op.run(src.getByte(index)));
        }
    }

    public static void transform(RawData src1, RawData src2, RawData dest, IBinaryOperation op)
    {
        assert null != src1;
        assert null != src2;
        assert null != dest;
        assert null != op;
        assert (src1.getBitSize() == dest.getBitSize()) && (src2.getBitSize() == dest.getBitSize());

        for (int index = 0; index < dest.getByteSize(); ++index)
        {
            dest.setByte(index, op.run(src1.getByte(index), src2.getByte(index)));
        }
    }
}
