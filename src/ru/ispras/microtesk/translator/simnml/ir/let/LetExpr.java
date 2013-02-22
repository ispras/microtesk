/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * LetExpr.java, Oct 22, 2012 1:49:26 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.let;

public final class LetExpr
{
	private String   text;
	private Class<?> type;
	private Object   value;

	public LetExpr(String text, Class<?> type, Object value)
	{
		this.text  = text;
		this.type  = type;
		this.value = value;
	}

	public String getText()
	{
		return text;
	}

	public Class<?> getJavaType()
	{
		return type;
	}

	public Object getValue()
	{
	    return value;	    
	}

    @Override
    public String toString()
    {
        return String.format(
           "LetExpr [text=%s, type=%s, value=%s]", text, type, value);
    }
}
