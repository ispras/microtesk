package ru.ispras.microtesk.model.api.mmu;

import ru.ispras.microtesk.model.api.mmu.buffer.Address;

public class PLRU extends Policy
{
	public int bits;  

    public PLRU(int associativity)
    {
		super(associativity);

		assert associativity > 0;
		assert associativity <= Integer.SIZE - 1;

		resetBits();
	}
    
    private void resetBits()
    {
    	this.bits = 0;
    }
    
    /**
     * If bits access to i_th cell, then 1 is written to i_th bit. 
     *
     * If all bits equal to 1, then they all are set to zero.  
     * 
     */
    
    private void setBit(int bitIndex)
    {
    	bits |= (1 << bitIndex);    // *

    	if(bits == ((1 << getAssociativity()) - 1)) // **
    	    resetBits();
    }
    
    public void accessLine(int index)
    {
    	setBit(index);
    }
  
    /**
     * If miss happened the index of first nonzero bit is look for.
     */
    
    public int choseVictim()
    {
    	for(int index = 0; index < getAssociativity(); ++index)
    	{
    		if((bits & (1 << index)) == 0)
    		{ 
    			setBit(index);
    			return index;
    	    }
        }

    	assert false : "Incorrect state: all bits are set to 1";
    	return -1;
    }

    @Override
	public void hit(Address address)
    {
		// TODO: Remove this method.
    }

	@Override
	public int miss(Address address)
	{
		// TODO: Remove this method.
		return 0;
	}
}
