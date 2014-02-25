package ru.ispras.microtesk.translator.mmu.ir;

class LengthExp
{
	private int length;

	public LengthExp(int length)
	{
		this.length = length;
	}
	
	public int getNumber()
	{
		return length;
	}	
}

public class LengthExpr
{
public static void main(String[] args)
{
    LengthExp length = new LengthExp(40);
    System.out.println(length.getNumber());
}
}