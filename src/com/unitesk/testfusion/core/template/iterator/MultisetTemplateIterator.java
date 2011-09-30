/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: MultisetTemplateIterator.java,v 1.9 2009/11/12 13:28:30 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator;

import java.util.ArrayList;

import com.unitesk.testfusion.core.generator.Random;
import com.unitesk.testfusion.core.model.Instruction;
import com.unitesk.testfusion.core.model.Program;

/**
 * Class <code>MultisetTemplateIterator</code> represents iterator that
 * consider templates as multisets of size from <code>minSize</code> to
 * <code>maxSize</code> containing from <code>minRepetition</code> to
 * <code>maxRepetition</code> instructions of the same equivalence class.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class MultisetTemplateIterator extends TemplateIterator
{
    /** Minimum number of instructions of the same equivalence class. */
    protected int minRepetition;
    
    /** Maximum number of instructions of the same equivalence class. */
    protected int maxRepetition;
    
    /** Minimum size of template. */
    protected int minSize;
    
    /** Maximum size of template. */
    protected int maxSize;
    
    /** Flag that refrects availability of the template. */
    protected boolean hasValue;

    /** Array of counts. */
    private int[] count;
    
    /**
     * Constructor.
     * 
     * @param <code>minRepetition</code> the minimum number of instructions of
     *        the same equivalence class.
     *        
     * @param <code>maxRepetition</code> the maximum number of instructions of
     *        the same equivalence class.
     *        
     * @param <code>minSize</code> the minimum size of template.
     * 
     * @param <code>maxSize</code> the maximum size of template.
     */
    public MultisetTemplateIterator(int minRepetition, int maxRepetition, int minSize, int maxSize)
    {
        this.minRepetition = minRepetition;
        this.maxRepetition = maxRepetition;
        this.minSize  = minSize;
        this.maxSize  = maxSize;
        
        this.hasValue = false;
    }

    /**
     * Constructor.
     * 
     * @param <code>minRepetition</code> the minimum number of instructions of
     *        the same equivalence class.
     *        
     * @param <code>maxRepetition</code> the maximum number of instructions of
     *        the same equivalence class.
     *        
     * @param <code>size</code> the template size.
     */
    public MultisetTemplateIterator(int minRepetition, int maxRepetition, int size)
    {
        this(minRepetition, maxRepetition, size, size);
    }

    /**
     * Constructor. Template size is not fixed.
     * 
     * @param <code>minRepetition</code> the minimum number of instructions of
     *        the same equivalence class.
     * 
     * @param <code>maxRepetition</code> the maximum number of instructions of
     *        the same equivalence class.
     */
    public MultisetTemplateIterator(int minRepetition, int maxRepetition)
    {
        this(minRepetition, maxRepetition, Integer.MAX_VALUE);
    }

    /**
     * Default constructor. <code>minRepetition</code> is assumed to be zero.
     * <code>maxRepetition</code> is assumed to be one.
     *
     */
    public MultisetTemplateIterator()
    {
        this(0, 1);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to a multiset template iterator object.
     */
    protected MultisetTemplateIterator(MultisetTemplateIterator r)
    {
        int i, size;
        
        hasValue      = r.hasValue;
        minRepetition = r.minRepetition;
        maxRepetition = r.maxRepetition;
        minSize       = r.minSize;
        maxSize       = r.maxSize;

        count = new int[size = r.count.length];
        for(i = 0; i < size; i++)
            { count[i] = r.count[i]; }
    }
    
    /**
     * Returns the minimum number of instructions of the same equivalence class.
     *   
     * @return the minimum number of instructions of the same equivalence class.
     */
    public int getMinRepetition()
    {
        return minRepetition;
    }
    
    /**
     * Sets the minimum number of instructions of the same equivalence class.
     * 
     * @param <code>minRepetition</code> the minumum number of instructions of
     *        the same equivalence class.
     */
    public void setMinRepetition(int minRepetition)
    {
        this.minRepetition = minRepetition;
    }
    
    /**
     * Returns the maximum number of instructions of the same equivalence class.
     * 
     * @return the maximum number of instructions of the same equivalence class.
     */
    public int getMaxRepetition()
    {
        return maxRepetition;
    }
    
    /**
     * Sets the maximum number of instructions of the same equivalence class.
     * 
     * @param <code>maxRepetition</code> the maximum number of instructions of
     *        the same equivalence class.
     */
    public void setMaxRepetition(int maxRepetition)
    {
        this.maxRepetition = maxRepetition;
    }
    
    /**
     * Returns the minumum size of template.
     * 
     * @return the minimum size of template.
     */
    public int getMinSize()
    {
        return minSize;
    }
    
    /**
     * Sets the minimum size of template.
     * 
     * @param <code>minSize</code> the minimum size of template.
     */
    public void setMinSize(int minSize)
    {
        this.minSize = minSize;
    }
    
    /**
     * Returns the maximum size of template.
     * 
     * @return the maximum size of template.
     */
    public int getMaxSize()
    {
        return maxSize;
    }
    
    /**
     * Sets the maximum size of template.
     * 
     * @param <code>maxSize</code> the maximum size of template.
     */
    public void setMaxSize(int maxSize)
    {
        this.maxSize = maxSize;
    }
    
    /**
     * Sets the template size (minimum size is equal to maximum size).
     *  
     * @param <code>size</code> the template size.
     */
    public void setSize(int size)
    {
        setMinSize(size);
        setMaxSize(size);
    }
    
    /**
     * Returns the size of the current template.
     * 
     * @return the size of the current template.
     */
    protected int size()
    {
        int i, size, result;
        
        size = count.length;
        for(i = result = 0; i < size; i++)
            { result += count[i]; }
        
        return result;
    }
    
    /** Initializes the iterator. */
    @Override
    public void init()
    {
        int i, size;

        count = new int[size = countEquivalenceClass()];

        if(!(hasValue = (size * maxRepetition < minSize)))
            { return; }
        
        for(i = 0; i < size; i++)
            { count[i] = minRepetition; }

        while((size() < minSize || size() > maxSize) && hasValue())
            { step(); }
    }
    
    /**
     * Checks if the iterator is not exhausted (i.e., a template is available).
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean hasValue()
    {
        return hasValue;
    }
    
    /** Stops the iterator. */
    @Override
    public void stop()
    {
        hasValue = false;
    }
    
    /**
     * Returns the current template.
     * 
     * @return the current template.
     */
    @Override
    public Program value()
    {
        int i, j, size1, size2, size3;
        
        Program template = new Program();
        ArrayList<Instruction> multiset = new ArrayList<Instruction>();
        
        size1 = count.length;
        for(i = 0; i < size1; i++)
        {
            ArrayList<Instruction> instructions = getEquivalenceClass(i);
            
            if((size2 = instructions.size()) == 0)
                { continue; }
            
            size3 = count[i];
            for(j = 0; j < size3; j++)
            {
                Instruction instruction = instructions.get(Random.int32_non_negative_less(size2));
                
                multiset.add(instruction.clone());
            }
        }

        while(!multiset.isEmpty())
        {
            i = Random.int32_non_negative_less(multiset.size());
            Instruction instruction = multiset.get(i);

            template.append(instruction);
            multiset.remove(i);
        }
        
        return template;
    }
    
    /** Step of iteration. */
    protected void step()
    {
        int i, size;
        
        if(!hasValue)
            { return; }
        
        size = count.length;
        for(i = size - 1; i >= 0; i--)
        {
            if(count[i] < maxRepetition)
                { count[i]++; return; }
            
            count[i] = minRepetition;
        }
        
        hasValue = false;
    }
    
    /** Randomizes test template within one iteration. */
    @Override
    public void randomize()
    {
        // Do nothing, because value() method is randomized
    }
    
    /** Makes iteration. */
    @Override
    public void next()
    {
        do { step(); }
        while((size() < minSize || size() > maxSize) && hasValue());
    }
    
    /**
     * Returns a copy of the multiset template iterator.
     * 
     * @return a copy of the multiset template iterator.
     */
    @Override
    public MultisetTemplateIterator clone()
    {
        return new MultisetTemplateIterator(this);
    }
}
