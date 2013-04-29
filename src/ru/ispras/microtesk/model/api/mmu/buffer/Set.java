package ru.ispras.microtesk.model.api.mmu.buffer;

import java.util.ArrayList;

import ru.ispras.microtesk.model.api.mmu.Policy;
import ru.ispras.microtesk.model.api.rawdata.RawData;

public class Set<T extends Line, P extends Policy>
{
	private P policy;

	public Set(int numberoflines)
	{
		super();
		
		setPolicy(P(numberoflines));
	}
	
	private P P(int numberoflines) {
		return null;
	}

	public boolean match(Address address) 
	{
		for(T line: set)
		{
			if(line.match(address))
			    { return true; }
		}

		return false;
    }
	
	public RawData read(Address address)
	{
		for(T line: set)
		{
			if(line.match(address))
			{
				policy.hit(address);
				return line.read(address); 
		    }
		
		}
		
		int victim = policy.miss(address);
		T line = set.get(victim);

		// TODO: to have access to the next buffer to replace line[victim]

		// TODO: replace
		return line.read(address);
	} 			
	
	public RawData write (Address address, RawData data)
	{
		for(T line: set)
		{
			if(line.match(address))
			{
				policy.hit(address);
				return line.write(address, data); 
		    }
		}
		
		int victim = policy.miss(address);
		T line = set.get(victim);
		
		// TODO: write throught, etc.
		
		return line.write(address, data);
    }
	
	protected ArrayList<T> set = new ArrayList<T>();

	public int index(Address address)
	{
		return 0;
	}

	public P getPolicy()
	{
		return policy;
	}

	public void setPolicy(P policy)
	{
		this.policy = policy;
	}
}
