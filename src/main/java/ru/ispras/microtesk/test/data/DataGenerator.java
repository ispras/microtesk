/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * DataGenerator.java, May 13, 2013 11:32:21 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.data;

import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IAddressingModeBuilder;
import ru.ispras.microtesk.model.api.instruction.IArgumentBuilder;
import ru.ispras.microtesk.model.api.instruction.IInstruction;
import ru.ispras.microtesk.model.api.instruction.IInstructionCallBuilder;
import ru.ispras.microtesk.model.api.instruction.IInstructionCallBuilderEx;
import ru.ispras.microtesk.model.api.situation.ISituation;
import ru.ispras.microtesk.test.block.AbstractCall;
import ru.ispras.microtesk.test.block.Argument;
import ru.ispras.microtesk.test.block.Situation;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.fortress.solver.Environment;

public final class DataGenerator
{
    private final IModel model;
    private SequenceBuilder<ConcreteCall> sequenceBuilder;

    public DataGenerator(IModel model)
    {
        if (null == model)
            throw new NullPointerException();

        initializeSolverEngine();

        this.model = model;
        this.sequenceBuilder = null;
    }

    private void initializeSolverEngine()
    {
        final String MICROTESK_HOME = System.getenv().get("MICROTESK_HOME");
        
        if (Environment.isUnix())
        {
            Environment.setSolverPath(MICROTESK_HOME + "/tools/z3/unix/z3");
        }
        else if(Environment.isWindows())
        {
            Environment.setSolverPath(MICROTESK_HOME + "/tools/z3/windows/z3.exe");
        }
        else if(Environment.isOSX())
        {
            Environment.setSolverPath(MICROTESK_HOME + "/tools/z3/osx/z3");
        }
        else
        {
            throw new IllegalStateException(String.format(
                "Failed to initialize the solver engine. " + 
                "Unsupported platform: %s", System.getProperty("os.name")));
        }
    }

    public Sequence<ConcreteCall> generate(
        Sequence<AbstractCall> abstractSequence) throws ConfigurationException
    {
        assert null != model;
        assert null != abstractSequence;
        assert null == sequenceBuilder;

        sequenceBuilder = new SequenceBuilder<ConcreteCall>();

        try
        {
            for (AbstractCall abstractCall : abstractSequence)
                processAbstractCall(abstractCall);

            return sequenceBuilder.build();
        }
        finally
        {
            sequenceBuilder = null;
        }
    }

    private void processAbstractCall(
        AbstractCall abstractCall) throws ConfigurationException
    {
        final IInstruction instruction =
            model.getInstruction(abstractCall.getName());

        final IInstructionCallBuilderEx callBuilder = 
            instruction.createCallBuilder();

        for (Argument argument : abstractCall.getArguments().values())
            addArgumentToInstructionCall(argument, callBuilder);

        final ConcreteCall concreteCall = new ConcreteCall(
            abstractCall.getAttributes(),
            callBuilder.getCall()
            );

        sequenceBuilder.addCall(concreteCall);

        final Situation situationInfo = abstractCall.getSituation();
        if (null == situationInfo)
            return;

        System.out.printf("%nTrying to solve situation: %s%n", abstractCall.getSituation().getName());
        System.out.printf("for instruction '%s' (modes:", abstractCall.getName());

        for (Argument argument : abstractCall.getArguments().values())
            System.out.print(" " + argument.getModeName());

        System.out.println(")");

        final ISituation situation =
            instruction.createSituation(situationInfo.getName());

        // This is needed for situations like random that do not have a signature 
        // and generate values for any parameters the client code might request.
        // Other situations may ignore these calls.

        for (Argument argument : abstractCall.getArguments().values())
            situation.setOutput(argument.getName());

        Map<String, Data> output = null;

        try 
        {
            output = situation.solve();
        }
        catch (ConfigurationException e)
        {
            System.out.printf("Warning! Failed to generate test data for the %s situation.\nReason: %s.\n",
                situationInfo.getName(), e.getMessage());

            return;
        }

        for (Map.Entry<String, Data> entry : output.entrySet())
        {
            final Argument argument = abstractCall.getArguments().get(entry.getKey());

            if (null == argument)
            {
                System.out.printf("Argument %s is not defined for instruction %s.%n", entry.getKey(), abstractCall.getName());
                continue;
            }

            insertInitializingCalls(argument, entry.getValue());
        }
    }

    private void insertInitializingCalls(Argument argument, Data value) throws ConfigurationException
    {
        System.out.printf("Initializer: argument: %7s, mode: %10s, value: %s (%s) %n",
            argument.getName(),
            argument.getModeName(),
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
                 argument.getModeName()
            )
        );
    }

    private static void addArgumentToInstructionCall(
        Argument argument,
        IInstructionCallBuilder callBuilder) throws ConfigurationException
    {
        final IArgumentBuilder argumentBuilder = 
            callBuilder.getArgumentBuilder(argument.getName());

        final IAddressingModeBuilder modeBuilder =
            argumentBuilder.getModeBuilder(argument.getModeName());

        for (Argument.ModeArg modeArg : argument.getModeArguments().values())
        {
            if (!modeArg.isRandom)
            {
                modeBuilder.setArgumentValue(modeArg.name, modeArg.value);
            }
            else
            {
                // TODO Generate a random value within the bounds.
                // TODO Bounds are unknown, we use 0 as a default value.
                modeBuilder.setArgumentValue(modeArg.name, 0);
            }
        }
    }
}
