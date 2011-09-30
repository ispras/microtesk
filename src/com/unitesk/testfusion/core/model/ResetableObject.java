/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: ResetableObject.java,v 1.2 2008/08/19 10:59:27 kamkin Exp $
 */

package com.unitesk.testfusion.core.model;

/**
 * Interface of an object that can reset its state.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface ResetableObject extends StatefulObject
{
    /** Resets the state of the object. */
    public void reset();
}
