/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * CallSimulator.java, Jun 27, 2014 3:30:20 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.api.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IAddressingModeBuilder;
import ru.ispras.microtesk.model.api.instruction.IArgumentBuilder;
import ru.ispras.microtesk.model.api.instruction.IInstruction;
import ru.ispras.microtesk.model.api.instruction.IInstructionCallBuilderEx;
import ru.ispras.microtesk.model.api.simnml.instruction.InstructionCall;

public abstract class CallSimulator
{
    protected final static class Mode
    {
        private final String name;
        private final Map<String, Integer> parameters;

        public Mode(String name, Map<String, Integer> parameters)
        {
            this.name = name;
            this.parameters = parameters;
        }

        public void visit(IArgumentBuilder argBuilder) throws ConfigurationException
        {
            final IAddressingModeBuilder modeBuilder =
                argBuilder.getModeBuilder(name);

            for (Map.Entry<String, Integer> e : parameters.entrySet())
                modeBuilder.setArgumentValue(e.getKey(), e.getValue());
        }        
    }
    
    protected static final class Argument
    {
        private final String name;
        private final Mode mode;

        public Argument(String name, Mode mode)
        {
            assert null != name;
            assert null != mode;

            this.name = name;
            this.mode = mode;
        }

        public String getName()
        {
            return name;
        }

        public Mode getMode()
        {
            return mode;
        }
    }

    private final IModel model;
    private final List<InstructionCall> calls;

    protected CallSimulator(IModel model)
    {
        if (null == model)
            throw new NullPointerException();

        this.model = model;
        this.calls = new ArrayList<InstructionCall>();
    }

    protected final void addCall(String name, Argument ... args) throws ConfigurationException
    {
        if (null == name)
            throw new NullPointerException();

        final IInstruction instruction = model.getInstruction(name);
        final IInstructionCallBuilderEx callBuilder = instruction.createCallBuilder();

        for (Argument arg : args)
        {
            final IArgumentBuilder argBuilder = callBuilder.getArgumentBuilder(arg.getName());
            arg.getMode().visit(argBuilder);
        }

        final InstructionCall call = callBuilder.getCall();
        calls.add(call);
    }
    
    public final void execute()
    {
        for (InstructionCall call : calls)
            call.execute();
    }
    
    public final void print()
    {
        System.out.println("************************************************");

        for (InstructionCall call : calls)
            call.print();
    }
}
