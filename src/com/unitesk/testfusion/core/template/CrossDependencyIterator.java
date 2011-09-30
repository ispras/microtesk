/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: CrossDependencyIterator.java,v 1.3 2008/09/03 09:38:57 kamkin Exp $
 */

package com.unitesk.testfusion.core.template;

import com.unitesk.testfusion.core.model.Program;

/**
 * Iterator of dependencies between two sub-templates. 
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class CrossDependencyIterator extends SectionDependencyIterator
{
    /** Start position of the first sub-template. */
    protected int start1;
    
    /** End position of the first sub-template. */
    protected int end1;
    
    /** Start position of the second sub-template. */
    protected int start2;
    
    /** End position of the second sub-template. */
    protected int end2;
    
    /**
     * Constructor.
     * 
     * @param <code>template</code> the template.
     * 
     * @param <code>start1</code> the start position of the first sub-template.
     * 
     * @param <code>end1</code> the end position of the first sub-template.
     * 
     * @param <code>start2</code> the start position of the second sub-template.
     * 
     * @param <code>end2</code> the end position of the second sub-template.
     */
    public CrossDependencyIterator(int start1, int end1, int start2, int end2)
    {
        super();
        
        if(start1 > end1 || start2 > end2 || end1 >= start2)
            { throw new IllegalArgumentException("Incorrect positions of sub-templates"); }
        
        this.start1 = start1;
        this.end1   = end1;
        
        this.start2 = start2;
        this.end2   = end2;
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to cross dependency iterator.
     */
    protected CrossDependencyIterator(CrossDependencyIterator r)
    {
        super(r);
        
        this.start1 = r.start1;
        this.end1   = r.end1;
        
        this.start2 = r.start2;
        this.end2   = r.end2;
    }
    
    /** Builds the set of dependency instances to be iterated. */
    public void process(Program template)
    {
        int i, size;
        
        this.template = template;
        
        size = iterators.size();
        for(i = 0; i < size; i++)
        {
            DependencyIterator iterator = iterators.get(i);
            
            iterator.process(template, start1, end1, start2, end2);
        }
    }
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public CrossDependencyIterator clone()
    {
        return new CrossDependencyIterator(this);
    }
}
