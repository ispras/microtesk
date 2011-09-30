/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: DependencyType.java,v 1.13 2009/04/23 13:06:29 kamkin Exp $
 */

package com.unitesk.testfusion.core.dependency;

import com.unitesk.testfusion.core.model.ContentType;
import com.unitesk.testfusion.core.model.Operand;
import com.unitesk.testfusion.core.model.OperandType;

/**
 * Type of dependecy between instructions.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class DependencyType
{
    /** Name of the dependency type. */
    protected String name;

    /** Register dependency or not. */
    protected boolean isRegister;
    
    /** Operand type. */
    protected OperandType operandType;
    
    /** Content type. */
    protected ContentType contentType;

    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of the dependency type.
     * 
     * @param <code>isRegister</code> is dependency register or not.
     * 
     * @param <code>operand</code> the operand type.
     * 
     * @param <code>content</code> the content type.
     */
    public DependencyType(String name, boolean isRegister, OperandType operand, ContentType content)
    {
        this.name = name;
        this.isRegister = isRegister;
        this.operandType = operand;
        this.contentType = content;
    }

    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of the dependency type.
     * 
     * @param <code>operand</code> the operand type.
     * 
     * @param <code>content</code> the content type.
     */
    public DependencyType(String name,OperandType operand, ContentType content)
    {
        this(name, false, operand,  content);
    }
    
    /**
     * Constructor of operand dependency type.
     * 
     * @param <code>name</code> the name of the dependency type.
     * 
     * @param <code>isRegister</code> is dependency register or not.
     * 
     * @param <code>operand</code> the operand type.
     */
    public DependencyType(String name, boolean isRegister, OperandType operand)
    {
        this(name, isRegister, operand, null);
    }

    /**
     * Constructor of operand dependency type.
     * 
     * @param <code>name</code> the name of the dependency type.
     * 
     * @param <code>operand</code> the operand type.
     */
    public DependencyType(String name, OperandType operand)
    {
        this(name, false, operand, null);
    }
    
    /**
     * Constructor of content dependency type.
     * 
     * @param <code>name</code> the name of the dependency type.
     * 
     * @param <code>content</code> the content type.
     */
    public DependencyType(String name, ContentType content)
    {
        this(name, null, content);
    }

    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to dependency type object.
     */
    protected DependencyType(DependencyType r)
    {
        name = r.name;
        isRegister = r.isRegister;
        operandType = r.operandType;
        contentType = r.contentType;
    }

    /**
     * Returns the name of the dependency type.
     * 
     * @return the name of the dependency type.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Returns the operand type.
     * 
     * @return the operand type.
     */
    public OperandType getOperandType()
    {
        return operandType;
    }
    
    /**
     * Returns the content type.
     * 
     * @return the content type.
     */
    public ContentType getContentType()
    {
        return contentType;
    }

    /**
     * Checks if this is the register dependency type.
     * 
     * @return <code>true</code> if this is the register dependency type;
     *         <code>false</code> otherwise.
     */
    public boolean isRegisterDependency()
    {
        return isRegister;
    }
    
    /**
     * Checks if dependency of the type can be applied to the given operand.
     */
    public boolean isApplicableTo(Operand dependentOperand)
    {
        OperandType dependentOperandType = dependentOperand.getOperandType();
        ContentType dependentContentType = dependentOperand.getContentType();
        
        if(operandType != null)
        {
            if(!dependentOperandType.isSubtypeOf(operandType))
                { return false; }
        }
        
        if(contentType != null)
        {
            if(!contentType.equals(dependentContentType))
                { return false; }
        }
            
        return true;
    }
    
    /**
     * Checks if dependency of this type may tie two operands.
     * 
     * @param  <code>deps</code> the dependent operand.
     * 
     * @param  <code>from</code> the determinant operand.
     * 
     * @return <code>true</code> if dependency of this type may tie two
     *         operands.
     */
    public boolean isApplicableTo(Operand deps, Operand from)
    {
        if(!isApplicableTo(deps) || !isApplicableTo(from))
            { return false; }

        OperandType depsOperandType = deps.getOperandType();
        OperandType fromOperandType = from.getOperandType();
        
        if(!depsOperandType.isSubtypeOf(fromOperandType) &&
           !fromOperandType.isSubtypeOf(depsOperandType))
            { return false; }
        
        return true;
    }
    
    /**
     * Compares two dependencies types.
     * 
     * @param  <code>o</code> the dependency type to be compared.
     * 
     * @return <code>true</code> if the dependencies types are equal;
     *         <code>false</code> otherwise.
     */
    public boolean equals(Object o)
    {
        if(!(o instanceof DependencyType))
            { return false; }
        
        DependencyType r = (DependencyType)o;
        
        return name.equals(r.name);
    }
    
    /**
     * Returns a hash code value for the object.
     */
    public int hashCode()
    {
        return name.hashCode();
    }
    
    /**
     * Returns the name of the dependency type.
     * 
     * @return the name of the dependency type.
     */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer(getName());
        
        buffer.append(":");
        buffer.append(operandType != null ? operandType.toString() : "");
        buffer.append(":");
        buffer.append(contentType != null ? contentType.toString() : "");
        
        return buffer.toString();
    }
    
    /**
     * Returns a copy of the dependency type.
     * 
     * @return a copy of the dependency type.
     */
    public DependencyType clone()
    {
        return new DependencyType(this);
    }
}
