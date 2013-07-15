/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ConstExpr.java, Oct 22, 2012 1:40:19 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.mmu.ir.expression;

/**
 * The ConstExpr class stores an intermediate representation of a constant expression.
 * The representation includes text of the expression converted from ADL to Java and the
 * Java type that corresponds to it.  
 * 
 * @author Andrei Tatarnikov
 */

public final class ConstExpr
{
	private Class<?>    type;
    private Object      value;	
	private String      text;
	private ConstExprOp opInfo;

	/**
	 * Creates a constant expression object.
	 * 
	 * @param type Java-type of the expression.
	 * @param value Calculated value of the expression.
	 * @param text Textual representation of the expression.
	 * @param opInfo Information about the expression operator.
	 */

	public ConstExpr(Class<?> type, Object value, String text, ConstExprOp opInfo)
	{
		this.type     = type;
		this.value    = value;
		this.text     = text;
		this.opInfo   = opInfo;
	}
	
	/**
	 * Creates a constant expression object (for value expressions with no operators).
	 * 
	 * @param type Java-type of the expression.
	 * @param value Calculated value of the expression.
	 * @param text Textual representation of the expression.
	 */

	public ConstExpr(Class<?> type, Object value, String text)
	{
		this(type, value, text, null);
	}

	/**
	 * Returns the Java type of the expression.
	 * 
	 * @return Java type associated with the constant expression.
	 */

	public Class<?> getJavaType()
	{
		return type;
	}
	
	/**
	 * Returns the value of the expression.
	 * 
	 * @return Value object to be cast to the type returned by the getJavaType method.
	 */
	
	public Object getValue()
	{
	    return value;	    
	}

	/**
	 * Returns textual representation of the expression translated into Java-compatible format.
	 * 
	 * @return Text containing a Java expression.  
	 */

	public String getText()
	{
		return text;
	}
	
	/**
	 * Returns information about the operator.
	 * 
	 * @return Information about the operator.
	 */
	
	public ConstExprOp getOperatorInfo()
	{
		return opInfo;
	}

    @Override
    public String toString()
    {
        return "ConstExpr [type=" + type + ", value=" + value + ", text="
                + text + "]";
    }
}
