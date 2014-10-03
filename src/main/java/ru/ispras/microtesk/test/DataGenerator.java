/*
 * Copyright (c) 2013 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * DataGenerator.java, May 13, 2013 11:32:21 AM Andrei Tatarnikov
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

package ru.ispras.microtesk.test;

import ru.ispras.fortress.solver.Environment;

import ru.ispras.microtesk.model.api.ICallFactory;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.instruction.IAddressingModeBuilder;
import ru.ispras.microtesk.model.api.instruction.IOperation;
import ru.ispras.microtesk.model.api.instruction.IOperationBuilder;
import ru.ispras.microtesk.model.api.instruction.InstructionCall;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.sequence.SequenceBuilder;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.RandomValue;
import ru.ispras.microtesk.test.template.Situation;

public final class DataGenerator
{
    private final ICallFactory callFactory;
    private final DataEngine dataEngine;

    private SequenceBuilder<ConcreteCall> sequenceBuilder;

    public DataGenerator(IModel model) 
    {
        checkNotNull(model);

        this.dataEngine = new DataEngine();
        this.callFactory = model.getCallFactory();
        this.sequenceBuilder = null;

        final String home = 
            System.getenv().get("MICROTESK_HOME") + "/tools/z3";

        if (Environment.isUnix())
        {
            Environment.setSolverPath(home + "/unix/z3");
        }
        else if(Environment.isWindows())
        {
            Environment.setSolverPath(home + "/windows/z3.exe");
        }
        else if(Environment.isOSX())
        {
            Environment.setSolverPath(home + "/osx/z3");
        }
        else
        {
            throw new IllegalStateException(String.format(
                "Failed to initialize the solver engine. " + 
                "Unsupported platform: %s", System.getProperty("os.name")));
        }
    }

    public Sequence<ConcreteCall> generate(
        Sequence<Call> abstractSequence) throws ConfigurationException
    {
        checkNotNull(abstractSequence);

        sequenceBuilder = new SequenceBuilder<ConcreteCall>();

        try
        {
            for (Call abstractCall : abstractSequence)
                processAbstractCall(abstractCall);

            return sequenceBuilder.build();
        }
        finally
        {
            sequenceBuilder = null;
        }
    }

    private void processAbstractCall(
        Call abstractCall) throws ConfigurationException
    {
        checkNotNull(abstractCall);

        if (!abstractCall.isExecutable())
        {
            sequenceBuilder.add(new ConcreteCall(abstractCall));
            return;
        }

        final Primitive rootOp = abstractCall.getRootOperation();
        checkRootOp(rootOp);

        System.out.printf(
            "%nProcessing instruction (root: %s)...%n", rootOp.getName());

        resolveSituations(rootOp);

        final IOperation modelOp = makeOp(rootOp);
        final InstructionCall modelCall = callFactory.newCall(modelOp);

        sequenceBuilder.add(new ConcreteCall(abstractCall, modelCall));
    }

    private void resolveSituations(Primitive p)
    {
        checkNotNull(p);

        for (Argument arg: p.getArguments().values())
        {
            if (Argument.Kind.OP == arg.getKind())
                resolveSituations((Primitive) arg.getValue());
        }

        final Situation situation = p.getSituation();

        // No situation is associated with the given primitive.  
        if (null == situation)
            return;

        dataEngine.generateData(situation, p);
        // TODO
    }

    private int makeImm(Argument argument)
    {
        checkArgKind(argument, Argument.Kind.IMM);

        return (Integer) argument.getValue();
    }

    private int makeImmRandom(Argument argument)
    {
        checkArgKind(argument, Argument.Kind.IMM_RANDOM);

        return ((RandomValue) argument.getValue()).getValue();
    }

    private IAddressingMode makeMode(Argument argument)
        throws ConfigurationException
    {
        checkArgKind(argument, Argument.Kind.MODE);

        final Primitive mode = 
            (Primitive) argument.getValue();

        final IAddressingModeBuilder builder =
            callFactory.newMode(mode.getName());

        for (Argument arg: mode.getArguments().values())
        {
            final String argName = arg.getName();
            switch (arg.getKind())
            {
            case IMM:
                builder.setArgumentValue(argName, makeImm(arg));
                break;

            case IMM_RANDOM:
                builder.setArgumentValue(argName, makeImmRandom(arg));
                break;

            case IMM_UNKNOWN:
                // TODO
                throw new UnsupportedOperationException(arg.getKind().name());

            default:
                throw new IllegalArgumentException(
                    "Illegal kind: " + arg.getKind());
            }
        }

        return builder.getProduct();
    }

    private IOperation makeOp(Argument argument)
        throws ConfigurationException
    {
        checkArgKind(argument, Argument.Kind.OP);

        final Primitive abstractOp = 
            (Primitive) argument.getValue();

        return makeOp(abstractOp);
    }

    private IOperation makeOp(Primitive abstractOp)
        throws ConfigurationException
    {
        checkOp(abstractOp);

        final String name = abstractOp.getName();
        final String context = abstractOp.getContextName();

        final IOperationBuilder builder = 
            callFactory.newOp(name, context);

        for (Argument arg : abstractOp.getArguments().values())
        {
            final String argName = arg.getName();
            switch(arg.getKind())
            {
            case IMM:
                builder.setArgument(argName, makeImm(arg));
                break;

            case IMM_RANDOM:
                builder.setArgument(argName, makeImmRandom(arg));
                break;

            case IMM_UNKNOWN:
                // TODO
                throw new UnsupportedOperationException(arg.getKind().name());

            case MODE:
                builder.setArgument(argName, makeMode(arg));
                break;

            case OP:
                builder.setArgument(argName, makeOp(arg));
                break;

            default:
                throw new IllegalArgumentException(
                    "Illegal kind: " + arg.getKind());
            }
        }

        return builder.build();
    }

    private static void checkNotNull(Object o)
    {
        if (null == o)
            throw new NullPointerException();
    }

    private static void checkOp(Primitive op)
    {
        if (Primitive.Kind.OP != op.getKind())
            throw new IllegalArgumentException(String.format(
                "%s is not an operation.", op.getName()));
    }

    private static void checkRootOp(Primitive op)
    {
        checkOp(op);
        if (!op.isRoot())
            throw new IllegalArgumentException(String.format(
                "%s is not a root operation!", op.getName()));
    }

    private static void checkArgKind(Argument arg, Argument.Kind expected)
    {
        if (arg.getKind() != expected)
            throw new IllegalArgumentException(String.format(
                "Argument %s has kind %s while %s is expected.",
                arg.getName(), arg.getKind(), expected));
    }
}
