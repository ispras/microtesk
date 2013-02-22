/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ConstExprFactory.java, Oct 22, 2012 1:40:39 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

import java.util.Map;

import org.antlr.runtime.RecognitionException;
import ru.ispras.microtesk.translator.antlrex.IErrorReporter;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.simnml.errors.UndefinedConstant;
import ru.ispras.microtesk.translator.simnml.errors.UnsupportedConstOperation;
import ru.ispras.microtesk.translator.simnml.errors.ValueParsingFailure;
import ru.ispras.microtesk.translator.simnml.errors.ValueTypeMismatch;
import ru.ispras.microtesk.translator.simnml.ir.let.LetExpr;

/**
 * The ConstExprFactory class provides logic for semantic analysis of 
 * constant expression (user in let, reg, mem, var constructions).
 * Each of the factory methods creates a Java-compatible representation
 * of a constant Sim-nML expression.
 * 
 * @author Andrei Tatarnikov
 */

public class ConstExprFactory
{	
	private static final int HEX_RADIX = 16;
	private static final int DEC_RADIX = 10;

	private static final String HEX_PREFIX        = "0x";
	private static final String INTEGER_TYPE_NAME = "integer";
	private static final String REAL_TYPE_NAME    = "real";
	private static final String BRACKETS_FORMAT   = "(%s)";
	
	private final Map<String, LetExpr> lets;
	private final IErrorReporter       reporter; 

	/**
	 * Creates a ConstExprFactory object.
	 * 
	 * @param symbols Table to symbols.
	 * @param reporter Object for reposing problems (raises exceptions).
	 */

	public ConstExprFactory(Map<String, LetExpr> lets, IErrorReporter reporter)
	{
		this.lets     = lets;		
		this.reporter = reporter;
	}

	/**
	 * Creates an expression that refers to a constant string. In the current
	 * implementation, such expression do not support any operations.
	 *
	 * @param text Constant string.
	 * @return A constant expression object (a type and text representation pair).
	 */

	public ConstExpr createStringConst(String text)
	{
		return new ConstExpr(String.class, text, text);		
	}
	
	/**
	 * Creates a reference expression which refers to another constant
	 * (let definition) declared earlier. 
	 * 
	 * @param name The name of a constant (let definition) the label refers to.
	 * @return A constant expression object.
	 * @throws RecognitionException A semantic exception.
	 */

	public ConstExpr createReference(String name) throws RecognitionException
	{
		if (!lets.containsKey(name))
			reporter.raiseError(new UndefinedConstant(name));

		final LetExpr letExpr = lets.get(name); 
		return new ConstExpr(letExpr.getJavaType(), letExpr.getValue(), name);
	}
	
	/**
	 * Creates an expression that consists of an integer constant.
	 * 
	 * @param text Textual representation of an integer literal.
	 * @param radix Radix to be used for conversion.
	 * @return A constant expression object.
	 * @throws RecognitionException A semantic exception.
	 */

	public ConstExpr createIntegerConst(String text, int radix) throws RecognitionException
	{
		final boolean hex = (HEX_RADIX == radix);

		try 
        {			
			final Integer v = Integer.parseInt(text, radix);			
            return new ConstExpr(
            	int.class,
            	v,
            	hex ? HEX_PREFIX + Integer.toString(v, HEX_RADIX) : Integer.toString(v, DEC_RADIX)
            	);           
        }
        catch (NumberFormatException e) {}

		try 
        {
            final Long v = Long.parseLong(text, radix);
            return new ConstExpr(
            	long.class,
            	v,
            	hex ? HEX_PREFIX + Long.toString(v, HEX_RADIX) : Long.toString(v, DEC_RADIX)
            	);
        }
        catch (NumberFormatException e) {}

		reporter.raiseError(new ValueParsingFailure(text, INTEGER_TYPE_NAME));
		return null;
	}

	/**
	 * Creates an expression that consists of a floating point constant.
	 * 
	 * @param text Textual representation of a floating point literal.
	 * @return A constant expression object.
	 * @throws RecognitionException A semantic exception.
	 */

	public ConstExpr createRealConst(String text) throws RecognitionException
	{
		try 
        {
            final Double v = Double.parseDouble(text);
            return new ConstExpr(double.class, v, Double.toString(v));
        }
        catch (NumberFormatException e) {}

		reporter.raiseError(new ValueParsingFailure(text, REAL_TYPE_NAME));
		return null;
	}
	
	/**
	 * Creates an expression that consists of a binary operator and its 
	 * two operands. There are two preconditions:
	 * - Both parameters MUST be supported.
	 * - Both parameters MUST be equal or compatible.
	 *
	 * @param op Operator string.
	 * @param left Left operand (subexpression).
	 * @param right Right operand (subexpression).
	 * @return A constant expression object.
	 * @throws RecognitionException A semantic exception.
	 */

	public ConstExpr createBinaryExpr(Where where, String opText, ConstExpr left, ConstExpr right) throws RecognitionException
	{
	    final String ERROR_FORMAT =
	        "The binary operator '%s' is absent from the supported operator map.";
	    
		assert null != left;
		assert null != right;

		final ConstExprOp opInfo = ConstExprRules.getOperatorInfo(opText, false);
		assert (null != opInfo) : String.format(ERROR_FORMAT, opText);

		if (!opInfo.isParamSupported(left.getJavaType()))
			reporter.raiseError(where, new UnsupportedConstOperation(opText, left.getText(), left.getJavaType(), right.getJavaType()));

		if (!opInfo.isParamSupported(right.getJavaType()))
			reporter.raiseError(where, new UnsupportedConstOperation(opText, right.getText(), left.getJavaType(), right.getJavaType()));

		final Class<?> castType = ConstExprRules.getCastType(left.getJavaType(), right.getJavaType());
		if (null == castType)
			reporter.raiseError(where, new ValueTypeMismatch(left.getJavaType(), right.getJavaType()));

		final IConstBinaryOperator operator =
		    ConstExprOperators.getBinaryOperator(opText, left.getJavaType(), right.getJavaType());

		if (null == operator)
		    reporter.raiseError(where, new UnsupportedConstOperation(opText, right.getText(), left.getJavaType(), right.getJavaType()));

		final Object value = operator.calculate(left.getValue(), right.getValue());
		final Class<?> resultType = ConstExprOperators.getPrimitive(value.getClass());

		String leftText = left.getText();
		if (left.getOperatorInfo() != null && left.getOperatorInfo().getOrder() < opInfo.getOrder())
			leftText = String.format(BRACKETS_FORMAT, leftText);

		String rightText = right.getText();
		if (right.getOperatorInfo() != null && right.getOperatorInfo().getOrder() <= opInfo.getOrder())
			rightText = String.format(BRACKETS_FORMAT, rightText);

		return new ConstExpr(
			resultType,
			value,
			String.format(opInfo.getFormat(), leftText, rightText, "(" + resultType.getSimpleName() + ")"),
			opInfo
			);
	}

	/**
	 * Creates an expression that consists of an unary operator and its operand.
	 * 
	 * @param op Operator string.
	 * @param operand Unary operand (subexpression).
     * @return A constant expression object.
	 * @throws RecognitionException A semantic exception.
	 */
	
	public ConstExpr createUnaryExpr(Where where, String opText, ConstExpr operand) throws RecognitionException
	{	
	    final String ERROR_FORMAT =
	        "The unary operator '%s' is absent from the map of supported operators.";

		assert null != operand;

		final ConstExprOp opInfo = ConstExprRules.getOperatorInfo(opText, true);
		assert (null != opInfo) : String.format(ERROR_FORMAT, opText);

		if (!opInfo.isParamSupported(operand.getJavaType()))
			reporter.raiseError(where, new UnsupportedConstOperation(opText, operand.getText(), operand.getJavaType()));

		final IConstUnaryOperator operator = 
            ConstExprOperators.getUnaryOperator(opText, operand.getJavaType());
		
		if (null == operator)
		    reporter.raiseError(where, new UnsupportedConstOperation(opText, operand.getText(), operand.getJavaType()));
		
		final Object value = operator.calculate(operand.getValue());
		final Class<?> resultType = ConstExprOperators.getPrimitive(value.getClass());
		
		return new ConstExpr(
		    resultType,
			value,
			String.format(opInfo.getFormat(), operand.getText()),
			opInfo
			);
	}
}

