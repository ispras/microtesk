package ru.ispras.microtesk.model.api.mmu;


public class PolicyTestCase
{}
  
/**    private void check(int[] indexes, int expectedVictim)
    {
        final Policy policy = new Policy();

        for (int i : indexes) 
        {
            policy.accessLine(i);
        }

        final int victim = policy.chooseVictim();

    }
    
    /**
     * * If hit: return this line
     * 
     */
    //public void accessLine(int index);
    
    
    
        /** If miss: 
        *          search first free line
        *          yes: then change this line
        *          no: change any line    
        */
 
    /**public int chooseVictim()
    {
        for (int index = 0; index < getAssociativity(); ++index)
        {
            if ((bits & (1 << index)) == 0)
            {
                setBit(index);
                return index;
            }
    }
    
    */