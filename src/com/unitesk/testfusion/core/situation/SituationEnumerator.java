/* 
 * Copyright (c) 2007-2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: SituationEnumerator.java,v 1.2 2009/08/13 15:54:18 kamkin Exp $
 */

package com.unitesk.testfusion.core.situation;

/**
 * Composite situation that iterates basic situations.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class SituationEnumerator extends CompositeSituation
{
    /** Default constructor. */
    public SituationEnumerator() {}
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the composite situation object.
     */
    protected SituationEnumerator(SituationEnumerator r)
    {
        super(r);
    }
    
    @Override
    public void nextSituation()
    {
        if(++currentSituation >= countSituation())
            { hasValue = false; }
    }

    @Override
    public SituationEnumerator clone()
    {
        return new SituationEnumerator(this);
    }
}
