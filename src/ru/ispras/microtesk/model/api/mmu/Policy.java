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
    //return null;

	public void accessLine(int i) {
		// TODO Auto-generated method stub
	}

	public int choseVictim() {
		// TODO Auto-generated method stub
		return 0;
	}
}