/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ValueInfoCalculator.java, Jan 17, 2014 12:30:18 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.valueinfo;

import java.util.List;

import ru.ispras.microtesk.translator.antlrex.ISemanticError;
import ru.ispras.microtesk.translator.antlrex.SemanticError;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.ir.expression.Operator;

public final class ValueInfoCalculator extends WalkerFactoryBase
{
    public ValueInfoCalculator(WalkerContext context)
    {
        super(context);
    }

    public ValueInfo cast(
        Where w, ValueInfo.Kind target, List<ValueInfo> values) throws SemanticException
    {
        final ValueInfo castValueInfo = ValueInfoCast.getCast(target, values);

        if (null == castValueInfo)
            raiseError(w, new IncompatibleTypes(values));

        return castValueInfo;
    }

    public ValueInfo calculate(
        Where w, Operator op, ValueInfo castValueInfo, List<ValueInfo> values) throws SemanticException
    {
        if (!op.operation().isSupportedFor(castValueInfo))
            raiseError(w, new UnsupportedOperandType(op, castValueInfo));

        final ValueInfo resultValueInfo = op.operation().calculate(castValueInfo, values);
        return resultValueInfo;
    }
}

final class IncompatibleTypes implements ISemanticError
{
    private static final String FORMAT = "Incompatible types: %s.";

    private final List<ValueInfo> values;

    public IncompatibleTypes(List<ValueInfo> values)
    {
        this.values = values;
    }

    @Override
    public String getMessage()
    {
        final StringBuilder sb = new StringBuilder();

        for (ValueInfo vi : values)
        {
            if (sb.length() != 0) sb.append(", ");
            sb.append(vi.getTypeName());
        }

        return String.format(FORMAT, sb.toString());
    }
}

final class UnsupportedOperandType extends SemanticError
{
    private static final String FORMAT = "The %s type is not supported by the %s operator.";

    public UnsupportedOperandType(Operator op, ValueInfo vi)
    {
        super(String.format(FORMAT, vi.getTypeName(), op.text()));
    }
}
