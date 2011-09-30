/* 
 * Copyright (c) 2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: BranchTraceSegments.java,v 1.2 2009/03/19 11:37:54 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator.branch;

import java.util.ArrayList;

/**
 * Sequence of trace segments.
 *  
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class BranchTraceSegments extends ArrayList<BranchTraceSegment>
{
    public static final long serialVersionUID = 0;

    /** Default constructor. */
    public BranchTraceSegments() {}
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to an trace segments object.
     */
    public BranchTraceSegments(BranchTraceSegments r)
    {
        super(r);
    }
    
    /**
     * Returns a copy of the trace segments object.
     * 
     * @return a copy of the trace segments object.
     */
    public BranchTraceSegments clone()
    {
        return new BranchTraceSegments(this);
    }
}
