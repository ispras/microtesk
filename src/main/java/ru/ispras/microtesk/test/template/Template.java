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

import ru.ispras.microtesk.model.api.metadata.MetaModel;

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
        _header("Begin Template");

        if (null == metaModel)
            throw new NullPointerException();

        this.metaModel = metaModel;

        this.blockBuilders = new LinkedList<BlockBuilder>();
        this.blockBuilders.push(new BlockBuilder());

        this.sequences = null;
    }

    public IIterator<Sequence<Call>> build()
    {
        _header("End Template");

        if (null != sequences)
            throw new IllegalStateException("The template is already built.");

        final BlockBuilder rootBuilder = blockBuilders.getLast();
        final Block rootBlock = rootBuilder.build();

        sequences = rootBlock.getIterator();
        return sequences;
    }

    public IIterator<Sequence<Call>> getSequences()
    {
        return sequences;
    }

    public BlockBuilder beginBlock()
    {
        _trace("Begin block");

        final BlockBuilder parent = blockBuilders.peek();
        final BlockBuilder current = new BlockBuilder(parent);

        blockBuilders.push(current);
        return current;
    }

    public void endBlock()
    {
        _trace("End block");

        final BlockBuilder builder = blockBuilders.pop();
        final Block block = builder.build();

        blockBuilders.peek().addBlock(block);
    }

    public void addLabel(String name)
    {
        final String uniqueName =
            name + blockBuilders.getFirst().getBlockId();

        _trace("Label: " + uniqueName); 
        callBuilder.addItemToAttribute("b_labels", uniqueName);
    }

    public void addOutput(Output output)
    {
        _trace(output.toString());

        /*
        if (output.isRuntime())
            callBuilder.addItemToAttribute("b_runtime", output);
        else
            callBuilder.addItemToAttribute("b_output", output);
        */
    }

    public CallBuilder getCurrentCallBuilder()
    {
        return callBuilder;
    }

    public void endBuildingCall()
    {
        _trace("Ended building a call");

        //final AbstractCall call = callBuilder.build();
        //blockBuilders.getFirst().addCall(call);

        this.callBuilder = new CallBuilder();
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
