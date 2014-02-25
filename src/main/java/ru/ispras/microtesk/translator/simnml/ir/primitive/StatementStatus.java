/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * StatementStatus.java, Jul 24, 2013 1:01:21 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import ru.ispras.microtesk.model.api.state.Status;

public final class StatementStatus extends Statement
{
    private final Status status;
    private final int newValue;

    StatementStatus(Status status, int newValue)
    {
        super(Kind.STATUS);

        this.status = status;
        this.newValue  = newValue;
    }

    public Status getStatus()
    {
        return status;
    }

    public int getNewValue()
    {
        return newValue;
    }
}
