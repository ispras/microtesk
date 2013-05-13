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

import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IInstruction;
import ru.ispras.microtesk.model.api.instruction.IInstructionCallBuilderEx;
import ru.ispras.microtesk.test.block.AbstractCall;
import ru.ispras.microtesk.test.block.Argument;
import ru.ispras.microtesk.test.core.Sequence;

public class DataGenerator
{
    private final IModel model;

    public DataGenerator(IModel model)
    {
        this.model = model;
    }

    public Sequence<ConcreteCall> generate(
        Sequence<AbstractCall> abstractSequence) throws ConfigurationException
    {
        final Sequence<ConcreteCall> result = new Sequence<ConcreteCall>();

        for (AbstractCall abstractCall : abstractSequence)
            result.add(createConcreteCall(abstractCall));

        return result;
    }

    private ConcreteCall createConcreteCall(
        AbstractCall abstractCall) throws ConfigurationException
    {
        final IInstruction instruction =
            model.getInstruction(abstractCall.getName());

        final IInstructionCallBuilderEx callBuilder = 
            instruction.createCallBuilder();

        for (Argument argument : abstractCall.getArguments().values())
            argument.addToInstructionCall(callBuilder);

        return new ConcreteCall(
            abstractCall.getName(),
            abstractCall.getAttributes(),
            callBuilder.getCall()
            );
    }
}
