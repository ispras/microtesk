package ru.ispras.microtesk.translator.mmu.generation.builders;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.simnml.instruction.AddressingMode;
import ru.ispras.microtesk.translator.generation.ITemplateBuilder;

public class AddressingModeOrSTBuilder implements ITemplateBuilder
{
    private final String   specFileName;
    private final String   packageName;
    private final String   name;
    private final String[] modes; 

    public AddressingModeOrSTBuilder(String specFileName, String packageName, String name, String[] modes)
    {
        this.specFileName = specFileName;
        this.packageName  = packageName;
        this.name         = name;
        this.modes        = modes;
    }

    @Override
    public ST build(STGroup group)
    {
        
        final ST t = group.getInstanceOf("modeor");

        t.add("name", name);
        t.add("file", specFileName);
        t.add("pack", packageName);

        t.add("imps", AddressingMode.class.getName());
        t.add("base", AddressingMode.class.getSimpleName());
        
        t.add("modes", modes);
        
        return t;
    }
}

