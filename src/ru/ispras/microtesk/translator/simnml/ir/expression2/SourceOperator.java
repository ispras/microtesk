/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * SourceOperator.java, Jan 28, 2014 11:17:27 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.microtesk.translator.simnml.ir.expression.Operator;
import ru.ispras.microtesk.translator.simnml.ir.valueinfo.ValueInfo;

public final class SourceOperator
{
    private final Operator  operator;
    private final ValueInfo castValueInfo;
    private final ValueInfo resultValueInfo;

    SourceOperator(Operator operator, ValueInfo castValueInfo, ValueInfo resultValueInfo)
    {
        if (null == operator)
            throw new NullPointerException();

        if (null == castValueInfo)
            throw new NullPointerException();

        if (null == resultValueInfo)
            throw new NullPointerException();

        this.operator = operator;
        this.castValueInfo = castValueInfo;
        this.resultValueInfo = resultValueInfo;
    }

    public Operator getOperator()
    {
        return operator;
    }

    public ValueInfo getCastValueInfo()
    {
        return castValueInfo;
    }

    public ValueInfo getResultValueInfo()
    {
        return resultValueInfo;
    }
}
