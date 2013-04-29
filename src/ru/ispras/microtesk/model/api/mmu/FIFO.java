package ru.ispras.microtesk.model.api.mmu;

import java.util.LinkedList;

import ru.ispras.microtesk.model.api.mmu.buffer.Address;

public class FIFO extends Policy
{
    private LinkedList<Integer> fifo = new LinkedList<Integer>();

    public FIFO(int number_of_lines)
    {
        super(number_of_lines);

        for (int lineIndex = 0; lineIndex < number_of_lines; lineIndex++)
        {
            fifo.add(lineIndex);
        }
    }

    public void accessLine(int index)
    {
        int i = 0;
        for (int j = 0; j < fifo.size(); j++)
        {
            if (fifo.get(j) == 0)
            {
                fifo.remove(j);
                fifo.add(i);
                return;
            }
        }

        assert false;
    }

    public int choseVictim()
    {
        return fifo.peek();
    }

    @Override
    public void hit(Address address)
    {
    }

    @Override
    public int miss(Address address)
    {
        return 0;
    }
}