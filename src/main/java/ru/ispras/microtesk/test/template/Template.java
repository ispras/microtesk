/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * TemplateBuilder.java, Aug 5, 2014 4:41:17 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.test.template;

import java.util.Deque;
import java.util.LinkedList;

import ru.ispras.microtesk.model.api.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.api.metadata.MetaInstruction;
import ru.ispras.microtesk.model.api.metadata.MetaModel;
import ru.ispras.microtesk.model.api.metadata.MetaOperation;
import ru.ispras.microtesk.model.api.metadata.MetaShortcut;

import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.sequence.iterator.IIterator;

public final class Template
{
    private final MetaModel metaModel;

    private final Deque<BlockBuilder> blockBuilders;
    private CallBuilder callBuilder;

    private IIterator<Sequence<Call>> sequences;

    public Template(MetaModel metaModel)
    {
        _header("Started Processing Template");

        if (null == metaModel)
            throw new NullPointerException();

        this.metaModel = metaModel;

        this.blockBuilders = new LinkedList<BlockBuilder>();
        this.blockBuilders.push(new BlockBuilder());
        this.callBuilder = new CallBuilder();

        this.sequences = null;
    }

    public IIterator<Sequence<Call>> build()
    {
        endBuildingCall();
        
        _header("Ended Processing Template");

        if (null != sequences)
            throw new IllegalStateException("The template is already built.");

        final BlockBuilder rootBuilder = blockBuilders.getLast();
        final Block rootBlock = rootBuilder.build();

        sequences = rootBlock.getIterator();
        return sequences;
    }

    public IIterator<Sequence<Call>> getSequences()
    {
        if (null == sequences)
            build();

        return sequences;
    }

    public BlockId getCurrentBlockId()
    {
        return blockBuilders.peek().getBlockId();
    }

    public BlockBuilder beginBlock()
    {
        endBuildingCall();
        
        final BlockBuilder parent = blockBuilders.peek();
        final BlockBuilder current = new BlockBuilder(parent);

        _trace("Begin block: " + current.getBlockId());

        blockBuilders.push(current);
        return current;
    }

    public void endBlock()
    {
        endBuildingCall();

        _trace("End block: " + getCurrentBlockId());

        final BlockBuilder builder = blockBuilders.pop();
        final Block block = builder.build();

        blockBuilders.peek().addBlock(block);
    }

    public Label addLabel(String name)
    {
        final Label label = new Label(name, getCurrentBlockId());
        _trace("Label: " + label.toString()); 

        callBuilder.addLabel(label);
        return label;
    }

    public void addLabelReference(
        String labelName, String argName, int argValue)
    {
        final LabelReference labelRef = new LabelReference(
            labelName, getCurrentBlockId(), argName, argValue);

        _trace(labelRef.toString());

        callBuilder.addLabelReference(labelRef);
    }

    public void addOutput(Output output)
    {
        _trace(output.toString());
        callBuilder.addOutput(output);
    }
    
    public void setSituation(String name)
    {
        callBuilder.setSituation(name);
    }

    public void setRootOperation(Primitive rootOperation)
    {
        callBuilder.setRootOperation(rootOperation);
    }

    public void endBuildingCall()
    {
        final Call call = callBuilder.build();
        blockBuilders.peek().addCall(call);

        _trace(String.format(
            "Ended building a call (empty = %b, executable = %b)",
             call.isEmpty(), call.isExecutable()));

        this.callBuilder = new CallBuilder();
    }

    public PrimitiveBuilder newInstructionBuilder(String name)
    {
        if (null == name)
            throw new NullPointerException();

        _trace("Instruction: " + name);

        final MetaInstruction metaData = metaModel.getInstruction(name);
        if (null == metaData)
            throw new IllegalArgumentException("No such instruction: " + name);

        return new PrimitiveBuilder(metaData);
    }

    public PrimitiveBuilder newOperationBuilder(String name, String contextName)
    {
        if (null == name)
            throw new NullPointerException();

        _trace(String.format("Operation: %s (context: %s)", name, contextName));

        final MetaOperation metaData = metaModel.getOperation(name);
        if (null == metaData)
            throw new IllegalArgumentException("No such operation: " + name);

        if (null != contextName)
        {
            final MetaShortcut metaShortcut =
                metaData.getShortcut(contextName);

            if (null != metaShortcut)
                new PrimitiveBuilder(metaShortcut.getOperation());
        }

        return new PrimitiveBuilder(metaData);
    }

    public PrimitiveBuilder newAddressingModeBuilder(String name)
    {
        if (null == name)
            throw new NullPointerException();

        _trace("Addressing mode: " + name);

        final MetaAddressingMode metaData = metaModel.getAddressingMode(name);
        if (null == metaData)
            throw new IllegalArgumentException(
                "No such addressing mode: " + name);

        return new PrimitiveBuilder(metaData);
    }

    public RandomValueBuilder newRandom(int from, int to)
    {
        return new RandomValueBuilder(from, to);
    }

    public OutputBuilder newOutput(boolean isRuntime, String format)
    {
        return new OutputBuilder(isRuntime, format);
    }

    private static void _trace(String s)
    {
        System.out.println(s);
    }

    private static void _header(String text)
    {
        final int LINE_WIDTH = 80;

        final int  prefixWidth = (LINE_WIDTH - text.length()) / 2;
        final int postfixWidth = LINE_WIDTH - prefixWidth - text.length();

        final StringBuilder sb = new StringBuilder();

        sb.append("\r\n");
        for(int i = 0; i < prefixWidth - 1; ++i) sb.append('-');
        sb.append(' ');

        sb.append(text);

        sb.append(' ');
        for(int i = 0; i < postfixWidth - 1; ++i) sb.append('-');
        sb.append("\r\n");

        _trace(sb.toString());
    }
}
