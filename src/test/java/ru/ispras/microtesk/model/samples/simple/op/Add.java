/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Add.java, Nov 20, 2012 1:28:50 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.samples.simple.op;

import ru.ispras.microtesk.model.api.instruction.Operation;
import ru.ispras.microtesk.model.api.data.DataEngine;
import ru.ispras.microtesk.model.api.data.EOperatorID;

import static ru.ispras.microtesk.model.samples.simple.shared.Shared.*;

/*
    op Add()
    syntax = "add"
    image  = "00"
    action = {
        DEST = SRC1 + SRC2; 
    }
*/

public class Add extends Operation
{
    public static final IInfo INFO = new Info(Add.class, Add.class.getSimpleName(), new ParamDecl[] {});

    @Override
    public String syntax() { return "add"; }

    @Override
    public String image() { return "00"; }

    @Override
    public void action()
    {
        DEST.access().store(
            DataEngine.execute(
                EOperatorID.PLUS,
                SRC1.access().load(),
                SRC2.access().load()
            )
        );
    }
}
