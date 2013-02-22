package ru.ispras.microtesk.model.api.mmu.buffer;

import ru.ispras.microtesk.model.api.rawdata.RawData;
import ru.ispras.microtesk.model.api.rawdata.RawDataStore;

public abstract class Line extends Buffer<Line, Policy>
{
	private RawData data;

	public Line(int size)
	{
		super(1, 1);
		this.data = new RawDataStore(size);
	}

	public abstract boolean match(Address address);
	
	public int index(Address address)
	{
		return 0;
	}	
	
	public RawData getData()
	{
		return data;
	}

	public void setData(RawData data)
	{
		this.data = data;
	}
	
	public RawData read(Address address)
	{
		return getData();
	} 			

	public RawData write (Address address, RawData data)
	{
		if(match(address))
		{
			RawData old = data;
			
			setData(data);
			
			return old;
		}
		
		return null;
    }
}