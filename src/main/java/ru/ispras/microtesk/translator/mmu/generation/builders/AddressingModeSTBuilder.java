package ru.ispras.microtesk.translator.mmu.generation.builders;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import java.util.Map;
import ru.ispras.microtesk.model.api.type.Type; 
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.memory.Location;
import ru.ispras.microtesk.model.api.simnml.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.simnml.instruction.AddressingMode;
import ru.ispras.microtesk.translator.generation.ITemplateBuilder;

public class AddressingModeSTBuilder implements ITemplateBuilder
{
    private final String specFileName;
    private final String packageName;
    private final String name;

    public AddressingModeSTBuilder(String specFileName, String packageName, String name)
    {
        this.specFileName = specFileName;
        this.packageName  = packageName;
        this.name         = name;
    }
    
    @Override
    public ST build(STGroup group)
    {
        final ST t = group.getInstanceOf("mode");

        t.add("name", name);
        t.add("file", specFileName);
        t.add("pack", packageName);

        t.add("imps", Map.class.getName());
        t.add("imps", Type.class.getName());
        t.add("imps", Data.class.getName());
        t.add("imps", Location.class.getName());
        t.add("imps", IAddressingMode.class.getName());
        t.add("imps", AddressingMode.class.getName());
        
        t.add("base", AddressingMode.class.getSimpleName());
        
        return t;
    }
}
