/*
 * Copyright (c) 2012 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * STBuilderInstructionSet.java, Dec 7, 2012 11:35:35 AM Andrei Tatarnikov
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

import java.util.List;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import ru.ispras.microtesk.model.api.instruction.IInstructionEx;
import ru.ispras.microtesk.model.api.instruction.InstructionSet;

import ru.ispras.microtesk.translator.generation.ITemplateBuilder;

import static ru.ispras.microtesk.translator.generation.PackageInfo.*;

class STBuilderInstructionSet implements ITemplateBuilder
{
    private final String specFileName;
    private final String modelName;
    private final List<String> instructionClassNames;

    public STBuilderInstructionSet(
        String specFileName,
        String modelName,
        List<String> instructionClassNames
        )
    {
        this.specFileName = specFileName;
        this.modelName = modelName;
        this.instructionClassNames = instructionClassNames;
    }

    @Override
    public ST build(STGroup group)
    {
        final ST t = group.getInstanceOf("instruction_set");

        t.add("file", specFileName);
        t.add("pack", String.format(INSTRUCTION_PACKAGE_FORMAT, modelName));

        t.add("imps", IInstructionEx.class.getName());
        t.add("imps", InstructionSet.class.getName());
        t.add("base", InstructionSet.class.getSimpleName());

        t.add("instr_type", IInstructionEx.class.getSimpleName());
        t.add("instrs", instructionClassNames);

        return t;
    }
}
