/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Action.java, Jan 17, 2014 5:57:41 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.valueinfo;

import ru.ispras.microtesk.translator.simnml.ir.expression.Operands;

abstract class Action
{
    private final Class<?> type;
    private final int  operands;

    public Action(Class<?> type, Operands operands)
    {
        assert null != type; 
        assert null != operands;

        this.type = type;
        this.operands = operands.count();
    }

    public final Class<?> getType() { return type; }
    public final int  getOperands() { return operands; }
}

abstract class UnaryAction extends Action
{
    public UnaryAction(Class<?> type) { super(type, Operands.UNARY); }
    public abstract Object calculate(Object value);
}

abstract class BinaryAction extends Action
{
    public BinaryAction(Class<?> type) { super(type, Operands.BINARY); }
    public abstract Object calculate(Object left, Object right);
}
