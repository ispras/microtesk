/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: SequenceTemplateIterator.java,v 1.4 2009/05/21 12:31:21 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator;

import java.util.HashSet;

import com.unitesk.testfusion.core.model.Instruction;

/**
 * Class <code>SequenceTemplateIterator</code> extends
 * <code>ProductTemplateIterator</code> in what connected with instruction order
 * within a template.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class SequenceTemplateIterator extends ProductTemplateIterator
{
    /**
     * Constructor.
     * 
     * @param <code>templateSize</code> the template size.
     */
    public SequenceTemplateIterator(int templateSize)
    {
        super(templateSize);
    }
    
    /** Default constructor. Template size is assumed to be one. */
    public SequenceTemplateIterator()
    {
        this(1);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to sequence template iterator object.
     */
    protected SequenceTemplateIterator(SequenceTemplateIterator r)
    {
        super(r);
    }

    /**
     * Checks the correctness of the instruction order.
     * 
     * @return <code>true</code> if the order is correct; <code>false</code>
     *         otherwise.
     */
    protected boolean checkSequence()
    {
        int i, j, size;
        
        size = template.length;
        for(i = 0; i < size; i++)
        {
            j = template[i];
            
            EquivalenceClass equivalenceClass = getEquivalenceClass(j);
            Instruction instruction = equivalenceClass.get(index[j]  % equivalenceClass.size());
            
            HashSet<Integer> indexes = order.get(instruction.getName());
            
            if(indexes != null && !indexes.contains(new Integer(i)))
                { return false; }
        }
        
        return true;
    }
    
    /** Initializes the iterator. */
    public void init()
    {
        super.init();
        
        while(hasValue() && !checkSequence()) { super.next(); } 
    }
    
    /** Makes iteration. */
    public void next()
    {
        do { super.next(); } while(hasValue() && !checkSequence());
    }
    
    /**
     * Returns a copy of the sequence template iterator.
     * 
     * @return a copy of the sequence template iterator.
     */
    public SequenceTemplateIterator clone()
    {
        return new SequenceTemplateIterator(this);
    }
}
