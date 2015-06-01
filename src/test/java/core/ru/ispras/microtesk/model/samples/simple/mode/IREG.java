/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IREG.java, Nov 20, 2012 12:27:16 PM Andrei Tatarnikov
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

import java.util.Map;
import ru.ispras.microtesk.model.api.instruction.AddressingMode;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.memory.Location;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.DataEngine;

import static ru.ispras.microtesk.model.samples.simple.shared.Shared.*;

/*
mode IREG(i: nibble) = M[R[i]]
syntax = format("(R%d)", i)
image  = format("00%4b", i)
*/    

public class IREG extends AddressingMode
{
    private static final class Info extends InfoAndRule
    {
        Info()
        {
            super(
               IREG.class,
               "IREG",
               byte_t,
               new ParamDecls().declareParam("i", nibble)
            );
        }
        
        @Override
        public IAddressingMode create(Map<String, Data> args)
        {
            final Location i = getArgument("i", args);
            return new IREG(i);
        }
    }

    public static final IInfo INFO = new Info();

    private Location i;

    public IREG(Location i)
    {
        this.i = i;
    }

    @Override
    public String syntax()
    { 
        return String.format("(R%d)", i.getValue());
    }

    @Override
    public String image()
    {
        // TODO: NOT SUPPORTED
        // image  = format("00%4b", i)
        return null;
    }

    public void action()
    {
       // NOTHING
    }

    @Override
    public Location access()
    {
        return M.access(DataEngine.intValue(R.access(DataEngine.intValue(i.load())).load()));
    }
}
