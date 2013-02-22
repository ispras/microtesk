package ru.ispras.microtesk.translator.mmu.generation.builders;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.simnml.instruction.Operation;
import ru.ispras.microtesk.translator.generation.ITemplateBuilder;

public class OperationSTBuilder implements ITemplateBuilder
{
    private final String specFileName;
    private final String packageName;
    private final String name;

    public OperationSTBuilder(String specFileName, String packageName, String name)
    {
        this.specFileName = specFileName;
        this.packageName  = packageName;
        this.name         = name;
    }
    
    @Override
    public ST build(STGroup group)
    {
        final ST t = group.getInstanceOf("op");

        t.add("name", name);
        t.add("file", specFileName);
        t.add("pack", packageName);
        
        t.add("imps", Operation.class.getName());
        t.add("base", Operation.class.getSimpleName());
        
        return t;
    }
}
