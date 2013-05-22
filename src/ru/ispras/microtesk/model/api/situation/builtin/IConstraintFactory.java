/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IConstraintFactory.java, May 22, 2013 5:07:29 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.situation.builtin;

import ru.ispras.solver.api.interfaces.IConstraint;

public interface IConstraintFactory
{
    public IConstraint create();
}
