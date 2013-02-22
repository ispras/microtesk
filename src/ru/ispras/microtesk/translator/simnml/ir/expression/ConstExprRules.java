/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ConstExprRules.java, Oct 22, 2012 1:48:30 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

class ConstExprRules
{
	private static final Class<?> CAST_TYPE_MAP[][]=
	{
		{ null,           int.class,     long.class,    double.class, boolean.class },
		{ int.class,      int.class,     long.class,    double.class, null          },
		{ long.class,     long.class,    long.class,    double.class, null          },
		{ double.class,   double.class,  double.class,  double.class, null          },
		{ boolean.class,  null,          null,          null,         boolean.class },
	};
	
	public static Class<?> getCastType(Class<?> left, Class<?> right)
	{
		int col=0; // left -> col
		for (int index = 1; index < CAST_TYPE_MAP[0].length; ++index)
		{
			if (CAST_TYPE_MAP[0][index].equals(left))
			{
				col = index;
				break;
			}
		}

		if (0 == col) // left is not found
			return null;

		int row = 0; // right -> row
		for (int index = 1; index < CAST_TYPE_MAP.length; ++index)
		{
			if (CAST_TYPE_MAP[index][0].equals(right))
			{
				row = index;
				break;
			}
		}

		if (0 == row) // right is not found
			return null;

		return CAST_TYPE_MAP[col][row];
	}
			
	private static int group = 0;
	
	private static Class<?>         ALL_TYPES[] = { int.class, long.class, double.class, boolean.class };
	private static Class<?>  BOOLEAN_OP_TYPES[] = { boolean.class };
	private static Class<?>	 NUMERIC_OP_TYPES[] = { int.class, long.class, double.class };
	private static Class<?>  INTEGER_OP_TYPES[] = { int.class, long.class };
	
	private static Class<?>      BIT_OP_TYPES[] = { int.class, long.class, boolean.class };
	private static Class<?>   ROTATE_OP_TYPES[] = { int.class };

	private static final ConstExprOp[] OPERATOR_MAP = 
	{
		new ConstExprOp("||",  "%s || %s", ++group,  boolean.class, BOOLEAN_OP_TYPES), // OR            
		new ConstExprOp("&&",  "%s && %s", ++group,  boolean.class, BOOLEAN_OP_TYPES), // AND 
		new ConstExprOp("|",   "%s | %s",  ++group,  null,          BIT_OP_TYPES),     // VERT_BAR
		new ConstExprOp("^",   "%s ^ %s",  ++group,  null,          BIT_OP_TYPES),     // UP_ARROW  
		new ConstExprOp("&",   "%s & %s",  ++group,  null,          BIT_OP_TYPES),     // UP_AMPER      

		new ConstExprOp("==", "%s == %s",  ++group,  boolean.class, ALL_TYPES),        // EQ   	      
		new ConstExprOp("!=", "%s != %s",    group,  boolean.class, ALL_TYPES),        // NEQ 	      
		new ConstExprOp("<=", "%s <= %s",  ++group,  boolean.class, NUMERIC_OP_TYPES), // LEQ  	      
		new ConstExprOp(">=", "%s >= %s",    group,  boolean.class, NUMERIC_OP_TYPES), // GEQ           
		new ConstExprOp("<",   "%s < %s",    group,  boolean.class, NUMERIC_OP_TYPES), // LEFT_BROCKET 
		new ConstExprOp(">",   "%s > %s",    group,  boolean.class, NUMERIC_OP_TYPES), // RIGHT_BROCKET
		
		new ConstExprOp("<<",  "%s << %s", ++group,  null,          INTEGER_OP_TYPES), // LEFT_SHIFT    
		new ConstExprOp(">>",  "%s >> %s",   group,  null,          INTEGER_OP_TYPES), // RIGHT_SHIFT   
		new ConstExprOp("<<<", "Integer.rotateLeft(%s, %s)",  group, null, ROTATE_OP_TYPES), // ROTATE_LEFT   
		new ConstExprOp(">>>", "Integer.rotateRight(%s, %s)", group, null, ROTATE_OP_TYPES), // ROTATE_RIGHT  
		
		new ConstExprOp("+",   "%s + %s",  ++group,  null,          NUMERIC_OP_TYPES), // PLUS          
		new ConstExprOp("-",   "%s - %s",    group,  null,          NUMERIC_OP_TYPES), // MINUS         
		new ConstExprOp("*",   "%s * %s",  ++group,  null,          NUMERIC_OP_TYPES), // MUL           
		new ConstExprOp("/",   "%s / %s",    group,  null,          NUMERIC_OP_TYPES), // DIV           
		new ConstExprOp("%",   "%s % %s",    group,  null,          NUMERIC_OP_TYPES), // REM           
		new ConstExprOp("**", "%3$sMath.pow(%1$s, %2$s)",  ++group, null, NUMERIC_OP_TYPES), // DOUBLE_STAR   
	
		new ConstExprOp("UNARY_PLUS",  "+%s",  ++group,  null,      NUMERIC_OP_TYPES, true), // Unary PLUS
		new ConstExprOp("UNARY_MINUS", "-%s",    group,  null,      NUMERIC_OP_TYPES, true), // Unary MINUS
		new ConstExprOp("~",           "~%s",    group,  null,      INTEGER_OP_TYPES, true), // TILDE         
		new ConstExprOp("!",           "!%s",    group,  null,      BOOLEAN_OP_TYPES, true), // NOT           		
	};
	
	public static ConstExprOp getOperatorInfo(String name, boolean unary)
	{
		for (ConstExprOp op : OPERATOR_MAP)
		{
			if (name.equals(op.getText()) && op.isUnary() == unary)
				return op;			
		}
		
		return null;
	}
}