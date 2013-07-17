/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * PrimitiveOR.java, Jul 9, 2013 12:42:01 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import java.util.List;

import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;

public final class PrimitiveOR extends Primitive
{
    private final List<Primitive> ors;

    PrimitiveOR(String name, Kind kind, List<Primitive> ors)
    {
        super(name, kind, true, getReturnType(ors));
        this.ors = ors;
    }

    public List<Primitive> getORs()
    {
        return ors;
    }

    private static TypeExpr getReturnType(List<Primitive> ors)
    {
        assert null != ors;
        return ors.get(0).getReturnType();
    }
}
