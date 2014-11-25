package ru.ispras.microtesk.translator.mmu.ir;

import ru.ispras.microtesk.model.api.mmu.Address;
import ru.ispras.microtesk.translator.mmu.ir.expression.ConstExpr;

public class IndexExpr
{
	public int addr(int a)
	{	
		return a;
	}
}

abstract class PA extends Address
{
	public int getBitField(int i, int j) 
	{
		return 0;
	}
 }

class IndexIR
{
    IndexExpr expr;
    AddressExpr addrType;
}

class AddrBitFieldExpr
{
    ConstExpr from;
    ConstExpr to;   
}

class LineAttrAccess
{
    String attrName; // = "tag"
}

// index(addr:PA) = addr<9**8>;

@SuppressWarnings("rawtypes")
class L1 extends BufferExpr
{
    @SuppressWarnings("unchecked")
	public L1(Object s, Object li, Object l, IndexExpr in, MatchExpr ma,
			Object po) {
		super(s, li, l, in, ma, po);
	}

	static int index(PA addr)
    {
        return addr.getBitField(8, 9);
    }

    static boolean match(PA addr, LineExpr line)
    {
        return LineExpr.tag() == addr.getBitField(10, 39);
    }
}
