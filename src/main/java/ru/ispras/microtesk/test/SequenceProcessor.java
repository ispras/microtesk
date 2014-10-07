/*
 * Copyright (c) 2013 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * SequenceProcessor.java, May 13, 2013 11:32:21 AM Andrei Tatarnikov
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

import java.util.Map;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.microtesk.model.api.ICallFactory;
import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.instruction.IAddressingModeBuilder;
import ru.ispras.microtesk.model.api.instruction.IOperation;
import ru.ispras.microtesk.model.api.instruction.IOperationBuilder;
import ru.ispras.microtesk.model.api.instruction.InstructionCall;
import ru.ispras.microtesk.test.data.TestBase;
import ru.ispras.microtesk.test.data.TestBaseQueryCreator;
import ru.ispras.microtesk.test.preparator.Preparator;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.sequence.SequenceBuilder;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.RandomValue;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.test.template.UnknownValue;
import ru.ispras.microtesk.utils.FortressUtils;
import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestBaseQueryResult;
import ru.ispras.testbase.TestData;
import ru.ispras.testbase.TestDataProvider;

/**
 * The SequenceProcessor class processes an abstract instruction call
 * sequence that may use hold symbolic values to build a concrete
 * instruction call sequence that uses only concrete values and
 * can be simulated and used to generate source code in assembly language.      
 * 
 * @author Andrei Tatarnikov
 */

final class SequenceProcessor
{
    private final IModel model; 
    private final ICallFactory callFactory;
    private final TestBase testBase;

    private SequenceBuilder<ConcreteCall> sequenceBuilder;

    SequenceProcessor(IModel model) 
    {
        checkNotNull(model);

        this.model = model;
        this.callFactory = model.getCallFactory();
        this.testBase = new TestBase();
        this.sequenceBuilder = null;
    }

    public Sequence<ConcreteCall> process(
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

        // Only executable calls are worth printing.
        System.out.printf(
            "%nProcessing %s...%n", abstractCall.getText());

        final InstructionCall modelCall = makeModelCall(abstractCall);
        sequenceBuilder.add(new ConcreteCall(abstractCall, modelCall));
    }

    private void generateData(Primitive primitive)
    {
        if (null == primitive)
            throw new NullPointerException();
        
        if (!primitive.hasSituation())
            throw new IllegalArgumentException(
                "No situations are linked to " + primitive);
        
        final Situation situation = primitive.getSituation();

        System.out.printf("Processing situation %s for %s...%n",
                situation, primitive.getSignature());

        final TestBaseQueryCreator queryCreator =
            new TestBaseQueryCreator(model.getName(), situation, primitive);

        final TestBaseQuery query = queryCreator.getQuery(); 
        System.out.println("Query to TestBase: " + query);

        final Map<String, UnknownValue> unknownValues = queryCreator.getUnknownValues();
        System.out.println("Unknown values: " + unknownValues.keySet());

        final Map<String, Primitive> modes = queryCreator.getModes();
        System.out.println("Modes used as arguments: " + modes);

        final TestBaseQueryResult queryResult = testBase.executeQuery(query);
        if (TestBaseQueryResult.Status.OK != queryResult.getStatus())
        {
            printErrors(queryResult);
            return;
        }

        final TestDataProvider dataProvider = queryResult.getDataProvider(); 
        if (!dataProvider.hasNext())
        {
            System.out.println("No data was generated for the query.");
            return;
        }

        final TestData testData = dataProvider.next();
        System.out.println(testData);

        // Set unknown immediate values
        for (Map.Entry<String, UnknownValue> e : unknownValues.entrySet())
        {
            final String name = e.getKey();
            final UnknownValue target = e.getValue();

            final Node value = testData.getBindings().get(name);
            final int intValue = FortressUtils.extractInt(value);

            target.setValue(intValue);
        }

        // Set model state using preparators that create initializing
        // sequences based on addressing modes.
        for (Map.Entry<String, Node> e : testData.getBindings().entrySet())
        {
            final String name = e.getKey();
            final Primitive targetMode = modes.get(name);

            if (null == targetMode)
                continue;

            final BitVector value =
                FortressUtils.extractBitVector(e.getValue());

            System.out.println("Value: " + value);

            final Preparator preparator = getPreparator(targetMode);
            if (null == preparator)
            {
                System.out.printf(
                    "No suitable preparator is found for argument %s (%s).%n",
                    name, targetMode.getSignature());
                continue;
            }

            preparator.makeInitializer(targetMode, value);
        }
    }
    
    private void printErrors(TestBaseQueryResult queryResult)
    {
        final StringBuilder sb = new StringBuilder(String.format(
            "Failed to execute the query. Status: %s.", queryResult.getStatus()));

        if (queryResult.hasErrors())
        {
            sb.append(" Errors: ");
            for (String error : queryResult.getErrors())
            {
                sb.append("\r\n  ");
                sb.append(error);
            }
        }

        System.out.println(sb);
    }

    private Preparator getPreparator(Primitive targetMode)
    {
        return null;
    }

    private InstructionCall makeModelCall(
        Call abstractCall) throws ConfigurationException
    {
        final Primitive rootOp = abstractCall.getRootOperation();
        checkRootOp(rootOp);

        final IOperation modelOp = makeOp(rootOp);
        return callFactory.newCall(modelOp);
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
    
    private int makeImmUnknown(Argument argument)
    {
        checkArgKind(argument, Argument.Kind.IMM_UNKNOWN);

        return ((UnknownValue) argument.getValue()).getValue();
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
                builder.setArgumentValue(argName, makeImmUnknown(arg));
                break;

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

        if (abstractOp.hasSituation())
            generateData(abstractOp);

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
                builder.setArgument(argName, makeImmUnknown(arg));
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
