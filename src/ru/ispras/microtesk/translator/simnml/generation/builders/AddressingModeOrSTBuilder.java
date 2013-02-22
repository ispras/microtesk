/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * AddressingModeOrSTBuilder.java, Dec 7, 2012 1:58:00 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.generation.builders;

import java.util.List;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.simnml.instruction.AddressingMode;
import ru.ispras.microtesk.translator.generation.ITemplateBuilder;

import static ru.ispras.microtesk.translator.generation.PackageInfo.*;

public final class AddressingModeOrSTBuilder implements ITemplateBuilder
{
    private final String specFileName;
    private final String modelName;
    private final String name;
    private final List<String> modes; 

    public AddressingModeOrSTBuilder(
        String specFileName,
        String modelName,
        String name,
        List<String> modes
        )
    {
        this.specFileName = specFileName;
        this.modelName    = modelName;
        this.name         = name;
        this.modes        = modes;
    }

    @Override
    public ST build(STGroup group)
    {
        final ST t = group.getInstanceOf("modeor");

        t.add("name", name);
        t.add("file", specFileName);
        t.add("pack", String.format(MODE_PACKAGE_FORMAT, modelName));

        t.add("imps", AddressingMode.class.getName());
        t.add("base", AddressingMode.class.getSimpleName());

        t.add("modes", modes);

        return t;
    }
}

