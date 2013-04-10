/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ModelMain.java, Mar 14, 2013 4:08:01 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.debug;

import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.metadata.IMetaLocationStore;
import ru.ispras.microtesk.model.api.monitor.IModelStateMonitor;
import ru.ispras.microtesk.model.api.monitor.IStoredValue;

public final class ModelStatePrinter
{
    private final IModel model;
    
    public ModelStatePrinter(IModel model)
    {
        assert null != model;
        this.model = model;
    }
    
    public void printAll()
    {
        printSepator();
        System.out.println("MODEL STATE:");
        
        printRegisters();
        printMemory();
        
        printSepator();
    }

    public void printSepator()
    {
        System.out.println("************************************************");
    }

    public void printRegisters()
    {
        printSepator();

        System.out.println("REGISTER STATE:");
        System.out.println();

        final IModelStateMonitor monitor = model.getModelStateMonitor();
        for (IMetaLocationStore r: model.getMetaData().getRegisters())
        {
            for (int index = 0; index < r.getCount(); ++index)
            {
                final IStoredValue value = monitor.readLocationValue(r.getName(), index);
                System.out.printf("%s[%d] = %s %n", r.getName(), index, value.toBinString());
            }
            System.out.println();
        }
    }

    public void printMemory()
    {
        printSepator();

        System.out.println("MEMORY STATE:");
        System.out.println();

        final IModelStateMonitor monitor = model.getModelStateMonitor();
        for (IMetaLocationStore r: model.getMetaData().getMemoryStores())
        {
            for (int index = 0; index < r.getCount(); ++index)
            {
                final IStoredValue value = monitor.readLocationValue(r.getName(), index);
                System.out.printf("%s[%d] = %s %n", r.getName(), index, value.toBinString());
            }
            System.out.println();
        }
    }
}
