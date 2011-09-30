/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Operand.java,v 1.9 2009/04/27 09:30:24 kamkin Exp $
 */

package com.unitesk.testfusion.core.model;

import com.unitesk.testfusion.core.dependency.Dependencies;
import com.unitesk.testfusion.core.dependency.Dependency;
import com.unitesk.testfusion.core.model.register.Register;

/**
 * Operand of microprocessor instruction.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Operand
{
    /** Input operand. */
    public static final int INPUT  = 0x01;
    
    /** Output operand. */
    public static final int OUTPUT = 0x02;
    
    /** Input/output operand. */
    public static final int INOUT  = Operand.INPUT | Operand.OUTPUT;
    
    /** Order number of the operand. */
    protected int number;
    
    /** Name of the operand. */
    protected String name;
    
    /** Value of the operand. */
    protected long[] value;
    
    /** Descriptor of the operand value. */
    protected Object descriptor;
    
    /** Register of the operand. */
    protected Register register;
    
    /** Input operand flag. */
    protected boolean input;
    
    /** Output operand flag. */
    protected boolean output;
    
    /** Type of the operand. */
    protected OperandType operand;
    
    /** Content type of the operand. */
    protected ContentType content;

    /** Fixed operand flag. */
    protected boolean fixed;
    
    /** Instruction of the operand. */
    protected Instruction instruction;
    
    /** Forward dependencies. */
    protected Dependencies forward = new Dependencies();
    
    /** Back dependencies. */
    protected Dependencies backward = new Dependencies();
    
    /**
     * Constructor.
     * 
     * @param <code>instruction</code> the instruction of the operand.
     * 
     * @param <code>name</code> the name of the operand.
     * 
     * @param <code>inout</code> the data flow direction (<code>INPUT</code>,
     *        <code>OUTPUT</code>, or <code>INOUT</code>). 
     * 
     * @param <code>operand</code> the type of the operand.
     * 
     * @param <code>content</code> the content type of the operand.
     * 
     * @param <code>fixed</code> the fixed operand flag.
     */
    public Operand(Instruction instruction, String name, int inout,
        OperandType operand, ContentType content, boolean fixed)
    {
        this.instruction = instruction;
        this.name        = name;
        this.input       = (inout & Operand.INPUT)  != 0;
        this.output      = (inout & Operand.OUTPUT) != 0;
        this.operand     = operand;
        this.content     = content;
        this.fixed       = fixed;
        
        this.value = new long[operand.isBlock() ? operand.getBlockSize() : 1];
    }

    /**
     * Constructor.
     * 
     * @param <code>instruction</code> the instruction of the operand.
     * 
     * @param <code>name</code> the name of the operand.
     * 
     * @param <code>inout</code> the data flow direction (<code>INPUT</code>,
     *        <code>OUTPUT</code>, or <code>INOUT</code>). 
     * 
     * @param <code>operand</code> the type of the operand.
     * 
     * @param <code>content</code> the content type of the operand.
     */
    public Operand(Instruction instruction, String name, int inout,
        OperandType operand, ContentType content)
    {
        this(instruction, name, inout, operand, content, false);
    }
    
    /**
     * Returns the order number of the operand.
     * 
     * @return the order number of the operand.
     */
    public int getNumber()
    {
        return number;
    }
    
    /**
     * Sets the order number of the operand.
     * 
     * @param <code>number</code> the order number of the operand.
     */
    public void setNumber(int number)
    {
        this.number = number;
    }
    
    /**
     * Returns the name of the operand.
     * 
     * @return the name of the operand.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Checks if the operand has unpredictable value.
     * 
     * @return <code>true</code> if the operand has unpredictable balue;
     *         <code>false</code> otherwise.
     */
    public boolean isUnpredictable()
    {
        return operand.isRegister() && register.isUnpredictable();
    }
    
    /**
     * Sets the unpredictable/predictable status of the operand.
     * 
     * @param <code>unpredictable</code> the unpredictable/predictable status
     *        of the operand.
     */
    public void setUnpredictable(boolean unpredictable)
    {
        if(operand.isRegister())
            { register.setUnpredictable(unpredictable); }
    }

    /**
     * Returns the operand value.
     * 
     * @param <code>n</code> the number of operand part.
     * 
     * @param <code>value</code> the operand value.
     */
    public boolean getBooleanValue(int n)
    {
        return value[n] != 0;
    }
    
    /**
     * Sets the operand value.
     * 
     * @param <code>n</code> the number of operand part.
     * 
     * @param <code>value</code> the register value.
     */
    public void setBooleanValue(int n, boolean value)
    {
        this.value[n] = value ? 1 : 0;
    }
    
    /**
     * Returns the operand value.
     * 
     * @param <code>n</code> the number of operand part.
     * 
     * @param <code>value</code> the operand value.
     */
    public byte getByteValue(int n)
    {
        return (byte)value[n];
    }
    
    /**
     * Sets the operand value.
     * 
     * @param <code>n</code> the number of operand part.
     * 
     * @param <code>value</code> the register value.
     */
    public void setByteValue(int n, byte value)
    {
        this.value[n] = value;
    }

    /**
     * Returns the operand value.
     * 
     * @param <code>n</code> the number of operand part.
     * 
     * @param <code>value</code> the operand value.
     */
    public short getShortValue(int n)
    {
        return (short)value[n];
    }
    
    /**
     * Sets the operand value.
     * 
     * @param <code>n</code> the number of operand part.
     * 
     * @param <code>value</code> the register value.
     */
    public void setShortValue(int n, short value)
    {
        this.value[n] = value;
    }
        
    /**
     * Returns the operand value.
     * 
     * @param <code>n</code> the number of operand part.
     * 
     * @param <code>value</code> the operand value.
     */
    public int getIntegerValue(int n)
    {
        return (int)value[n];
    }
    
    /**
     * Sets the operand value.
     * 
     * @param <code>n</code> the number of operand part.
     * 
     * @param <code>value</code> the register value.
     */
    public void setIntegerValue(int n, int value)
    {
        this.value[n] = value;
    }
    
    /**
     * Returns the operand value.
     * 
     * @param <code>n</code> the number of operand part.
     * 
     * @param <code>value</code> the operand value.
     */
    public long getLongValue(int n)
    {
        return value[n];
    }
    
    /**
     * Sets the operand value.
     * 
     * @param <code>n</code> the number of operand part.
     * 
     * @param <code>value</code> the register value.
     */
    public void setLongValue(int n, long value)
    {
        this.value[n] = value;
    }

    /**
     * Returns the operand value.
     * 
     * @param <code>n</code> the number of operand part.
     * 
     * @param <code>value</code> the operand value.
     */
    public float getSingleValue(int n)
    {
        return Float.intBitsToFloat(getIntegerValue(n));
    }
    
    /**
     * Sets the operand value.
     * 
     * @param <code>n</code> the number of operand part.
     * 
     * @param <code>value</code> the register value.
     */
    public void setSingleValue(int n, float value)
    {
        this.value[n] = Float.floatToRawIntBits(value);
    }
    
    /**
     * Returns the operand value.
     * 
     * @param <code>n</code> the number of operand part.
     * 
     * @param <code>value</code> the operand value.
     */
    public double getDoubleValue(int n)
    {
        return Double.longBitsToDouble(value[n]);
    }
    
    /**
     * Sets the operand value.
     * 
     * @param <code>n</code> the number of operand part.
     * 
     * @param <code>value</code> the register value.
     */
    public void setDoubleValue(int n, double value)
    {
        this.value[n] = Double.doubleToRawLongBits(value);
    }
    
    /**
     * Returns the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public boolean getBooleanValue()
    {
        return getBooleanValue(0);
    }
    
    /**
     * Sets the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public void setBooleanValue(boolean value)
    {
        setBooleanValue(0, value);
    }
    
    /**
     * Returns the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public byte getByteValue()
    {
        return getByteValue(0);
    }
    
    /**
     * Sets the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public void setByteValue(byte value)
    {
        setByteValue(0, value);
    }

    /**
     * Returns the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public short getShortValue()
    {
        return getShortValue(0);
    }
    
    /**
     * Sets the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public void setShortValue(short value)
    {
        setShortValue(0, value);
    }
        
    /**
     * Returns the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public int getIntegerValue()
    {
        return getIntegerValue(0);
    }
    
    /**
     * Sets the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public void setIntegerValue(int value)
    {
        setIntegerValue(0, value);
    }
    
    /**
     * Returns the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public long getLongValue()
    {
        return getLongValue(0);
    }
    
    /**
     * Sets the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public void setLongValue(long value)
    {
        setLongValue(0, value);
    }

    /**
     * Returns the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public float getSingleValue()
    {
        return getSingleValue(0);
    }
    
    /**
     * Sets the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public void setSingleValue(float value)
    {
        setSingleValue(0, value);
    }
    
    /**
     * Returns the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public double getDoubleValue()
    {
        return getDoubleValue(0);
    }
    
    /**
     * Sets the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public void setDoubleValue(double value)
    {
        setDoubleValue(0, value);
    }
    
    /**
     * Returns the descriptor of the operand.
     * 
     * @return the descriptor of the operand.
     */
    public Object getDescriptor()
    {
        return descriptor;
    }
    
    /**
     * Sets the descriptor of the operand.
     * 
     * @param <code>descriptor</code> the descriptor of the operand.
     */
    public void setDescriptor(Object descriptor)
    {
        this.descriptor = descriptor;
    }
    
    /**
     * Checks if the operand is input.
     * 
     * @return <code>true</code> if the operand is input; <code>false</code>
     *         otherwise.
     */
    public boolean isInput()
    {
        return input;
    }
    
    /**
     * Checks if the operand is output.
     * 
     * @return <code>true</code> if the operand is output; <code>false</code>
     *         otherwise.
     */
    public boolean isOutput()
    {
        return output;
    }
    
    /**
     * Returns the type of the operand.
     * 
     * @return the type of the operand.
     */
    public OperandType getOperandType()
    {
        return operand;
    }
    
    /**
     * Checks if the operand is immediate.
     * 
     * @return <code>true</code> if the operand is immediate; <code>false</code>
     *         otherwise.
     */
    public boolean isImmediate()
    {
        return operand.isImmediate();
    }
    
    /**
     * Checks if the operand is register.
     * 
     * @return <code>true</code> if the operand is register; <code>false</code>
     *         otherwise.
     */
    public boolean isRegister()
    {
        return operand.isRegister();
    }

    /**
     * Checks if the operand is block of registers.
     * 
     * @return <code>true</code> if the operand is register; <code>false</code>
     *         otherwise.
     */
    public boolean isBlock()
    {
        return operand.isBlock();
    }
    
    /**
     * Returns block size.
     * 
     * @return block size.
     */
    public int getBlockSize()
    {
        return operand.getBlockSize();
    }
    
    /**
     * Returns the content type of the operand.
     * 
     * @return the content type of the operand.
     */
    public ContentType getContentType()
    {
        return content;
    }
    
    /**
     * Checks if the operand is fixed.
     * 
     * @return <code>true</code> if the operand is fixed; <code>false</code>
     *         otherwise.
     */
    public boolean isFixed()
    {
        return fixed;
    }
    
    /**
     * Returns the instruction of the operand.
     * 
     * @return the instruction of the operand.
     */
    public Instruction getInstruction()
    {
        return instruction;
    }

    /**
     * Returns the number of forward dependencies.
     * 
     * @return the number of forward dependencies.
     */
    public int countForwardDependency()
    {
        return forward.size();
    }
    
    /**
     * Returns the number of backward dependencies.
     * 
     * @return the number of backward dependencies.
     */
    public int countBackwardDependency()
    {
        return backward.size();
    }
    
    /**
     * Returns the forward dependencies of the instruction.
     * 
     * @return the forward dependencies of the instruction.
     */
    public Dependencies getForwardDependencies()
    {
        return forward;
    }

    /**
     * Returns the set of active dependencies.
     * 
     * @return the set of active dependencies.
     */
    public Dependencies getActiveDependencies()
    {
        int i, size;
        
        Dependencies deps = new Dependencies();
        
        size = forward.size();
        for(i = 0; i < size; i++)
        {
            Dependency dep = forward.get(i);
            
            if(dep.isActive())
                { deps.add(dep); }
        }
        
        return deps;
    }
    
    /**
     * Returns the backward dependencies of the instruction.
     * 
     * @return the backward dependencies of the instruction.
     */
    public Dependencies getBackwardDependencies()
    {
        return backward;
    }
    
    /**
     * Returns the <code>i</code>-th forward dependency of the instruction.
     * 
     * @param  <code>i</code> the index of the forward dependency.
     * 
     * @return the <code>i</code>-th forward dependency of the instruction.
     */
    public Dependency getForwardDependency(int i)
    {
        return forward.get(i);
    }
    
    /**
     * Returns the <code>i</code>-th backward dependency of the instruction.
     * 
     * @param  <code>i</code> the index of the backward dependency.
     * 
     * @return the <code>i</code>-th backward dependency of the instruction.
     */
    public Dependency getBackwardDependency(int i)
    {
        return backward.get(i);
    }
    
    /**
     * Adds the forward dependency to the operand, i.e. from this operand to an
     * other.
     * 
     * @param <code>dep</code> the forward dependency.
     */
    public void registerForwardDependency(Dependency dep)
    {
        forward.add(dep);
    }
    
    /**
     * Adds the backward dependency to the operand, i.e. to this operand from an
     * other.
     * 
     * @param <code>dep</code> the backward dependency.
     */
    public void registerBackwardDependency(Dependency dep)
    {
        backward.add(dep);
    }

    /**
     * Checks if the operand is dependent, i.e. there is an active dependency
     * exists.
     * 
     * @return <code>true</code> if the operand is dependent; <code>false</code>
     *         otherwise.
     */
    public boolean isDependent()
    {
        int i, size;
        
        size = forward.size();
        for(i = 0; i < size; i++)
        {
            Dependency dep = forward.get(i);
            
            if(dep.isActive())
                { return true; }
        }
            
        return false;
    }
    
    /**
     * Checks if the operand is dependent via register dependency.
     * 
     * @return <code>true</code> if the operand is dependent via register
     *         dependency; <code>false</code> otherwise.
     */
    public boolean isRegisterDependent()
    {
        int i, size;
        
        size = forward.size();
        for(i = 0; i < size; i++)
        {
            Dependency dependency = forward.get(i);
            
            if(dependency.isActive() && dependency.isRegisterDependency())
                { return true; }
        }
        
        return false;
    }
    
    /**
     * Returns the register of the operand.
     * 
     * @return the register of the operand.
     */
    public Register getRegister()
    {
        return register;
    }
    
    /**
     * Sets the register of the operand.
     * 
     * @param <code>register</code> the register of the operand.
     */
    public void setRegister(Register register)
    {
        this.register = register;
    }

    /**
     * Returns a string representation of the operand.
     * 
     * @return a string representation of the operand.
     */
    public String toString()
    {
        return instruction.getName() + "[" + instruction.getPosition() + "]." + name + "[" + number + "]";
    }
    
    /**
     * Returns a string representation of the operand value.
     * 
     * @return a string representation of the operand value.
     */
    public String getDescription()
    {
        int i, size;
        
        StringBuffer description = new StringBuffer(content.getDescription(value[0]));
        
        size = value.length;
        for(i = 1; i < size; i++)
        {
            description.append(", ");
            description.append(content.getDescription(value[i]));
        }
        
        return (isRegister() && register != null ? register.toString() + "[" + name + "]" : name) + "=" +
            description + (descriptor != null ? " (" + descriptor + ")" : "");
    }
}
