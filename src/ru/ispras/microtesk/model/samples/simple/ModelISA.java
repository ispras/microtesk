/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ModelISA.java, Dec 1, 2012 11:46:09 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.samples.simple;

import java.util.Collections;

import ru.ispras.microtesk.model.api.debug.AbstractModelISA;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;

class ModelISA extends AbstractModelISA
{
    private ModelISA() {}

    public static void mov(Mode op1, Mode op2) throws ConfigurationException
    {
        setupCall("Mov", new Argument("op1", op1), new Argument("op2", op2));        
    }

    public static void add(Mode op1, Mode op2) throws ConfigurationException
    {
        setupCall("Add", new Argument("op1", op1), new Argument("op2", op2)); 
    }

    public static void sub(Mode op1, Mode op2) throws ConfigurationException
    {
        setupCall("Sub", new Argument("op1", op1), new Argument("op2", op2)); 
    }

    public static Mode reg(int i)
    {
        return new Mode("REG", Collections.singletonMap("i", i));
    }

    public static Mode ireg(int i)
    {
        return new Mode("IREG", Collections.singletonMap("i", i));
    }

    public static Mode mem(int i)
    {
        return new Mode("MEM", Collections.singletonMap("i", i));
    }

    public static Mode imm(int i)
    {
        return new Mode("IMM", Collections.singletonMap("i", i));
    }
}
