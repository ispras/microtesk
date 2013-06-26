package ru.ispras.microtesk.model.api.mmu;

public class PolicyFactory
{
    public static Policy create(final EPolicy policy, int associativity)
    {
        switch(policy)
        {
        case FIFO: return new FIFO(associativity);
        default: throw new NullPointerException();
        }
    }
}
