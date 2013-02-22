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
mode IREG(i: nibble)=M[R[i]]
syntax = format("(R%d)", i)
image  = format("00%4b", i)
*/    

public class IREG extends AddressingMode
{
    public static final String NAME = "IREG";

    public static final ParamDecl[] PARAMS = new ParamDecl[] 
    {
        new ParamDecl("i", nibble)
    };

    public static final IFactory FACTORY = new IFactory()
    {
        @Override
        public IAddressingMode create(Map<String, Data> args)
        {
            return new IREG(args);
        }
    };

    public static final Map<String, Type> DECLS = createDeclarations(PARAMS);
    public static final IInfo INFO = new Info(IREG.class, NAME, FACTORY, DECLS);
    
    private Location i;

    public IREG(Map<String, Data> args)
    {
        this.i = getArgument("i", DECLS, args);
    }

    @Override
    public String syntax()
    { 
        return String.format("(R%d)", DataEngine.intValue(i.getDataCopy()));
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
        return M.access(DataEngine.intValue(R.access(DataEngine.intValue(i.load())).load()));
    }
}
