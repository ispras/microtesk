/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IModelStateMonitor.java, Nov 7, 2012 3:22:58 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.monitor;

/**
 * The IModelStateMonitor interface provides methods for requesting information on
 * the current state of the model.
 * 
 * @author Andrei Tatarnikov
 */

public interface IModelStateMonitor
{
    /**
     * Returns the value stored in the first location in the specified register array. 
     * 
     * @param name Name of the register array.
     * @return Value stored in the register.
     */

    public IStoredValue readRegisterValue(String name);

    /**
     * Returns the value stored in the specified location of the specified register array.
     * 
     * @param name Name of the register array.
     * @param index Index of the location in the register array.
     * @return Value stored in the register.
     */

    public IStoredValue readRegisterValue(String name, int index);

    /**
     * Returns the value stored in the first location in the specified memory array.
     * 
     * @param name Name of the memory array.
     * @return Value stored in a memory location. 
     */

    public IStoredValue readMemoryValue(String name);

    /**
     * Returns the value stored in the specified location of the specified memory array.
     * 
     * @param name Name of the memory array.
     * @param index Index of the location in the memory array.
     * @return Value stored in a memory location.
     */

    public IStoredValue readMemoryValue(String name, int index);
}
