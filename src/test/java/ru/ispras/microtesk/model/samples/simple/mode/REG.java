/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * REG.java, Nov 20, 2012 12:24:58 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.samples.simple.mode;

import java.util.Map;
import ru.ispras.microtesk.model.api.simnml.instruction.AddressingMode;
import ru.ispras.microtesk.model.api.simnml.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.memory.Location;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.DataEngine;
import ru.ispras.microtesk.model.api.type.Type;

import static ru.ispras.microtesk.model.samples.simple.shared.Shared.*;

/*
mode REG(i: nibble)=R[i]
syntax = format("R%d", i)
image = format("01%4b", i)
*/

public class REG extends AddressingMode
{
    public static final String NAME = "REG";
    
    public static final Map<String, Type> DECLS = new ParamDeclBuilder()
        .declareParam("i", nibble).build();

    public static final IFactory FACTORY = new IFactory()
    {
        @Override
        public IAddressingMode create(Map<String, Data> args)
        {
            return new REG(args);
        }
    };

    public static final IInfo INFO = new Info(REG.class, NAME, FACTORY, DECLS);
    
    private Location i;

    public REG(Map<String, Data> args)
    {
        this.i = getArgument("i", DECLS, args);
    }

    @Override
    public String syntax()
    { 
        return String.format("R%d", DataEngine.intValue(i.getDataCopy()));
    }

    @Override
    public String image()
    {
        // TODO: NOT SUPPORTED
        // image = format("01%4b", i)
        return null;
    }

    public void action()
    {
        // NOTHING
    }

    @Override
    public Location access()
    {
        return R.access(DataEngine.intValue(i.load()));
    }
}
