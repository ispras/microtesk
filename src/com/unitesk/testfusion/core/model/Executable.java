/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Executable.java,v 1.2 2008/08/18 14:45:54 kamkin Exp $
 */

package com.unitesk.testfusion.core.model;

/**
 * General interface of objects that can be executed. 
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface Executable
{
    /**
     * Executes the executable object on the processor.
     * 
     * @param <code>processor</code> the processor.
     */
    public void execute(Processor processor);
}
