/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Sub.java, Nov 20, 2012 1:26:58 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.samples.simple.op;

import ru.ispras.microtesk.model.api.simnml.instruction.Operation;
import ru.ispras.microtesk.model.api.data.DataEngine;
import ru.ispras.microtesk.model.api.data.EOperatorID;

import static ru.ispras.microtesk.model.samples.simple.shared.Shared.*;

/*
    op Sub()
    syntax = "sub"
    image  = "01"
    action = {
                 DEST = SRC1 - SRC2; 
             }
*/

public class Sub extends Operation
{
    public static final IInfo INFO = new Info(Sub.class, Sub.class.getSimpleName(), new ParamDecl[] {});

    @Override 
    public String syntax() { return "sub"; }

    @Override
    public String image()  { return "01";  }

    @Override
    public void action()
    {
        DEST.access().store(
            DataEngine.execute(
                EOperatorID.MINUS,
                SRC1.access().load(),
                SRC2.access().load()
            )
        );
    }        
}
