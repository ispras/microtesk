/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: SituationRandomizator.java,v 1.2 2009/08/13 15:54:18 kamkin Exp $
 */

package com.unitesk.testfusion.core.situation;

import com.unitesk.testfusion.core.generator.Random;

/**
 * Composite situation that randomizes basic situations according to their weights.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class SituationRandomizator extends CompositeSituation
{
    /** Default constructor. */
    public SituationRandomizator() {}

    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the situation randomizator object.
     */
    protected SituationRandomizator(SituationRandomizator r)
    {
        super(r);
    }
    
    @Override
    public void init()
    {
        super.init();
        
        currentSituation = Random.int32_non_negative_less(countSituation());
    }
    
    @Override
    public void nextSituation()
    {
        hasValue = false;
    }

    @Override
    public SituationRandomizator clone()
    {
        return new SituationRandomizator(this);
    }
}
