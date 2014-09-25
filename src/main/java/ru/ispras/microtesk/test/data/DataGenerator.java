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

import ru.ispras.fortress.solver.Environment;

import ru.ispras.microtesk.model.api.ICallFactory;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.AddressingModeImm;
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

        final Primitive rootOperation =
            abstractCall.getRootOperation();
        
        if (rootOperation.getKind() != Primitive.Kind.OP)
            throw new IllegalArgumentException(String.format(
                "Wrong kind: %s. %s must be an operation.",
                rootOperation.getKind(), rootOperation.getName()));

        System.out.println("Processing call: " + rootOperation.getName());
        
        final ConcreteCall concreteCall = new ConcreteCall(
            abstractCall, getCall(rootOperation));

        sequenceBuilder.addCall(concreteCall);

        final String situationName = abstractCall.getSituation();
        if (null == situationName)
            return;

        System.out.printf("%nTrying to solve situation: %s%n", situationName);
        System.out.printf("for instruction '%s' (modes:", rootOperation.getName());

        for (Argument argument : rootOperation.getArguments().values())
        {
            System.out.print(" ");
            System.out.print(argument.isImmediate() ?
               AddressingModeImm.PARAM_NAME :
               ((Primitive) argument.getValue()).getName()); 
        }

        System.out.println(")");
        
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
    }

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
   
    private ICallFactory getCallFactory()
    {
        return model.getCallFactory();
    }

    private int getImm(Argument argument)
    {
        if (argument.getKind() != Argument.Kind.IMM)
            throw new IllegalArgumentException();

        return (Integer) argument.getValue();
    }

    private int getImmRandom(Argument argument)
    {
        if (argument.getKind() != Argument.Kind.IMM_RANDOM)
            throw new IllegalArgumentException();

        return ((RandomValue) argument.getValue()).getValue();
    }
    
    private IAddressingMode getMode(Argument argument)
        throws ConfigurationException
    {
        if (argument.getKind() != Argument.Kind.MODE)
            throw new IllegalArgumentException();

        final Primitive abstractMode = 
            (Primitive) argument.getValue();

        final IAddressingModeBuilder builder =
            getCallFactory().newModeInstance(abstractMode.getName());

        for (Argument arg: abstractMode.getArguments().values())
        {
            switch (arg.getKind())
            {
            case IMM:
                builder.setArgumentValue(arg.getName(), getImm(arg));
                break;

            case IMM_RANDOM:
                builder.setArgumentValue(arg.getName(), getImmRandom(arg));
                break;

             default:
                 throw new IllegalArgumentException(
                    "Illegal argument kind: " + argument.getKind());
            }
        }

        return builder.getProduct();
    }
    
    private IOperation getOp(Argument argument)
        throws ConfigurationException
    {
        if (argument.getKind() != Argument.Kind.OP)
            throw new IllegalArgumentException();

        final Primitive abstractOp = 
            (Primitive) argument.getValue();

        return getOp(abstractOp);
    }

    private IOperation getOp(Primitive abstractOp)
        throws ConfigurationException
    {
        final IOperationBuilder builder = getCallFactory().newOpInstance(
            abstractOp.getName(), abstractOp.getContextName());

        for (Argument argument : abstractOp.getArguments().values())
        {
            switch(argument.getKind())
            {
            case IMM:
                builder.setArgument(argument.getName(), getImm(argument));
                break;

            case IMM_RANDOM:
                builder.setArgument(argument.getName(), getImmRandom(argument));
                break;

            case MODE:
                builder.setArgument(argument.getName(), getMode(argument));
                break;

            case OP:
                builder.setArgument(argument.getName(), getOp(argument));
                break;

            default:
                throw new IllegalArgumentException(
                    "Unsupported kind: " + argument.getKind());
            }
        }

        return builder.build();
    }

    private InstructionCall getCall(Primitive rootOperation)
        throws ConfigurationException
    {
        return getCallFactory().newCall(getOp(rootOperation));
    }
}
