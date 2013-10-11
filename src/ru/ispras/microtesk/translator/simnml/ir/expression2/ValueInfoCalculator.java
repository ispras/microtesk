/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ValueInfoCalculator.java, Oct 11, 2013 9:19:13 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import java.util.List;

import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public final class ValueInfoCalculator extends WalkerFactoryBase
{
    private final Where                w;
    private final ValueKind       target;
    private final Operator            op;
    private final List<ValueInfo> values;

    public ValueInfoCalculator(
        WalkerContext context,
        Where w,
        ValueKind target,
        Operator op,
        List<ValueInfo> values
        )
    {
        super(context);

        this.w      = w;
        this.target = target; 
        this.op     = op;
        this.values = values;
    }

    public ValueInfo calculate() throws SemanticException
    {
        final List<ValueInfo> castValues = cast(values); 

        if (1 == op.operands())
            return calculate(values.get(0));

        return calculate(values.get(0), values.get(1));
    }
    
    private static List<ValueInfo> cast(List<ValueInfo> values)
    {
        if (values.size() < 2)
            return values;

        return null;
    }

    public ValueInfo calculate(ValueInfo value) throws SemanticException
    {
        
        
        
        
        final ValueKind kind = value.getValueKind(); 
        assert ValueKind.MODEL == kind || ValueKind.NATIVE == kind;

        if (kind == ValueKind.MODEL)
            return value;

        if (!value.isConstant())
            return value;

        return null;
    }

    public ValueInfo calculate(ValueInfo left, ValueInfo right) throws SemanticException
    {
        assert null != left;
        assert null != right;
        
        if (left.getValueKind() == right.getValueKind())
        {
            if (ValueKind.MODEL == left.getValueKind())
                return calculateModel(left, right);
            else
                return calculateNative(left, right);
        }

        return calculateMixed(left, right);
    }

    private ValueInfo calculateModel(ValueInfo left, ValueInfo right) throws SemanticException
    {
        final Type type = ModelTypeCastRules.getCastType(left.getModelType(), right.getModelType());

        if (type.equals(left.getModelType()))
            return left;

        if (type.equals(right.getModelType()))
            return right;

        return ValueInfo.createModel(type);
    }
    
    private ValueInfo calculateNative(ValueInfo left, ValueInfo right) throws SemanticException
    {
        return null;
    }
    
    private ValueInfo calculateMixed(ValueInfo left, ValueInfo right) throws SemanticException
    {
        final ValueInfo result = target == left.getValueKind() ? left : right;

        if (result.isConstant())
            return ValueInfo.createNativeType(result.getNativeType());

        return result;
    }
}

