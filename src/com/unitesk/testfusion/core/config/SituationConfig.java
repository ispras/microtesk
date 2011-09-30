/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SituationConfig.java,v 1.13 2008/08/22 12:56:26 kamkin Exp $
 */

package com.unitesk.testfusion.core.config;

import com.unitesk.testfusion.core.situation.Situation;

/**
 * Test situation configuration.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class SituationConfig extends SelectionConfig
{
    /** Test situation object. */
    protected Situation situation;
    
    /**
     * Basic constructor.
     * 
     * @param <code>situation</code> the test situation object.
     */
    public SituationConfig(Situation situation)
    {
        super(situation.getName());
        
        this.situation = situation;
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to test situation configuration
     *        object.
     */
    protected SituationConfig(SituationConfig r)
    {
        super(r);
        
        // Test situation object is not copied deeply.
        situation = r.situation;
    }

    /**
     * Returns the fully qualified name of the test situation.
     * 
     * @return the fully qualified name of the test situation.
     */
    public String getFullName()
    {
        return parent.getFullName() + "." + getName();
    }
    
    /**
     * Returns the test situation object.
     * 
     * @return the test situation object.
     */
    public Situation getSituation()
    {
        return situation;
    }
    
    /**
     * Returns a copy of the test situation.
     *
     * @return a copy of the test situation.
     */
    public SituationConfig clone()
    {
        return new SituationConfig(this);
    }
}
