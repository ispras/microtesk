package ru.ispras.microtesk.model.api.mmu;

import ru.ispras.microtesk.model.api.mmu.buffer.Address;

public abstract class Policy
{
    private int associativity;

    public Policy(int associativity)
    {
        this.associativity = associativity;
    }

    /**
     * Returns associativity (number of lines)
     */

    public int getAssociativity()
    {
        return associativity;
    }

    public abstract void hit(Address address);

    // @returns the address to be replaced
    public abstract int miss(Address address);

    // return null;

    public abstract void accessLine(int index);

    public abstract int choseVictim();
}