/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: RegisterSet.java,v 1.2 2009/04/21 10:25:33 kamkin Exp $
 */

package com.unitesk.testfusion.core.context;

import java.util.ArrayList;

import com.unitesk.testfusion.core.generator.Random;
import com.unitesk.testfusion.core.model.register.Register;

/**
 * Set of registers associated with a certain register type.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class RegisterSet extends ArrayList<Register>
{
    private static final long serialVersionUID = 1L;
    
    /** Default constructor. */
    public RegisterSet() {}
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to a register set object.
     */
    protected RegisterSet(RegisterSet r)
    {
       super(r); 
    }
    
    /**
     * Returns random register.
     * 
     * @return random register.
     */
    public Register randomRegister()
    {
        return get(Random.int32_non_negative_less(size()));
    }
    
    /**
     * Returns a copy of the register set object.
     * 
     * @return a copy of the register set object.
     */
    public RegisterSet clone()
    {
        return new RegisterSet(this);
    }
}
