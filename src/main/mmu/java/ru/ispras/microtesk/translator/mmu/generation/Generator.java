package ru.ispras.microtesk.translator.mmu.generation;

import java.io.IOException;

import ru.ispras.microtesk.translator.generation.IClassGenerator;
import ru.ispras.microtesk.translator.mmu.ir.IR;

public final class Generator
{
    private final GeneratorFactory factory;
    private final IR ir;
    
    public Generator(String modelName, String specFileName, IR ir)
    {
        this.factory = new GeneratorFactory(modelName, specFileName);
        this.ir = ir;
    }

    public void generate()
    {
        try 
        {
            generateShared();
            generateInstructionSet();
            generateModel();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void generateModel() throws IOException
    {
        final IClassGenerator model = factory.createModelGenerator();
        model.generate();
    }

    private void generateShared() throws IOException
    {
        final IClassGenerator shared = factory.createSharedGenerator(ir);
        shared.generate();
    }

    private void generateInstructionSet() throws IOException
    {
        final IClassGenerator instructionSet = factory.createInstructionSet();
        instructionSet.generate();
    }

}
