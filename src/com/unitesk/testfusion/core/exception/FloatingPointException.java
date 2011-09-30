/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: FloatingPointException.java,v 1.2 2008/08/26 12:17:51 kamkin Exp $
 */

package com.unitesk.testfusion.core.exception;

/**
 * Floating-point exception.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class FloatingPointException extends ProcessorException
{
    /** None exception. */
    public static final int NONE = -1;
    
    /** Invalid operation exception. */
    public static final int INVALID = 0;
    
    /** Division by zero exception. */
    public static final int DIVISION_BY_ZERO = 1;
    
    /** Overflow exception. */
    public static final int OVERFLOW = 2;
    
    /** Underflow exception. */
    public static final int UNDERFLOW = 3;
    
    /** Inexact exception. */
    public static final int INEXACT = 4;

    /**
     * Converts the integer code of exception into the string.
     * 
     * @param <code>exception</code> the integer code of exception.
     * 
     * @return a string representation of the exception.
     */
    public static String toString(int exception)
    {
        switch(exception) 
        {
            case INVALID :
                { return "Invalid"; }
            case DIVISION_BY_ZERO:
                { return "DivisionByZero"; }
            case OVERFLOW:
                { return "Overflow"; }
            case UNDERFLOW:
                { return "Underflow"; }
            case INEXACT:
                { return "Inexact"; }
            default:
                { return "None"; }
        }
    }
    
    /** Flag of invalid operation exception. */
    protected boolean v;

    /** Flag of division by zero exception. */
    protected boolean z;

    /** Flag of overflow exception. */
    protected boolean o;

    /** Flag of underflow exception. */
    protected boolean u;

    /** Flag of inexact exception. */
    protected boolean i;
    
    /**
     * Constructor.
     * 
     * @param <code>v</code> the flag of invalid operation exception.
     * 
     * @param <code>z</code> the flag of division by zero exception.
     * 
     * @param <code>o</code> the flag of overflow exception.
     * 
     * @param <code>u</code> the flag of underflow exception.
     * 
     * @param <code>i</code> the inexact exception.
     */
    public FloatingPointException(boolean v, boolean z, boolean o, boolean u, boolean i, int type)
    {
        super("Floating Point Exception", POST_EXCEPTION);
        
        this.v = v;
        this.z = z;
        this.o = o;
        this.u = u;
        this.i = i;
    }
    
    /**
     * Constructor.
     * 
     * @param <code>exception</code> the integer code of exception.
     */
    public FloatingPointException(int exception)
    {
        super("Floating Point Exception", POST_EXCEPTION);
        
        switch(exception)
        {
        case INVALID:
            { this.v = true; return; }
        case DIVISION_BY_ZERO:
            { this.z = true; return; }
        case OVERFLOW:
            { this.o = this.i = true; return; }
        case UNDERFLOW:
            { this.u = this.i = true; return; }
        case INEXACT:
            { this.i = true; return; }
        }
    }

    /**
     * Returns the flag of invalid operation exception.
     * 
     * @return the flag of invalid operation exception.
     */
    public boolean isInvalid()
    {
        return v;
    }
    
    /**
     * Returns the flag of division by zero exception.
     * 
     * @return the flag of division by zero exception.
     */
    public boolean isDivisionByZero()
    {
        return z;
    }

    /**
     * Returns the flag of overflow exception.
     * 
     * @return the flag of overflow exception.
     */
    public boolean isOverflow()
    {
        return o;
    }

    /**
     * Returns the flag of underflow exception.
     * 
     * @return the flag of underflow exception.
     */
    public boolean isUnderflow()
    {
        return u;
    }

    /**
     * Returns the flag of inexact exception.
     * 
     * @return the flag of inexact exception.
     */
    public boolean isInexact()
    {
        return i;
    }
}
