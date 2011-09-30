/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: ConstructDependencyException.java,v 1.3 2008/08/26 10:00:26 kamkin Exp $
 */

package com.unitesk.testfusion.core.engine;

import com.unitesk.testfusion.core.dependency.Dependency;

/**
 * Exception of postcondition violation of the <code>construct()</code> method
 * of <code>Dependency</code>.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ConstructDependencyException extends RuntimeException
{
    public static final long serialVersionUID = 0L;
    
    /** Default constructor. */
    public ConstructDependencyException() {}
    
    /**
     * Constructor.
     * 
     * @param <code>message</code> the message.
     */
    public ConstructDependencyException(String message)
    {
        super(message);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>dependency</code> the dependency.
     */
    public ConstructDependencyException(Dependency dependency)
    {
        this("Incorrect construction of dependency " + dependency);
    }
}
