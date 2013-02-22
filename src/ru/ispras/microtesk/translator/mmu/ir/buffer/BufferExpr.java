package ru.ispras.microtesk.translator.mmu.ir.buffer;

import ru.ispras.microtesk.translator.mmu.ir.index.IndexExpr;
import ru.ispras.microtesk.translator.mmu.ir.match.MatchExpr;
import ru.ispras.microtesk.translator.mmu.ir.parameter.ParameterExpr;

class BufferExp
	{
		private int sets = 4;
		private int lines = 128;
		private String policy = "LRU";

		public BufferExp(int sets, int lines, String policy)
		{
			this.sets = sets;
			this.lines = lines;
			this.policy = "LRU";
		}
		
		public int getNumbersets()
		{
			return sets;
		}
	
		public int getNumberlines()
		{
			return lines;
		}
		
		public String getString()
		{
			return policy;
		}
	}

@SuppressWarnings("hiding")
public class BufferExpr<SetsExpr, LinesExpr, LineExpr, IndexExpr, MatchExpr, EPolicyType> 
{
		public BufferExpr(ParameterExpr a) 
		{}

		public BufferExpr(Object s, Object li, Object l, IndexExpr in, MatchExpr ma, Object po) 
		{}

		public static void main(String[] args)
	    {
	        BufferExp L1 = new BufferExp(4, 128, "LRU");
	 		System.out.println(L1);
	    }
	    
}
