/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Procedure.java,v 1.2 2008/08/19 10:59:27 kamkin Exp $
 */

package com.unitesk.testfusion.core.model;

/**
 * Class that represents a procedure.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Procedure
{
    /** Procedure call. */
    protected Program call;
    
    /** Procedure body. */
    protected Program body;

    /**
     * Constructor.
     * 
     * @param <code>call</code> the procedure call.
     * 
     * @param <code>body</code> the procedure body.
     */
    public Procedure(Program call, Program body)
    {
        this.call = call;
        this.body = body;
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to procedure object.
     */
    protected Procedure(Procedure r)
    {
        call = r.call.clone();
        body = r.body.clone();
    }
    
    /**
     * Returns the procedure call.
     * 
     * @return the procedure call.
     */
    public Program call()
    {
        return call;
    }
    
    /**
     * Returns the procedure body.
     * 
     * @return the procedure body.
     */
    public Program body()
    {
        return body;
    }
    
    /**
     * Returns a copy of the procedure.
     * 
     * @return a copy of the procedure.
     */
    public Procedure clone()
    {
        return new Procedure(this);
    }
}
