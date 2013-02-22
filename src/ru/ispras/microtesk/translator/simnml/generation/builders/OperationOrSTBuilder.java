/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * OperationOrSTBuilder.java, Dec 7, 2012 2:46:37 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.generation.builders;

import java.util.List;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.simnml.instruction.Operation;
import ru.ispras.microtesk.translator.generation.ITemplateBuilder;

import static ru.ispras.microtesk.translator.generation.PackageInfo.*;

public final class OperationOrSTBuilder implements ITemplateBuilder
{
    private final String specFileName;
    private final String modelName;
    private final String name;
    private final List<String> ops; 

    public OperationOrSTBuilder(
        String specFileName,
        String modelName,
        String name,
        List<String> ops
        )
    {
        this.specFileName = specFileName;
        this.modelName    = modelName;
        this.name         = name;
        this.ops          = ops;
    }

    @Override
    public ST build(STGroup group)
    {
        final ST t = group.getInstanceOf("op");

        t.add("name", name);
        t.add("file", specFileName);
        t.add("pack", String.format(OP_PACKAGE_FORMAT, modelName));

        t.add("imps", Operation.class.getName());
        t.add("base", Operation.class.getSimpleName());
        
        t.add("ops", ops);
        return t;
    }
}
