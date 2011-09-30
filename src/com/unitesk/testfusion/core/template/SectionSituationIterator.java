/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: SectionSituationIterator.java,v 1.2 2009/03/26 17:23:09 kamkin Exp $
 */

package com.unitesk.testfusion.core.template;

import com.unitesk.testfusion.core.iterator.Iterator;
import com.unitesk.testfusion.core.model.Instruction;
import com.unitesk.testfusion.core.model.Program;
import com.unitesk.testfusion.core.situation.Situation;

/**
 * Iterator of test template situations. 
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class SectionSituationIterator implements Iterator
{
    /** Test template. */
    protected Program template;
    
    /** Flags that indicates if the iterator of test situations is not exhaused. */
    protected boolean hasValue;

    /**
     * Constructor.
     * 
     * @param <code>template</code> the test template.
     */
    public SectionSituationIterator(Program template)
    {
        this.template = template;
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to iterator of test situations.
     */
    protected SectionSituationIterator(SectionSituationIterator r)
    {
        template = r.template.clone();
        hasValue = r.hasValue;
    }
    
    /** Initializes the iterator of situations. */
    public void init()
    {
    	int i, size;
    	
    	hasValue = true;
    	
    	size = template.countInstruction();
    	for(i = 0; i < size; i++)
    	{
    		Instruction instruction = template.getInstruction(i);
    		Situation situation = instruction.getSituation();
    		
            if(situation != null)
  		    {
                situation.init();
                hasValue &= situation.hasValue();
            }
    	}
    }

    /**
     * Returns the test template annotated with test situations.
     * 
     * @return the test template annotated with test situations.
     */
    public Program value()
    {
    	return template.clone();
    }
    
    /**
     * Checks if the iterator of test situations is not exhausted.
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    public boolean hasValue()
    {
    	return hasValue;
    }
    
    /** Makes iteration of test situations. */
    public void next()
    {
    	int i, size;
    	
    	size = template.countInstruction();
    	for(i = 0; i < size; i++)
    	{
    		Instruction instruction = template.getInstruction(i);
    		Situation situation = instruction.getSituation();
    		
            if(situation == null)
                { continue; }
            
            if(situation.hasValue())
            {
                situation.next();
                
                if(situation.hasValue())
                    { return; }
            }
            
            situation.init();
    	}
    	
    	hasValue = false;
    }
    
    /** Stop the iterator of test situations. */
    public void stop()
    {
        hasValue = false;
    }
    
    /**
     * Returns a copy of the iterator of test situations.
     * 
     * @return a copy of the iterator of test situations.
     */
    public SectionSituationIterator clone()
    {
        return new SectionSituationIterator(this);
    }
}
