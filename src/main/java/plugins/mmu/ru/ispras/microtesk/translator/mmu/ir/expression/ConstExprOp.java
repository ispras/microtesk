/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ConstExprOp.java, Oct 22, 2012 1:47:49 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.mmu.ir.expression;

/**
 * 
 * @author andrewt
 *
 */

class ConstExprOp
{
	private final String       text;
	private final String     format;
	private final int         order;
	private final Class<?>   result;
	private final Class<?>[] params;
	private final boolean     unary;

	/**
	 * 
	 * @param text
	 * @param format
	 * @param order
	 * @param result
	 * @param params
	 */
	
	public ConstExprOp(
		String text,
		String format,
		int order,
		Class<?> result,
		Class<?>[] params
		)
	{
		this(text, format, order, result, params, false);
	}

	public ConstExprOp(
		String text,
		String format,
		int order,
		Class<?> result,
		Class<?>[] params,
		boolean unary
		)
	{
		this.text   = text;
		this.format = format;
		this.order  = order;
		this.result = result;
		this.params = params;
		this.unary  = unary;
	}

	public String getText()
	{
		return text;
	}

	public String getFormat()
	{
		return format;
	}

	public int getOrder()
	{
		return order;
	}

	public Class<?> getResult()
	{
		return result;
	}
	
	public Class<?>[] getParams()
	{
		return params;
	}
	
	public boolean isParamSupported(Class<?> param)
	{
		for (Class<?> p : params)
		{
			if (p.equals(param))
				return true;			
		}
		
		return false;		
	}

	public boolean isUnary()
	{
		return unary;
	}
}