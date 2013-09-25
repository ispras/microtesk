/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprNodeNamedConst.java, Aug 20, 2013 8:12:47 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.microtesk.translator.simnml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public final class ExprNodeNamedConst extends Expr
{
    private final LetConstant constant;

    private final class Info implements ValueInfo
    {
        @Override public ValueKind getValueKind() { return ValueKind.INTEGER; }
        @Override public int         getBitSize() { return getValueBitSize(); }
        @Override public boolean     isConstant() { return true; }
        @Override public long      integerValue() { return getValue(); }
        @Override public boolean   booleanValue() { return 0 != integerValue(); }
        @Override public Type      locationType() { assert false; return null; }
    }

    private final Info info;

    ExprNodeNamedConst(LetConstant constant)
    {
        super(NodeKind.NAMED_CONST);

        assert null  != constant;
        this.constant = constant;
        this.info     = new Info();
    }

    public LetConstant getConstant()
    {
        return constant;
    }

    @Override
    public ValueInfo getValueInfo()
    {
        return info;
    }
    
    private long getValue()
    {
        return ((Number)constant.getExpression().getValue()).longValue();
    }
    
    private int getValueBitSize()
    {   
        assert false : "Not implemented"; 
        return 0;
    }
}
