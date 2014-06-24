/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Mov.java, Nov 20, 2012 1:25:27 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.samples.simple.op;

import ru.ispras.microtesk.model.api.simnml.instruction.Operation;

import static ru.ispras.microtesk.model.samples.simple.shared.Shared.*;

/*
    op Mov()
    syntax = "mov"
    image  = "10"
    action = {
                 DEST = SRC2; 
             }
*/

public class Mov extends Operation
{
    public static final IInfo INFO = new Info(Mov.class, Mov.class.getSimpleName(), new ParamDecl[] {});

    @Override 
    public String syntax() { return "mov"; }

    @Override
    public String image()  { return "10";  }

    @Override
    public void action()
    {
        DEST.access().assign(SRC2.access());
    }
}
