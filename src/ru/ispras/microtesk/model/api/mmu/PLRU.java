package ru.ispras.microtesk.model.api.mmu;

import java.util.LinkedList;

import ru.ispras.microtesk.model.api.mmu.buffer.Address;
import ru.ispras.microtesk.model.api.mmu.buffer.Policy;

public class PLRU extends Policy 
{
	private static int N;
	public int bits;
	
	private LinkedList<Integer> plru = new LinkedList<Integer>();

    public PLRU(int N)
    {
    	super (N);
        for(int i = 0; i < N; i++)
            { plru.add(i); }
    }
    
    @Override
    public void hit(Address address)
    {
    	for(int i = 0; i < N; i++)
        {
    		if (bits != (1 << i)) 
    		{
    			bits = 1;
    		} 
    		
    		if(bits == (1 << N) - 1) 
    		{ 
    			bits = 0;
    		}
        }
        assert false;
    }
    
    @Override
    public int miss(Address address)
    {
    	for(int i = 0; i < N; i++)
    	{
    		if ((bits & (1<<i)) !=0) 
    			{ 
    				return i; 
    			} 
    	}
		return bits;
  	}	
}
