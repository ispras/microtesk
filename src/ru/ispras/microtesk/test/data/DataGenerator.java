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

import java.util.ArrayList;
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
import ru.ispras.microtesk.test.core.Sequence;
import ru.ispras.solver.api.Environment;

public class DataGenerator
{
    private final IModel model;
    private SequenceBuilder sequenceBuilder;
    private List<IInitializerGenerator> initializerGenerators;

    public DataGenerator(IModel model)
    {
        initializeSolverEngine();
        
        this.model = model;
        this.sequenceBuilder = null;
        this.initializerGenerators = new ArrayList<IInitializerGenerator>();
        
        initializerGenerators.add(new ArmRegInitializerGenerator(model));
        initializerGenerators.add(new ArmRegisterXInitializerGenerator(model));
    }
    
    private void initializeSolverEngine()
    {
        if (Environment.isUnix())
        {
            Environment.setSolverPath("tools/z3/unix/z3");
        }
        else if(Environment.isWindows())
        {
            Environment.setSolverPath("tools/z3/windows/z3.exe");
        }
        else
        {
            // TODO: add initialization code for other platforms.
            assert false : 
                String.format(
                    "Please set up paths for the external engine. Platform: %s",
                    System.getProperty("os.name")
                    );
        }
    }

    public void addInitializerGenerator(IInitializerGenerator ig)
    {
        assert null != ig;
        initializerGenerators.add(ig);
    }

    public Sequence<ConcreteCall> generate(
        Sequence<AbstractCall> abstractSequence) throws ConfigurationException
    {
        assert null != model;
        assert null != abstractSequence;
        assert null == sequenceBuilder;

        sequenceBuilder = new SequenceBuilder();

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
            abstractCall.getName(),
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

        for (Argument argument : abstractCall.getArguments().values())
            situation.setOutput(argument.getName());

        final Map<String, Data> output =
            situation.solve();

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
        System.out.printf("Initializer: argument: %7s, mode: %10s, value: %s%n",
            argument.getName(),
            argument.getModeName(),
            Integer.toHexString(value.getRawData().intValue())
        );

        for(IInitializerGenerator ig : initializerGenerators)
        {
            if (!ig.isCompatible(argument))
                continue;

            final List<ConcreteCall> calls =
                ig.createInitializingCode(argument, value);

            sequenceBuilder.addInitializingCalls(calls);
        }

        /*
        assert false :
            String.format(
                "Failed to find an initializer generator for argument %s (addressing mode: %s).",
                 argument.getName(),
                 argument.getModeName()
            );
        */
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
            modeBuilder.setArgumentValue(modeArg.name, modeArg.value);
    }
}

final class SequenceBuilder
{
    private final List<ConcreteCall> calls;
    private final List<ConcreteCall> initialisingCalls;

    public SequenceBuilder()
    {
        this.calls = new ArrayList<ConcreteCall>();
        this.initialisingCalls = new ArrayList<ConcreteCall>();
    }

    public void addCall(ConcreteCall call)
    {
        assert null != call;
        calls.add(call); 
    }
    
    public void addCalls(List<ConcreteCall> calls)
    {
        assert null != calls;
        calls.addAll(calls); 
    }

    public void addInitializingCall(ConcreteCall call)
    {
        assert null != call;
        initialisingCalls.add(call); 
    }
    
    public void addInitializingCalls(List<ConcreteCall> calls)
    {
        assert null != calls;
        initialisingCalls.addAll(calls); 
    }

    public Sequence<ConcreteCall> build()
    {
        final Sequence<ConcreteCall> result = new Sequence<ConcreteCall>();

        result.addAll(initialisingCalls);
        result.addAll(calls);

        return result;
    }
}
