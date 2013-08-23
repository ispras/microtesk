/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Stored.java, Aug 16, 2013 3:43:37 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public interface ValueInfo
{
    public static enum ValueKind
    {
        LOCATION,
        INTEGER,
        BOOLEAN
    }

    public ValueKind getKind();
    public int getBitSize();
    public boolean isConstant();

    public long integerValue();
    public boolean booleanValue();
    public Type locationType();
}
