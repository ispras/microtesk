package ru.ispras.microtesk.model.api.mmu.buffer;

public abstract class Policy 
{
	private int number_of_lines;  
	
    public Policy(int number_of_lines)
    {
        this.number_of_lines = number_of_lines;
    }

    public int getSize()  // buffer size
    {
        return number_of_lines;
    }

    public abstract void hit(Address address);

    // @returns the address to be replaced 
    public abstract int miss(Address address);
    //return null;
}