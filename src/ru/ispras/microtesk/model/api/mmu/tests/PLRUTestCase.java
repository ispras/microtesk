/*
 * Copyright 2013 ISP RAS (http://www.ispras.ru), UniTESK Lab (http://www.unitesk.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.ispras.microtesk.model.api.mmu.tests;

import org.junit.Assert;
import org.junit.Test;

import ru.ispras.microtesk.model.api.mmu.policy.PLRU;
import ru.ispras.microtesk.model.api.mmu.policy.Policy;

public class PLRUTestCase
{
    private void check(int[] indexes, int expectedVictim)
    {
        final PLRU policy = new PLRU(4);

        for (int i : indexes) 
        {
            policy.accessLine(i);
        }

        final int victim = policy.chooseVictim();

        Assert.assertTrue(
            String.format("Victim: %d, Expected victim: %d", victim, expectedVictim),
            victim == expectedVictim
            );
    }

    @Test
    public void test()
    {
        check(new int[] { 0, 1, 2, 3 }, 0);
        check(new int[] { 0, 1, 3 }, 2);

        System.out.println("End of test!");
    }

    private void checkAllBits(int associativity)
    {
        final Policy policy = new PLRU(associativity);

        for (int i = 0; i < associativity; ++i)
            policy.accessLine(i);

        final int expectedVictim = 0;
        final int victim = policy.chooseVictim();

        Assert.assertTrue(
            String.format("Victim: %d, Expected victim: %d", victim, expectedVictim),
            victim == expectedVictim
            );
    }

    @Test
    public void testAllBits()
    {
        checkAllBits(4);
        checkAllBits(16);
        checkAllBits(31);

        System.out.println("End of testAllBits!");
    }
}
