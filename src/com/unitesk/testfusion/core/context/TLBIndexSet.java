/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: TLBIndexSet.java,v 1.1 2009/04/21 10:25:33 kamkin Exp $
 */

package com.unitesk.testfusion.core.context;

import java.util.ArrayList;

import com.unitesk.testfusion.core.generator.Random;

/**
 * Set of registers associated with a certain register type.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class TLBIndexSet extends ArrayList<Integer>
{
    private static final long serialVersionUID = 1L;
    
    /** Default constructor. */
    public TLBIndexSet() {}
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to a TLB index set object.
     */
    protected TLBIndexSet(TLBIndexSet r)
    {
       super(r); 
    }
    
    /**
     * Returns random TLB index.
     * 
     * @return random TLB index.
     */
    public int randomTLBIndex()
    {
        return get(Random.int32_non_negative_less(size()));
    }
    
    /**
     * Returns a copy of the TLB index set object.
     * 
     * @return a copy of the TLB index set object.
     */
    public TLBIndexSet clone()
    {
        return new TLBIndexSet(this);
    }
}
