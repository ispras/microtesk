/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: AssociativeCache.java,v 1.4 2008/08/15 09:50:53 kamkin Exp $
 */

package com.unitesk.testfusion.core.model.cache;

import com.unitesk.testfusion.core.model.Processor;

/**
 * Abstract class <code>AssociativeCache</code> represents a fully associative
 * cache memory.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class AssociativeCache extends Cache
{
    /**
     * Constructor.
     * 
     * @param <code>processor</code> the processor object.
     */
    public AssociativeCache(Processor processor)
    {
        super(processor);
    }
}
