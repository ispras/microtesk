/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * InstructionSetSTBuilder.java, Dec 7, 2012 11:35:35 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.generation;

import java.util.List;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.instruction.IInstructionEx;
import ru.ispras.microtesk.model.api.instruction.InstructionSet;

import ru.ispras.microtesk.translator.generation.ITemplateBuilder;

import static ru.ispras.microtesk.translator.generation.PackageInfo.*;

class InstructionSetSTBuilder implements ITemplateBuilder
{
    private final String specFileName;
    private final String modelName;
    private final List<String> instructionClassNames;

    public InstructionSetSTBuilder(
        String specFileName,
        String modelName,
        List<String> instructionClassNames
        )
    {
        this.specFileName = specFileName;
        this.modelName = modelName;
        this.instructionClassNames = instructionClassNames;
    }

    @Override
    public ST build(STGroup group)
    {
        final ST t = group.getInstanceOf("instruction_set");

        t.add("file", specFileName);
        t.add("pack", String.format(INSTRUCTION_PACKAGE_FORMAT, modelName));

        t.add("imps", IInstructionEx.class.getName());
        t.add("imps", InstructionSet.class.getName());
        t.add("base", InstructionSet.class.getSimpleName());

        t.add("instr_type", IInstructionEx.class.getSimpleName());
        t.add("instrs", instructionClassNames);

        return t;
    }
}
