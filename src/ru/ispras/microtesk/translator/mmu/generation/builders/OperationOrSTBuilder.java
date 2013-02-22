package ru.ispras.microtesk.translator.mmu.generation.builders;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.simnml.instruction.Operation;

import ru.ispras.microtesk.translator.generation.ITemplateBuilder;

public class OperationOrSTBuilder implements ITemplateBuilder
{
    private final String   specFileName;
    private final String   packageName;
    private final String   name;
    private final String[] ops; 

    public OperationOrSTBuilder(
        String specFileName,
        String packageName,
        String name,
        String[] ops
        )
    {
        this.specFileName = specFileName;
        this.packageName  = packageName;
        this.name         = name;
        this.ops          = ops;
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
        
        t.add("ops", ops);
        return t;
    }
}
