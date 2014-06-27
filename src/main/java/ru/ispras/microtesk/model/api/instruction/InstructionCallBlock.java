/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * InstructionCallBlock.java, Nov 16, 2012 3:30:14 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.instruction;

import java.util.List;

import ru.ispras.microtesk.model.api.simnml.instruction.InstructionCall;

/**
 * The InstructionCallBlock class is a basic implementation of
 * an instruction call block. It stores a list of instruction
 * call objects and allows running them and getting their textual
 * representation.   
 * 
 * @author Andrei Tatarnikov
 */

@Deprecated
public class InstructionCallBlock implements IInstructionCallBlock
{
    /**
     * The CallEntry class is a class for storing an instruction
     * call object and the name of corresponding instruction. This class
     * is need to initialize the enclosing InstructionCallBlock class.
     * 
     * @author Andrei Tatarnikov
     */
    
    public static class CallEntry
    {
        public final String name;
        public final InstructionCall call;

        public CallEntry(String name, InstructionCall call)
        {
            this.name = name;
            this.call = call;
        }
    }

    private final List<CallEntry> callEntries;

    public InstructionCallBlock(List<CallEntry> callEntries)
    {
        this.callEntries = callEntries;
    }

    public void execute()
    {
        for (CallEntry entry : callEntries)
        {
            final InstructionCall call = entry.call; 
            call.execute();
        }
    }

    public String getText()
    {
        final StringBuilder sb = new StringBuilder();

        for (CallEntry entry : callEntries)
        {
            final InstructionCall call = entry.call;

            if (0 != sb.length())
                sb.append("%n");

            sb.append(call.getText());            
        }

        return sb.toString();
    }

    public void print()
    {
        for (CallEntry entry : callEntries)
        {
            final InstructionCall call = entry.call; 
            call.print();
        }
    }

    @Override
    public int getCount()
    {
        return callEntries.size();
    }

    @Override
    public String getCallName(int index)
    {
        return callEntries.get(index).name;
    }

    @Override
    public InstructionCall getCall(int index)
    {
        return callEntries.get(index).call;
    }
}
