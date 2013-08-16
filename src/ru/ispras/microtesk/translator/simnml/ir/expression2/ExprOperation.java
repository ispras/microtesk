/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprOperation.java, Aug 14, 2013 12:45:13 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;


public class ExprOperation extends Expr
{
    
    
    public ExprOperation()
    {
        super(Kind.OPERATION);
    }

    @Override
    public ValueInfo getValueInfo()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
