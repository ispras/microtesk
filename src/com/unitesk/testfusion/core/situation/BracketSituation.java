/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: BracketSituation.java,v 1.2 2008/08/25 10:32:24 kamkin Exp $
 */

package com.unitesk.testfusion.core.situation;

/**
 * Test situation interface for bracket instructions, like BEGIN and END. 
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface BracketSituation
{
    /**
     * Sets the order number of the bracket.
     * 
     * @param <code>i</code> the order number of the bracket.
     */
    public void setBracketNumber(int i);
}
