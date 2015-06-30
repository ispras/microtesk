/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IMM.java, Dec 1, 2012 2:32:22 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.samples.simple.mode;

import static ru.ispras.microtesk.model.samples.simple.shared.Shared.byte_t;

import java.util.Map;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.instruction.AddressingMode;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.memory.Location;

/*
mode IMM(i: byte)=i
syntax = format("[%d]", i)
image = format("11%4b", i)
*/

public final class IMM extends AddressingMode
{
    private static final class Info extends InfoAndRule
    {
        Info()
        {
            super(
                IMM.class,
                "IMM",
                byte_t,
                new ParamDecls()
                    .declareParam("i", byte_t),
                false
            );
        }
        
        @Override
        public IAddressingMode create(Map<String, Data> args)
        {
            final Location i = getArgument("i", args);
            return new IMM(i);
        }
    }

    public static final IInfo INFO = new Info();

    private Location i;

    public IMM(Location i)
    {
        this.i = i;
    }

    @Override
    public String syntax()
    {
        return String.format("[%d]", i.getValue());
    }

    @Override
    public String image()
    {
        // TODO: NOT SUPPORTED
        // image = format("11%4b", i)
        return null;
    }

    @Override
    public void action()
    {
        // NOTHING
    }

    @Override
    public Location access()
    {
         return i;
    }
}
