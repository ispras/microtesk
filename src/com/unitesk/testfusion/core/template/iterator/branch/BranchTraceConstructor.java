/* 
 * Copyright (c) 2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: BranchTraceConstructor.java,v 1.7 2009/03/31 12:36:16 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator.branch;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Iterator of different execution traces of branch templates.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class BranchTraceConstructor
{
    /**
     * Trace segment constructor.
     * 
     * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
     */
    protected class SegmentConstructor extends BranchEntryVisitor
    {
        /** Maps branch entry indexes into current block segments. */
        protected HashMap<Integer, BranchTraceSegment> blockSegments = new HashMap<Integer, BranchTraceSegment>();

        /** Maps branch entry indexes into current slot segments. */
        protected HashMap<Integer, BranchTraceSegment> slotSegments = new HashMap<Integer, BranchTraceSegment>();
        
        /**
         * Process the branch node.
         * 
         * @param <code>index</code> the index of the branch node.
         * 
         * @param <code>entry</code> the branch node.
         * 
         * @param <code>execution</code> the execution of the branch node.
         */
        public void onBranch(int index, BranchEntry entry, BranchExecution execution)
        {
            // Process block segments.
            BranchTraceSegment newBlockSegment = execution.getBlockSegment();

            blockSegments.put(index, newBlockSegment);

            // Process slot segments.
            BranchTraceSegment newSlotSegment = execution.getSlotSegment();

            slotSegments.put(index, newSlotSegment);
        }
        
        /**
         * Process the delay slot node.
         * 
         * @param <code>index</code> the index of the delay slot node.
         * 
         * @param <code>entry</code> the delay slot node.
         */
        public void onSlot(int index, BranchEntry entry)
        {
            for(Iterator<BranchTraceSegment> iterator = slotSegments.values().iterator(); iterator.hasNext();)
            {
                BranchTraceSegment segment = iterator.next();
                segment.add(index);
            }
        }
        
        /**
         * Process the basic block node.
         * 
         * @param <code>index</code> the index of the basic block node.
         * 
         * @param <code>entry</code> the basic block node.
         */
        public void onBlock(int index, BranchEntry entry)
        {
            for(Iterator<BranchTraceSegment> iterator = blockSegments.values().iterator(); iterator.hasNext();)
            {
                BranchTraceSegment segment = iterator.next();
                segment.add(index);
            }
        }
    }
    
    /**
     * Block coverage counter.
     * 
     * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
     */
    protected class CoverageCounter extends BranchEntryVisitor
    {
        private int blockCount;
        private int slotCount;
        
        private int index;
        private BranchEntry entry;
        
        /**
         * Constructor.
         * 
         * @param <code>entry</code> the entry to be processed.
         */
        public CoverageCounter(int index, BranchEntry entry)
        {
            this.blockCount = 0;
            this.slotCount = 0;
            this.index = index;
            this.entry = entry;
        }
        
        /**
         * Process the branch node.
         * 
         * @param <code>index</code> the index of the branch node.
         * 
         * @param <code>entry</code> the branch node.
         * 
         * @param <code>execution</code> the execution of the branch node.
         */
        public void onBranch(int index, BranchEntry entry, BranchExecution execution)
        {
            if(this.entry == entry)
            {
                execution.setBlockCoverageCount(blockCount);
                blockCount = 0;
                
                execution.setSlotCoverageCount(slotCount);
                slotCount = 0;
            }
        }
        
        /**
         * Process the delay slot node.
         * 
         * @param <code>index</code> the index of the delay slot node.
         * 
         * @param <code>entry</code> the delay slot node.
         */
        public void onSlot(int index, BranchEntry entry)
        {
            // Use only one delay slot
            if(index == this.index + 1)
                { slotCount++; }
        }
        
        /**
         * Process the basic block node.
         * 
         * @param <code>index</code> the index of the basic block node.
         * 
         * @param <code>entry</code> the basic block node.
         */
        public void onBlock(int index, BranchEntry entry)
        {
            BranchTraceSegment blockCoverage = this.entry.getBlockCoverage();
            
            if(blockCoverage == null)
                { return; }
            
            if(blockCoverage.contains(index))
                { blockCount++; }
        }
    }
    
    /** Do not use delay slots. */
    public static final int DO_NOT_USE_DELAY_SLOTS = 0x001;
    
    /** Heuristics flags used by default. */
    public static final int DEFAULT_FLAGS = 0x000;

    /** Heuristics flags. */
    protected int flags;
    
    /** Branch structure. */
    protected BranchStructure branchStructure;
    
    /**
     * Constructor.
     * 
     * @param <code>branchStructure</code> the branch structure.
     * 
     * @param <code>flags</code> the heuristics flags.
     */
    public BranchTraceConstructor(BranchStructure branchStructure, int flags)
    {
        this.branchStructure = branchStructure;
        this.flags = flags;
    }
    
    /**
     * Constructor.
     * 
     * @param <code>branchStructure</code> the branch structure.
     */
    public BranchTraceConstructor(BranchStructure branchStructure)
    {
        this(branchStructure, DEFAULT_FLAGS);
    }

    /** Constructs trace segments. */
    protected void constructSegments()
    {
        int i, j, size1, size2;
        
        size1 = branchStructure.size();
        for(i = 0; i < size1; i++)
        {
            BranchEntry entry = branchStructure.get(i);
            BranchTrace trace = entry.getBranchTrace();
            
            size2 = trace.size();
            for(j = 0; j < size2; j++)
            {
                BranchExecution execution = trace.get(j);
                execution.clear();
            }
        }
        
        BranchStructureWalker walker = new BranchStructureWalker(branchStructure, new SegmentConstructor());
        
        walker.start();
    }
    
    /**
     * Calculates union of blocks in segments of the branch entry.
     * 
     * @param <code>entry</code> the branch entry.
     * 
     * @return union of blocks in segments of the branch entry.
     */
    protected BranchTraceSegment blockUnion(BranchEntry entry)
    {
        int i, size; 
        
        BranchTraceSegment segment = new BranchTraceSegment();
        BranchTrace trace = entry.getBranchTrace();
        
        size = trace.size();
        for(i = 0; i < size; i++)
        {
            BranchExecution execution = trace.get(i);
            
            segment.addAll(execution.getBlockSegment());
        }
        
        return segment;
    }

    /**
     * Calculates intersection of slots in segments of the branch entry.
     * 
     * @param <code>entry</code> the branch entry.
     * 
     * @return itersection of slots in segments of the branch entry.
     */
    protected BranchTraceSegment slotIntersection(BranchEntry entry)
    {
        int i, size; 
        
        BranchTraceSegment intersection = new BranchTraceSegment();
        BranchTrace trace = entry.getBranchTrace();
        
        if((size = trace.size()) == 0)
            { return intersection; }

        BranchExecution execution = trace.get(0);
        intersection.addAll(execution.getSlotSegment());
        
        BranchTraceSegment remove = new BranchTraceSegment();
        
        for(Iterator<Integer> iterator = intersection.iterator(); iterator.hasNext(); )
        {
            Integer block = iterator.next();
            
            for(i = 1; i < size; i++)
            {
                execution = trace.get(i);
                BranchTraceSegment segment = execution.getSlotSegment(); 

                if(!segment.contains(block))
                    { i = 0; break; }
            }
            
            if(i == 0)
                { remove.add(block); }
        }
        
        intersection.removeAll(remove);
        
        return intersection;
    }
    
    /**
     * Returns trace segments where condition changes.
     * 
     * @param <code>entry</code> the branch entry.
     * 
     * @return trace segments where condition changes.
     */
    protected BranchTraceSegments getChangeSegments(BranchEntry entry)
    {
        int i, size; 
        
        BranchTraceSegments segments = new BranchTraceSegments();
        BranchTrace trace = entry.getBranchTrace();
        
        size = trace.size();
        for(i = 0; i < size - 1; i++)
        {
            BranchExecution pre  = trace.get(i);
            BranchExecution post = trace.get(i + 1);
            
            BranchTraceSegment segment = pre.getBlockSegment();

            if(pre.condition() != post.condition())
                { segments.add(segment); }
        }
        
        return segments;
    }
    
    /**
     * Constructs block and slot coverage for the branch entry.
     * 
     * @param <code>entry</code> the branch entry.
     * 
     * @return <code>true</code> if construction successful;
     *         <code>false</code> otherwise.
     */
    protected boolean constructCoverage(BranchEntry entry)
    {
        int i, size;
        
        // Get all blocks from all segments of the branch
        BranchTraceSegment blocks = blockUnion(entry);
        
        // Get set of segments to be covered
        BranchTraceSegments segments = getChangeSegments(entry);
        
        size = segments.size();

        entry.setBlockCoverage(null);
        entry.setSlotCoverage(null);
        
        // Unreachable or fictitious branching
        if(size == 0)
        {
            entry.setBlockCoverage(new BranchTraceSegment());
            
            return true;
        }
        
        for(i = 0; i < size; i++)
        {
            BranchTraceSegment segment = segments.get(i);
            
            // Can not cover empty segment
            if(segment.isEmpty())
            {
                if((flags & DO_NOT_USE_DELAY_SLOTS) != 0)
                    { return false; }
                else
                {
                    entry.setSlotCoverage(slotIntersection(entry));
                    
                    return true;
                }
            }
        }

        BranchTraceSegment coverage = new BranchTraceSegment();
        
        // Simple branching
        if(size == 1)
        {
            // Get random block from segment
            BranchTraceSegment segment = segments.get(0);
            Integer block = segment.randomBlock(); 

            // Add block to the coverage
            coverage.add(block);
            entry.setBlockCoverage(coverage);
            
            return true;
        }
        
        // Complex branching
        HashMap<Integer, Integer> counts = new HashMap<Integer, Integer>();
        
        int maxCount = 0;
        
        // While uncovered segments exist
        while(!segments.isEmpty())
        {
            // Calculate coverage count for blocks
            for(Iterator<Integer> iterator = blocks.iterator(); iterator.hasNext();)
            {
                Integer block = iterator.next();
                
                size = segments.size();
                for(i = 0; i < size; i++)
                {
                    BranchTraceSegment segment = segments.get(i);
                    
                    if(segment.contains(block))
                    {
                        Integer count = counts.get(block);
                        
                        if(count == null)
                            { count = new Integer(0); }
                        else
                            { count = new Integer(count + 1); }
                        
                        if(count > maxCount)
                            { maxCount = count; }
                        
                        counts.put(block, count);
                    }
                }
            }
            
            // Choose block with max coverage count
            BranchTraceSegment bests = new BranchTraceSegment();
            
            for(Iterator<Entry<Integer, Integer>> iterator = counts.entrySet().iterator(); iterator.hasNext();)
            {
                Entry<Integer, Integer> pair = iterator.next();
                
                if(pair.getValue() == maxCount)
                    { bests.add(pair.getKey()); }
            }
            
            Integer block = bests.randomBlock(); 
            
            // Add block to the coverage
            coverage.add(block);

            // Change coverage
            for(i = 0; i < segments.size(); i++)
            {
                BranchTraceSegment segment = segments.get(i);
                
                if(segment.contains(block))
                    { segments.remove(segment); i--; }
            }
            
            counts.clear();
            maxCount = 0;
        }

        // Set coverage blocks to entry
        entry.setBlockCoverage(coverage);
            
        return true;
    }
    
    /**
     * Calculates block coverage counts for the branch executions.
     * 
     * @param <code>entry</code> the branch entry.
     */
    protected void calculateCoverageCounts(int index, BranchEntry entry)
    {
        BranchStructureWalker walker = new BranchStructureWalker(branchStructure, new CoverageCounter(index, entry));
        
        walker.start();
    }
    
    /**
     * Constructs coverage for all branch entries.
     * 
     * @return <code>true</code> if construction successful;
     *         <code>false</code> otherwise.
     */
    public boolean construct()
    {
        int i, size;
        
        constructSegments();
        
        size = branchStructure.size();
        for(i = 0; i < size; i++)
        {
            BranchEntry entry = branchStructure.get(i);
            
            if(!constructCoverage(entry))
                { return false; }
            
            calculateCoverageCounts(i, entry);
        }
        
        return true;
    }
}
