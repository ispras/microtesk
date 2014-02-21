/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ArithmUnaryMinus.java, Dec 2, 2012 12:48:33 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data.operations;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.IUnaryOperator;
import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.model.api.type.Type;

public class ArithmUnaryMinus implements IUnaryOperator
{
    // Sim-nML spec: these operators (unary +,-) are used only for
    // INT, FLOAT and FIX data types.

    private final static Set<ETypeID> SUPPORTED_TYPES = Collections.unmodifiableSet(EnumSet.of(
        ETypeID.INT
        //, ETypeID.FLOAT // NOT SUPPORTED IN THIS VERSION
        //, ETypeID.FIX   // NOT SUPPORTED IN THIS VERSION
    ));
    
    protected static Data minus(Data arg)
    {   
        // TODO: TEMPORARY. WILL BE REMOVED!
        
        // Negation algorithm: "-arg = ~arg + 1".
        //final Data not = BitNot.bitnot(arg);
        //final Data one = new Data(BitVector.valueOf(1, arg.getType().getBitSize()), arg.getType());

        //return ArithmPlus.plus(not, one);
        
        return null;
    }
    
    @Override
    public Data execute(Data arg)
    {      
        return minus(arg);
    }

    @Override
    public boolean supports(Type argType)
    {
        return SUPPORTED_TYPES.contains(argType.getTypeID());
    }
}
