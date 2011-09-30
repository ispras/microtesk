/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: ProcessorException.java,v 1.3 2009/01/30 10:40:02 kozlov Exp $
 */

package com.unitesk.testfusion.core.exception;

/**
 * Abstract class that represents processor exception.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class ProcessorException
{
    /** Pre-exception type (exception occures before instruction is executed). */
    public static final int PRE_EXCEPTION  = 0;
    
    /** Post-exception type (exception occures after instruction is executed). */
    public static final int POST_EXCEPTION = 1;
    
    /** Exception name. */
    protected String name;
    
    /**
     * Exception type. It can possess one of the following values:
     * 
     * <code>PRE_EXCEPTION</code> -
     * pre-exception (exception occures before instruction is executed);
     * 
     * <code>POST_EXCEPTION</code> -
     * post-exception (exception occures after instruction is executed).
     */
    protected int type;
    
    /**
     * Constructor.
     * 
     * @param <code>name</code> the exception name.
     * 
     * @param <code>type</code> the exception type (<code>PRE_EXCEPTION</code>
     *        or <code>POST_EXCEPTION</code>)
     */
    public ProcessorException(String name, int type)
    {
        this.name = name;
        this.type = type;
    }
    
    /**
     * Constructor of pre-exception.
     * 
     * @param <code>name</code> the exception name.
     */
    public ProcessorException(String name)
    {
        this(name, PRE_EXCEPTION);
    }
    
    /**
     * Returns the name of the exception.
     * 
     * @return the name of the exception.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Checks if the exception is pre-exception.
     * 
     * @return <code>true</code> if the exception is pre-exception;
     *         <code>false</code> otherwise.
     */
    public boolean isPreException()
    {
        return type == PRE_EXCEPTION;
    }
    
    /**
     * Checks if the exception is post-exception.
     * 
     * @return <code>true</code> if the exception is pre-exception;
     *         <code>false</code> otherwise.
     */
    public boolean isPostException()
    {
        return type == POST_EXCEPTION;
    }
    
    /**
     * Compares the exception with the other one.
     * 
     * @param  <code>o</code> the exception to be compared.
     * 
     * @return <code>true</code> if exceptions are equal (have the name);
     *         <code>false</code> otherwise. 
     */
    public boolean equals(Object o)
    {
        if(!(o instanceof ProcessorException))
            { return false; }
        
        ProcessorException r = (ProcessorException)o;
        
        return name.equals(r.name);
    }
    
    /**
     * Returns a hash code value for the object.
     */
    public int hashCode()
    {
        return name.hashCode();
    }
    
    /**
     * Returns a string representation of the exception.
     * 
     * @return a string representation of the exception.
     */
    public String toString()
    {
        return getName();
    }
}
