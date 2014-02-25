/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IOperation.java, Nov 9, 2012 8:01:52 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.simnml.instruction;

/**
 * The IOperation interface is the base interfaces for operations
 * described by Op statements in the Sim-nML language. 
 * 
 * @author Andrei Tatarnikov
 */

public interface IOperation extends IPrimitive
{
    /**
     * The IInfo interface provides information on an operation object or
     * a group of operation object united by an OR rule. This information
     * is needed for runtime checks to make sure that instructions are
     * configured with proper operation objects.
     * 
     * @author Andrei Tatarnikov
     */

    public interface IInfo
    {
        /**
         * Returns the name of the operation or the name of the OR rule used
         * for grouping operations.
         * 
         * @return The mode name.
         */

        public String getName();

        /**
         * Checks if the current operation (or group of operations) implements
         * (or contains) the specified operation. This method is used in runtime
         * checks to make sure that the object composition in the model is valid.   
         * 
         * @param op An operation object.
         * @return true if the operation is supported or false otherwise. 
         */

        public boolean isSupported(IOperation op); 
    }
}
