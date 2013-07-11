/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * LocationExprClass.java, Jan 23, 2013 10:32:15 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;

final class LocationExprClass implements LocationExpr 
{
    private final   String text;
    private final TypeExpr type;

    public LocationExprClass(String text, TypeExpr type)
    {
        assert null != text;
        assert null != type;
        
        this.text = text;
        this.type = type;
    }

    @Override
    public String getText()
    {
        return text;
    }

    @Override
    public TypeExpr getType()
    {
        return type;
    }
}
