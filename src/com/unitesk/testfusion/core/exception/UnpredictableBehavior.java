/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: UnpredictableBehavior.java,v 1.2 2008/08/15 10:08:59 kamkin Exp $
 */

package com.unitesk.testfusion.core.exception;

/**
 * Pseudo-exception that represents unpredictable behavior.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class UnpredictableBehavior extends PseudoException
{
    /** Default constructor. */
    public UnpredictableBehavior()
    {
        super("Unpredictable Behavior");
    }
}
