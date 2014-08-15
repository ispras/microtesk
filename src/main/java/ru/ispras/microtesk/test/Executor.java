/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Executor.java, Aug 12, 2014 2:23:17 PM Andrei Tatarnikov
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

import java.util.List;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.InstructionCall;
import ru.ispras.microtesk.model.api.state.IModelStateObserver;
import ru.ispras.microtesk.test.data.ConcreteCall;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.Output;

/**
 * The role of the Executor class is to execute (simulate) sequences of 
 * instruction calls (concrete calls). It executes instruction by
 * instruction, perform control transfers by labels (if needed) and 
 * prints information about important events to the simulator log
 * (currently, the console). 
 *  
 * @author Andrei Tatarnikov
 */

public final class Executor
{
    private final IModelStateObserver observer;
    private final boolean logExecution;

    /**
     * Constructs an Executor object.
     * 
     * @param observer Model state observer to evaluate simulation-time outputs. 
     * @param logExecution Specifies whether printing to the simulator log 
     * is enabled.
     * 
     * @throws NullPointerException if the <code>observer</code> parameter
     * is <code>null</code>.
     */

    public Executor(IModelStateObserver observer, boolean logExecution)
    {
        if (null == observer)
            throw new NullPointerException();

        this.observer = observer;
        this.logExecution = logExecution;
    }

    /**
     * Executes the specified sequence of instruction calls (concrete calls)
     * and prints information about important events to the simulator log.
     * 
     * @param sequence Sequence of executable (concrete) instruction calls.
     * 
     * @throws NullPointerException if the parameter is <code>null</code>.
     * @throws IllegalArgumentException if an attribute of a call in the
     * sequence has an invalid format. 
     * @throws ConfigurationException if during the interaction with
     * the microprocessor model an error caused by an invalid format of 
     * the request has occurred (typically, it happens when evaluating an
     * {@link Output} object causes an invalid request to the model
     * state observer).
     */

    public void executeSequence(Sequence<ConcreteCall> sequence)
        throws ConfigurationException
    {
        if (null == sequence)
            throw new NullPointerException();

        // Remember all labels defined by the sequence and its positions.
        final LabelManager labelManager = new LabelManager();
        for (int index = 0; index < sequence.size(); ++index)
        {
            final ConcreteCall instr = sequence.get(index);
            addToLabelManager(labelManager, instr.getAttribute("f_labels"), index + 1);
            addToLabelManager(labelManager, instr.getAttribute("b_labels"), index);
        }

        int currentPos = 0;
        final int endPos = sequence.size();

        while (currentPos < endPos)
        {
            final ConcreteCall instr = sequence.get(currentPos);
            currentPos = executeCall(labelManager, instr, currentPos);
        }
    }

    /**
     * Adds all labels packed into an object to a label manager
     * (is the object is not equal to <code>null</code>).
     * 
     * @param labelManager Label manager the labels are to be added to.
     * @param labels An object that is supposed to hold a list of labels. 
     * @param position Position in a sequence the labels point to. 
     * 
     * @IllegalArgumentException is the <code>labels</code> object is not
     * a list of the objects it contains are not labels.
     */

    private static void addToLabelManager(
        LabelManager labelManager, Object labels, int position)
    {
        if (null == labels)
            return;
        
        final List<?> labelList = toList(labels);
        labelManager.addAllLabels(labelList, position);
    }

    /**
     * Executes the specified instruction call (concrete call)
     * and returns the position of the next instruction call to
     * be executed. Also, it prints the textual representation of
     * the call and debugging outputs linked to the call to
     * the simulator log (if logging is enabled). If the method fails
     * to deal with a control transfer in a proper way it prints
     * a warning message and returns the position of the instruction
     * call that immediately follows the current one.
     * 
     * @param labelManager Label manager to resolve label references. 
     * @param instr Instruction call to be executed. 
     * @param currentPos Position of the current call.
     * @return Position of the next instruction call to be executed.
     * 
     * @throws IllegalArgumentException if an attribute of the call
     * has an invalid format.
     * @throws ConfigurationException if failed to evaluate an Output
     * object associated with the instruction call.
     */

    private int executeCall(
        LabelManager labelManager, ConcreteCall instr, int currentPos)
        throws ConfigurationException
    {
        logOutputs(instr.getAttribute("b_runtime"));

        final InstructionCall call = instr.getExecutable();
        logText(call.getText());
        call.execute();

        logOutputs(instr.getAttribute("f_runtime"));

        // TODO: Support instructions with 2+ labels (needs API)
        final int transferStatus = observer.getControlTransferStatus();

        // If there are no transfers, continue to the next instruction.
        if (0 == transferStatus)
            return currentPos + 1;

        final Object labels = instr.getAttribute("labels");
        if (null == labels)
        {
            logText(MSG_NO_LABEL_LINKED);
            return currentPos + 1;
        }

        final List<?> labelList = toList(labels);
        // List: [label, argument name associated with the label]
        final List<?> labelEntry = toList(labelList.get(0));

        // targetLabel = inst.getAttribute("labels").first.first
        final Label targetLabel = (Label) labelEntry.get(0);
        if (null == targetLabel)
            throw new IllegalArgumentException(
                String.format(MSG_LABEL_IS_NULL, transferStatus));

        final LabelManager.Target target = labelManager.resolve(targetLabel);
        if (null == target)
        {
            logText(String.format(MSG_NO_LABEL_DEFINED, targetLabel.getName()));
            return currentPos + 1;
        }

        logText("Jump to label: " + target.getLabel().getUniqueName());
        return target.getPosition();
    }

    /**
     * Evaluates and prints the collection of {@link Output} objects
     * packed into an Object instance if it is not equal to <code>null</code>.  
     *  
     * @param o List of {@link Output} objects packed into an Object instance.
     * 
     * @throws IllegalArgumentException if the object is not a List or
     * the objects it holds are not Output objects.
     * @throws ConfigurationException if failed to evaluate the information
     * in an Output object due to an incorrect request to the model
     * state observer.
     */

    private void logOutputs(Object o) throws ConfigurationException
    {
        if (null == o)
            return;

        final List<?> list = toList(o);
        for (Object item : list)
        {
            if (!(item instanceof Output))
                throw new IllegalArgumentException(
                    item + " is not an Output object!");

            final Output output = (Output) item;
            logText(output.evaluate(observer));
        }
    }

    /**
     * Prints the text to the simulator log if logging is enabled.
     * 
     * @param text Text to be printed.
     */

    private void logText(String text)
    {
        if (logExecution)
            System.out.println(text);
    }

    /**
     * Casts the specified object to a list (see the {@link List} class).
     * 
     * @param o Object to be cast to a list.
     * @return Object cast to a list.
     * 
     * @throw IllegalArgumentException if the object is not a list 
     */

    private static List<?> toList(Object o)
    {
        if (!(o instanceof List))
            throw new IllegalArgumentException(
                o + " is not a List object.");

        return (List<?>) o;
    }

    private static final String MSG_HAVE_TO_CONTINUE = 
        "Have to continue to the next instruction.";

    private static final String MSG_NO_LABEL_LINKED = 
        "Warning: No label to jump is linked to the current instruction. " +
        MSG_HAVE_TO_CONTINUE;

    private static final String MSG_NO_LABEL_DEFINED =
        "Warning: No label called %s is defined in the current sequence. " +
        MSG_HAVE_TO_CONTINUE;
    
    private static final String MSG_LABEL_IS_NULL =
        "Label reference associated with transfer status %d is null.";
}
