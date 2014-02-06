/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprOperator.java, Aug 14, 2013 12:45:13 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression_;

import java.util.List;

import ru.ispras.microtesk.translator.simnml.ir.expression.Operands;
import ru.ispras.microtesk.translator.simnml.ir.expression.Operator;
import ru.ispras.microtesk.translator.simnml.ir.valueinfo.ValueInfo;

public final class ExprNodeOperator extends ExprAbstract
{
    private final Operator   operator;
    private final List<Expr> operands;
    private final ValueInfo      cast;

    ExprNodeOperator(Operator operator, List<Expr> operands, ValueInfo resultValueInfo, ValueInfo castValueInfo)
    {
        super(NodeKind.OPERATOR, resultValueInfo);

        assert null != operator;
        this.operator = operator;

        assert null != operands;
        this.operands = operands;

        this.cast = castValueInfo;
    }

    public Operator getOperator()
    {
        return operator;
    }

    public List<Expr> getOperands()
    {
        return operands;
    }

    public ValueInfo getCast()
    {
        return cast;
    }

    @Override
    public boolean isEquivalent(Expr expr)
    {
        if (this == expr) return true;
        if (expr == null) return false;

        if (getValueInfo().isConstant() && getValueInfo().equals(expr.getValueInfo()))
            return true;

        if (!getValueInfo().hasEqualType(expr.getValueInfo()))
            return false;

        if (getClass() != expr.getClass())
            return false;

        final ExprNodeOperator other = (ExprNodeOperator) expr;

        if (operator != other.getOperator())
            return false;

        if (operator.operands() == Operands.UNARY.count())
            return operands.get(0).isEquivalent(other.operands.get(0));

        return operands.get(0).isEquivalent(other.operands.get(0)) && 
               operands.get(1).isEquivalent(other.operands.get(1));
    }
}
