/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExpressionVariable.java, Aug 14, 2013 12:31:16 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.microtesk.translator.simnml.ir.expression.Location;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public final class ExprVariable extends Expr implements ValueLocation
{
    private final Location location;

    public ExprVariable(Location location)
    {
        super(Kind.VARIABLE);

        assert null != location;
        this.location = location;
    }

    @Override
    public int getBitSize()
    {
        return ((Number)getType().getBitSize().getValue()).intValue();
    }

    public Location getLocation()
    {
        return location;
    }

    @Override
    public Type getType()
    {
        return location.getType();
    }

    @Override
    public ValueInfo getValueInfo()
    {
        return this;
    }
}
