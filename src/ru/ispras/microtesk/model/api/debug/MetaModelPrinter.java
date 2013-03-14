/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ModelMain.java, Mar 14, 2013 4:08:24 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.debug;

import ru.ispras.microtesk.model.api.metadata.IMetaAddressingMode;
import ru.ispras.microtesk.model.api.metadata.IMetaArgument;
import ru.ispras.microtesk.model.api.metadata.IMetaInstruction;
import ru.ispras.microtesk.model.api.metadata.IMetaLocationStore;
import ru.ispras.microtesk.model.api.metadata.IMetaModel;
import ru.ispras.microtesk.model.api.metadata.IMetaSituation;

public final class MetaModelPrinter
{
    private final IMetaModel metaModel;

    public MetaModelPrinter(IMetaModel metaModel)
    {
        assert null != metaModel;
        this.metaModel = metaModel;
    }

    public void printAll()
    {
        printSepator();
        printRegisterMetaData();
        
        printSepator();
        printMemoryMetaData();
        
        printSepator();
        printInstructionMetaData();
        
        printSepator();
        printSituationMetaData();
    }

    public void printSepator()
    {
        System.out.println("************************************************");
    }

    public void printRegisterMetaData()
    {
        System.out.println("REGISTERS:");

        for (IMetaLocationStore r: metaModel.getRegisters())
            System.out.printf("Name: %s, Size: %d%n", r.getName(), r.getCount());
    }

    public void printMemoryMetaData()
    {
        System.out.println("MEMORY STORES:");

        for (IMetaLocationStore m: metaModel.getMemoryStores())
            System.out.printf("Name: %s, Size: %d%n", m.getName(), m.getCount());
    }

    public void printInstructionMetaData()
    {
        System.out.println("INSTRUCTIONS:");

        for (IMetaInstruction i : metaModel.getInstructions())
        {
            System.out.printf("Name: %s, Parameters: ", i.getName());

            final StringBuilder asb = new StringBuilder(); 
            for(IMetaArgument a : i.getArguments())
            {
                if (asb.length() > 0) asb.append(", ");
                asb.append(a.getName());

                asb.append(" [");

                boolean isFirstMode = true;
                for (IMetaAddressingMode am : a.getAddressingModes())
                {
                    if (isFirstMode) isFirstMode = false;
                    else asb.append(", ");

                    asb.append(am.getName());

                    asb.append("(");
                    boolean isFirstArg = true;
                    for (String an : am.getArgumentNames())
                    {
                        if (isFirstArg) isFirstArg = false;
                        else asb.append(", ");

                        asb.append(an);
                    }
                    asb.append(")");
                }
                asb.append("]");
            }
            System.out.println(asb);
        }
    }

    public void printSituationMetaData()
    {
        System.out.println("SITUATIONS:");

        for (IMetaSituation s: metaModel.getSituations())
            System.out.println("Name: " + s.getName());
    }
}
