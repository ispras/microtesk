package ru.ispras.microtesk.translator.mmu.generation.builders;

import java.util.ArrayList;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.translator.generation.ITemplateBuilder;
import ru.ispras.microtesk.translator.mmu.ir.IR;

public class SharedSTBuilder implements ITemplateBuilder
{
    private static final String REGISTERS = "__REGISTERS";
    private static final String MEMORY    = "__MEMORY";

    public final String specFileName;
    public final String packageName;
    public final IR ir;

    public SharedSTBuilder(IR ir, String specFileName, String packageName)
    {
        this.specFileName = specFileName;
        this.packageName  = packageName;
        this.ir = ir;
    }
    
    private void insertEmptyLine(ST t)
    {
        t.add("members", "");
    }

    private void buildHeader(ST t)
    {
        t.add("file",  specFileName);
        t.add("pack",  packageName);
        t.add("imps", Memory.class.getName());
    }

    private void buildMemory(STGroup group, ST t)
    {
   
        final ArrayList<String> registers = new ArrayList<String>();
        final ArrayList<String> memory = new ArrayList<String>();
        
        insertEmptyLine(t);
        
        final ST tRegisters = group.getInstanceOf("memory_array");

        tRegisters.add("name", REGISTERS);
        tRegisters.add("items", registers);

        t.add("members", tRegisters);

        final ST tMemory = group.getInstanceOf("memory_array");

        tMemory.add("name", MEMORY);
        tMemory.add("items", memory);

        t.add("members", tMemory);
    }

    @Override
    public ST build(STGroup group)
    {
        final ST t = group.getInstanceOf("shared");

        buildHeader(t);
        buildMemory(group, t);

        return t;
    }
}
