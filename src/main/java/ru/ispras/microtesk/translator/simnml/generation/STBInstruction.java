/*
 * Copyright (c) 2012 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * STBInstruction.java, Dec 7, 2012 3:29:34 PM Andrei Tatarnikov
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.translator.simnml.generation;

import java.util.Map;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.instruction.IInstructionCallBuilderEx;
import ru.ispras.microtesk.model.api.instruction.AddressingModeImm;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.instruction.InstructionBase;
import ru.ispras.microtesk.model.api.instruction.InstructionCall;
import ru.ispras.microtesk.model.api.situation.ISituation;
import ru.ispras.microtesk.model.api.situation.builtin.RandomSituation;
import ru.ispras.microtesk.model.api.situation.builtin.ZeroSituation;
import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;

import ru.ispras.microtesk.translator.simnml.ir.primitive.Instruction;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Situation;
import ru.ispras.microtesk.translator.generation.ITemplateBuilder;

import static ru.ispras.microtesk.translator.generation.PackageInfo.*;

final class STBInstruction implements ITemplateBuilder
{
    private final String specFileName;
    private final String modelName;
    private final Instruction instruction;

    private boolean    immsImported = false;
    private boolean needModeImports = false;
    private boolean needSitsImports = false;
    
    public STBInstruction(
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
        t.add("imps", IInstructionCallBuilderEx.class.getName());
        
        t.add("imps", InstructionCall.class.getName());
        t.add("imps", InstructionBase.class.getName());
        t.add("imps", IAddressingMode.class.getName());
        t.add("imps", ISituation.class.getName());

        if (needModeImports)
        	t.add("imps", String.format(MODE_CLASS_FORMAT, modelName, "*"));
        t.add("imps", String.format(OP_CLASS_FORMAT, modelName, "*"));
        if (needSitsImports)
            t.add("imps", String.format(SITUATION_CLASS_FORMAT, modelName, "*"));

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

    private void buildSituations(ST t)
    {
        for (Situation situation : instruction.getAllSituations())
        {
            t.add("situation_names", situation.getFullName());
            if (!needSitsImports) needSitsImports = true;
        }

        // TODO: Temporary code that assigns the "random" and "zero" situations to all instructions
        // that have at least one parameter. This is not exactly how it should be.
        // It should work only for parameters that represent input values (not flags, not output values). 

        if (!instruction.getArguments().isEmpty())
        {
            if (!instruction.isSituationDefined(RandomSituation.INFO.getName()))
            {
                t.add("situation_names", RandomSituation.class.getSimpleName());
                t.add("imps", RandomSituation.class.getName());
            }

            if (!instruction.isSituationDefined(ZeroSituation.INFO.getName()))
            {
                t.add("situation_names", ZeroSituation.class.getSimpleName());
                t.add("imps", ZeroSituation.class.getName());
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
                assert !e.getValue().isOrRule() : String.format("%s is an OR rule: %s", e.getKey(), e.getValue().getName());
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
        buildSituations(t);
        buildPrimitiveTree(group, t);
        buildHeader(t);

        return t;
    }
}
