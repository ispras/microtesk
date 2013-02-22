/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Op.java, Dec 19, 2012 2:33:26 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.modeop;

import java.util.List;
import java.util.Map;

public final class Op extends Primitive<Op>
{
    public Op(String name, List<Op> ors)
    {
        super(name, ors);
    }

    public Op(String name,  Map<String, Argument> args, Map<String, Attribute> attrs)
    {
        super(name, args, attrs);
    }
}
