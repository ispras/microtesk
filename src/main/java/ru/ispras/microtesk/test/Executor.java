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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.InstructionCall;
import ru.ispras.microtesk.model.api.state.IModelStateObserver;
import ru.ispras.microtesk.test.data.ConcreteCall;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.template.BlockId;
import ru.ispras.microtesk.test.template.BlockId.Distance;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.Output;

public final class Executor
{
    private final IModelStateObserver observer;
    private final boolean logExecution;

    public Executor(IModelStateObserver observer, boolean logExecution)
    {
        if (null == observer)
            throw new NullPointerException();

        this.observer = observer;
        this.logExecution = logExecution;
    }

    public void executeSequence(Sequence<ConcreteCall> sequence) throws ConfigurationException
    {
        if (null == sequence)
            throw new NullPointerException();

        // Remember all labels defined by the sequence and its positions.
        final LabelManager labelManager = new LabelManager();
        for (int index = 0; index < sequence.size(); ++index)
        {
            final ConcreteCall instr = sequence.get(index);
            
            final List<?> f_labels = toList(instr.getAttribute("f_labels"));
            labelManager.addAllLabels(f_labels, index);

            final List<?> b_labels = toList(instr.getAttribute("b_labels"));
            labelManager.addAllLabels(b_labels, index + 1);
        }
        
        int currentPos = 0;
        final int endPos = sequence.size();
        while (currentPos < endPos)
        {
            final ConcreteCall instr = sequence.get(currentPos);

            logOutputs(instr.getAttribute("b_runtime"));

            final InstructionCall call = instr.getExecutable();
            logText(call.getText());
            call.execute();

            logOutputs(instr.getAttribute("f_runtime"));

            // TODO: Support instructions with 2+ labels (needs API)
            final int ctStatus = observer.getControlTransferStatus();
            if (ctStatus > 0)
            {
                final List<?> instrLabels = 
                    toList(instr.getAttribute("labels"));
                
                // TODO: What to do if there are no labels?
                if (instrLabels.isEmpty())
                    throw new IllegalStateException();

                // First - label, Second - instruction argument name 
                final List<?> instrLabelEntry = toList(instrLabels.get(0));
                
                // target = inst.getAttribute("labels").first.first
                final Label targetLabel = (Label) instrLabelEntry.get(0);  
                if (null == targetLabel)
                {
                    System.out.println("Jump to nil label, transfer status: " + ctStatus);
                    currentPos++; // continue to the next instruction, no other idea
                }
                else
                {
                    final LabelManager.Target target = labelManager.resolve(targetLabel);
                    if (null == target)
                        throw new IllegalStateException("No label called" + targetLabel.getName() + "is defined");
                    
                    currentPos = target.getJumpPos();
                    logLabelJump(target.getLabel());
                }
            }
            else // If there are no transfers, continue to the next instruction
            {
                currentPos++;
            }
        }
    }

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
    
    private void logLabelJump(Label target)
    {
        logText("Jump (internal) to label: " + target.getUniqueName());
    }

    private void logText(String text)
    {
        if (logExecution)
            System.out.println(text);
    }

    private static List<?> toList(Object o)
    {
        if (!(o instanceof List))
            throw new IllegalArgumentException(
                o + " is not a List object.");

        return (List<?>) o;
    }
}

/**
 * The job of the LabelManager class is to manage jumps to labels that
 * have the same names, but are defined in different blocks. It stores
 * all labels defined by the sequence and their relative positions grouped
 * by name. When it is required to perform a jump to a label with a specific
 * name, it chooses the most suitable label depending on the block from 
 * which the jump is to be performed. Here are the rules according to which
 * the choice is made: 
 * 
 *  <p>1. If there is only one such label (no other choice), choose it.
 * 
 *  <p>2. Choose a label defined in the current block,
 *        it there is such a label defined in the current block.
 * 
 *  <p>3. Choose a label defined in the closest child,
 *        it there are such labels defined in child blocks.
 * 
 *  <p>4. Choose a label defined in the closest parent,
 *        it there are such labels defined in parent blocks.
 *  
 *  <p>5. Choose a label defined in the closest sibling.
 * 
 * @author Andrei Tatarnikov
 */

final class LabelManager
{
    public static final class Target
    {
        private final Label label;
        private final int jumpPos;

        public Target(Label label, int jumpPos)
        {
            if (null == label)
                throw new NullPointerException();

            if (jumpPos < 0)
                throw new IllegalArgumentException();

            this.label = label;
            this.jumpPos = jumpPos;
        }

        public Label getLabel()
        {
            return label;
        }

        public int getJumpPos()
        {
            return jumpPos;
        }
    }
    
    private static final class TargetDistance implements Comparable<TargetDistance>
    {
        private final Target target;
        private final BlockId.Distance distance;
        
        private TargetDistance(Target target, Distance distance)
        {
            this.target = target;
            this.distance = distance;
        }

        @Override
        public int compareTo(TargetDistance o)
        {
            // TODO Auto-generated method stub
            return 0;
        } 
    }

    private final Map<String, List<Target>> table;

    public LabelManager()
    {
        this.table = new HashMap<String, List<Target>>();
    }

    public void addLabel(Label label, int jumpPos)
    {
        if (null == label)
            throw new NullPointerException();

        final Target target = new Target(label, jumpPos);

        final List<Target> targets;
        if (table.containsKey(label.getName()))
        {
            targets = table.get(label.getName());
        }
        else
        {
            targets = new ArrayList<Target>();
            table.put(label.getName(), targets);
        }

        targets.add(target);
    }

    public void addAllLabels(Collection<?> labels, int jumpPos)
    {
        if (null == labels)
            throw new NullPointerException();

        for (Object item : labels)
        {
            if (!(item instanceof Label))
                throw new IllegalArgumentException(
                    item + " is not a Label object!");

            addLabel((Label) item, jumpPos);
        }
    }

    public Target resolve(Label label)
    {
        // Find a label defined in the current block
        // Find a label defined in the closest child
        // Find a label defined in the closest parent
        // Find a label defined in the closest sibling
        
        if (null == label)
            throw new NullPointerException();

        if (!table.containsKey(label.getName()))
            return null;

        final List<Target> targets = 
            table.get(label.getName());

        // If there is only one target, there is no other choice.
        if (1 == targets.size())
            return targets.get(0);

        final List<TargetDistance> distances = 
             new ArrayList<TargetDistance>(targets.size());

        for (int index = 0; index < targets.size(); ++index)
        {
            final Target target = targets.get(index);
            final Label targetLabel = target.getLabel();

            final BlockId.Distance distance = 
                label.getBlockId().getDistance(targetLabel.getBlockId());

            distances.add(new TargetDistance(target, distance));
        }
        
        Collections.sort(distances);
        
        return distances.get(0).target;
    }
}
