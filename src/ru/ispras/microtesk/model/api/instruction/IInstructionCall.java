/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IInstructionCall.java, Nov 6, 2012 3:30:15 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.instruction;

/**
 * The IInstructionCall interface provides methods to run execution
 * simulation of some instruction within the processor model.   
 *
 * @author Andrei Tatarnikov
 */

public interface IInstructionCall
{
    /**
     * Runs executing simulation of a corresponding instruction described within the model.
     */

    public void execute();

    /**
     * Return the assembly code for the specified call (for example, the addition instruction 
     * of a MIPS processor: addu $1, $1, $2).
     * 
     * @return Text for the instruction call (assembler code). 
     */

    public String getText();

    /**
     * Prints the assembler code of the current instruction call to the output
     * stream provided by MicroTESK. 
     */

    public void print();
}
