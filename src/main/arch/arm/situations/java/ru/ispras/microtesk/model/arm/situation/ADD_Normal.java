/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ADD_Normal.java, Mar 20, 2014 10:14:25 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.arm.situation;

import ru.ispras.microtesk.model.api.situation.ISituation;
import ru.ispras.microtesk.model.api.situation.ConstraintBasedSituation;

public final class ADD_Normal extends ConstraintBasedSituation
{
    private static final   String    NAME = "normal";
    private static final IFactory FACTORY = new IFactory()
    {
        @Override
        public ISituation create() { return new ADD_Normal(); }
    };

    public static final IInfo INFO = new Info(NAME, FACTORY); 

    public ADD_Normal()
    {
        super(
           INFO,
           "xml/arm/ADD_Normal.xml"
        );
    }
}
