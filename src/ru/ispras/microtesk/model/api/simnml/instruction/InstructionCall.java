/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * InstructionCall.java, Nov 16, 2012 4:34:11 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.simnml.instruction;

import ru.ispras.microtesk.model.api.instruction.IInstructionCall;

/**
 * The InstructionCall class is a Sim-nML-based implementation
 * of the IInstructionCall interface. 
 * 
 * @author Andrei Tatarnikov
 */

public class InstructionCall implements IInstructionCall
{
    private final IOperation instruction;
    
    /**
     * Creates an instruction call object based on an Sim-nML operation.
     * The operation usually represents a composite object encapsulating
     * a hierarchy of aggregated operations that make up a microprocessor
     * instruction. 
     * 
     * @param instruction The root operation of the Sim-nML operation hierarchy.
     */
    
    public InstructionCall(IOperation instruction)
    {
        this.instruction = instruction;
    }

    @Override
    public void execute()
    {
        instruction.action();        
    }

    @Override
    public String getText()
    {
        return instruction.syntax();
    }

    @Override
    public void print()
    {
        System.out.println(getText());
    }
}
