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

public class StatementAttributeCall extends Statement
{
    private final Attribute        callee;
    private final String    calleeObjName;
    private final Primitive calleeObjType;

    StatementAttributeCall(
        Attribute callee,
        String calleeObjName,
        Primitive calleeObjType
        )
    {
        super(Kind.CALL);
        
        assert null != callee;
        assert null != calleeObjType;

        this.callee        = callee;
        this.calleeObjName = calleeObjName;
        this.calleeObjType = calleeObjType;
    }

    public Attribute getCallee()
    {
        return callee; 
    }

    public String getCalleeObjectName()
    {
        return calleeObjName;
    }

    public Primitive getCalleeObjectType()
    {
        return calleeObjType;
    }
}
