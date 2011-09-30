/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: SetTemplateIterator.java,v 1.3 2009/11/12 13:28:30 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator;

/**
 * Class <code>SetTemplateIterator</code> is simplified version of
 * <code>MultisetTemplateIterator</code>, where <code>minRepetition</code> is
 * zero and <code>maxRepetition</code> is one. 
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class SetTemplateIterator extends MultisetTemplateIterator
{
    /**
     * Constructor.
     * 
     * @param <code>minSize</code> the minimum size of test template.
     * 
     * @param <code>maxSize</code> the maximum size of test template.
     */
    public SetTemplateIterator(int minSize, int maxSize)
    {
        super(0, 1, minSize, maxSize);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>size</code> the template size.
     */
    public SetTemplateIterator(int size)
    {
        super(0, 1, size);
    }

    /** Default constructor. Template size is not fixed. */
    public SetTemplateIterator()
    {
        super(0, 1);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to a set template iterator object.
     */
    protected SetTemplateIterator(SetTemplateIterator r)
    {
        super(r);
    }
    
    /**
     * Returns a copy of the set template iterator.
     * 
     * @return a copy of the set template iterator.
     */
    @Override
    public SetTemplateIterator clone()
    {
        return new SetTemplateIterator(this);
    }
}
