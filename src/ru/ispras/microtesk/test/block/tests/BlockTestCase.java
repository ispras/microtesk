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

import ru.ispras.microtesk.test.block.Block;
import ru.ispras.microtesk.test.block.BlockBuilder;
import ru.ispras.microtesk.test.block.Call;
import ru.ispras.microtesk.test.block.CallBuilder;
import ru.ispras.microtesk.test.core.ECombinator;
import ru.ispras.microtesk.test.core.ECompositor;
import ru.ispras.microtesk.test.core.Sequence;
import ru.ispras.microtesk.test.core.iterator.IIterator;

public class BlockTestCase
{
    @Test
    public void test()
    {
        final BlockBuilder blockBuilder = new BlockBuilder();

        blockBuilder.setCombinator(ECombinator.RANDOM.name());
        blockBuilder.setCompositor(ECompositor.RANDOM.name());

        for(int i = 0; i < 10; ++i)
        {
            final CallBuilder callBuilder = new CallBuilder();
            callBuilder.setAttribute("Name", String.format("instructon %d", i));
            blockBuilder.addCall(callBuilder.createCall());
        }

        final Block rootBlock = blockBuilder.createBlock();

        int index = 0;

        final IIterator<Sequence<Call>> sequenceIterator = rootBlock.getIterator();
        for (sequenceIterator.init(); sequenceIterator.hasValue(); sequenceIterator.next())
        {
            System.out.println(String.format("Sequence #%d", index));

            final Sequence<Call> sequence = sequenceIterator.value();
            for (Call call : sequence)
                System.out.println(call.getAttribute("Name"));

            ++index;
        }

    }
}
