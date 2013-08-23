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

import java.util.List;

public class ExprOperation extends Expr
{
    private final Operator   operator;
    private final List<Expr> operands; 

    public ExprOperation(Operator operator, List<Expr> operands)
    {
        super(Kind.OPERATION);

        assert null != operator;
        assert null != operands;

        this.operator = operator;
        this.operands = operands;
    }

    public Operator getOperator()
    {
        return operator;
    }

    public List<Expr> getOperands()
    {
        return operands;
    }

    @Override
    public ValueInfo getValueInfo()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
