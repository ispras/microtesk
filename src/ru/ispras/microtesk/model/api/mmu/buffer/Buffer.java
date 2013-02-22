package ru.ispras.microtesk.model.api.mmu.buffer;

import java.util.HashMap;

import ru.ispras.microtesk.model.api.rawdata.RawData;

public class Buffer<T extends Line, P extends Policy>
{
	public Buffer(int number_of_sets, int number_of_lines)
	{
		for(int i = 0; i < number_of_sets; i++)
		{
			buffer.put(i, new Set<T, P>(number_of_lines));
		}
	}
	
	public int index(Address address) {
		return 0;
	} 
	
	public boolean match(Address address)
	{
		return getSet(index(address)).match(address);
	}
	
	private Set<T, P> getSet(int index) { return buffer.get(index); }
	
	public RawData read(Address address)
	{
		Set<T, P> set = getSet(index(address));
		
		return set.read(address);
	} 			

	public RawData write (Address address, RawData data) 
	{
		Set<T, P> set = getSet(index(address));
		
		return set.write(address,data);
    }
	
	protected HashMap<Integer, Set<T, P>> buffer = new HashMap<Integer, Set<T, P>>();

}
