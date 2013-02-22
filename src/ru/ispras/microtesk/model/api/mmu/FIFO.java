package ru.ispras.microtesk.model.api.mmu; 

import java.util.LinkedList;

import ru.ispras.microtesk.model.api.mmu.buffer.Address;
import ru.ispras.microtesk.model.api.mmu.buffer.Policy;

public final class FIFO extends Policy
{
	private LinkedList<Integer> fifo = new LinkedList<Integer>();

    public FIFO(int number_of_lines)
    {
        super(number_of_lines);

        for(int lineIndex = 0; lineIndex < number_of_lines; lineIndex++)
            { fifo.add(lineIndex); }
    }

    @Override
    public void hit(Address address)
    {
        int i = 0;
    	for(int j = 0; j < fifo.size(); j++)
        {
            if(fifo.get(j) == 0)
            {
                fifo.remove(j);
                fifo.add(i);
                return;
            }
        }

        assert false;
    }

    @Override
    public int miss(Address address)
    {
        return fifo.peek();
    }
}