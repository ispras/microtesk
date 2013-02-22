package ru.ispras.microtesk.translator.mmu.ir.address;

import ru.ispras.microtesk.model.api.mmu.buffer.*;

class AddressExp
{
	private int width;

	public AddressExp(int width)
	{
		this.width = width;
	}
	
	public int getNumber()
	{
		return width;
	}
}

public class AddressExpr extends Address 
{
	public AddressExpr(String text, Object object) 
	{}
	
	public static void main(String[] args)
    {
        AddressExp width = new AddressExp(40);
        System.out.println(width.getNumber());
    }

	@Override
	public int width() 
	{
		return 40;
	}
    
}