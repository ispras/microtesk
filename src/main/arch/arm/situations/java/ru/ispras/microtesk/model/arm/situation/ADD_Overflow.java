/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ADD_Overflow.java, Mar 20, 2014 10:14:35 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.arm.situation;

import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.microtesk.model.api.situation.ISituation;
import ru.ispras.microtesk.model.api.situation.builtin.ConstraintBasedSituation;
import ru.ispras.microtesk.model.api.situation.builtin.IConstraintFactory;

public final class ADD_Overflow extends ConstraintBasedSituation
{
    private static final   String    NAME = "overflow";
    private static final IFactory FACTORY = new IFactory()
    {
        @Override
        public ISituation create() { return new ADD_Overflow(); }
    };

    public static final IInfo INFO = new Info(NAME, FACTORY);

    public ADD_Overflow()
    {
        super(
            INFO,
            new ConstraintFactory()
        );
    }
    
    private static final class ConstraintFactory implements IConstraintFactory
    {
        @Override
        public Constraint create()
        {
            // TODO: Load constraint from ADD_Overflow.xml
            return null;
        }
    }
}
