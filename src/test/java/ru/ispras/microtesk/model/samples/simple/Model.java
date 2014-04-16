/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Model.java, Nov 15, 2012 8:23:24 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.samples.simple;

import ru.ispras.microtesk.model.api.debug.MetaModelPrinter;
import ru.ispras.microtesk.model.api.debug.ModelStatePrinter;
import ru.ispras.microtesk.model.api.simnml.SimnMLProcessorModel;
import ru.ispras.microtesk.model.samples.simple.instruction.ISA;
import ru.ispras.microtesk.test.data.IInitializerGenerator;

import static ru.ispras.microtesk.model.samples.simple.shared.Shared.*;

public final class Model extends SimnMLProcessorModel
{
    private static final IInitializerGenerator[] __INITIALIZERS = {};

    public Model()
    {
        super(
            new ISA(),
            __REGISTERS,
            __MEMORY,
            __LABELS,
            __STATUSES
        );
    }
    
    @Override
    public IInitializerGenerator[] getInitializers()
    {
        return __INITIALIZERS;
    }

    public static void printInformation()
    {
        final Model model = new Model();

        final MetaModelPrinter metaModelPrinter = new MetaModelPrinter(model.getMetaData());
        metaModelPrinter.printAll();

        final ModelStatePrinter modelStatePrinter = new ModelStatePrinter(model);
        modelStatePrinter.printRegisters();
    }

    public static void main(String[] args)
    {
        printInformation();
    }
}
