/* 
 * Copyright (c) 2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: BranchTraceIterator.java,v 1.9 2009/08/13 15:54:23 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator.branch;

import java.util.Stack;

import com.unitesk.testfusion.core.iterator.Iterator;

/**
 * Iterator of different execution traces of branch templates.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class BranchTraceIterator implements Iterator
{
    /** Current branch structure. */
    protected BranchStructure branchStructure;
    
    /** Upper bound of branch occurrences in a trace. */
    protected int maxBranchExecution;
    
    /** Current branch index. */
    protected int currentBranch;
    
    /** Stack of branches. */
    protected Stack<Integer> branchStack = new Stack<Integer>();

    /** Flag that reflects availability of the value. */
    protected boolean hasValue;
    
    /**
     * Constructor.
     * 
     * @param <code>structure</code> the branch structure.
     * @param <code>maxBranchExecution</code> the maximal number of branch
     *        execution.
     */
    public BranchTraceIterator(BranchStructure branchStructure, int maxBranchExecution)
    {
        this.branchStructure = branchStructure;
        this.maxBranchExecution = maxBranchExecution;
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to branch trace iterator.
     */
    public BranchTraceIterator(BranchTraceIterator r)
    {
        branchStructure = r.branchStructure.clone();
        maxBranchExecution = r.maxBranchExecution;
        currentBranch = r.currentBranch;
        hasValue = r.hasValue;

        branchStack = new Stack<Integer>();
        for(Integer item : r.branchStack)
            { branchStack.add(item); }
    }
    
    /** Initializes the trace iterator. */
    public void init()
    {
        branchStack.clear();
        
        for(currentBranch = 0; currentBranch < branchStructure.size(); currentBranch++)
        {
            BranchEntry entry = branchStructure.get(currentBranch);
            
            if(entry.isBranch())
                { break; }
        }
        
        hasValue = !branchStructure.isEmpty();
        
        // Find first branch execution trace.
        next();
    }
    
    /**
     * Checks if the iterator is not exhausted (template is available).
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    public boolean hasValue()
    {
        return hasValue;
    }
    
    /**
     * Returns the current branch structure.
     * 
     * @return the current branch structure.
     */
    public BranchStructure value()
    {
        return branchStructure;
    }
    
    private void performBranching()
    {
        BranchEntry entry = branchStructure.get(currentBranch);
        BranchTrace trace = entry.getBranchTrace();
        BranchExecution execution = trace.getLastExecution();

        currentBranch = execution.condition() ? entry.getBranchLabel() : currentBranch + 1;
    }
    
    private void searchNextBranch()
    {
        int size = branchStructure.size();
        
        for(; currentBranch < size; currentBranch++)
        {
            BranchEntry entry = branchStructure.get(currentBranch);
            
            if(entry.isBranch())
                { return; }
        }
        
        currentBranch = -1;
    }
    
    private void handleBranch()
    {
        performBranching();
        searchNextBranch();
    }
    
    private boolean isTraceCompleted()
    {
        return currentBranch == -1;
    }
    
    private BranchStructure nextBranchStructure()
    {
        while(hasValue())
        {
            boolean isTraceCompleted = isTraceCompleted();
            
            if(isTraceCompleted)
                { currentBranch = branchStack.peek(); }
                
            BranchEntry entry = branchStructure.get(currentBranch);
            BranchTrace trace = entry.getBranchTrace();

            BranchExecution execution = trace.isEmpty() ? null : trace.getLastExecution();

            if(!isTraceCompleted && trace.size() < maxBranchExecution)
            {
                // Prolong trace if it is not completed
                { trace.addExecution(entry.isConditionalBranch()); branchStack.push(currentBranch); }
                
                handleBranch();
                
                if(isTraceCompleted())
                    { return branchStructure; }
                else
                    { continue; }
            }
            else
            {
                if(isTraceCompleted)
                {
                    if(!trace.isEmpty() && execution.hasValue())
                    {
                        // Try to change last execution
                        execution.next();
                        
                        if(execution.hasValue())
                        {
                            handleBranch();
                            
                            if(isTraceCompleted())
                                { return branchStructure; }
                            else
                                { continue; }
                        }
                    }
                }

                // Backtracking
                while(!branchStack.isEmpty())
                {
                    currentBranch = branchStack.peek();
                    
                    entry = branchStructure.get(currentBranch);
                    trace = entry.getBranchTrace();

                    execution = trace.getLastExecution();

                    if(execution.hasValue())
                    {
                        execution.next();
                    
                        if(execution.hasValue())
                        {
                            handleBranch();
                            
                            if(isTraceCompleted())
                                { return branchStructure; }
                            else
                                { break; }
                        }
                    }
                    
                    { trace.removeLastExecution(); branchStack.pop(); }
                }
                
                if(branchStack.isEmpty())
                    { stop(); return branchStructure; }
                
                continue;
            }
        }
        
        return branchStructure;
    }
    
    /** Makes iteration. */
    public void next()
    {
        while(hasValue())
        {
            BranchTraceConstructor branchTraceConstructor = new BranchTraceConstructor(nextBranchStructure());
            
            if(hasValue() && branchTraceConstructor.construct())
                { break; }
        }
    }
    
    /** Stops iteration. */
    public void stop()
    {
        hasValue = false;
    }
    
    /**
     * Returns a copy of the trace iterator.
     * 
     * @return a copy of the trace iterator.
     */
    public BranchTraceIterator clone()
    {
        return new BranchTraceIterator(this);
    }
}
