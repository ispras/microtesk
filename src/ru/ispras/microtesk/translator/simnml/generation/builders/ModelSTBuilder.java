/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ModelSTBuilder.java, Dec 6, 2012 11:44:08 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.generation.builders;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.simnml.SimnMLProcessorModel;
import ru.ispras.microtesk.translator.generation.ITemplateBuilder;

import ru.ispras.microtesk.model.api.debug.MetaModelPrinter;
import ru.ispras.microtesk.model.api.debug.ModelStatePrinter;

import static ru.ispras.microtesk.translator.generation.PackageInfo.*;

public class ModelSTBuilder implements ITemplateBuilder
{
    private final String specFileName;
    private final String modelName;
    
    public ModelSTBuilder(String specFileName, String modelName)
    {
        this.specFileName = specFileName;
        this.modelName = modelName;
    }

    @Override
    public ST build(STGroup group)
    {
        final ST t = group.getInstanceOf("model");

        t.add("file",  specFileName);
        t.add("pack",  String.format(MODEL_PACKAGE_FORMAT, modelName));

        t.add("imps",  SimnMLProcessorModel.class.getName());
        t.add("imps",  String.format(INSTRUCTION_SET_CLASS_FORMAT, modelName));
        
        t.add("imps",  MetaModelPrinter.class.getName());
        t.add("imps",  ModelStatePrinter.class.getName());
        
        t.add("simps", String.format(SHARED_CLASS_FORMAT, modelName));

        t.add("base",  SimnMLProcessorModel.class.getSimpleName());
        
        final ST tc = group.getInstanceOf("constructor");

        tc.add("isaclass", INSTRUCTION_SET_CLASS_NAME);
        tc.add("reg",      SimnMLProcessorModel.SHARED_REGISTERS);
        tc.add("mem",      SimnMLProcessorModel.SHARED_MEMORY);

        t.add("members", tc);
        t.add("members", group.getInstanceOf("debug_block"));

        return t;
    }
}
