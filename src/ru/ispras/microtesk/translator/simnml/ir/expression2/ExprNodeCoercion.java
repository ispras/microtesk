/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprNodeCoercion.java, Sep 25, 2013 12:07:03 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public final class ExprNodeCoercion extends Expr
{
    private final Expr child;
    private final ValueInfo info;

    private final class InfoLocation implements ValueInfo
    {
        private final Type type;

        public InfoLocation(Type type) { this.type = type; }

        @Override public ValueKind getValueKind() { return ValueKind.LOCATION; }
        @Override public int         getBitSize() { return ((Number)type.getBitSize().getValue()).intValue(); }

        @Override public boolean isConstant() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public long integerValue() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override public boolean  booleanValue() { return integerValue() != 0; }
        @Override public Type     locationType() { return type; }
    }

    
    ExprNodeCoercion(Expr child, Type type)
    {
        super(NodeKind.COERCION);

        assert null != child;
        this.child = child;

        assert null != type;
        this.info = new InfoLocation(type);
    }

    public Expr getChild()
    {
        return child;
    }

    @Override
    public ValueInfo getValueInfo()
    {
        return info;
    }
}
