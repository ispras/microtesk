/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IInitializerGenerator.java, May 21, 2013 2:19:19 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.data;

import java.util.List;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.test.block.Argument;

public interface IInitializerGenerator
{
    public boolean isCompatible(Argument dest);
    public List<ConcreteCall> createInitializingCode(Argument dest, Data data); 
}
