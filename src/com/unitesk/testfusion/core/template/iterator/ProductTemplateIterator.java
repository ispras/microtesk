/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: ProductTemplateIterator.java,v 1.16 2009/08/06 10:27:36 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator;

import java.util.HashSet;

import com.unitesk.testfusion.core.model.Instruction;
import com.unitesk.testfusion.core.model.Program;

/**
 * Class <code>ProductTemplateIterator</code> implements iterator that produces
 * templates by Cartesian product of the registered instructions.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ProductTemplateIterator extends TemplateIterator
{
    /** Template size. */
    protected int templateSize;
    
	/** Array corresponding to current test template. */
	protected int[] template;
	
	/** Array of current indexes in the equivalence classes. */
	protected int[] index;
	
    /** Flag that refrects availability of the template. */
	protected boolean hasValue;
    
    /** Flag that shows if some instructions are not covered by test templates. */
	protected boolean someInstructionsAreNotCovered;
	
    /**
     * Constructor.
     * 
     * @param <code>templateSize</code> the template size.
     */
    public ProductTemplateIterator(int templateSize)
    {
    	if(templateSize <= 0)
    		{ throw new IllegalArgumentException("template size should be positive"); }
        
        this.templateSize = templateSize;
    }

    /** Default constructor. Template size is assumed to be one. */
    public ProductTemplateIterator()
    {
        this(1);
    }
    
    /** Copy constructor.
     * 
     * @param <code>r</code> the reference to an object of product template iterator.
     */
    protected ProductTemplateIterator(ProductTemplateIterator r)
    {
        super(r);
        
        int i, size;

        hasValue = r.hasValue;
        someInstructionsAreNotCovered = r.someInstructionsAreNotCovered;

        template = new int[size = r.template.length];
        for(i = 0; i < size; i++)
            { template[i] = r.template[i]; }
        
        index = new int[size = r.index.length];
        for(i = 0; i < size; i++)
            { index[i] = r.index[i]; }
    }
    
    /**
     * Returns the template size.
     * 
     * @return the template size.
     */
    public int getTemplateSize()
    {
        return templateSize;
    }
    
    /**
     * Sets the template size.
     * 
     * @param <code>templateSize</code> the template size.
     */
    public void setTemplateSize(int templateSize)
    {
        this.templateSize = templateSize;
    }
    
    /** Initializes the iterator. */
    @Override
    public void init()
    {
    	int i, size;

        template = new int[templateSize];
        
    	size = template.length;
    	for(i = 0; i < size; i++)
            { template[i] = 0; }
        
        size = countEquivalenceClass();
        
        index = new int[size];
        
        for(i = 0; i < size; i++)
            { index[i] = 0; }
        
        hasValue = size > 0;
        someInstructionsAreNotCovered = false;
    }
    
    /**
     * Checks if the iterator is not exhausted (template is available).
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean hasValue()
    {
    	return hasValue;
    }
    
    /**
     * Returns the current template.
     * 
     * @return the current template.
     */
    @Override
    public Program value()
    {
    	int i, size, count[];
    	Program test = new Program();
    	
    	size = countEquivalenceClass();
    	count = new int[size];
    	
    	for(i = 0; i < size; i++)
    		{ count[i] = 0; }

    	size = template.length;
    	for(i = 0; i < size; i++)
    	{
    		int j = template[i];

    		EquivalenceClass equivalenceClass = getEquivalenceClass(j);
    		Instruction instruction = equivalenceClass.get((index[j] + count[j]++) % equivalenceClass.size());
    		
    		test.append(instruction.clone());
    	}
    	
        return test;
    }
    
    private boolean doesTemplateCoverNewInstructions()
    {
    	int i, size;
    	
    	size = template.length;
    	for(i = 0; i < size; i++)
    	{
            int j = template[i];
            EquivalenceClass equivalenceClass = getEquivalenceClass(j);
            
            // Equivalence class is not exhausted.
    		if(index[j] < equivalenceClass.size())
    			{ return true; }
    	}
    	
    	return false;
    }
    
    /** Randomize test template within one iteration. */
    @Override
    public void randomize()
    {
        int i, size;
        HashSet<Integer> equivalenceClasses = new HashSet<Integer>();
        
        size = template.length;
        for(i = 0; i < size; i++)
            { equivalenceClasses.add(template[i]); }
        
        for(int j : equivalenceClasses)
            { index[j]++; }
    }
    
    /** Makes iteration. */
    @Override
    public void next()
    {
    	int i, size;
    	
    	if(!hasValue)
    		{ return; }
    	
    	size = template.length;
    	for(i = 0; i < size; i++)
            { index[template[i]]++; }
        
        size = countEquivalenceClass();
    	for(i = template.length - 1; i >= 0; i--)
    	{
    		if(template[i] < size - 1)
    		{
    			template[i]++;

    			// If new template or new instruction are covered.
    			if(!someInstructionsAreNotCovered || doesTemplateCoverNewInstructions())
    				{ break; }
    		}
    		else
    			{ template[i] = 0; }
    	}
    	
    	if(i == -1)
        {
    		someInstructionsAreNotCovered = false;
    		
    		for(i = 0; i < size; i++)
    		{
                EquivalenceClass equivalenceClass = getEquivalenceClass(i);
                
    			if(index[i] < equivalenceClass.size())
    				{ someInstructionsAreNotCovered = true; break; }
    		}
    		
            // All instructions of all equivalence classes are covered.
    		if(!someInstructionsAreNotCovered)
    			{ hasValue = false; return; }
        }
    }
    
    /** Stops the iterator. */
    @Override
    public void stop()
    {
        hasValue = false;
    }

    /**
     * Returns a string representation of the product template iterator.
     * 
     * @return a string representation of the product template iterator.
     */
    @Override
    public String toString()
    {
        int i, j, size1, size2;
        StringBuffer result = new StringBuffer();
        
        size1 = countEquivalenceClass();
        for(i = 0; i < size1; i++)
        {
            EquivalenceClass array = getEquivalenceClass(i);

            size2 = array.size();
            for(j = 0; j < size2; j++)
            {
                Instruction instruction = (Instruction)array.get(i);

                result.append(instruction.getName());
                
                if(j < size2 - 1)
                    { result.append(", "); }
            }
            
            result.append('\n');
        }
        
        return result.toString();
    }
    
    /**
     * Returns a copy of the product template iterator.
     * 
     * @return a copy of the product template iterator.
     */
    @Override
    public ProductTemplateIterator clone()
    {
        return new ProductTemplateIterator(this);
    }
}
