/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * LocationExpr.java, Jan 22, 2013 6:15:53 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;

public final class LocationExpr 
{
    private final   String text;
    private final TypeExpr type;

    public LocationExpr(String text, TypeExpr type)
    {
        assert null != text;
        assert null != type;
        
        this.text = text;
        this.type = type;
    }

    public String getText()
    {
        return text;
    }

    public TypeExpr getType()
    {
        return type;
    }
}
