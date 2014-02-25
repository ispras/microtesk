package ru.ispras.microtesk.translator.mmu.generation.builders;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.simnml.SimnMLProcessorModel;
import ru.ispras.microtesk.translator.generation.ITemplateBuilder;

public class ModelSTBuilder implements ITemplateBuilder
{
    private static final String   INSTRUCTION_PACKAGE = "instruction";
    private static final String INSTRUCTION_SET_CLASS = "ISA";
    
    private static final String        SHARED_PACKAGE = "shared";
    private static final String          SHARED_CLASS = "Shared";
    
    private static final String      SHARED_REGISTERS = "__REGISTERS";
    private static final String         SHARED_MEMORY = "__MEMORY";
    
    private final String specFileName;
    private final String packageName;
    
    private final String sharedClassName;
    private final String isaClassName;
    
    public ModelSTBuilder(String specFileName, String packageName)
    {
        this.specFileName = specFileName;
        this.packageName  = packageName;

        this.sharedClassName =
            packageName + "." + SHARED_PACKAGE + "." + SHARED_CLASS;

        this.isaClassName = 
            packageName + "." + INSTRUCTION_PACKAGE + "." + INSTRUCTION_SET_CLASS;  
    }

    @Override
    public ST build(STGroup group)
    {
        final ST t = group.getInstanceOf("model");

        t.add("file",  specFileName);
        t.add("pack",  packageName);

        t.add("imps",  SimnMLProcessorModel.class.getName());
        t.add("imps",  isaClassName);

        t.add("simps", sharedClassName);
        t.add("base",  SimnMLProcessorModel.class.getSimpleName());
        
        final ST tc = group.getInstanceOf("constructor");

        tc.add("isaclass", INSTRUCTION_SET_CLASS);
        tc.add("reg",      SHARED_REGISTERS);
        tc.add("mem",      SHARED_MEMORY);

        t.add("members", tc);

        return t;
    }
}
