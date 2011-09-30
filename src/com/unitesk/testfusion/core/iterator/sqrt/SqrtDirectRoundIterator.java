/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: SqrtDirectRoundIterator.java,v 1.1 2008/08/20 17:19:02 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.sqrt;

import com.unitesk.testfusion.core.iterator.Int32Iterator;
import com.unitesk.testfusion.core.iterator.Int32RangeIterator;
import com.unitesk.testfusion.core.iterator.Int64Iterator;

/**
 * Class <code>SqrtDirectRoundIterator</code> implements iteration of
 * hard-to-round cases for the square root operation in direct rounding mode.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class SqrtDirectRoundIterator extends Int64Iterator
{
    private class KIterator extends Int32Iterator
    {
        private int max;
        private int k;
        private int sign;
        private boolean hasValue;
        
        public KIterator(int max)
        {
            this.max = max;
        }
        
        public KIterator(KIterator r)
        {
            max = r.max;
            k = r.k;
            sign = r.sign;
            hasValue = r.hasValue;
        }
        
        public void init()
        {
            sign = k = 1;
            
            hasValue = true;
        }

        public boolean hasValue()
        {
            if(!hasValue)
                { return false; }
            
            return k <= max;
        }

        public int int32Value()
        {
            return sign != 0 ? k : -k + 2;
        }

        public void next()
        {
            if(!hasValue)
                { return; }
            
            if(sign == 1 && k > 1)
                { sign = 0; return; }

            k += 8;
            sign = 1;
        }
        
        public void stop()
        {
            hasValue = false;
        }
        
        public KIterator clone()
        {
            return new KIterator(this);
        }
    }
    
    private long i_n;
    private long r_n;
    private long i_n_1;
    private long r_n_1;

    private long mantissa_tacit_unit;
    private long fraction_upper_unit;
    private long fraction_upper_but_one_unit;
    private long fraction_upper_but_two_unit;

    private KIterator kIterator = new KIterator(1024);
    private Int32RangeIterator testIterator = new Int32RangeIterator(0, 3); 
    
    /** Precision of the floating-point numbers. */
    protected int precision;

    /** Flag that refrects availability of the value. */
    protected boolean hasValue;

    /**
     * Constructor.
     * 
     * @param <code>precision</code> the precision.
     */
    public SqrtDirectRoundIterator(int precision)
    {
        this.precision = precision;
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the iterator object.
     */
    protected SqrtDirectRoundIterator(SqrtDirectRoundIterator r)
    {
        i_n = r.i_n;
        r_n = r.r_n;
        i_n_1 = r.i_n_1;
        r_n_1 = r.r_n_1;

        mantissa_tacit_unit = r.mantissa_tacit_unit;
        fraction_upper_unit = r.fraction_upper_unit;
        fraction_upper_but_one_unit = r.fraction_upper_but_one_unit;
        fraction_upper_but_two_unit = r.fraction_upper_but_two_unit;

        kIterator = kIterator.clone();
        testIterator = testIterator.clone(); 

        precision = r.precision;
        hasValue = r.hasValue;
    }
    
    /** Initializes the iterator. */
    public void init()
    {
        kIterator.init();
        testIterator.init();
        
        hasValue = true;
    }
    
    /**
     * Checks if the iterator is not exhausted (value is available).
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    public boolean hasValue()
    {
        return hasValue;
    }

    /**
     * Returns the current value (fraction of operand).
     * 
     * @return the current value (fraction of operand).
     */
    public long int64Value()
    {
        long value = 0L;

        switch(testIterator.int32Value())
        {
        case 0:
            { value = fraction_upper_unit + 2 * i_n_1 + r_n_1; }
        case 1:
            { value = mantissa_tacit_unit + fraction_upper_but_two_unit + r_n_1 - 3 * i_n_1; }
        case 2:
            { value = fraction_upper_but_one_unit + i_n + r_n; }
        case 3:
            { value = mantissa_tacit_unit - 2 * i_n + r_n; }
        }
        
        while(((value & 0x8000000000000000L) == 0L) && (value != 0L))
           { value <<=1; }
        
        value <<= 1;
        
        return value;
    }
    
    private void init(int k)
    {
        int n;

        i_n_1 = 1;
        r_n_1 = (1 - k) / 8;

        mantissa_tacit_unit = 1L << precision;

        fraction_upper_unit = mantissa_tacit_unit >> 1;
        fraction_upper_but_one_unit = mantissa_tacit_unit >> 2;
        fraction_upper_but_two_unit = mantissa_tacit_unit >> 3;

        for(n = 4; n <= precision - 1; n++)
        {
            if((r_n_1 & 1L) == 1)
            {
                r_n_1 = (1L << (n - 4)) + (r_n_1 - i_n_1) / 2;
                i_n_1 = (1L << (n - 2)) - i_n_1;
            }
            else
            {
                r_n_1 = r_n_1 / 2;
            }
        }

        if((r_n_1 & 1L) == 1)
        {
            r_n = (1L << (n - 4)) + (r_n_1 - i_n_1) / 2;
            i_n = (1L << (n - 2)) - i_n_1;
        }
        else
        {
            r_n = r_n_1 / 2;
        }
    }

    /** Makes iteration. */
    public void next()
    {
        if(!hasValue())
            { return; }

        if(testIterator.hasValue())
        {
            testIterator.next();
            
            if(testIterator.hasValue())
                { return; }
        }
        
        testIterator.init();
        
        if(kIterator.hasValue())
        {
            kIterator.next();
            
            if(kIterator.hasValue())
                { init(kIterator.int32Value()); return; }
        }
        
        kIterator.init();
        
        stop();
    }
    
    /** Stops the iterator. */
    public void stop()
    {
        hasValue = false;
    }
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public SqrtDirectRoundIterator clone()
    {
        return new SqrtDirectRoundIterator(this);
    }
}
