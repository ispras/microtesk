/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * InstructionCallBlockBuilder.java, Nov 21, 2012 5:20:00 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.instruction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.exception.config.UnsupportedInstructionException;
import ru.ispras.microtesk.model.api.simnml.instruction.InstructionCall;

/**
 * The InstructionCallBlockBuilder class implements logic that builds 
 * an instruction call block.
 * 
 * @author Andrei Tatarnikov
 */

@Deprecated
public class InstructionCallBlockBuilder implements IInstructionCallBlockBuilder
{
    /**
     * The CallBuilderEntry class stores builders for individual instructions
     * and the names of corresponding instructions.
     * 
     * @author Andrei Tatarnikov
     */
    
    public static class CallBuilderEntry
    {
        public final String name;
        public final IInstructionCallBuilderEx builder;

        public CallBuilderEntry(String name, IInstructionCallBuilderEx builder)
        {
            assert null != name;
            assert null != builder;
            
            this.name = name;
            this.builder = builder;
        }
    }

    private final IInstructionSet                  instructionSet;
    private final Collection<CallBuilderEntry> callBuilderEntries;

    /**
     * Creates a builder of an instruction block.
     * 
     * @param instructionSet Set of supported instructions.
     */
    
    public InstructionCallBlockBuilder(IInstructionSet instructionSet)
    {
        this.instructionSet     = instructionSet;
        this.callBuilderEntries = new ArrayList<CallBuilderEntry>();
    }    

    @Override
    public IInstructionCallBuilder addCall(String name) throws ConfigurationException
    {
        checkSupportedInstruction(name);

        final IInstructionEx instruction        = instructionSet.getInstruction(name);
        final IInstructionCallBuilderEx builder = instruction.createCallBuilder();

        callBuilderEntries.add(new CallBuilderEntry(name, builder));
        return builder;
    }

    @Override
    public IInstructionCallBlock getCallBlock() throws ConfigurationException
    {
        checkEmptyBlock();

        final List<InstructionCallBlock.CallEntry> callEntries =
            new ArrayList<InstructionCallBlock.CallEntry>();

        for (CallBuilderEntry builderEntry : callBuilderEntries)
        {
            final InstructionCall call = builderEntry.builder.getCall();
            callEntries.add(new InstructionCallBlock.CallEntry(builderEntry.name, call));
        }        

        return new InstructionCallBlock(callEntries);
    }
    
    private void checkEmptyBlock() throws EmptyInstructionCallBlockException
    {
        if (callBuilderEntries.isEmpty())
            throw new EmptyInstructionCallBlockException();
    }

    private void checkSupportedInstruction(String name) throws UnsupportedInstructionException
    {
        if (!instructionSet.supportsInstruction(name))
           throw new UnsupportedInstructionException(name);
    }
    
    public static class EmptyInstructionCallBlockException extends ConfigurationException
    {
        private static final long serialVersionUID = 8527038351365119153L;

        private static final String ERROR_MESSAGE = "The current instruction call block is empty.";

        public EmptyInstructionCallBlockException()
        {
            super(ERROR_MESSAGE);
        }        
    }
}
