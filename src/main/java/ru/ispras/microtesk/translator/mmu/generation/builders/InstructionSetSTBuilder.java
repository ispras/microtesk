package ru.ispras.microtesk.translator.mmu.generation.builders;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.instruction.IInstructionEx;
import ru.ispras.microtesk.model.api.instruction.InstructionSet;

import ru.ispras.microtesk.translator.generation.ITemplateBuilder;

public class InstructionSetSTBuilder implements ITemplateBuilder
{
    private final String   specFileName;
    private final String    packageName;
    
    public InstructionSetSTBuilder(String specFileName, String packageName)
    {
        this.specFileName = specFileName;
        this.packageName  = packageName;
    }
    
    @Override
    public ST build(STGroup group)
    {
        final ST t = group.getInstanceOf("instruction_set");

        t.add("file",   specFileName);
        t.add("pack",   packageName);

        t.add("imps",   IInstructionEx.class.getName());
        t.add("imps",   InstructionSet.class.getName());
        t.add("base",   InstructionSet.class.getSimpleName());
        
        //t.add("instrs", instructions);

        return t;
    }
}
