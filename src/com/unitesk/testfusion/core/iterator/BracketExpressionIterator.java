/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: BracketExpressionIterator.java,v 1.4 2008/08/18 08:08:34 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator;

/**
 * Iterator of valid bracket expression.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class BracketExpressionIterator implements Iterator
{
    /** Code of opening bracket. */
    public static final boolean OPENING_BRACKET  = false;
    
    /** Code of closing bracked. */
    public static final boolean CLOSING_BRACKET = true;
    
    /** Number of brackets in expressions. */
    protected int number;
    
    /** Minimum depth of bracket expression. */
    protected int minDepth;
    
    /** Maximum depth of bracket expression. */
    protected int maxDepth;

    protected int opened;
    protected int position;
    
    /** Flag that refrects availability of the value. */
    protected boolean hasValue;
    
    protected int[] depth;
    
    /**
     * Constructor.
     * 
     * @param <code>number</code> the number of brackets in expressions.
     * 
     * @param <code>minDepth</code> the minimum depth of bracket expression.
     * 
     * @param <code>maxDepth</code> the maximum depth of bracket expression.
     */
    public BracketExpressionIterator(int number, int minDepth, int maxDepth)
    {
        this.number = number;
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
        
        this.depth = new int[number];

        init();
    }
    
    /**
     * Constructor.
     * 
     * @param <code>number</code> the number of brackets in expressions.
     */
    public BracketExpressionIterator(int number)
    {
        this(number, 0, number - 1);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the iterator object.
     */
    protected BracketExpressionIterator(BracketExpressionIterator r)
    {
        number = r.number;
        hasValue = r.hasValue;
        
        depth = new int[number];
        for(int i = 0; i < number; i++)
            { depth[i] = r.depth[i]; }
    }

    /**
     * Returns the depth of the bracket expression represented by
     * <code>depth</code> array.
     * 
     * @return the depth of the bracket expression represented by
     *         <code>depth</code> array.
     */
    protected int getDepth()
    {
        int result = 0;
        
        for(int i = 0; i < number; i++)
        {
            if(depth[i] > result)
                { result = depth[i]; }
        }
        
        return result;
    }
    
    /**
     * Checks if the depth of the bracket expression is inside the bounds.
     * 
     * @return <code>true</true> if the depth of the bracket expression is
     *         correct; <code>false</code> otherwise.
     */
    protected boolean checkDepth()
    {
        int depth = getDepth();
        
        return minDepth <= depth && depth <= maxDepth; 
    }
    
    /** Initializes the iterator. */
    public void init()
    {
        hasValue = true;

        opened = 0;
        position = number - 1;
        
        for(int i = 0; i < number; i++)
            { depth[i] = 0; }
        
        while(!checkDepth() && hasValue())
            { step(); }
    }
    
    /**
     * Checks if the iterator is not exhausted (value is available).
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    public boolean hasValue()
    {
        return hasValue;
    }
   
    /**
     * Returns the current bracket expression.
     * 
     * @return the current bracket expression.
     */
    public boolean[] value()
    {
        boolean[] bracketExpression = new boolean[2 * number];

        int j =  0;
        int d = -1;
        
        for(int i = 0; i < number; i++)
        {
            while(depth[i] <= d--)
                { bracketExpression[j++] = CLOSING_BRACKET; }
            
            bracketExpression[j++] = OPENING_BRACKET;

            d = depth[i];
        }
        
        while(0 <= d--)
            { bracketExpression[j++] = CLOSING_BRACKET; }
        
        return bracketExpression;
    }
    
    private void step()
    {
        int position = number - 1;
        
        if(!hasValue)
            { return; }
        
        while(position >= 0)
        {
            int max_depth = position == 0 ? 0 : depth[position - 1] + 1;
            
            if(depth[position] < max_depth)
                { depth[position]++; return; }
            
            depth[position--] = 0;
        }
        
        hasValue = false;
    }
    
    /** Makes the iteration. */
    public void next()
    {
        do { step(); } while(!checkDepth() && hasValue());
    }
    
    /** Stops the iterator. */
    public void stop()
    {
        hasValue = false;
    }
    
    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public BracketExpressionIterator clone()
    {
        return new BracketExpressionIterator(this);
    }
}
