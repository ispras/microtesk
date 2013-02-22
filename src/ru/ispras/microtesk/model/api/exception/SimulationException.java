/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * SimulationException.java, Nov 16, 2012 7:11:00 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.exception;

/**
 * The SimulationException exception is the base exception for all 
 * simulation exceptions (runtime exception) that occur in the model.
 * 
 * Simulation exceptions occur when during the execution of instructions
 * an incorrect state was reached or an incorrect result was produced.  
 * 
 * @author Andrei Tatarnikov
 */

public abstract class SimulationException extends MicroTESKException
{
    private static final long serialVersionUID = -2174246506454270262L;

    public SimulationException()
    {
    }

    public SimulationException(String message)
    {
        super(message);
    }

    public SimulationException(Throwable cause)
    {
        super(cause);
    }

    public SimulationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
