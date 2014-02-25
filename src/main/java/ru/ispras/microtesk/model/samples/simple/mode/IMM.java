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
 */

package ru.ispras.microtesk.model.samples.simple.mode;

import java.util.Map;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.DataEngine;
import ru.ispras.microtesk.model.api.memory.Location;
import ru.ispras.microtesk.model.api.simnml.instruction.AddressingMode;
import ru.ispras.microtesk.model.api.simnml.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.type.Type;

import static ru.ispras.microtesk.model.samples.simple.shared.Shared.*;

/*
mode IMM(i: byte)=i
syntax = format("[%d]", i)
image = format("11%4b", i)
*/

public class IMM extends AddressingMode
{
    public static final String NAME = "IMM";

    public static final ParamDecl[] PARAMS = new ParamDecl[]
    { 
        new ParamDecl("i", byte_t) 
    };

    public static final IFactory FACTORY = new IFactory()
    {
        @Override
        public IAddressingMode create(Map<String, Data> args)
        {
            return new IMM(args);
        }
    };

    public static final Map<String, Type> DECLS = createDeclarations(PARAMS);
    public static final IInfo INFO = new Info(IMM.class, NAME, FACTORY, DECLS);

    private Location i;

    public IMM(Map<String, Data> args)
    {
        this.i = getArgument("i", DECLS, args);
    }
    
    @Override
    public String syntax()
    {
        return String.format("[%d]", DataEngine.intValue(i.getDataCopy()));
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
    public void onBeforeLoad()
    {
        System.out.println(getClass().getSimpleName() + ": onBeforeLoad");
    }

    @Override
    public void onBeforeStore()
    {
        System.out.println(getClass().getSimpleName() + ": onBeforeStore"); 
    }

    @Override
    public Location access()
    {
         return i;
    }
}
