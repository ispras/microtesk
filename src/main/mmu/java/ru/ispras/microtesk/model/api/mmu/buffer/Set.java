/*
 * Copyright 2012-2013 ISP RAS (http://www.ispras.ru), UniTESK Lab (http://www.unitesk.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.ispras.microtesk.model.api.mmu.buffer;

import java.util.ArrayList;

import ru.ispras.microtesk.model.api.mmu.policy.EPolicy;
import ru.ispras.microtesk.model.api.mmu.policy.Policy;
import ru.ispras.microtesk.model.api.mmu.policy.PolicyFactory;
import ru.ispras.fortress.data.types.bitvector.BitVector;

/**
 * This class represents a set of lines.
 *
 * @author <a href="mailto:leonsia@ispras.ru">Tatiana Sergeeva</a>
 */
public class Set<L extends Line> extends Buffer<L>
{
    /// Array of lines.
    private ArrayList<L> set = new ArrayList<L>();
    /// Data replacement policy.
    private Policy policy;

	/**
	 * Constructs a set with a given associativity.
	 * 
	 * @param associativity the number of sets.
	 */
	public Set(final EPolicy policy, int associativity)
	{
		super(associativity, 1, policy);
		
		this.policy = PolicyFactory.create(policy, associativity);
		
	}

	/**
	 * For each line in buffer with given address it checks if there is
	 * a hit. Returns true if there is hit.
	 * 
	 * @param address the address.
	 * @return true iff there is a buffer hit, otherwise returns false.
	 */
	public boolean match(final Address address) 
	{
		for(L line: set)
		{
			if(line.match(address))
			    { return true; }
		}

		return false;
    }
	
	/**
	 * For each line in set checks if there is a hit, and if so it return 
	 * data with a given address.If there is miss chooses policy to be used
	 * to replace the data.
	 * 
	 * @param address the address.
	 * @return data with a given address.
	 */
	public BitVector read(Address address)
	{
		for(L line: set)
		{
			if(line.match(address))
			{
				policy.accessLine(index(address));
				return line.read(address); 
		    }
		}
		
		int victim = policy.chooseVictim();
		L line = set.get(victim);

		// TODO: to have access to the next buffer to replace line[victim]

		// TODO: replace
		return line.read(address);
	} 			

	/**
     * For each line in set checks if there is a hit, and if so it accesses
     * the line with address index and writes data with a given address. If 
     * there is miss chooses policy to be used to replace the data.
     *
     * @param address the address.
     * @return replaced data with a given address and data.
     */
	public BitVector write (Address address, BitVector data)
	{
		for(L line: set)
		{
			if(line.match(address))
			{
				policy.accessLine(index(address));
				return line.write(address, data); 
		    }
		}
		
		int victim = policy.chooseVictim();
		L line = set.get(victim);
		
		return line.write(address, data);
    }

	/**
     * {@inheritDoc}
	 */
	public int index(final Address address)
	{
	    return 0;
	}

	/**
	 * Returns policy.
	 * 
	 * @return policy.
	 */
	public Policy getPolicy()
	{
		return policy;
	}

	public void setPolicy(final Policy policy)
	{
		this.policy = policy;
	}
}
