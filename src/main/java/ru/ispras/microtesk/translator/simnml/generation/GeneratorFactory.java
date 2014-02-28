/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * GeneratorFactory.java, Dec 6, 2012 12:16:45 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.generation;

import java.util.List;

import ru.ispras.microtesk.translator.generation.*;
import ru.ispras.microtesk.translator.simnml.generation.builders.*;

import ru.ispras.microtesk.translator.simnml.ir.IR;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveOR;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Instruction;

import static ru.ispras.microtesk.translator.generation.PackageInfo.*;

public final class GeneratorFactory
{
    private final String       outDir;
    private final String specFileName;
    private final String    modelName;

    public GeneratorFactory(String outDir, String modelName, String specFileName)
    {
        this.outDir       = outDir;
        this.specFileName = specFileName;
        this.modelName    = modelName.toLowerCase();
    }

    public IClassGenerator createModelGenerator()
    {
        final String outputFileName =
            String.format(getModelFileFormat(outDir), modelName);

        final String[] templateGroups = new String[]
        {
            COMMON_TEMPLATE_DIR + "JavaCommon.stg",
            SIMNML_TEMPLATE_DIR + "Model.stg"
        };

        final ITemplateBuilder modelBuilder =
            new ModelSTBuilder(specFileName, modelName);

        return new ClassGenerator(outputFileName, templateGroups, modelBuilder);
    }

    public IClassGenerator createSharedGenerator(IR ir)
    {
        final String outputFileName =
            String.format(getSharedFileFormat(outDir), modelName);

        final String[] templateGroups = new String[]
        {
            COMMON_TEMPLATE_DIR + "JavaCommon.stg",
            SIMNML_TEMPLATE_DIR + "Shared.stg"
        };

        final ITemplateBuilder builder =
            new SharedSTBuilder(ir, specFileName, modelName);

        return new ClassGenerator(outputFileName, templateGroups, builder);
    }

    public IClassGenerator createInstructionSet(List<String> instructionClassNames)
    {
        final String outputFileName =
            String.format(getInstructionSetFileFormat(outDir), modelName);

        final String[] templateGroups = new String[]
        {
            COMMON_TEMPLATE_DIR + "JavaCommon.stg",
            SIMNML_TEMPLATE_DIR + "InstructionSet.stg"
        };

        final ITemplateBuilder builder =
            new InstructionSetSTBuilder(specFileName, modelName, instructionClassNames);

        return new ClassGenerator(outputFileName, templateGroups, builder);
    }
    
    public IClassGenerator createInstruction(Instruction instruction)
    {
        final String outputFileName =
            String.format(getInstructionFileFormat(outDir), modelName, instruction.getClassName());

        final String[] templateGroups = new String[]
        {
            COMMON_TEMPLATE_DIR + "JavaCommon.stg",
            SIMNML_TEMPLATE_DIR + "Instruction.stg"
        };

        final ITemplateBuilder builder =
            new InstructionSTBuilder(specFileName, modelName, instruction);

        return new ClassGenerator(outputFileName, templateGroups, builder);
    }

    public IClassGenerator createAddressingModeOr(PrimitiveOR mode)
    {
        final String outputFileName =
            String.format(getModeFileFormat(outDir), modelName, mode.getName());

        final String[] templateGroups = new String[]
        {
            COMMON_TEMPLATE_DIR + "JavaCommon.stg",
            SIMNML_TEMPLATE_DIR + "AddressingModeOr.stg"
        };

        final ITemplateBuilder builder =
            new AddressingModeOrSTBuilder(specFileName, modelName, mode);

        return new ClassGenerator(outputFileName, templateGroups, builder);
    }
    
    public IClassGenerator createAddressingMode(PrimitiveAND mode)
    {
        final String outputFileName =
            String.format(getModeFileFormat(outDir), modelName, mode.getName());

        final String[] templateGroups = new String[]
        {
            COMMON_TEMPLATE_DIR + "JavaCommon.stg",
            SIMNML_TEMPLATE_DIR + "AddressingMode.stg"
        };

        final ITemplateBuilder builder =
            new AddressingModeSTBuilder(specFileName, modelName, mode);

        return new ClassGenerator(outputFileName, templateGroups, builder);
    }

    public IClassGenerator createOperationOr(PrimitiveOR op)
    {
        final String outputFileName =
            String.format(getOpFileFormat(outDir), modelName, op.getName());

        final String[] templateGroups = new String[]
        {
            COMMON_TEMPLATE_DIR + "JavaCommon.stg",
            SIMNML_TEMPLATE_DIR + "OperationOr.stg"
        };

        final ITemplateBuilder builder =
             new OperationOrSTBuilder(specFileName, modelName, op);

        return new ClassGenerator(outputFileName, templateGroups, builder);
    }

    public IClassGenerator createOperation(PrimitiveAND op)
    {
        final String outputFileName =
            String.format(getOpFileFormat(outDir), modelName, op.getName());

        final String[] templateGroups = new String[]
        {
            COMMON_TEMPLATE_DIR + "JavaCommon.stg",
            SIMNML_TEMPLATE_DIR + "Operation.stg"
        };

        final ITemplateBuilder builder =
            new OperationSTBuilder(specFileName, modelName, op);

        return new ClassGenerator(outputFileName, templateGroups, builder);
    }
}
