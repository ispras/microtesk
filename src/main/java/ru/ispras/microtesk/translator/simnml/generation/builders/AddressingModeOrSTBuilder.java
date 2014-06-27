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

import java.util.ArrayList;
import java.util.List;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.instruction.AddressingMode;
import ru.ispras.microtesk.translator.generation.ITemplateBuilder;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveOR;

import static ru.ispras.microtesk.translator.generation.PackageInfo.*;

public final class AddressingModeOrSTBuilder implements ITemplateBuilder
{
    private final String specFileName;
    private final String modelName;
    private final PrimitiveOR mode;

    public AddressingModeOrSTBuilder(
        String specFileName,
        String modelName,
        PrimitiveOR mode
        )
    {
        assert mode.getKind() == Primitive.Kind.MODE;

        this.specFileName = specFileName;
        this.modelName = modelName;
        this.mode = mode;
    }

    @Override
    public ST build(STGroup group)
    {
        final ST t = group.getInstanceOf("modeor");

        t.add("name", mode.getName());
        t.add("file", specFileName);
        t.add("pack", String.format(MODE_PACKAGE_FORMAT, modelName));

        t.add("imps", AddressingMode.class.getName());
        t.add("base", AddressingMode.class.getSimpleName());
        
        final List<String> modeNames = new ArrayList<String>(mode.getORs().size());
        for (Primitive p : mode.getORs())
            modeNames.add(p.getName());

        t.add("modes", modeNames);

        return t;
    }
}

