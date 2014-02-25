/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * InstructionSTBuilder.java, Dec 7, 2012 3:29:34 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.generation.builders;

import java.util.Map;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.instruction.IInstructionCall;
import ru.ispras.microtesk.model.api.instruction.IInstructionCallBuilderEx;
import ru.ispras.microtesk.model.api.simnml.instruction.AddressingModeImm;
import ru.ispras.microtesk.model.api.simnml.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.simnml.instruction.InstructionBase;
import ru.ispras.microtesk.model.api.simnml.instruction.InstructionCall;
import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;

import ru.ispras.microtesk.translator.simnml.ir.primitive.Instruction;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.generation.ITemplateBuilder;

import static ru.ispras.microtesk.translator.generation.PackageInfo.*;

public class InstructionSTBuilder implements ITemplateBuilder
{
    private final String specFileName;
    private final String modelName;
    private final Instruction instruction;

    private boolean    immsImported = false;
    private boolean needModeImports = false;

    public InstructionSTBuilder(
        String specFileName,
        String modelName,
        Instruction instruction
        )
    {
        this.specFileName = specFileName;
        this.modelName = modelName;
        this.instruction = instruction;
    }

    private void buildHeader(ST t)
    {
        t.add("name", instruction.getName());
        t.add("class_name", instruction.getClassName());
        
        t.add("file", specFileName);
        t.add("pack", String.format(INSTRUCTION_PACKAGE_FORMAT, modelName));
        
        t.add("imps", ConfigurationException.class.getName());
        t.add("imps", IInstructionCall.class.getName());
        t.add("imps", IInstructionCallBuilderEx.class.getName());
        
        t.add("imps", InstructionCall.class.getName());
        t.add("imps", InstructionBase.class.getName());
        t.add("imps", IAddressingMode.class.getName());
        
        if (needModeImports)
        	t.add("imps", String.format(MODE_CLASS_FORMAT, modelName, "*"));
        t.add("imps", String.format(OP_CLASS_FORMAT, modelName, "*"));
        
        t.add("simps", String.format(SHARED_CLASS_FORMAT, modelName));

        t.add("base", InstructionBase.class.getSimpleName());
    }
    
    private void importImmDependencies(ST t)
    {
        if (!immsImported)
        {
            t.add("imps", AddressingModeImm.class.getName());
            t.add("imps", String.format("%s.*", Type.class.getPackage().getName()));
            immsImported = true;
        }
    }

    private void buildParameters(ST t)
    {
        for (Map.Entry<String, Primitive> e : instruction.getArguments().entrySet())
        {
            t.add("param_names", e.getKey());
            
            if (Primitive.Kind.MODE == e.getValue().getKind())
            	needModeImports = true;

            if (Primitive.Kind.MODE == e.getValue().getKind() ||
                Primitive.Kind.OP == e.getValue().getKind())
            {
                t.add("param_type_names",
                   String.format("%s.INFO", e.getValue().getName())
                );
            }
            else
            {
                importImmDependencies(t);
                
                t.add("param_type_names",
                    String.format("%s.INFO(%s)",
                        AddressingModeImm.class.getSimpleName(), e.getValue().getName())
                );
            }
        }
    }

    private void buildPrimitiveTree(STGroup group, ST t)
    {
        final PrimitiveAND root = instruction.getRootOperation();
        t.add("op_tree", creatOperationST(root, group));
    }

    private ST creatOperationST(PrimitiveAND op, STGroup group)
    {
        final ST t = group.getInstanceOf("instruction_operation");

        t.add("name", op.getName());
        for (Map.Entry<String, Primitive> e : op.getArguments().entrySet())
        {
            if (e.getValue().getKind() == Primitive.Kind.MODE)
            {
                t.add("params", e.getKey());
            }
            else if (e.getValue().getKind() == Primitive.Kind.OP)
            {
                assert !e.getValue().isOrRule();
                t.add("params", creatOperationST((PrimitiveAND) e.getValue(), group));
            }
            else
            {
                t.add("params", String.format("%s.access()", e.getKey()));
            }
        }

        return t;
    }

    @Override
    public ST build(STGroup group)
    {
        final ST t = group.getInstanceOf("instruction");

        buildParameters(t);
        buildPrimitiveTree(group, t);
        buildHeader(t);

        return t;
    }
}
