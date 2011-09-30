/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: DivNearestRoundIterator.java,v 1.1 2008/08/20 17:18:48 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.div;

import com.unitesk.testfusion.core.iterator.div.Fraction;

/**
 * Class <code>DivNearestRoundIterator</code> implements iteration of
 * hard-to-round cases for the division operation in round-to-nearest mode.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class DivNearestRoundIterator implements FractionIterator
{
    /** Precision of the floating-point numbers. */
    protected int precision;
    
    /** Upper unit of the fraction. */
    protected long fraction_upper_unit;
    
    /** Tacit unit of the mantissa. */
    protected long mantissa_tacit_unit;
    
    /** Flag that refrects availability of the value. */
    protected boolean hasValue;
    
    /** Current value. */
    protected Fraction value = new Fraction();
    
    /** Temporal fraction. */
    protected Fraction fraction = new Fraction();
    
    /**
     * Constructor.
     * 
     * @param <code>precision</code> the precision.
     */
    public DivNearestRoundIterator(int precision)
    {
        this.precision = precision;
        
        init();
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the iterator.
     */
    protected DivNearestRoundIterator(DivNearestRoundIterator r)
    {
        precision = r.precision;
        fraction_upper_unit = r.fraction_upper_unit;
        mantissa_tacit_unit = r.mantissa_tacit_unit;
        hasValue = r.hasValue;
        value = r.value.clone();
        fraction = r.value.clone();
    }
	
    public void init()
    {
        mantissa_tacit_unit = 1L << precision;

        fraction.n = mantissa_tacit_unit - 3;
        fraction.d = 3;
        
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
        if(!hasValue)
            { return false; }
        
        return hasValue = (fraction.d < (1 << ((precision + 1) >> 1)));
    }

    /**
     * Returns the current value.
     * 
     * @return the current value.
     */
	public Fraction value()
	{
        if(value != null)
            { return value; }
    
	    Fraction r  = new Fraction();
        
	    Fraction sp = new Fraction();
	    Fraction cp = new Fraction();
	    
        fraction.parentPartition(sp, cp);

        r.d = fraction.n;
        r.n = (sp.n & 1L) == 1 ? fraction.n + sp.n : (fraction.n << 1) - sp.n;

        long lhs = r.n << (63 - precision);
        long rhs = r.d << (64 - precision);
        
        while((lhs & 0x8000000000000000L) == 0 && lhs != 0L)
             { lhs <<= 1; }

        while((rhs & 0x8000000000000000L) == 0 && rhs != 0L)
             { rhs <<= 1; }

        lhs <<= 1;
        rhs <<= 1;
        
        value.n = lhs >>> (65 - precision);
        value.d = rhs >>> (65 - precision);
        
	    return value;
	}

    /** Makes iteration. */
	public void next()
	{
        if(!hasValue)
            { return; }
        
        value = null;
        
	    fraction.n -= 2;
	    fraction.d += 2;
	}
    
    /** Stops the iterator. */
    public void stop()
    {
        value = null;
        
        hasValue = false;
    }
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public DivNearestRoundIterator clone()
    {
        return new DivNearestRoundIterator(this);
    }
}