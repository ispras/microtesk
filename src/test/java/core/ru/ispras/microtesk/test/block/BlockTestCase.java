/*
 * Copyright 2013-2018 ISP RAS (http://www.ispras.ru)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

// TODO: This test is no longer needed because the "block" package
// is obsolete and will be removed. This test needs to be rewritten for
// the "template" package. The code is commented out and is left here
// only as a reminder.

package ru.ispras.microtesk.test.block;

// import static org.junit.Assert.*;

import org.junit.Test;

/*
import ru.ispras.microtesk.test.TestEngine;
import ru.ispras.microtesk.test.block.Block;
import ru.ispras.microtesk.test.block.BlockBuilder;
import ru.ispras.microtesk.test.block.AbstractCall;
import ru.ispras.microtesk.test.block.AbstractCallBuilder;
import ru.ispras.microtesk.test.sequence.ECombinator;
import ru.ispras.microtesk.test.sequence.ECompositor;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.sequence.iterator.IIterator;
*/

/**
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public class BlockTestCase {

  @Test
  public void test() {
        /*
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
        */
  }
}
