/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: ContentIterator.java,v 1.2 2009/11/02 15:31:50 vorobyev Exp $
 */

package com.unitesk.testfusion.core.template.content;

import com.unitesk.testfusion.core.dependency.Dependency;
import com.unitesk.testfusion.core.model.Program;
import com.unitesk.testfusion.core.template.DependencyIterator;

/**
 * Iterator of content dependencies between instructions of test template.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ContentIterator extends DependencyIterator
{
    /** Flag that indicates if the dependencies are enabled or not. */ 
    protected boolean enabled;
    
    /**
     * Constructor.
     * 
     * @param <code>dependency</code> the content dependency.
     * 
     * @param <code>enabled</code> the enabling status.
     */
    public ContentIterator(Dependency dependency, boolean enabled)
    {
        super(dependency);

        this.enabled = enabled;
    }
    
    /**
     * Constructor.
     * 
     * @param <code>dependency</code> the content dependency.
     */
    public ContentIterator(Dependency dependency)
    {
        this(dependency, true);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to content iterator.
     */
    protected ContentIterator(ContentIterator r)
    {
        super(r);
        
        enabled = r.enabled;
    }
    
    /**
     * Checks if the iterator is enabled or not.
     * 
     * @return <code>true</code> if the iterator is enabled;
     *         <code>false</code> otherwise.
     */
    public boolean isEnabled()
    {
        return enabled;
    }
    
    /**
     * Enables/disables the iterator.
     * 
     * @param <code>enabled</code> the enabling status.
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
    
    /** Initializes the iterator of dependencies. */
    public void init()
    {
    	if(isEnabled())
    	    { super.init(); }
    }
    
    /**
     * Processes test template (pre-initialization of dependency iterator).
     * 
     * @param <code>template</code> the test template.
     */
    public void process(Program template)
    {
        if(isEnabled())
            { super.process(template); }
    }

    /**
     * Processes test template (pre-initialization of the cross-section
     * dependency iterator).
     * 
     * @param <code>template</code> the test template.
     * 
     * @param <code>start1</code> the start position of the determinant section.
     * 
     * @param <code>end1</code> the end position of the determinant section.
     * 
     * @param <code>start2</code> the start position of the dependent section.
     * 
     * @param <code>end2</code> the end position of the dependent section.
     */
    public void process(Program template, int start1, int end1, int start2, int end2)
    {
        if(isEnabled())
            { super.process(template, start1, end1, start2, end2); }
    }
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public ContentIterator clone()
    {
        return new ContentIterator(this);
    }
}
