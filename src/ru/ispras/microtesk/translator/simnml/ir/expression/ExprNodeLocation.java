/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprNodeLocation.java, Aug 20, 2013 5:52:45 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

import ru.ispras.microtesk.translator.simnml.ir.expression.Location;

public final class ExprNodeLocation extends ExprAbstract
{
    private final Location location;

    ExprNodeLocation(Location location)
    {
        super(NodeKind.LOCATION, ValueInfo.createModel(location.getType()));
        this.location = location;
    }

    public Location getLocation()
    {
        return location;
    }
}
