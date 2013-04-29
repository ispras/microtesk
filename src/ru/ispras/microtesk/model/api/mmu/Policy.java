package ru.ispras.microtesk.model.api.mmu;

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

    public abstract void accessLine(int index);

    public abstract int choseVictim();
}