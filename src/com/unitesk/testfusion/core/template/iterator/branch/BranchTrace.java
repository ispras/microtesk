/* 
 * Copyright (c) 2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: BranchTrace.java,v 1.6 2009/03/31 12:36:16 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator.branch;

import java.util.ArrayList;

/**
 * Execution trace of a branch instruction.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class BranchTrace extends ArrayList<BranchExecution>
{
    public static final long serialVersionUID = 0;
    
    /** Default constructor. */
    public BranchTrace() {}

    /**
     * Copy constructor.
     * 
     * @param <code>r</code> reference to branch trace object.
     */
    protected BranchTrace(BranchTrace r)
    {
        int i, size;
        
        // Do not call super constructor.
        
        size = r.size();
        for(i = 0; i < size; i++)
        {
            BranchExecution execution = r.get(i);
            
            add(execution.clone());
        }
    }
    
    /**
     * Returns last execution of a branch instruction.
     * 
     * @return last execution of a branch instruction.
     */
    public BranchExecution getLastExecution()
    {
        return get(size() - 1);
    }
    
    /** Adds execution of a branch instruction. */
    public void addExecution()
    {
        add(new BranchExecution());
    }
    
    /**
     * Adds execution of a branch instruction.
     * 
     * @param <code>conditionalBranch</code> type of the branch instruction.
     */
    public void addExecution(boolean conditionalBranch)
    {
        add(new BranchExecution(conditionalBranch));
    }
    
    /** Removes last execution. */
    public void removeLastExecution()
    {
        remove(size() - 1);
    }
    
    /**
     * Returns number of true conditions in the trace.
     * 
     * @return number of true conditions in the trace.
     */
    public int getTrueNumber()
    {
        int i, size, result;
        
        result = 0;
        size = size();
        
        for(i = 0; i < size; i++)
        {
            BranchExecution now = get(i);
            
            if(now.condition())
                { result++; }
        }
        
        return result;
    }

    /**
     * Returns number of false conditions in the trace.
     * 
     * @return number of false conditions in the trace.
     */
    public int getFalseNumber()
    {
        int i, size, result;
        
        result = 0;
        size = size();
        
        for(i = 0; i < size; i++)
        {
            BranchExecution now = get(i);
            
            if(!now.condition())
                { result++; }
        }
        
        return result;
    }
    
    /**
     * Returns number of condition changes.
     * 
     * @return number of condition changes.
     */
    public int getChangeNumber()
    {
        int i, size, result;
        
        result = 0;
        size = size();
        
        for(i = 1; i < size; i++)
        {
            BranchExecution pre = get(i - 1);
            BranchExecution now = get(i);
            
            if(pre.condition() != now.condition())
                { result++; }
        }
        
        return result;
    }
    
    /**
     * Returns position of condition change for simple branching.
     * 
     * @return position of condition change.
     */
    public int getChangePosition()
    {
        int i, size;
        
        size = size();
        
        BranchExecution first = get(0);
        
        for(i = 1; i < size; i++)
        {
            BranchExecution now = get(i);
            
            if(first.condition() != now.condition())
                { return i; }
        }
        
        return 0;
    }
    
    /**
     * Checks if the branch is fictitious (condition does not change) or not.
     * 
     * @return <code>true</code> if the branch is fictitious;
     *         <code>false</code> otherwise.
     */
    public boolean isFictitious()
    {
        return getChangeNumber() == 0;
    }
    
    /**
     * Checks if the branch is simple (condition does not change more than one
     * time) or not.
     * 
     * @return <code>true</code> if the branch is simple;
     *         <code>false</code> otherwise.
     */
    public boolean isSimple()
    {
        return getChangeNumber() <= 1;
    }
    
    /**
     * Checks if branch is pointed (there is only one branch execution which
     * condition equals to the negation of the first one. 
     * 
     * @return <code>true</code> if the branch is pointed;
     *         <code>false</code> otherwise.
     */
    public boolean isPointed()
    {
        int i, count, size;
        
        count = 0;
        size = size();
        
        BranchExecution first = get(0);
        
        for(i = 1; i < size; i++)
        {
            BranchExecution now = get(i);
            
            if(first.condition() != now.condition())
                { count++; }
        }
        
        return count == 1;
    }
    
    /**
     * Returns condition value of the <code>i</code>-th execution.
     * 
     * @param <code>i</code> index of the execution.
     * 
     * @return condition value of the <code>i</code>-th execution.
     */
    public boolean getCondition(int i)
    {
        BranchExecution execution = get(i);
        
        return execution.condition(); 
    }
    
    /**
     * Returns a copy of the branch trace.
     * 
     * @return a copy of the branch trace.
     */
    public BranchTrace clone()
    {
        return new BranchTrace(this);
    }
}