/* 
 * Copyright (c) 2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: BranchTraceSegment.java,v 1.3 2009/03/26 17:23:13 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator.branch;

import java.util.HashSet;

import com.unitesk.testfusion.core.generator.Random;

/**
 * Trace segment (sequence of basic blocks between two consecutive executions
 * of a branch instruction).
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class BranchTraceSegment extends HashSet<Integer>
{
    public static final long serialVersionUID = 0;
    
    /** Default constructor. */
    public BranchTraceSegment() {}
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to a trace segment object.
     */
    public BranchTraceSegment(BranchTraceSegment r)
    {
        super(r);
    }
    
    /**
     * Returns random block of the trace segment.
     * 
     * @return random block of the trace segment.
     */
    public int randomBlock()
    {
        return (Integer)toArray()[Random.int32_non_negative_less(size())];        
    }
    
    /**
     * Returns a copy of the trace segment object.
     * 
     * @return a copy of the trace segment object.
     */
    public BranchTraceSegment clone()
    {
        return new BranchTraceSegment(this);
    }
}
