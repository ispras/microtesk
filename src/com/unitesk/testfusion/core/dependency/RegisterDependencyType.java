/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: RegisterDependencyType.java,v 1.15 2009/08/13 15:54:12 kamkin Exp $
 */

package com.unitesk.testfusion.core.dependency;

import com.unitesk.testfusion.core.model.ContentType;
import com.unitesk.testfusion.core.model.Operand;
import com.unitesk.testfusion.core.model.OperandType;

/**
 * Register dependency type.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class RegisterDependencyType extends DependencyType
{
    /**
     * Returns name of the dependency. 
     * 
     * @param <code>dependency</code> the dependency.
     * 
     * @return external name of the dependency.
     */
    protected static String getDependencyName(OperandType operandType, ContentType contentType)
    {
        String name = operandType.getName();

        if(contentType != null) 
            { name += " [" + contentType + "]"; } 
        
        return name;
    }

    /**
     * Constructor.
     * 
     * @param <code>type</code> the register type.
     */
    public RegisterDependencyType(OperandType type)
    {
        super(getDependencyName(type, null), true, type);
    }

    /**
     * Constructor.
     * 
     * @param <code>type</code> the register type.
     * 
     * @param <code>contentType</code> the content type.
     */
    public RegisterDependencyType(OperandType operandType, ContentType contentType)
    {
        super(getDependencyName(operandType, contentType), true, operandType, contentType);
    }
    
    /** Default constructor. */
    public RegisterDependencyType()
    {
        this(OperandType.REGISTER);
    }

    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to register dependency type object.
     */
    protected RegisterDependencyType(RegisterDependencyType r)
    {
        super(r);
    }
    
    /**
     * Checks if register dependency may tie two operands.
     * 
     * @param  <code>deps</code> the dependent operand.
     * 
     * @param  <code>from</code> the determinant operand.
     * 
     * @return <code>true</code> if register dependency may tie two operands.
     */
    @Override
    public boolean isApplicableTo(Operand deps, Operand from)
    {
        boolean isApplicable = true;
        
        if(!super.isApplicableTo(deps, from))
            { return false; }

        // Fixed dependency.
        if(deps.isFixed() || from.isFixed())
        {
            if(deps.isFixed() && from.isFixed())
                { return deps.getRegister().equals(from.getRegister()); }

            return !deps.isFixed();
        }
        
        // Define-use dependency.
        if(from.isOutput() && deps.isInput())
        {
            ContentType depsType = deps.getContentType();
            ContentType fromType = from.getContentType();
            
            isApplicable &= fromType.isCompatibleTo(depsType);
        }

        // Use-use dependency.
        if(from.isInput() && deps.isInput())
        {
            ContentType depsType = deps.getContentType();
            ContentType fromType = from.getContentType();
            
            isApplicable &= fromType.isCompatibleTo(depsType);
        }
        
        // Use-define dependency.
        if(from.isInput() && deps.isOutput())
        {
            // Do nothing.
        }
        
        // Define-define dependency.
        if(from.isOutput() && deps.isOutput())
        {
            // Do nothing.
        }
        
        return isApplicable;
    }
    
    /**
     * Returns a copy of the register dependency type.
     * 
     * @return a copy of the register dependency type.
     */
    @Override
    public RegisterDependencyType clone()
    {
        return new RegisterDependencyType(this);
    }
}
