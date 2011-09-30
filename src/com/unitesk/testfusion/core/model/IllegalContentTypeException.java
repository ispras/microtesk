/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: IllegalContentTypeException.java,v 1.2 2008/08/18 14:45:54 kamkin Exp $
 */

package com.unitesk.testfusion.core.model;

/**
 * Illegal content type exception.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class IllegalContentTypeException extends RuntimeException
{
    public static final long serialVersionUID = 0L;
    
    /** Default constructor. */
    public IllegalContentTypeException() {}
    
    /**
     * Constructor.
     * 
     * @param <code>message</code> the message.
     */
    public IllegalContentTypeException(String message)
    {
        super(message);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>content</code> the content type.
     */
    public IllegalContentTypeException(ContentType content)
    {
        this("Illegal content type " + content.getName());
    }
}
