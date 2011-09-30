/* 
 * Copyright (c) 2009 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: BranchTemplateIterator.java,v 1.21 2009/08/19 16:50:54 kamkin Exp $
 */

package com.unitesk.testfusion.core.template.iterator.branch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import com.unitesk.testfusion.core.context.GeneratorContext;
import com.unitesk.testfusion.core.generator.Random;
import com.unitesk.testfusion.core.iterator.IndexArrayIterator;
import com.unitesk.testfusion.core.iterator.Int32RangeIterator;
import com.unitesk.testfusion.core.iterator.ProductIterator;
import com.unitesk.testfusion.core.model.Instruction;
import com.unitesk.testfusion.core.model.Processor;
import com.unitesk.testfusion.core.model.Program;
import com.unitesk.testfusion.core.model.PseudoInstruction;
import com.unitesk.testfusion.core.situation.Situation;
import com.unitesk.testfusion.core.template.iterator.EquivalenceClass;
import com.unitesk.testfusion.core.template.iterator.InstructionFactorization;
import com.unitesk.testfusion.core.template.iterator.TemplateIterator;

/**
 * Class <code>BranchTemplateIterator</code> iterates different valid branch
 * structures for the given branch instructions (conditional and unconditional
 * jumps, procedures calls, etc.).
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class BranchTemplateIterator extends TemplateIterator
{
    /** Do not iterate consecutive basic blocks. */
    public static final int DO_NOT_ITERATE_CONSECUTIVE_BASIC_BLOCKS = 0x0001;
    
    /**
     * Do not insert instructions that can throw exceptions into branch delay
     * slots.
     */
    public static final int DO_NOT_USE_UNSAFE_DELAY_SLOTS = 0x0002;

    /**
     * Do not insert instructions that can throw exceptions into branch delay
     * slots if an exception can cause infinite looping.
     */
    public static final int DO_NOT_USE_UNSAFE_DELAY_SLOTS_IF_EXCEPTION_CAN_CAUSE_LOOPING = 0x0004;
    
    /** Heuristics flags used by default. */
    public static final int DEFAULT_FLAGS = DO_NOT_ITERATE_CONSECUTIVE_BASIC_BLOCKS |
                                            DO_NOT_USE_UNSAFE_DELAY_SLOTS_IF_EXCEPTION_CAN_CAUSE_LOOPING;
    
    /** Heuristics flags. */
    protected int flags;
    
    /** Existence of branch delay slot. */
    protected boolean delaySlot;
    
    /**
     * Minimal length of branch structure (number of branches and basic blocks).
     */
    protected int minLength;
    
    /**
     * Maximal length of branch structure (number of branches and basic blocks).
     */
    protected int maxLength;
    
    /** Minimal number of branch instructions. */
    protected int minBranchNumber;
    
    /** Maximal number of branch instructions. */
    protected int maxBranchNumber;
    
    /** Maximal number of branch execution. */
    protected int maxBranchExecution;

    /**
     * Equivalence classes of branch instructions (conditional and
     * unconditional jumps, procedure calls, etc.)
     */
    protected InstructionFactorization branches = new InstructionFactorization();
    
    /**
     * Equivalence classes of delay slots (arithmetical instructions,
     * FPU instructions, load/store instructions, etc.).
     */
    protected InstructionFactorization allSlots = new InstructionFactorization();
    
    /**
     * Equivalence classes of delay slots which do not cause an exception.
     */
    protected InstructionFactorization safeSlots = new InstructionFactorization();

    /**
     * All instructions that do not cause an exception.
     */
    protected EquivalenceClass safeInstructions = new EquivalenceClass();
    
    /** Equivalence classes of basic blocks. */
    protected InstructionFactorization allBlocks = new InstructionFactorization();

    /** Flag that reflects availability of the value. */
    protected boolean hasValue;
    
    /** Iterator of branch structure length. */
    protected Int32RangeIterator lengthIterator;
    
    /** Iterator of branch instruction number. */
    protected Int32RangeIterator branchNumberIterator;
    
    /** Iterator of branch positions. */
    protected IndexArrayIterator branchPositionIterator;
    
    /** Iterator of branch labels. */
    protected ProductIterator branchLabelIterator;
    
    /** Iterator of branch equivalence classes. */
    protected ProductIterator branchIterator;

    /** Iterator of slot equivalence classes. */
    protected ProductIterator slotIterator;

    /** Iterator of basic block equivalence classes. */
    protected ProductIterator blockIterator;

    /** Branch trace iterator. */
    protected BranchTraceIterator branchTraceIterator;
    
    private boolean[] conditionalBranch;

    /**
     * Constructor.
     * 
     * @param <code>delaySlot</code> boolean flag that shows if delay slots are
     *        supported by target microprocessor architecture.  
     * 
     * @param <code>minLength</code> minimal length of test template
     *        (delay slots are not considered).
     * 
     * @param <code>maxLength</code> maximal length of test template
     *        (delay slots are not considered).
     * 
     * @param <code>minBranchNumber</code> minimal number of branches in
     *        test template.
     * 
     * @param <code>maxBranchNumber</code> maximal number of branches in
     *        test template.
     * 
     * @param <code>maxBranchExecution</code> maximal length of branch execution
     *        trace.
     * 
     * @param <code>flags</code> heuristics flags.
     */
    public BranchTemplateIterator(boolean delaySlot, int minLength, int maxLength, int minBranchNumber, int maxBranchNumber, int maxBranchExecution, int flags)
    {
        if(minLength <= 0)
            { throw new IllegalArgumentException("minLength is not positive"); }
        
        if(minBranchNumber <= 0)
            { throw new IllegalArgumentException("minBranchNumber is not positive"); }
        
        if(maxBranchExecution <= 0)
            { throw new IllegalArgumentException("minBranchExecution is not positive"); }
        
        if(minLength > maxLength)
            { throw new IllegalArgumentException("minLength is greater than maxLength"); }
            
        if(minBranchNumber > maxBranchNumber)
            { throw new IllegalArgumentException("minBranchNumber is greater than maxBranchNumber"); }
        
        if(minBranchNumber > maxLength)
            { throw new IllegalArgumentException("minBranchNumber is greater than maxLength"); }
        
        if(maxBranchNumber > maxLength)
            { maxBranchNumber = maxLength; }
        
        if(minLength < minBranchNumber)
            { minLength = minBranchNumber; }
        
        if((flags & DO_NOT_ITERATE_CONSECUTIVE_BASIC_BLOCKS) != 0)
        {
            int maxLengthUpperBound = 3 * maxBranchNumber + 1;
            
            if(minLength > maxLengthUpperBound)
                { throw new IllegalArgumentException("minLength is greater than maxLengthUpperBound"); }

            if(maxLength > maxLengthUpperBound)
                { maxLength = maxLengthUpperBound; }
        }
        
        this.delaySlot          = delaySlot;
        this.minLength          = minLength;
        this.maxLength          = maxLength;
        this.minBranchNumber    = minBranchNumber;
        this.maxBranchNumber    = maxBranchNumber;
        this.maxBranchExecution = maxBranchExecution;
        
        this.flags              = flags;
    }

    /**
     * Constructor.
     * 
     * @param <code>delaySlot</code> boolean flag that shows if delay slots are
     *        supported by target microprocessor architecture.  
     * 
     * @param <code>minLength</code> minimal length of test template.
     * 
     * @param <code>maxLength</code> maximal length of test template.
     * 
     * @param <code>minBranchNumber</code> minimal number of branches in
     *        test template.
     * 
     * @param <code>maxBranchNumber</code> maximal number of branches in
     *        test template.
     * 
     * @param <code>maxBranchExecution</code> maximal length of branch execution
     *        trace.
     */
    public BranchTemplateIterator(boolean delaySlot, int minLength, int maxLength, int minBranchNumber, int maxBranchNumber, int maxBranchExecution)
    {
        this(delaySlot, minLength, maxLength, minBranchNumber, maxBranchNumber, maxBranchExecution, DEFAULT_FLAGS);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to an object of branch template
     *        iterator.
     */
    protected BranchTemplateIterator(BranchTemplateIterator r)
    {
        super(r);
        
        delaySlot              = r.delaySlot;
        minLength              = r.minLength;
        maxLength              = r.maxLength;
        minBranchNumber        = r.minBranchNumber;
        maxBranchNumber        = r.maxBranchNumber;
        maxBranchExecution     = r.maxBranchExecution;
        
        flags                  = r.flags;
        
        branches               = r.branches.clone();
        allSlots               = r.allSlots.clone();
        safeSlots              = r.safeSlots.clone();
        allBlocks              = r.allBlocks.clone();
        
        safeInstructions       = r.safeInstructions.clone();

        hasValue               = r.hasValue;
        
        lengthIterator         = r.lengthIterator.clone();
        branchNumberIterator   = r.branchNumberIterator.clone();
        branchIterator         = r.branchIterator.clone();
        slotIterator           = r.slotIterator.clone();
        blockIterator          = r.blockIterator.clone();
        branchPositionIterator = r.branchPositionIterator.clone();
        branchLabelIterator    = r.branchLabelIterator.clone();
        branchTraceIterator    = r.branchTraceIterator.clone();
        
        if(r.conditionalBranch == null)
            { conditionalBranch = null; }  
        else
            { System.arraycopy(r.conditionalBranch, 0, conditionalBranch = new boolean[r.conditionalBranch.length], 0, r.conditionalBranch.length); }
    }

    /**
     * Returns branch structure corresponding to iterators' states.
     * 
     * @return the current branch structure.
     */
    protected BranchStructure getBranchStructure()
    {
        int i, j, branch, block;
        
        int length = lengthIterator.int32Value();
        int branchNumber = branchNumberIterator.int32Value();

        BranchStructure structure = new BranchStructure(length + (delaySlot ? branchNumber : 0));
        
        // Positions of branch instructions in test template
        int[] array = branchPositionIterator.indexArrayValue();
        
        for(i = j = branch = block = 0; i < length; i++, j++)
        {
            BranchEntry entry = structure.get(j);
            
            // Process branch
            if(branch < branchNumber && i == array[branch])
            {
                int branchLabel = 0;
                int branchClass = 0;
                
                if(branchLabelIterator != null)
                    { branchLabel = (Integer)branchLabelIterator.value(branch); }
            
                if(branchIterator != null)
                    { branchClass = (Integer)branchIterator.value(branch); }

                entry.setType(BranchEntry.BRANCH);
                entry.setConditionalBranch(conditionalBranch[branchClass]);
                entry.setBranchLabel(branchLabel);
                entry.setEquivalenceClass(branchClass);
                
                // Process delay slot
                if(delaySlot)
                {
                    int slotClass = 0;
                    
                    if(slotIterator != null)
                        { slotClass = (Integer)slotIterator.value(branch); }
                    
                    BranchEntry slot = structure.get(++j);
                    
                    slot.setType(BranchEntry.SLOT);
                    slot.setEquivalenceClass(slotClass);
                }
                
                branch++;
            }
            // Process basic block
            else
            {
                int blockClass = 0;
                
                if(blockIterator != null)
                    { blockClass = (Integer)blockIterator.value(block); }

                entry.setType(BranchEntry.BLOCK);
                entry.setEquivalenceClass(blockClass);
                
                block++;
            }
        }
        
        return structure;
    }
    
    private Instruction randomBranch(int equivalenceClassIndex)
    {
        EquivalenceClass equivalenceClass = branches.getEquivalenceClass(equivalenceClassIndex);
        Instruction instruction = equivalenceClass.get(Random.int32_non_negative_less(equivalenceClass.size()));
        
        return instruction.clone();
    }
    
    private Instruction randomBlock(int equivalenceClassIndex)
    {
        EquivalenceClass equivalenceClass = allBlocks.getEquivalenceClass(equivalenceClassIndex);
        Instruction instruction = equivalenceClass.get(Random.int32_non_negative_less(equivalenceClass.size()));
        
        return instruction.clone();
    }
    
    private Instruction randomSlot(int equivalenceClassIndex)
    {
        EquivalenceClass equivalenceClass = allSlots.getEquivalenceClass(equivalenceClassIndex);     
        Instruction instruction = equivalenceClass.get(Random.int32_non_negative_less(equivalenceClass.size()));
        
        return instruction.clone();
    }
    
    private Instruction randomSafeSlot(int equivalenceClassIndex)
    {
        EquivalenceClass equivalenceClass = safeSlots.getEquivalenceClass(equivalenceClassIndex);
        
        if(equivalenceClass.isEmpty())
        {
            // Get safe slot from an other equivalence class
            if(safeInstructions.isEmpty())
                { throw new IllegalStateException("There are no safe instructions"); }
            
            return safeInstructions.get(Random.int32_non_negative_less(safeInstructions.size()));
        }
        
        Instruction instruction = equivalenceClass.get(Random.int32_non_negative_less(equivalenceClass.size()));
        
        return instruction.clone();
    }
    
    private boolean canExceptionCauseInfiniteLooping(int currentBranchIndex)
    {
        int i, size;
        
        BranchStructure structure = branchTraceIterator.value();
        BranchEntry branch = structure.get(currentBranchIndex);
        
        // Branch to the delay slot or to the next instruction
        if(branch.getBranchLabel() == currentBranchIndex + 1 || branch.getBranchLabel() == currentBranchIndex + 2)
            { return false; }
        
        size = structure.size();
        for(i = currentBranchIndex + 2; i < size; i++)
        {
            BranchEntry entry = structure.get(i);
            
            // Backward branch exists
            if(entry.isBranch() && entry.getBranchLabel() <= i)
                { return true; }
        }
        
        return false;
    }
    
    /**
     * Returns the current template.
     * 
     * @return the current template.
     */
    @Override
    public Program value()
    {
        int i, j, size;
        
        Program program = new Program();
        
        BranchStructure structure = branchTraceIterator.value();
        
        size = structure.size();
        for(i = j = 0; i < size; i++)
        {
            BranchEntry entry = structure.get(i);
            
            if(entry.isBranch())
            {
                Instruction instruction = randomBranch(entry.getEquivalenceClass());
                BranchTraceSituation situation = (BranchTraceSituation)instruction.getSituation();

                situation.setBranchNumber(j++);
                situation.setBranchIndex(i);
                situation.setBranchLabel(entry.getBranchLabel());
                situation.setBranchTrace(entry.getBranchTrace());
                situation.setBlockCoverage(entry.getBlockCoverage());
                situation.setSlotCoverage(entry.getSlotCoverage());

                program.append(instruction);
            }
            else if(entry.isBlock())
            {
                Instruction instruction = randomBlock(entry.getEquivalenceClass());
                program.append(instruction);
            }
            else
            {
                if((flags & DO_NOT_USE_UNSAFE_DELAY_SLOTS) != 0)
                {
                    Instruction instruction = randomSafeSlot(entry.getEquivalenceClass());
                    program.append(instruction);
                }
                else
                {
                    if((flags & DO_NOT_USE_UNSAFE_DELAY_SLOTS_IF_EXCEPTION_CAN_CAUSE_LOOPING) != 0 && canExceptionCauseInfiniteLooping(i - 1))
                    {
                        Instruction instruction = randomSafeSlot(entry.getEquivalenceClass());
                        program.append(instruction);
                    }
                    else
                    {
                        Instruction instruction = randomSlot(entry.getEquivalenceClass());
                        program.append(instruction);
                    }
                }
            }
        }
        
        return program;
    }
    
    /**
     * Performs auxiliary construction omitted by situations' constructors.
     * 
     * @param <code>processor</code> the processor.
     * 
     * @param <code>context</code> the context of generation.
     * 
     * @param <code>template</code> the test template.
     * 
     * @return <code>true</code> if construction successfull;
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean construct(Processor processor, GeneratorContext context, Program template)
    {
        int i, size;
        
        HashMap<Integer, Program> steps = new HashMap<Integer, Program>();
        HashSet<Integer> slots  = new HashSet<Integer>();
        
        size = template.countInstruction();
        for(i = 0; i < size; i++)
        {
            Instruction instruction = template.getInstruction(i);
            
            if(instruction.isBranchInstruction())
            {
                BranchTraceSituation situation = (BranchTraceSituation)instruction.getSituation();
                
                BranchTraceSegment blockCoverage = situation.getBlockCoverage();
                
                String labelString = context.getLabel(situation.getBranchLabel()) + ":";
                Program labelProgram = steps.get(situation.getBranchLabel());

                boolean insertLabel = true;
                
                if(labelProgram == null)
                    { labelProgram = new Program(); }
                else
                {
                    Instruction labelInstruction = labelProgram.getInstruction(0);
                    
                    if(labelInstruction instanceof PseudoInstruction)
                    {
                        PseudoInstruction label = (PseudoInstruction)labelInstruction;
                        
                        if(label.getText().equals(labelString))
                            { insertLabel = false; }
                    }
                }
                
                if(insertLabel)
                {
                    labelProgram.insert(new PseudoInstruction(labelString), 0);
                    steps.put(situation.getBranchLabel(), labelProgram);
                }
                
                situation.init(processor, context);

                // If can cover basic blocks
                if(situation.canInsertStepIntoBlock())
                {
                    for(Iterator<Integer> iterator = blockCoverage.iterator(); iterator.hasNext();)
                    {
                        int block = iterator.next();
                        
                        Program step = situation.step();
                        
                        if(!step.isEmpty())
                        {
                            Program program = steps.get(block);
                            
                            if(program == null)
                                { program = new Program(); }
                            
                            program.append(step);
                            
                            steps.put(block, program);
                        }
                    }
                }
                // Use delay slots
                else
                {
                    // If instruction can nullify delay slot, do not use delay slot
                    if(instruction.doesNullifyDelaySlot())
                        { context.reset(); return false; }
                    
                    // Paranoia: impossible situation
                    if(!situation.canInsertStepIntoSlot())
                        { context.reset(); return false; }
                    
                    Program step = situation.step();
                    
                    if(step == null)
                        { context.reset(); return false; }
                    
                    if(!step.isEmpty())
                    {
                        final int slotPosition = i + 1;
                        
                        // The only one instruction can be inserted into delay slot
                        if(step.countInstruction() > 1)
                            { context.reset(); return false; }

                        Program program = steps.get(slotPosition);
                        
                        if(program == null)
                            { program = new Program(); }
                        
                        program.append(step);
                        
                        slots.add(i + 1);
                        steps.put(i + 1, program);
                    }
                }
            }
        }

        int correction = 0;

        for(Iterator<Entry<Integer, Program>> iterator = steps.entrySet().iterator(); iterator.hasNext();)
        {
            Entry<Integer, Program> entry = iterator.next();
            Integer position = entry.getKey();
            Program program = entry.getValue();

            template.insert(program, position + correction);

            if(slots.contains(position))
                { template.remove(position + correction-- + program.countInstruction()); }
            
            correction += program.countInstruction();
        }
        
        return true;
    }

    private boolean filterBranchPositionIterator_ConsecutiveBasicBlocks()
    {
        int i, size;

        int branchNumber = branchNumberIterator.int32Value();
        int branchNumberLowerBound = 0;
        
        BranchStructure structure = getBranchStructure();
        
        branchNumberLowerBound = 0;
        size = structure.size();
        for(i = 1; i < size; i++)
        {
            BranchEntry pre = structure.get(i - 1);
            BranchEntry now = structure.get(i);
            
            if(pre.isBlock() && now.isBlock())
                { branchNumberLowerBound++; }
        }
        
        if(branchNumber < branchNumberLowerBound)
            { return false; }
        
        return true;
    }
    
    private boolean filterBranchPositionIterator()
    {
        if((flags & DO_NOT_ITERATE_CONSECUTIVE_BASIC_BLOCKS) != 0)
        {
            if(!filterBranchPositionIterator_ConsecutiveBasicBlocks())
                { return false; }
        }
        
        return true;
    }
    
    private boolean filterBranchLabelIterator_ConsecutiveBasicBlocks()
    {
        int i, size;
        BranchStructure structure = getBranchStructure();
        
        HashSet<Integer> jumps  = new HashSet<Integer>();
        HashSet<Integer> blocks = new HashSet<Integer>();
        
        size = structure.size();
        for(i = 0; i < size; i++)
        {
            BranchEntry now = structure.get(i);
            
            if(now.isBranch())
                { jumps.add(now.getBranchLabel()); }
            else if(i > 0)
            {
                BranchEntry pre = structure.get(i - 1);
    
                if(pre.isBlock() && now.isBlock())
                    { blocks.add(i); }
            }
        }
        
        return jumps.containsAll(blocks);
    }
    
    private boolean filterBranchLabelIterator()
    {
        if((flags & DO_NOT_ITERATE_CONSECUTIVE_BASIC_BLOCKS) != 0)
        {
            if(!filterBranchLabelIterator_ConsecutiveBasicBlocks())
                { return false; }
        }
        
        return true;
    }
    
    private boolean initLengthIterator()
    {
        lengthIterator = new Int32RangeIterator(minLength, maxLength);
        lengthIterator.init();
        
        return lengthIterator.hasValue();
    }
    
    private boolean initBranchNumberIterator()
    {
        branchNumberIterator = new Int32RangeIterator(minBranchNumber, maxBranchNumber);
        branchNumberIterator.init();
        
        return branchNumberIterator.hasValue();
    }
    
    private boolean initBranchPositionIterator_NoFiltration(int length, int branchNumber)
    {
        branchPositionIterator = new IndexArrayIterator(0, length - 1, branchNumber);
        branchPositionIterator.init();
        
        return true;
    }

    private boolean initBranchPositionIterator(int length, int branchNumber)
    {
        branchPositionIterator = new IndexArrayIterator(0, length - 1, branchNumber);
        branchPositionIterator.init();
        
        while(branchPositionIterator.hasValue())
        {
            if(filterBranchPositionIterator())
                { return true; }
            
            branchPositionIterator.next();
        }
        
        return false;
    }
    
    private boolean initBranchLabelIterator(int length, int branchNumber)
    {
        branchLabelIterator = new ProductIterator();
        
        for(int i = 0; i < branchNumber; i++)
            { branchLabelIterator.registerIterator(new Int32RangeIterator(0, (length - 1) + (delaySlot ? branchNumber : 0))); }
        
        branchLabelIterator.init();
        
        while(branchLabelIterator.hasValue())
        {
            if(filterBranchLabelIterator())
            {
                // If there are no feasible traces, try use other labels
                if(initBranchTraceIterator(getBranchStructure(), maxBranchExecution))
                    { return true; }
            }
            
            branchLabelIterator.next();
        }
        
        return false;
    }

    private boolean initBranchIterator(int branchNumber)
    {
        int count = branches.countEquivalenceClass();
        
        branchIterator = new ProductIterator();
        
        for(int i = 0; i < branchNumber; i++)
            { branchIterator.registerIterator(new Int32RangeIterator(0, count - 1)); }
        
        branchIterator.init();
        
        return branchIterator.hasValue();
    }
    
    private boolean initSlotIterator(int branchNumber)
    {
        int count = allSlots.countEquivalenceClass();
        
        slotIterator = new ProductIterator();
        
        for(int i = 0; i < branchNumber; i++)
            { slotIterator.registerIterator(new Int32RangeIterator(0, count - 1)); }
        
        slotIterator.init();
        
        return slotIterator.hasValue();
    }
    
    private boolean initBlockIterator(int length, int branchNumber)
    {
        int count = allBlocks.countEquivalenceClass();
        
        blockIterator = new ProductIterator();
        
        for(int i = 0; i < length - branchNumber; i++)
            { blockIterator.registerIterator(new Int32RangeIterator(0, count - 1)); }
        
        blockIterator.init();
        
        return blockIterator.hasValue();
    }
    
    private boolean initBranchTraceIterator(BranchStructure structure, int maxBranchExecution)
    {
        branchTraceIterator = new BranchTraceIterator(structure, maxBranchExecution);
        
        branchTraceIterator.init();
        
        return branchTraceIterator.hasValue();
    }
    
    private void classifyAndCheckInstructions()
    {
        int i, j, size1, size2;
        
        branches.clear();
        allSlots.clear();
        safeSlots.clear();
        allBlocks.clear();
        
        // Classify instructions
        size1 = countInstruction();
        for(i = 0; i < size1; i++)
        {
            Instruction instruction = getInstruction(i);
            
            if(instruction.isBranchInstruction())
                { branches.registerInstruction(instruction.getEquivalenceClass(), instruction); }
            else
            {
                allSlots.registerInstruction(instruction.getEquivalenceClass(), instruction);
                allBlocks.registerInstruction(instruction.getEquivalenceClass(), instruction);
                
                if(!instruction.canThrowException())
                {
                    safeSlots.registerInstruction(instruction.getEquivalenceClass(), instruction);
                    safeInstructions.add(instruction);
                }
                else
                {
                    safeSlots.registerInstructions(new EquivalenceClass(instruction.getEquivalenceClass()));
                }
            }
        }
        
        // Check branch instructions
        if((size1 = branches.countEquivalenceClass()) == 0)
            { throw new IllegalStateException("There are no branch instructions, while minimal branch number is not zero"); }
        
        conditionalBranch = new boolean[size1];
        
        for(i = 0; i < size1; i++)
        {
            if((size2 = branches.countInstruction(i)) == 0)
                { continue; }
            
            conditionalBranch[i] = branches.getInstruction(i, 0).isConditionalBranch();
            
            for(j = 1; j < size2; j++)
            {
                Instruction instruction = branches.getInstruction(i, j);
                Situation situation = instruction.getSituation();
                
                if(situation == null)
                    { throw new IllegalStateException("Situation for branch instruction is null"); }
                
                if(!(situation instanceof BranchTraceSituation))
                    { throw new IllegalStateException("Incorrect class of situation for branch instruction"); }
                
                if(instruction.isConditionalBranch() != conditionalBranch[i])
                    { throw new IllegalStateException("Equivalence class contains both conditional and unconditional branches"); }
            }
        }
    }
    
    /** Initializes the iterator. */
    @Override
    public void init()
    {
        hasValue = true;
        
        classifyAndCheckInstructions();
        
        if(!initLengthIterator())
            { stop(); return; }
        if(!initBranchNumberIterator())
            { stop(); return; }
        if(!initBranchPositionIterator(minLength, minBranchNumber))
            { stop(); return; }
        if(!initBranchLabelIterator(minLength, minBranchNumber))
            { stop(); return; }
        if(!initBranchIterator(minBranchNumber))
            { stop(); return; }
        if(delaySlot && !initSlotIterator(minBranchNumber))
            { stop(); return; }
        if(!initBlockIterator(minLength, minBranchNumber))
            { stop(); return; }
        if(!initBranchTraceIterator(getBranchStructure(), maxBranchExecution))
            { stop(); return; }
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

    private boolean nextLengthIterator()
    {
        if(lengthIterator.hasValue())
        {
            lengthIterator.next();

            if(lengthIterator.hasValue())
            {
                int length = lengthIterator.int32Value();
                int branchNumber = branchNumberIterator.int32Value();

                if(!initBranchIterator(branchNumber))
                    { return false; }
                if(!initSlotIterator(branchNumber))
                    { return false; }
                if(!initBlockIterator(length, branchNumber))
                    { return false; }
                if(!initBranchPositionIterator(length, branchNumber))
                    { return false; }
                if(!initBranchLabelIterator(length, branchNumber))
                    { return false; }
                if(!initBranchTraceIterator(getBranchStructure(), maxBranchExecution))
                    { return false; }
                
                return true;
            }
        }
        
        return false;
    }
    
    private boolean nextBranchNumberIterator()
    {
        if(branchNumberIterator.hasValue())
        {
            branchNumberIterator.next();

            if(branchNumberIterator.hasValue())
            {
                int length = lengthIterator.int32Value();
                int branchNumber = branchNumberIterator.int32Value();

                if(!initBranchIterator(branchNumber))
                    { return false; }
                if(!initSlotIterator(branchNumber))
                    { return false; }
                if(!initBlockIterator(length, branchNumber))
                    { return false; }
                if(!initBranchPositionIterator_NoFiltration(length, branchNumber))
                    { return false; }
                if(!initBranchLabelIterator(length, branchNumber))
                    { return false; }
                if(!initBranchTraceIterator(getBranchStructure(), maxBranchExecution))
                    { return false; }
                
                return true;
            }
        }
        
        return false;
    }

    private boolean nextBranchPositionIterator()
    {
        int length = lengthIterator.int32Value();
        int branchNumber = branchNumberIterator.int32Value();
        
        while(branchPositionIterator.hasValue())
        {
            branchPositionIterator.next();
            
            if(branchPositionIterator.hasValue())
            {
                if(filterBranchPositionIterator())
                {
                    if(!initBranchLabelIterator(length, branchNumber))
                        { return false; }
                    if(!initBranchIterator(branchNumber))
                        { return false; }
                    if(!initSlotIterator(branchNumber))
                        { return false; }
                    if(!initBlockIterator(length, branchNumber))
                        { return false; }
                    if(!initBranchTraceIterator(getBranchStructure(), maxBranchExecution))
                        { return false; }
                    
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean nextBranchLabelIterator()
    {
        int length = lengthIterator.int32Value();
        int branchNumber = branchNumberIterator.int32Value();

        while(branchLabelIterator.hasValue())
        {
            branchLabelIterator.next();
            
            if(branchLabelIterator.hasValue())
            {
                if(filterBranchLabelIterator())
                {
                    if(!initBranchIterator(branchNumber))
                        { return false; }
                    if(!initSlotIterator(branchNumber))
                        { return false; }
                    if(!initBlockIterator(length, branchNumber))
                        { return false; }
                    
                    // If there are no feasible traces, try use other labels
                    if(!initBranchTraceIterator(getBranchStructure(), maxBranchExecution))
                        { continue; }
                    
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean nextBranchIterator()
    {
        int length = lengthIterator.int32Value();
        int branchNumber = branchNumberIterator.int32Value();
        
        if(branchIterator.hasValue())
        {
            branchIterator.next();
                        
            if(branchIterator.hasValue())
            {
                if(!initSlotIterator(branchNumber))
                    { return false; }
                if(!initBlockIterator(length, branchNumber))
                    { return false; }
                if(!initBranchTraceIterator(getBranchStructure(), maxBranchExecution))
                    { return false; }

                return true;
            }
        }
        
        return false;
    }
    
    private boolean nextSlotIterator()
    {
        int length = lengthIterator.int32Value();
        int branchNumber = branchNumberIterator.int32Value();
        
        if(slotIterator.hasValue())
        {
            slotIterator.next();

            if(slotIterator.hasValue())
            {
                if(!initBlockIterator(length, branchNumber))
                    { return false; }
                if(!initBranchTraceIterator(getBranchStructure(), maxBranchExecution))
                    { return false; }
                
                return true;
            }
        }
        
        return false;
    }

    private boolean nextBlockIterator()
    {
        if(blockIterator.hasValue())
        {
            blockIterator.next();
            
            if(blockIterator.hasValue())
            {
                if(!initBranchTraceIterator(getBranchStructure(), maxBranchExecution))
                    { return false; }
                
                return true;
            }
        }
        
        return false;
    }    
    
    private boolean nextBranchTraceIterator()
    {
        if(branchTraceIterator.hasValue())
        {
            branchTraceIterator.next();
            
            if(branchTraceIterator.hasValue())
                { return true; }
        }
        
        return false;
    }
    
    /** Randomizes test template within one iteration. */
    @Override
    public void randomize()
    {
        
    }
    
    /** Makes iteration iteration. */
    @Override
    public void next()
    {
        if(!hasValue())
            { return; }
        if(nextBranchTraceIterator())
            { return; }
        if(nextBlockIterator())
            { return; }
        if(delaySlot && nextSlotIterator())
            { return; }
        if(nextBranchIterator())
            { return; }
        if(nextBranchLabelIterator())
            { return; }
        if(nextBranchPositionIterator())
            { return; }
        if(nextBranchNumberIterator())
            { return; }
        if(nextLengthIterator())
            { return; }
        
        stop();
    }

    /** Stops the iterator. */
    @Override
    public void stop()
    {
        hasValue = false;
    }

    /** Returns a copy of the iterator. */
    @Override
    public BranchTemplateIterator clone()
    {
        return new BranchTemplateIterator(this);
    }
}
