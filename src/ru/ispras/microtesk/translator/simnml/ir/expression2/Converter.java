/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Converter.java, Jan 30, 2014 4:53:53 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.fortress.data.Data;
import ru.ispras.microtesk.translator.simnml.ir.expression.Operator;
import ru.ispras.microtesk.translator.simnml.ir.valueinfo.ValueInfo;

final class Converter
{
    private Converter() {}
    
    static Data toFortressData(ValueInfo valueInfo)
    {
        return null;
    }

    static Enum<?> toFortressOperator(Operator operator, ValueInfo valueInfo)
    {
        return null;
    }
}
