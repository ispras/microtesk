package ru.ispras.microtesk.translator.mmu.generation;

import ru.ispras.microtesk.translator.generation.ClassGenerator;
import ru.ispras.microtesk.translator.generation.IClassGenerator;
import ru.ispras.microtesk.translator.generation.ITemplateBuilder;
import ru.ispras.microtesk.translator.mmu.generation.builders.AddressingModeOrSTBuilder;
import ru.ispras.microtesk.translator.mmu.generation.builders.AddressingModeSTBuilder;
import ru.ispras.microtesk.translator.mmu.generation.builders.InstructionSetSTBuilder;
import ru.ispras.microtesk.translator.mmu.generation.builders.ModelSTBuilder;
import ru.ispras.microtesk.translator.mmu.generation.builders.OperationOrSTBuilder;
import ru.ispras.microtesk.translator.mmu.generation.builders.OperationSTBuilder;
import ru.ispras.microtesk.translator.mmu.generation.builders.SharedSTBuilder;
import ru.ispras.microtesk.translator.mmu.ir.IR;

import static ru.ispras.microtesk.translator.generation.PackageInfo.*;

public class GeneratorFactory
{
    private final String     specFileName;
    private final String modelRootPackage;
    private final String    modelRootPath;

    public GeneratorFactory(String modelName, String specFileName)
    {
        this.specFileName     = specFileName;
        this.modelRootPackage = MODEL_PACKAGE + "." + modelName.toLowerCase();
        this.modelRootPath    = getModelOutDir(DEFAULT_OUTDIR) + "/" + modelName.toLowerCase();
    }

    public IClassGenerator createModelGenerator()
    {
        final String outputFileName =
            modelRootPath + "/Model.java";

        final String[] templateGroups = new String[]
        {
            COMMON_TEMPLATE_DIR + "JavaCommon.stg",
            SIMNML_TEMPLATE_DIR + "Model.stg"
        };

        final ITemplateBuilder modelBuilder =
            new ModelSTBuilder(specFileName, modelRootPackage);

        return new ClassGenerator(outputFileName, templateGroups, modelBuilder);
    }
    
    public IClassGenerator createSharedGenerator(IR ir)
    {
        final String outputFileName =
            modelRootPath + "/shared/Shared.java";

        final String packageName = 
            modelRootPackage + ".shared";

        final String[] templateGroups = new String[]
        {
            COMMON_TEMPLATE_DIR + "JavaCommon.stg",
            SIMNML_TEMPLATE_DIR + "Shared.stg"
        };

        final ITemplateBuilder builder =
            new SharedSTBuilder(ir, specFileName, packageName);

        return new ClassGenerator(outputFileName, templateGroups, builder);
    }
    
    public IClassGenerator createInstructionSet()
    {
        final String outputFileName =
            modelRootPath + "/instruction/ISA.java";

        final String packageName = 
            modelRootPackage + ".instruction";

        final String[] templateGroups = new String[]
        {
            COMMON_TEMPLATE_DIR + "JavaCommon.stg",
            SIMNML_TEMPLATE_DIR + "InstructionSet.stg"
        };

        final ITemplateBuilder builder =
            new InstructionSetSTBuilder(specFileName, packageName);

        return new ClassGenerator(outputFileName, templateGroups, builder);
    }
    
    public IClassGenerator createAddressingModeOr(String name, String[] modes)
    {
        final String outputFileName = modelRootPath + "/mode/" + name + ".java";
        final String    packageName =  modelRootPackage + ".mode";

        final String[] templateGroups = new String[]
        {
            COMMON_TEMPLATE_DIR + "JavaCommon.stg",
            SIMNML_TEMPLATE_DIR + "AddressingModeOr.stg"
        };

        final ITemplateBuilder builder =
             new AddressingModeOrSTBuilder(specFileName, packageName, name, modes);

        return new ClassGenerator(outputFileName, templateGroups, builder);
    }
    
    public IClassGenerator createAddressingMode(String name)
    {
        final String outputFileName = modelRootPath + "/mode/" + name + ".java";
        final String    packageName =  modelRootPackage + ".mode";

        final String[] templateGroups = new String[]
        {
            COMMON_TEMPLATE_DIR + "JavaCommon.stg",
            SIMNML_TEMPLATE_DIR + "AddressingMode.stg"
        };

        final ITemplateBuilder builder =
             new AddressingModeSTBuilder(specFileName, packageName, name);

        return new ClassGenerator(outputFileName, templateGroups, builder);
    }

    public IClassGenerator createOperationOr(String name, String[] ops)
    {
        final String outputFileName = modelRootPath + "/op/" + name + ".java";
        final String    packageName = modelRootPackage + ".op";

        final String[] templateGroups = new String[]
        {
            COMMON_TEMPLATE_DIR + "JavaCommon.stg",
            SIMNML_TEMPLATE_DIR + "OperationOr.stg"
        };

        final ITemplateBuilder builder =
             new OperationOrSTBuilder(specFileName, packageName, name, ops);

        return new ClassGenerator(outputFileName, templateGroups, builder);
    }
    
    public IClassGenerator createOperation(String name)
    {
        final String outputFileName = modelRootPath + "/op/" + name + ".java";
        final String    packageName = modelRootPackage + ".op";

        final String[] templateGroups = new String[]
        {
            COMMON_TEMPLATE_DIR + "JavaCommon.stg",
            SIMNML_TEMPLATE_DIR + "Operation.stg"
        };

        final ITemplateBuilder builder =
             new OperationSTBuilder(specFileName, packageName, name);

        return new ClassGenerator(outputFileName, templateGroups, builder);
    }
}
