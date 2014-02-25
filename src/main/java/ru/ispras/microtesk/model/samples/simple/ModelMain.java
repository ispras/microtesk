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
import ru.ispras.microtesk.model.api.debug.MetaModelPrinter;
import ru.ispras.microtesk.model.api.debug.ModelStatePrinter;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IInstructionCallBlock;
import ru.ispras.microtesk.model.api.instruction.IInstructionCallBlockBuilder;

import static ru.ispras.microtesk.model.samples.simple.ModelISA.*;

public class ModelMain
{
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

        final MetaModelPrinter metaModelPrinter = new MetaModelPrinter(model.getMetaData());
        metaModelPrinter.printAll();

        final ModelStatePrinter modelStatePrinter = new ModelStatePrinter(model);
        modelStatePrinter.printRegisters();

        try
        {
            executeSimpleTest(model.getSimulator());
        }
        catch (ConfigurationException e)
        {
            System.out.println(e.getMessage());
        }

        modelStatePrinter.printRegisters();
    }
}
