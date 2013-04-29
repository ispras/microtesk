package ru.ispras.microtesk.model.api.mmu.tests;

import org.junit.Assert;
import org.junit.Test;

import ru.ispras.microtesk.model.api.mmu.PLRU;
import ru.ispras.microtesk.model.api.mmu.Policy;

public class PLRUTestCase
{
    private void check(int[] indexes, int expectedVictim)
    {
        final PLRU policy = new PLRU(4);

        for (int i : indexes) 
        {
            policy.accessLine(i);
        }

        final int victim = policy.choseVictim();

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
        final int victim = policy.choseVictim();

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
