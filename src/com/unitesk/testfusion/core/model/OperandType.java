/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: OperandType.java,v 1.11 2009/04/22 11:32:07 kamkin Exp $
 */

package com.unitesk.testfusion.core.model;

/**
 * Type of instruction operand.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class OperandType
{
    /** Immediate operand. */
    public static final OperandType IMMEDIATE = new OperandType("Immediate", false);
    
    /** Register operand. */
    public static final OperandType REGISTER  = new OperandType("Register", true);
    
    /** Name of the operand type. */
    protected String name;
    
    /**
     * Flag that is <code>true</code> if the operand is register;
     * <code>false</code> otherwise.
     */
    protected boolean register;
    
    /**
     * Size of the register block if a group of register is used as an operand.
     * Sometimes, instructions use pairs of instructions.
     */
    protected int blockSize;
    
    /**
     * Basic operand type for register blocks and sub-types. If operand type is
     * not register block or sub-type, basic type is equal to <code>this</code>.
     */
    protected OperandType basicOperandType;
    
    /**
     * Constructor of register block.
     * 
     * @param <code>name</code> the name of the operand type.
     * 
     * @param <code>basicOperandType</code> the basic operand type.
     * 
     * @param <code>blockSize</code> the size of register block.
     */
    protected OperandType(String name, OperandType basicOperandType, int blockSize)
    {
        this.name = name;
        this.register = basicOperandType.isRegister();
        this.basicOperandType = basicOperandType;
        this.blockSize = blockSize;
    }

    /**
     * Constructor of operand sub-type.
     * 
     * @param <code>name</code> the name of the operand type.
     * 
     * @param <code>register</code> the flag that is <code>true</code>
     * 
     * @param <code>basicOperandType</code> the basic operand type.
     */
    protected OperandType(String name, OperandType basicOperandType)
    {
        this(name, basicOperandType, 1);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of the operand type.
     * 
     * @param <code>register</code> the flag that is <code>true</code>
     */
    protected OperandType(String name, boolean register)
    {
        this.name = name;
        this.register = register;
        this.basicOperandType = null;
        this.blockSize = 1;
    }
    
    /**
     * Constructor of the register operand type.
     * 
     * @param <code>name</code> the name of the operand type.
     */
    protected OperandType(String name)
    {
        this(name, true);
    }

    /**
     * Returns the name of the operand type.
     * 
     * @return the name of the operand type.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Checks if the operand type is immediate.
     * 
     * @return <code>true</code> if the operand type is immediate;
     *         <code>false</code> otherwise.
     */
    public boolean isImmediate()
    {
        return !isRegister();
    }
    
    /**
     * Checks if the operand type is register.
     * 
     * @return <code>true</code> if the operand type is register;
     *         <code>false</code> otherwise.
     */
    public boolean isRegister()
    {
        return register;
    }

    /**
     * Returns basic operand type.
     * 
     * @return basic operand type.
     */
    public OperandType getBasicOperandType()
    {
        return basicOperandType;
    }

    /**
     * Checks if the operand type is root of sub-type hierarchy.
     *  
     * @return <code>true</code> if the operand type is root of sub-type
     *         hierarchy; <code>false</code> otherwise.
     */
    public boolean isRoot()
    {
        return basicOperandType == null;
    }
    
    /**
     * Returns root operand type.
     * 
     * @return root operand type.
     */
    public OperandType getRootOperandType()
    {
        if(isRoot())
            { return this; }
        
        return basicOperandType.getRootOperandType();
    }
    
    /**
     * Checks if the operand type is a sub-type of <code>operandType</code>.
     * 
     * @param <code>operandType</code> the other operand type. 
     * 
     * @return <code>true</code> if the operand type is a sub-type of
     *         <code>operandType</code>; <code>false</code> otherwise.
     */
    public boolean isSubtypeOf(OperandType operandType)
    {
        if(equals(operandType))
            { return true; }
        
        if(isRoot())
            { return false; }
        
        return basicOperandType.isSubtypeOf(operandType);
    }
    
    /**
     * Checks if the operand type is a proper sub-type of
     * <code>operandType</code>.
     * 
     * @param <code>operandType</code> the other operand type. 
     * 
     * @return <code>true</code> if the operand type is a proper sub-type of
     *         <code>operandType</code>; <code>false</code> otherwise.
     */
    public boolean isProperSubtypeOf(OperandType operandType)
    {
        if(isRoot())
            { return false; }
        
        return basicOperandType.isSubtypeOf(operandType);
    }
    
    /**
     * Checks if the operand type corresponds to block of registers.
     * 
     * @return <code>true</code> if the operand type is block of registers;
     *         <code>false</code> otherwise.
     */
    public boolean isBlock()
    {
        return blockSize > 1;
    }
    
    /**
     * Checks if the operand type corresponds to block of registers of the
     * given type.
     * 
     * @param <code>operandType</code> the register type.
     * 
     * @return <code>true</code> if the operand type is block of registers of
     *         the given type; <code>false</code> otherwise.
     */
    public boolean isBlockConsistingOf(OperandType operandType)
    {
        return isBlock() && basicOperandType.equals(operandType);
    }
    
    /**
     * Returns block size.
     * 
     * @return block size.
     */
    public int getBlockSize()
    {
        return blockSize;
    }
    
    /**
     * Compares the operand types.
     * 
     * @param  <code>o</code> the operand type to be compared.
     * 
     * @return <code>true</code> if operand types are equal; <code>false</code>
     *         otherwise.
     */
    public boolean equals(Object o)
    {
        if(!(o instanceof OperandType))
            { return false; }
        
        OperandType r = (OperandType)o;
        
        return r.name.equals(name);
    }
    
    /**
     * Returns a hash code value for the object.
     */
    public int hashCode()
    {
        return name.hashCode();
    }
    
    /**
     * Returns the name of the operand type.
     * 
     * @return the name of the operand type.
     */
    public String toString()
    {
        return getName();
    }
}
