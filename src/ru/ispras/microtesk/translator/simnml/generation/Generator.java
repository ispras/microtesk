/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Generator.java, Dec 14, 2012 12:18:04 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.generation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.ispras.microtesk.translator.generation.IClassGenerator;
import ru.ispras.microtesk.translator.simnml.ir.IR;
import ru.ispras.microtesk.translator.simnml.ir.modeop.Mode;
import ru.ispras.microtesk.translator.simnml.ir.modeop.Op;
import ru.ispras.microtesk.translator.simnml.ir.instruction.Instruction;

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
            generateModes();
            generateOps();
            generateInstructions();
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

    private void generateInstructions() throws IOException
    {
        final List<String> instructionClassNames = new ArrayList<String>();
        for (Instruction i : ir.getInstructions().values())
        {
            final IClassGenerator instruction = factory.createInstruction(i);
            instruction.generate();

            instructionClassNames.add(i.getClassName());
        }

        final IClassGenerator instructionSet = 
            factory.createInstructionSet(instructionClassNames);

        instructionSet.generate();
    }

    private void generateModes() throws IOException
    {
        for (Mode m : ir.getModes().values())
        {
            final IClassGenerator mode = m.isOrRule() ?
                factory.createAddressingModeOr(m) :
                factory.createAddressingMode(m);

            mode.generate();
        }
    }

    private void generateOps() throws IOException
    {
        for (Op o : ir.getOps().values())
        {
            final IClassGenerator op = o.isOrRule() ?
                factory.createOperationOr(o) :
                factory.createOperation(o);

            op.generate();
        }
    }
}
