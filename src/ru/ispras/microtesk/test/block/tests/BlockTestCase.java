/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * BlockTestCase.java, May 7, 2013 12:09:57 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.block.tests;

// import static org.junit.Assert.*;

import org.junit.Test;

import ru.ispras.microtesk.test.TestEngine;
import ru.ispras.microtesk.test.block.Block;
import ru.ispras.microtesk.test.block.BlockBuilder;
import ru.ispras.microtesk.test.block.AbstractCall;
import ru.ispras.microtesk.test.block.AbstractCallBuilder;
import ru.ispras.microtesk.test.core.ECombinator;
import ru.ispras.microtesk.test.core.ECompositor;
import ru.ispras.microtesk.test.core.Sequence;
import ru.ispras.microtesk.test.core.iterator.IIterator;

public class BlockTestCase
{
    @Test
    public void test()
    {
        final TestEngine testEngine = TestEngine.getInstance(null);
        final BlockBuilder blockBuilder = testEngine.getBlockBuilders().newBlockBuilder();

        blockBuilder.setCombinator(ECombinator.RANDOM.name());
        blockBuilder.setCompositor(ECompositor.RANDOM.name());

        for(int i = 0; i < 15; ++i)
        {
            final AbstractCallBuilder callBuilder =
                testEngine.getBlockBuilders().newAbstractCallBuilder("Add");

            callBuilder.setAttribute("Name", String.format("instructon %d", i));
            blockBuilder.addCall(callBuilder.build());
        }

        final Block rootBlock = blockBuilder.build();

        int index = 0;

        final IIterator<Sequence<AbstractCall>> sequenceIterator = rootBlock.getIterator();
        for (sequenceIterator.init(); sequenceIterator.hasValue(); sequenceIterator.next())
        {
            System.out.println(String.format("Sequence #%d", index));

            final Sequence<AbstractCall> sequence = sequenceIterator.value();
            for (AbstractCall call : sequence)
                System.out.println(call.getAttribute("Name"));

            ++index;
        }
    }
}
