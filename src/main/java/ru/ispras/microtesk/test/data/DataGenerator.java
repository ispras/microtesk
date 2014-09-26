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

package ru.ispras.microtesk.test.data;

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
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.RandomValue;
import ru.ispras.microtesk.test.template.Situation;

public final class DataGenerator
{
    private final IModel model;
    private SequenceBuilder<ConcreteCall> sequenceBuilder;

    public DataGenerator(IModel model)
    {
        if (null == model)
            throw new NullPointerException();

        this.model = model;
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

    private ICallFactory getCallFactory()
    {
        return model.getCallFactory();
    }

    public Sequence<ConcreteCall> generate(
        Sequence<Call> abstractSequence) throws ConfigurationException
    {
        if (null == abstractSequence)
            throw new NullPointerException();

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
        if (null == abstractCall)
            throw new NullPointerException();

        if (!abstractCall.isExecutable())
        {
            sequenceBuilder.addCall(new ConcreteCall(abstractCall));
            return;
        }

        final Primitive rootOp = abstractCall.getRootOperation();
        checkRootOp(rootOp);

        System.out.println("Processing: " + rootOp.getName());
        resolveSituations(rootOp);

        final IOperation modelOp = makeOp(rootOp);
        final InstructionCall modelCall = getCallFactory().newCall(modelOp);

        sequenceBuilder.addCall(new ConcreteCall(abstractCall, modelCall));
    }

    private void resolveSituations(Primitive p)
    {
        if (null == p)
            throw new NullPointerException();

        for (Argument arg: p.getArguments().values())
        {
            if (Argument.Kind.OP == arg.getKind())
                resolveSituations((Primitive) arg.getValue());
        }

        final Situation situation = p.getSituation(); 

        if (null == situation)
            return;

        System.out.printf(
           "Trying to solve situation: %s...%n%n", situation);

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
            getCallFactory().newMode(mode.getName());

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

             default:
                 throw new IllegalArgumentException(
                    "Illegal kind: " + argument.getKind());
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
            getCallFactory().newOp(name, context);

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


/*

final Situation situation = abstractCall.getSituation();
if (null == situation)
    return;

System.out.printf("%nTrying to solve situation: %s%n", situation);
System.out.printf("for instruction '%s' (modes:", abstractCall.getName());

final Primitive rootOperation =
    abstractCall.getRootOperation();

for (Argument argument : rootOperation.getArguments().values())
{
    System.out.print(" ");
    System.out.print(argument.isImmediate() ?
       AddressingModeImm.PARAM_NAME :
       ((Primitive) argument.getValue()).getName()); 
}

System.out.println(")");

*/

/*

final ISituation situation =
    instruction.createSituation(situationName);

// This is needed for situations like random that do not have a signature 
// and generate values for any parameters the client code might request.
// Other situations may ignore these calls.

for (Argument argument : rootOperation.getArguments().values())
    situation.setOutput(argument.getName());

Map<String, Data> output = null;

try 
{
    output = situation.solve();
}
catch (ConfigurationException e)
{
    System.out.printf("Warning! Failed to generate test data for the %s situation.\nReason: %s.\n",
        situationName, e.getMessage());

    return;
}

for (Map.Entry<String, Data> entry : output.entrySet())
{
    final Argument argument = rootOperation.getArguments().get(entry.getKey());

    if (null == argument)
    {
        System.out.printf("Argument %s is not defined for instruction %s.%n",
           entry.getKey(), rootOperation.getName());
        continue;
    }

    insertInitializingCalls(argument, entry.getValue());
}
*/

/*
private void insertInitializingCalls(Argument argument, Data value) throws ConfigurationException
{
    final String argumentTypeName = argument.isImmediate() ?
        AddressingModeImm.NAME : ((Primitive) argument.getValue()).getName();
    
    System.out.printf(
        "Initializer: argument: %7s, mode: %10s, value: %s (%s) %n",
        argument.getName(),
        argumentTypeName,
        Integer.toHexString(value.getRawData().intValue()),
        value.getRawData().toBinString()
    );

    for(IInitializerGenerator ig : model.getInitializers())
    {
        if (ig.isCompatible(argument))
        {
            final List<ConcreteCall> calls = ig.createInitializingCode(argument, value);
            sequenceBuilder.addInitializingCalls(calls);
            return;
        }
    }

    System.out.println(
        String.format(
            "Error! Failed to find an initializer generator for argument %s (addressing mode: %s).",
             argument.getName(),
             argumentTypeName
        )
    );
}
*/
