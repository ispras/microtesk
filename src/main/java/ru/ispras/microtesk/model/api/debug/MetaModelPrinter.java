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
        printAddressingModeMetaData();

        printSepator();
        printInstructionMetaData();
        
        printSepator();
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
    
    public void printAddressingModeMetaData()
    {
        System.out.println("ADDRESSING MODES:");

        for (IMetaAddressingMode am : metaModel.getAddressingModes())
        {
            final StringBuilder sb = new StringBuilder();

            sb.append(String.format("Name: %s", am.getName()));
            sb.append(", Parameters: ");

            boolean isFirstArg = true;
            for (String an : am.getArgumentNames())
            {
                if (isFirstArg) isFirstArg = false; else sb.append(", ");
                sb.append(an);
            }

            System.out.println(sb);
        }
    }

    public void printInstructionMetaData()
    {
        System.out.println("INSTRUCTIONS:");

        for (IMetaInstruction i : metaModel.getInstructions())
        {
            System.out.println(String.format("Name: %s", i.getName()));
            System.out.println("Parameters:");

            for(IMetaArgument a : i.getArguments())
            {
                final StringBuilder asb = new StringBuilder();

                asb.append("   ");
                asb.append(a.getName());
                asb.append(" [");

                boolean isFirstMode = true;
                for (IMetaAddressingMode am : a.getAddressingModes())
                {
                    if (isFirstMode) isFirstMode = false; else asb.append(", ");
                    asb.append(am.getName());
                }

                asb.append("]");
                System.out.println(asb);
            }

            printSituationMetaData(i);

            System.out.println();
        }
    }
    
    public void printSituationMetaData(IMetaInstruction metaInstruction)
    {
        System.out.println("Situations:");

        for (IMetaSituation s: metaInstruction.getSituations())
            System.out.println("   " + s.getName());
    }
}
