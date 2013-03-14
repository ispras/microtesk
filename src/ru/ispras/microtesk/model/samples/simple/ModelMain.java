/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ModelMain.java, Nov 22, 2012 11:46:54 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.samples.simple;

import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.ISimulator;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IInstructionCallBlock;
import ru.ispras.microtesk.model.api.instruction.IInstructionCallBlockBuilder;
import ru.ispras.microtesk.model.api.metadata.IMetaAddressingMode;
import ru.ispras.microtesk.model.api.metadata.IMetaArgument;
import ru.ispras.microtesk.model.api.metadata.IMetaInstruction;
import ru.ispras.microtesk.model.api.metadata.IMetaLocationStore;
import ru.ispras.microtesk.model.api.metadata.IMetaModel;
import ru.ispras.microtesk.model.api.metadata.IMetaSituation;
import ru.ispras.microtesk.model.api.monitor.IModelStateMonitor;
import ru.ispras.microtesk.model.api.monitor.IStoredValue;

import static ru.ispras.microtesk.model.samples.simple.ModelISA.*;

public class ModelMain
{
    public static void printMetaData(IMetaModel metaModel)
    {
        printRegisterMetaData(metaModel);
        printMemoryMetaData(metaModel);
        printInstructionMetaData(metaModel);
        printSituationMetaData(metaModel);
    }

    public static void printRegisterMetaData(IMetaModel metaModel)
    {
        System.out.println("************************************************");
        System.out.println("REGISTERS:");

        for (IMetaLocationStore r: metaModel.getRegisters())
            System.out.printf("Name: %s, Size: %d%n", r.getName(), r.getCount());
    }

    public static void printMemoryMetaData(IMetaModel metaModel)
    {
        System.out.println("************************************************");
        System.out.println("MEMORY STORES:");

        for (IMetaLocationStore m: metaModel.getMemoryStores())
            System.out.printf("Name: %s, Size: %d%n", m.getName(), m.getCount());
    }

    public static void printInstructionMetaData(IMetaModel metaModel)
    {
        System.out.println("************************************************");
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

    public static void printSituationMetaData(IMetaModel metaModel)
    {
        System.out.println("************************************************");
        System.out.println("SITUATIONS:");

        for (IMetaSituation s: metaModel.getSituations())
            System.out.println("Name: " + s.getName());
    }

    public static void printModelState(IModel model)
    {
        System.out.println("************************************************");
        System.out.println("MODEL STATE:");

        final IModelStateMonitor monitor = model.getModelStateMonitor();

        for (IMetaLocationStore r: model.getMetaData().getRegisters())
        {
            for (int index = 0; index < r.getCount(); ++index)
            {
                final IStoredValue value = monitor.readRegisterValue(r.getName(), index);
                System.out.printf("%s[%d] = %s %n", r.getName(), index, value.toBinString());
            }
            System.out.println();
        }

        for (IMetaLocationStore r: model.getMetaData().getMemoryStores())
        {
            for (int index = 0; index < r.getCount(); ++index)
            {
                final IStoredValue value = monitor.readMemoryValue(r.getName(), index);
                System.out.printf("%s[%d] = %s %n", r.getName(), index, value.toBinString());
            }
            System.out.println();
        }
    }

    public static void executeSimpleTest(ISimulator simulator) throws ConfigurationException
    {
        System.out.println("************************************************");

        final IInstructionCallBlockBuilder blockBuilder = simulator.createCallBlock();
        setCurrentBlockBuilder(blockBuilder);

        mov(reg(0), imm(0x0F));
        mov(reg(1), imm(0x01));

        mov(reg(2), reg(0));
        mov(reg(3), reg(1));

        add(reg(3), reg(1));
        add(reg(3), reg(1));

        mov(reg(4), imm(0x01));
        sub(reg(5), reg(4));
        
        final IInstructionCallBlock block = blockBuilder.getCallBlock();

        block.print();
        block.execute();
    }

    public static void main(String[] args)
    {
        final IModel model = new Model();

        printMetaData(model.getMetaData());

        printModelState(model);

        try
        {
            executeSimpleTest(model.getSimulator());
        }
        catch (ConfigurationException e)
        {
            System.out.println(e.getMessage());
        }

        printModelState(model);
    }
}
