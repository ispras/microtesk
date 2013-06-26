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

import java.util.HashMap;

import ru.ispras.microtesk.model.api.mmu.Policy;
import ru.ispras.microtesk.model.api.rawdata.RawData;

/**
 * This class represents a partially associative buffer.
 *
 * @author <a href="mailto:leonsia@ispras.ru">Tatiana Sergeeva</a>
 */
public abstract class Buffer<L extends Line, P extends Policy>
{
    /// Internal representation of the buffer (Index -> Set).
    protected HashMap<Integer, Set<L, P>> buffer = new HashMap<Integer, Set<L, P>>();

    /**
     * Constructs a buffer with the given number of sets and lines.
     *
     * @param number_of_sets the number of sets in the buffer.
     * @param number_of_lines the number of lines in a set.
     */
	public Buffer(int number_of_sets, int number_of_lines)
	{
		for(int i = 0; i < number_of_sets; i++)
		{
			buffer.put(i, new Set<T, P>(number_of_lines));
		}
	}
	
    /**
     * Returns the index of the buffer set for the given address.
     *
     * @return the index in the buffer set.
     */
	public abstract int index(final Address address);
	
    /**
     * Checks whether there is a buffer hit for the given address.
     *
     * @param address the address.
     * @return true iff there is a buffer hit.
     */
	public boolean match(final Address address)
	{
		return getSet(index(address)).match(address);
	}
	
	private Set<T, P> getSet(int index)
    {
        return buffer.get(index);
    }
	
	public RawData read(Address address)
	{
		Set<T, P> set = getSet(index(address));
		
		return set.read(address);
	} 			

	public RawData write (Address address, RawData data) 
	{
		Set<L, P> set = getSet(index(address));

		return set.write(address,data);
    }
	
}
