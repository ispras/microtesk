package ru.ispras.microtesk.model.api.mmu.buffer;

import java.util.ArrayList;

import ru.ispras.microtesk.model.api.mmu.Policy;
import ru.ispras.microtesk.model.api.rawdata.RawData;

public class Set<T extends Line, P extends Policy>
{
	private P policy;

	public Set(int associativity)
	{
		super();
		
		setPolicy(P(associativity));
	}
	
	private P P(int associativity) {
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
				policy.accessLine(index(address));
				return line.read(address); 
		    }
		
		}
		
		int victim = policy.chooseVictim();
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
				policy.accessLine(index(address));
				return line.write(address, data); 
		    }
		}
		
		int victim = policy.chooseVictim();
		T line = set.get(victim);
		
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
