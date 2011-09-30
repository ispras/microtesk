/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Div32Adapter.java,v 1.1 2008/08/20 17:18:48 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator.div;

import com.unitesk.testfusion.core.generator.Random;
import com.unitesk.testfusion.core.type.SingleType;

/**
 * Adapts <code>FractionIterator</code> to <code>Div32Iterator</code>.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Div32Adapter implements Div32Iterator
{
    /** Fraction iterator. */
    protected FractionIterator iterator;
    
    private int sign;
    private int exponent;
    private int mantissa;
    
    /**
     * Constructor.
     * 
     * @param <code>iterator</code> the fraction iterator.
     */
    public Div32Adapter(FractionIterator iterator)
    {
        this.iterator = iterator;
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to fraction iterator object.
     */
    public Div32Adapter(Div32Adapter r)
    {
        iterator = r.iterator.clone();
    }
    
    /** Initializes the iterator. */
    public void init()
    {
        iterator.init();
    }
    
    /**
     * Checks if the iterator is not exhausted (value is available).
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    public boolean hasValue()
    {
        return iterator.hasValue();
    }

    /**
     * Returns the current fraction.
     * 
     * @return the current fraction.
     */
    public Fraction value()
    {
        return iterator.value();
    }
    
    /** Makes iteration. */
    public void next()
    {
        iterator.next();
    }
    
    /** Stops the iterator. */
    public void stop()
    {
        iterator.stop();
    }
    
    /**
     * Returns the current dividend. Call this method before calling
     * <code>getDivisor()</code>.
     * 
     * @return the current dividend.
     */
    public float getDividend()
    {
        Fraction fraction = iterator.value();
        
        sign = Random.int32_non_negative_less(0x2);
        exponent = Random.int32_range(SingleType.MIN_NORMALIZED_EXPONENT, SingleType.MAX_NORMALIZED_EXPONENT);
        mantissa = (int)fraction.getNumerator();
        
        return SingleType.createSingle(sign, exponent, mantissa);
    }

    /**
     * Returns the current divisor. Call this method after calling
     * <code>getDividend</code>.
     * 
     * @return the current divisor.
     */
    public float getDivisor()
    {
        Fraction fraction = iterator.value();
        
        sign = Random.int32_non_negative_less(0x2);
        exponent = Random.int32_range(SingleType.MIN_NORMALIZED_EXPONENT, exponent);
        mantissa = (int)fraction.getDenominator();
        
        return SingleType.createSingle(sign, exponent, mantissa);
    }

    /**
     * Returns a copy of the adapter.
     * 
     * @return a copy of the adapter.
     */
    public Div32Adapter clone()
    {
        return new Div32Adapter(this);
    }
}
