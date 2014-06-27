/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ModelISA.java, Dec 1, 2012 11:46:09 PM Andrei Tatarnikov
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.model.samples.simple;

import java.util.Collections;

import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.debug.CallSimulator;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;

abstract class ModelISA extends CallSimulator
{
    public ModelISA(IModel model)
    {
        super(model);
    }

    public final void mov(Mode op1, Mode op2) throws ConfigurationException
    {
        addCall("Mov", new Argument("op1", op1), new Argument("op2", op2));        
    }

    public final void add(Mode op1, Mode op2) throws ConfigurationException
    {
        addCall("Add", new Argument("op1", op1), new Argument("op2", op2)); 
    }

    public final void sub(Mode op1, Mode op2) throws ConfigurationException
    {
        addCall("Sub", new Argument("op1", op1), new Argument("op2", op2)); 
    }

    public final Mode reg(int i)
    {
        return new Mode("REG", Collections.singletonMap("i", i));
    }

    public final Mode ireg(int i)
    {
        return new Mode("IREG", Collections.singletonMap("i", i));
    }

    public final Mode mem(int i)
    {
        return new Mode("MEM", Collections.singletonMap("i", i));
    }

    public final Mode imm(int i)
    {
        return new Mode("IMM", Collections.singletonMap("i", i));
    }
}
