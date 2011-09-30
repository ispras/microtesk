/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: StatefulObject.java,v 1.7 2008/08/19 10:59:27 kamkin Exp $
 */

package com.unitesk.testfusion.core.model;

/**
 * Interface of a stateful object.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface StatefulObject
{
    /**
     * Returns a copy of the object.
     * 
     * @return a copy of the object.
     */
    public Object clone();
}
