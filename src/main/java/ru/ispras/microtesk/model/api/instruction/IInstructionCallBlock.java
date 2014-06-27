/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IInstructionBlockCall.java, Nov 15, 2012 10:30:21 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.instruction;

import ru.ispras.microtesk.model.api.simnml.instruction.InstructionCall;

/**
 * The IInstructionCallBlock interface describes a block of instruction
 * calls. It allows execution of the entire block and accessing individual calls.
 * 
 * @author Andrei Tatarnikov
 */

@Deprecated
public interface IInstructionCallBlock
{
    /**
     * Returns the number of calls in the block.
     * 
     * @return Call count.
     */
    
    public int getCount();
    
    /**
     * Returns the name of the instruction to be called by the index of the call.
     * 
     * @param index Index of the call in the block.
     * @return Instruction name.
     */
    
    public String getCallName(int index);
    
    /**
     * Returns the call by its index.
     * 
     * @param index Index of the call in the block.
     * @return Instruction call object.
     */
    
    public InstructionCall getCall(int index);
    
    public void execute();
    public void print();
}
