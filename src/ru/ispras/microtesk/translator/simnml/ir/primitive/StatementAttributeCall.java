/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * StatementAttributeCall.java, Jul 19, 2013 12:12:24 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.primitive;

public final class StatementAttributeCall extends Statement
{
    private final Primitive     callee;
    private final String    calleeName;
    private final String attributeName;

    StatementAttributeCall(Primitive callee, String calleeName, String attributeName)
    {
        super(Kind.CALL);
        
        assert null != callee;
        assert null != attributeName;

        this.callee        = callee;
        this.calleeName    = calleeName;
        this.attributeName = attributeName;
    }

    public Primitive getCallee()
    {
        return callee;
    }

    public final String getCalleeName()
    {
        return calleeName;
    }

    public String getAttributeName()
    {
        return attributeName;
    }
}
