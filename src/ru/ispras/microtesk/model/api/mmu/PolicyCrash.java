package ru.ispras.microtesk.model.api.mmu;

public abstract class PolicyCrash
    {
/**        private int associativity;

        public PolicyCrash(int associativity)
        {this.associativity = associativity;}

        public int getAssociativity()
        { return associativity;}

        
        /** If hit: return this line  */
   /**    public abstract void accessLine(int index);
       
       return index;
       
       /** If miss: 
        *          search first free line
        *          yes: then change this line
        *          no: change any line    
        */
/**       public abstract int chooseVictim();
      
       for (int index = 0; index < getAssociativity(); ++index)
       {
           if ((bits & (1 << index)) == 0)
           {
               setBit(index);
               return index;
           }
           **/
    }