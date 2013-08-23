/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprLocation.java, Aug 20, 2013 5:52:45 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.microtesk.translator.simnml.ir.expression.Location;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public final class ExprLocation extends Expr
{
    private final Location location;

    private final class Info implements ValueInfo
    {
        @Override public ValueKind    getKind() { return ValueKind.LOCATION; }
        @Override public int       getBitSize() { return ((Number)locationType().getBitSize().getValue()).intValue(); }
        @Override public boolean   isConstant() { return false; }
        @Override public long    integerValue() { assert false; return 0; }
        @Override public boolean booleanValue() { assert false; return false; }
        @Override public Type    locationType() { return location.getType(); }
    }

    private final Info info;

    ExprLocation(Location location)
    {
        super(Kind.LOCATION);

        assert null != location;
        this.location = location;

        this.info = new Info();
    }

    public Location getLocation()
    {
        return location;
    }

    @Override
    public ValueInfo getValueInfo()
    {
        return info;
    }
}
