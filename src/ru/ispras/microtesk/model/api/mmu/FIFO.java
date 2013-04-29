package ru.ispras.microtesk.model.api.mmu;

import java.util.LinkedList;

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

    @Override
    public int choseVictim()
    {
        return fifo.peek();
    }
}