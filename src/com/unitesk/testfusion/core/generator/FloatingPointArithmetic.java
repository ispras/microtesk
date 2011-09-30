/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: FloatingPointArithmetic.java,v 1.10 2008/08/22 08:14:29 kamkin Exp $
 */

package com.unitesk.testfusion.core.generator;

import com.unitesk.testfusion.core.arithmetic.SoftFloatUtils;
import com.unitesk.testfusion.core.iterator.div.*;
import com.unitesk.testfusion.core.iterator.sqrt.*;
import com.unitesk.testfusion.core.type.DoubleType;
import com.unitesk.testfusion.core.type.SingleType;

/**
 * Random generator for floating-point arithmetic operations.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:chupilko@ispras.ru">Mikhail Chupilko</a>
 */
public class FloatingPointArithmetic
{
    /** 32-bit left-hand operand. */
	public static float  lhs32;
    
    /** 32-bit right-hand operand. */
    public static float  rhs32;

    /** 64-bit left-hand operand. */
    public static double lhs64;

    /** 64-bit right-hand operand. */
    public static double rhs64;

    /**
     * Iteration of hard-to-round cases for the single-precision division
     * operation in direct rounding mode.
     */
    public static Div32DirectRoundIterator div32DirectRoundIterator = new Div32DirectRoundIterator();

    /**
     * Iteration of hard-to-round cases for the single-precision division
     * operation in round-to-nearest mode.
     */
    public static Div32NearestRoundIterator div32NearestRoundIterator = new Div32NearestRoundIterator();

    /**
     * Iteration of hard-to-round cases for the double-precision division
     * operation in direct rounding mode.
     */
    public static Div64DirectRoundIterator div64DirectRoundIterator = new Div64DirectRoundIterator();

    /**
     * Iteration of hard-to-round cases for the double-precision division
     * operation in round-to-nearest mode.
     */
    public static Div64NearestRoundIterator div64NearestRoundIterator = new Div64NearestRoundIterator();
    
    /**
     * Iteration of hard-to-round cases for the single-precision square root
     * operation in direct rounding.
     */
    public static Sqrt32DirectRoundIterator sqrt32DirectRoundIterator = new Sqrt32DirectRoundIterator();

    /**
     * Iteration of hard-to-round cases for the single-precision square root
     * operation in round-to-nearest mode.
     */
    public static Sqrt32NearestRoundIterator sqrt32NearestRoundIterator = new Sqrt32NearestRoundIterator();
    
    /**
     * Iteration of hard-to-round cases for the double-precision square root
     * operation in direct rounding.
     */
    public static Sqrt64DirectRoundIterator sqrt64DirectRoundIterator = new Sqrt64DirectRoundIterator();

    /**
     * Iteration of hard-to-round cases for the double-precision square root
     * operation in round-to-nearest.
     */
    public static Sqrt64NearestRoundIterator sqrt64NearestRoundIterator = new Sqrt64NearestRoundIterator();
    
    //**********************************************************************************************
    // ADD32 Normal 
    //**********************************************************************************************

    /**
     * Checks if the single-precision addition operation does not cause
     * exceptions.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation does not cause exceptions;
     *         <code>false</code> otherwise.
     */
    public static boolean check_add32_normal(float lhs, float rhs)
    {
    	return !check_add32_overflow(lhs, rhs)
            && !check_add32_underflow(lhs, rhs)
            && !check_add32_inexact(lhs, rhs);
    }

    /**
     * Checks if the single-precision addition operation does not cause
     * exceptions.
     * 
     * @return <code>true</code> if the operation does not cause exceptions;
     *         <code>false</code> otherwise.
     */
    public static boolean check_add32_normal()
    {
        return check_add32_normal(lhs32, rhs32);
    }
    
    /**
     * Tries to generate operands for the single-precision addition operation
     * that do not cause exceptions.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add32_normal(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(lhs_fixed && rhs_fixed)
        	{ return check_add32_normal(); }

        if(lhs_fixed && lhs32 == 0 || rhs_fixed && rhs32 == 0)
        	{ return false; }
    
        if(!lhs_fixed && !rhs_fixed)
        {
        	boolean sign = Random.bit();
        	int exponent = Random.int32_positive() & SingleType.MAX_EXPONENT;
        	int fraction = Random.int32() & SingleType.MAX_FRACTION;
            
        	if(exponent >= SingleType.MAX_NORMALIZED_EXPONENT)
        		{ exponent = SingleType.MAX_NORMALIZED_EXPONENT - 1; }
            
        	lhs32 = SingleType.createSingle(sign ? 1 : 0, exponent, fraction);
        }
        
        float fixed = rhs_fixed ? rhs32 : lhs32;
        float spare = rhs_fixed ? lhs32 : rhs32;

        int fixedExponent = SingleType.getExponent(fixed);
        
        if(SingleType.isNormalized(fixed))
        {
        	int sign = SingleType.getSign(fixed);
        	int exponent = fixedExponent == SingleType.MAX_NORMALIZED_EXPONENT ? fixedExponent - 25 : fixedExponent;
        	int fraction = 0;
        	
        	spare = SingleType.createSingle(sign, exponent, fraction);
        	
        	rhs32 = rhs_fixed ? fixed : spare;
        	lhs32 = rhs_fixed ? spare : fixed;
        	
        	return true;
        }
        
        spare = Random.float32_normalized();

    	rhs32 = rhs_fixed ? fixed : spare;
    	lhs32 = rhs_fixed ? spare : fixed;

    	return false;
    }
    
    /**
     * Tries to generate operands for the single-precision addition operation
     * that do not cause exceptions.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
	public static boolean add32_normal()
	{
	    return add32_normal(false, false);
	}

    //**********************************************************************************************
    // ADD32 Overflow 
    //**********************************************************************************************

    /**
     * Checks if the single-precision addition operation causes the overflow
     * exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the overflow exception;
     *         <code>false</code> otherwise.
     */
	public static boolean check_add32_overflow(float lhs, float rhs)
    {
		if(!SingleType.isNormalized(lhs) || !SingleType.isNormalized(rhs))
			{ return false; }
		
		if(SingleType.getSign(lhs) == SingleType.getSign(rhs))
		{
			int normexponentlhs = SingleType.MAX_NORMALIZED_EXPONENT - SingleType.getExponent(lhs);
			int fractionlhs = SingleType.getFraction(lhs) | (SingleType.MAX_FRACTION + 1);
			int normexponentrhs = SingleType.MAX_NORMALIZED_EXPONENT - SingleType.getExponent(rhs);
			int fractionrhs = SingleType.getFraction(rhs) | (SingleType.MAX_FRACTION + 1);

			if(normexponentlhs > 32 || normexponentrhs > 32)
				{ return false; }
			
			if((fractionlhs >> normexponentlhs) + (fractionrhs >> normexponentrhs) >= SingleType.MAX_FRACTION + 1 << 1)
            {
				if(normexponentlhs == 0 || normexponentrhs == 0)
					{ return true; }
            }
		}
		return false;
    }

    /**
     * Checks if the single-precision addition operation causes the overflow
     * exception.
     * 
     * @return <code>true</code> if the operation causes the overflow exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_add32_overflow()
    {
        return check_add32_overflow(lhs32, rhs32);
    }

    /**
     * Tries to generate operands for the single-precision addition operation
     * that cause the overflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add32_overflow(boolean lhs_fixed, boolean rhs_fixed)
	{
        if(lhs_fixed && rhs_fixed)
        	{ return check_add32_overflow(); }

        if(lhs_fixed && lhs32 == 0 || rhs_fixed && rhs32 == 0)
        	{ return false; }
    
        if(!lhs_fixed && !rhs_fixed)
        {
        	boolean sign = Random.bit();
        	int exponent = SingleType.MAX_NORMALIZED_EXPONENT;
        	int fraction = Random.int32() & SingleType.MAX_FRACTION;
            
        	lhs32 = SingleType.createSingle(sign ? 1 : 0, exponent, fraction);
        }
        
        float fixed = rhs_fixed ? rhs32 : lhs32;
        float spare = rhs_fixed ? lhs32 : rhs32;

        int fixedSign = SingleType.getSign(fixed);
        int fixedExponent = SingleType.getExponent(fixed);
        int fixedFraction = SingleType.getFraction(fixed);
        
        if(SingleType.isNormalized(fixed) && fixedExponent >= SingleType.MAX_NORMALIZED_EXPONENT - SingleType.FRACTION_LENGTH)
        {
            int sign = fixedSign;
            int exponent;
            int fraction;
            int delta;
            
        	if(fixedExponent == SingleType.MAX_NORMALIZED_EXPONENT)
        	{
        		for(delta = 0; delta < SingleType.FRACTION_LENGTH; delta++)
        		{
        			if(((fixedFraction >> (SingleType.FRACTION_LENGTH - 1) - delta) & 0x1) == 0x0)
        				break;
        		}

        		exponent = SingleType.MAX_NORMALIZED_EXPONENT - Random.int32_non_negative_less_or_equal(delta);
        		fraction = Random.int32() & SingleType.MAX_FRACTION;
        	}
        	else
        	{
        		int alpha = SingleType.MAX_NORMALIZED_EXPONENT - fixedExponent;
        		
        		if(fixedExponent == SingleType.MAX_NORMALIZED_EXPONENT - SingleType.FRACTION_LENGTH)
        			{ delta = 0; }
        		else
        			{ delta = Random.int32_non_negative_less_or_equal(SingleType.FRACTION_LENGTH - alpha); }
        		
                exponent = SingleType.MAX_NORMALIZED_EXPONENT;   
                fraction = ~((1 << delta) - 1) & SingleType.MAX_FRACTION;
        	}

        	spare = SingleType.createSingle(sign, exponent, fraction);

            rhs32 = rhs_fixed ? fixed : spare;
            lhs32 = rhs_fixed ? spare : fixed;

            return true;
        }
        
        spare = Random.float32_normalized();
        
        rhs32 = rhs_fixed ? fixed : spare;
        lhs32 = rhs_fixed ? spare : fixed;
        
    	return false;
	}

    /**
     * Tries to generate operands for the single-precision addition operation
     * that cause the overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add32_overflow()
    {
        return add32_overflow(false, false);
    }

    //**********************************************************************************************
    // ADD32 Underflow 
    //**********************************************************************************************

    /**
     * Checks if the single-precision addition operation causes the underflow
     * exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the underflow exception;
     *         <code>false</code> otherwise.
     */
	public static boolean check_add32_underflow(float lhs, float rhs)
    {
		if(!SingleType.isNormalized(lhs) || !SingleType.isNormalized(rhs))
			{ return false; }
		
		if(SingleType.getSign(lhs) != SingleType.getSign(rhs))
		{
			int explhs = SingleType.getExponent(lhs);
			int exprhs = SingleType.getExponent(rhs);
			
			if(explhs == SingleType.MIN_NORMALIZED_EXPONENT && exprhs == SingleType.MIN_NORMALIZED_EXPONENT)
			{
				int fractionlhs = SingleType.getFraction(lhs) | (SingleType.MAX_FRACTION + 1);
				int fractionrhs = SingleType.getFraction(rhs) | (SingleType.MAX_FRACTION + 1);
				int lhs_great = fractionlhs > fractionrhs ? 1 : -1;
				int rhs_great = lhs_great == 1 ? -1 : 1;
				
				if( lhs_great * (fractionlhs >> explhs) + rhs_great * (fractionrhs >> exprhs) < SingleType.MAX_FRACTION + 1)
					{ return true; }
			}
		}
		
		return false;
    }

    /**
     * Checks if the single-precision addition operation causes the underflow
     * exception.
     * 
     * @return <code>true</code> if the operation causes the underflow exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_add32_underflow()
    {
        return check_add32_underflow(lhs32, rhs32);
    }

    /**
     * Tries to generate operands for the single-precision addition operation
     * that cause the underflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add32_underflow(boolean lhs_fixed, boolean rhs_fixed)
    {
    	if(lhs_fixed && rhs_fixed)
    		{ return check_add32_underflow(); }
    	
        if(lhs_fixed && lhs32 == 0 || rhs_fixed && rhs32 == 0)
        	{ return false; }

    	if(!lhs_fixed && !rhs_fixed)
    	{
    		boolean sign = Random.bit();
    		int exponent = SingleType.MIN_NORMALIZED_EXPONENT;
    		int fraction = Random.int32() & SingleType.MAX_FRACTION;
            
    		lhs32 = SingleType.createSingle(sign? 1 : 0, exponent, fraction);
    	}
    	
    	float fixed = rhs_fixed ? rhs32 : lhs32;
    	float spare = rhs_fixed ? lhs32 : rhs32;
    	
    	int fixedSign = SingleType.getSign(fixed);
    	int fixedExponent = SingleType.getExponent(fixed);
    	int fixedFraction = SingleType.getFraction(fixed);
    	
    	if(fixedExponent == SingleType.MIN_NORMALIZED_EXPONENT)
    	{
    		int sign = fixedSign == 1? 0 : 1;
    		int exponent = SingleType.MIN_NORMALIZED_EXPONENT;
    		int fraction = Random.int32() & SingleType.MAX_FRACTION;

    		if(fraction == fixedFraction)
    			{ fraction = (fraction & 2L) == 0L ? fraction + 1 : fraction - 1; }
    		
    		spare = SingleType.createSingle(sign, exponent, fraction);

            rhs32 = rhs_fixed ? fixed : spare;
            lhs32 = rhs_fixed ? spare : fixed;

            return true;

    	}
    	
        spare = Random.float32_normalized();
        
        rhs32 = rhs_fixed ? fixed : spare;
        lhs32 = rhs_fixed ? spare : fixed;
        
    	return false;
    }
    
    /**
     * Tries to generate operands for the single-precision addition operation
     * that cause the underflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add32_underflow()
    {
        return add32_underflow(false, false);
    }

    //**********************************************************************************************
    // ADD32 Inexact 
    //**********************************************************************************************
    
    /**
     * Checks if the single-precision addition operation causes the inexact
     * exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the inexact exception;
     *         <code>false</code> otherwise.
     */
	public static boolean check_add32_inexact(float lhs, float rhs)
    {
		if(!SingleType.isNormalized(lhs) || !SingleType.isNormalized(rhs))
			{ return false; }
		
		int explhs = SingleType.getExponent(lhs);
		int exprhs = SingleType.getExponent(rhs);
		int fractlhs = SingleType.getFraction(lhs);
		int fractrhs = SingleType.getFraction(rhs);
		
		if(fractlhs == 0 || fractrhs == 0)
			{ return false; }
		
		if(explhs > exprhs)
		{
			if(explhs - exprhs >= SingleType.FRACTION_LENGTH)
				{ return true; }
		}
		else if(explhs < exprhs)
		{
			if(exprhs - explhs >= SingleType.FRACTION_LENGTH)
				{ return true; }
		}

		return false;
    }

    /**
     * Checks if the single-precision addition operation causes the inexact
     * exception.
     * 
     * @return <code>true</code> if the operation causes the inexact exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_add32_inexact()
    {
        return check_add32_inexact(lhs32, rhs32);
    }

    /**
     * Tries to generate operands for the single-precision addition operation
     * that cause the inexact exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add32_inexact(boolean lhs_fixed, boolean rhs_fixed)
	{
    	if(lhs_fixed && rhs_fixed)
    		{ return check_add32_inexact(); }
    	
        if(lhs_fixed && lhs32 == 0 || rhs_fixed && rhs32 == 0)
        	{ return false; }

    	if(!lhs_fixed && !rhs_fixed)
    	{
    		boolean sign = Random.bit();
    		int exponent = Random.int32_non_negative_less(SingleType.MAX_NORMALIZED_EXPONENT - SingleType.FRACTION_LENGTH) + SingleType.PRECISION;
    		int fraction = Random.int32() & SingleType.MAX_FRACTION;

    		if(fraction == 0)
    			{ fraction++; } 
    		
    		lhs32 = SingleType.createSingle(sign? 1 : 0, exponent, fraction);
    	}
    	
    	float fixed = rhs_fixed ? rhs32 : lhs32;
    	float spare = rhs_fixed ? lhs32 : rhs32;
    	
    	int fixedFraction = SingleType.getFraction(fixed);
    	
    	if(SingleType.isNormalized(fixed) && fixedFraction != 0)
    	{
        	int fixedExponent = SingleType.getExponent(fixed);
    		boolean sign = Random.bit();
    		int exponent;
    		int fraction = Random.int32() & SingleType.MAX_FRACTION;
    		
    		if(fraction == 0)
    			{ fraction++; }
    		
    		if(fixedExponent < SingleType.BIAS)
    		{
    			int delta = fixedExponent + SingleType.FRACTION_LENGTH;
    			exponent = delta + Random.int32_non_negative_less(SingleType.MAX_EXPONENT - delta);
    		}
    		else
    		{
    			int delta = fixedExponent - SingleType.FRACTION_LENGTH;
    			exponent = delta - Random.int32_non_negative_less(delta);
    		}
    		
    		spare = SingleType.createSingle(sign? 1 : 0, exponent, fraction);
    		
    		rhs32 = rhs_fixed ? fixed : spare;
    		lhs32 = rhs_fixed ? spare : fixed;
    		
    		return true;
    	}

    	spare = Random.float32_normalized();
    	
    	rhs32 = rhs_fixed ? fixed : spare;
    	lhs32 = rhs_fixed ? spare : fixed;
    	
    	return false;
	}

    /**
     * Tries to generate operands for the single-precision addition operation
     * that cause the inexact exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add32_inexact()
    {
        return add32_inexact(false, false);
    }
    
    //**********************************************************************************************
    // ADD64 Normal 
    //**********************************************************************************************

    /**
     * Checks if the double-precision addition operation does not cause
     * exceptions.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation does not cause exceptions;
     *         <code>false</code> otherwise.
     */
    public static boolean check_add64_normal(double lhs, double rhs)
    {
    	return !check_add64_overflow(lhs, rhs)
    		&& !check_add64_underflow(lhs, rhs) && !check_add64_inexact(lhs, rhs);
    }

    /**
     * Checks if the double-precision addition operation does not cause
     * exceptions.
     * 
     * @return <code>true</code> if the operation does not cause exceptions;
     *         <code>false</code> otherwise.
     */
    public static boolean check_add64_normal()
    {
        return check_add64_normal(lhs64, rhs64);
    }
    
    /**
     * Tries to generate operands for the double-precision addition operation
     * that do not cause exceptions.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add64_normal(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(lhs_fixed && rhs_fixed)
        	return check_add64_normal();

        if(lhs_fixed && lhs64 == 0 || rhs_fixed && rhs64 == 0)
        	{ return false; }
    
        if(!lhs_fixed && !rhs_fixed)
        {
        	boolean sign = Random.bit();
        	long exponent = Random.int64_positive() & DoubleType.MAX_EXPONENT;
        	long fraction = Random.int64() & DoubleType.MAX_FRACTION;

        	if(exponent >= DoubleType.MAX_NORMALIZED_EXPONENT)
        		{ exponent = DoubleType.MAX_NORMALIZED_EXPONENT - 1; }
        	
        	lhs64 = DoubleType.createDouble(sign? 1 : 0, exponent, fraction);
        }
        
        double fixed = rhs_fixed ? rhs64 : lhs64;
        double spare = rhs_fixed ? lhs64 : rhs64;

        long fixedExponent = DoubleType.getExponent(fixed);
        
        if(DoubleType.isNormalized(fixed))
        {
        	int sign = DoubleType.getSign(fixed);
        	long exponent = fixedExponent == DoubleType.MAX_NORMALIZED_EXPONENT ? fixedExponent - 54 : fixedExponent;
        	long fraction = 0;
        	
        	spare = DoubleType.createDouble(sign, exponent, fraction);
        	
        	rhs64 = rhs_fixed ? fixed : spare;
        	lhs64 = rhs_fixed ? spare : fixed;
        	
        	return true;
        }
        
        spare = Random.float64_normalized();

    	rhs64 = rhs_fixed ? fixed : spare;
    	lhs64 = rhs_fixed ? spare : fixed;

    	return false;
    }
    
    /**
     * Tries to generate operands for the double-precision addition operation
     * that do not cause exceptions.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
	public static boolean add64_normal()
	{
	    return add64_normal(false, false);
	}

    //**********************************************************************************************
    // ADD64 Overflow
    //**********************************************************************************************

    /**
     * Checks if the double-precision addition operation causes the overflow
     * exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the overflow exception;
     *         <code>false</code> otherwise.
     */
	public static boolean check_add64_overflow(double lhs, double rhs)
    {
		if(!DoubleType.isNormalized(lhs) || !DoubleType.isNormalized(rhs))
			{ return false; }
		
		if(DoubleType.getSign(lhs) == DoubleType.getSign(rhs))
		{
			long normexponentlhs = DoubleType.MAX_NORMALIZED_EXPONENT - DoubleType.getExponent(lhs);
			long fractionlhs = DoubleType.getFraction(lhs) | (DoubleType.MAX_FRACTION + 1);
			long normexponentrhs = DoubleType.MAX_NORMALIZED_EXPONENT - DoubleType.getExponent(rhs);
			long fractionrhs = DoubleType.getFraction(rhs) | (DoubleType.MAX_FRACTION + 1);

			if((fractionlhs >> normexponentlhs) + (fractionrhs >> normexponentrhs) >= DoubleType.MAX_FRACTION + 1 << 1)
			{
				if(normexponentlhs == 0 || normexponentrhs == 0)
					{ return true; }
			}
		}
		
		return false;
    }

    /**
     * Checks if the double-precision addition operation causes the overflow
     * exception.
     * 
     * @return <code>true</code> if the operation causes the overflow exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_add64_overflow()
    {
        return check_add64_overflow(lhs64, rhs64);
    }

    /**
     * Tries to generate operands for the double-precision addition operation
     * that cause the overflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add64_overflow(boolean lhs_fixed, boolean rhs_fixed)
	{
        if(lhs_fixed && rhs_fixed)
        	{ return check_add64_overflow(); }

        if(lhs_fixed && lhs64 == 0 || rhs_fixed && rhs64 == 0)
        	{ return false; }
    
        if(!lhs_fixed && !rhs_fixed)
        {
        	boolean sign = Random.bit();
        	long exponent = DoubleType.MAX_NORMALIZED_EXPONENT;
        	long fraction = Random.int64() & DoubleType.MAX_FRACTION;
        	
        	lhs64 = DoubleType.createDouble(sign? 1 : 0, exponent, fraction);
        }
        
        double fixed = rhs_fixed ? rhs64 : lhs64;
        double spare = rhs_fixed ? lhs64 : rhs64;

        int fixedSign = DoubleType.getSign(fixed);
        long fixedExponent = DoubleType.getExponent(fixed);
        long fixedFraction = DoubleType.getFraction(fixed);
        
        if(DoubleType.isNormalized(fixed) && fixedExponent >= DoubleType.MAX_NORMALIZED_EXPONENT - DoubleType.FRACTION_LENGTH)
        {
            int sign = fixedSign;
            long exponent;
            long fraction;
            long delta;
            
        	if(fixedExponent == DoubleType.MAX_NORMALIZED_EXPONENT)
        	{
        		for(delta = 0; delta < DoubleType.FRACTION_LENGTH; delta++)
        		{
        			if(((fixedFraction >> (DoubleType.FRACTION_LENGTH - 1) - delta) & 0x1) == 0x0)
        				{ break; }
        		}

        		exponent = DoubleType.MAX_NORMALIZED_EXPONENT - Random.int64_non_negative_less_or_equal(delta);
        		fraction = Random.int64() & DoubleType.MAX_FRACTION;
        	}
        	else
        	{
        		long alpha = DoubleType.MAX_NORMALIZED_EXPONENT - fixedExponent;
        		
        		if(fixedExponent == DoubleType.MAX_NORMALIZED_EXPONENT - DoubleType.FRACTION_LENGTH)
        			{ delta = 0; }
        		else
        			{ delta = Random.int64_non_negative_less_or_equal(DoubleType.FRACTION_LENGTH - alpha); }
        		
                exponent = DoubleType.MAX_NORMALIZED_EXPONENT;   
                fraction = ~((1 << delta) - 1) & DoubleType.MAX_FRACTION;
        	}

        	spare = DoubleType.createDouble(sign, exponent, fraction);

            rhs64 = rhs_fixed ? fixed : spare;
            lhs64 = rhs_fixed ? spare : fixed;

            return true;
        }
        
        spare = Random.float64_normalized();
        
        rhs64 = rhs_fixed ? fixed : spare;
        lhs64 = rhs_fixed ? spare : fixed;
        
    	return false;
	}

    /**
     * Tries to generate operands for the double-precision addition operation
     * that cause the overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add64_overflow()
    {
        return add64_overflow(false, false);
    }    
    
    //**********************************************************************************************
    // ADD64 Underflow 
    //**********************************************************************************************

    /**
     * Checks if the double-precision addition operation causes the underflow
     * exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the underflow exception;
     *         <code>false</code> otherwise.
     */
	public static boolean check_add64_underflow(double lhs, double rhs)
    {
		if(!DoubleType.isNormalized(lhs) || !DoubleType.isNormalized(rhs))
			{ return false; }

		if(DoubleType.getSign(lhs) != DoubleType.getSign(rhs))
		{
			long explhs = DoubleType.getExponent(lhs);
			long exprhs = DoubleType.getExponent(rhs);
			if(explhs == DoubleType.MIN_NORMALIZED_EXPONENT && exprhs == DoubleType.MIN_NORMALIZED_EXPONENT)
			{
				long fractionlhs = DoubleType.getFraction(lhs) | (DoubleType.MAX_FRACTION + 1);
				long fractionrhs = DoubleType.getFraction(rhs) | (DoubleType.MAX_FRACTION + 1);
				int lhs_great = fractionlhs > fractionrhs ? 1 : -1;
				int rhs_great = lhs_great == 1 ? -1 : 1;
				
				if(lhs_great * (fractionlhs >> explhs) + rhs_great * (fractionrhs >> exprhs) < DoubleType.MAX_FRACTION + 1)
					{ return true; }
			}
		}
		
		return false;
    }

    /**
     * Checks if the double-precision addition operation causes the underflow
     * exception.
     * 
     * @return <code>true</code> if the operation causes the underflow exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_add64_underflow()
    {
        return check_add64_underflow(lhs64, rhs64);
    }

    /**
     * Tries to generate operands for the double-precision addition operation
     * that cause the underflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add64_underflow(boolean lhs_fixed, boolean rhs_fixed)
    {
    	if(lhs_fixed && rhs_fixed)
    		{ return check_add64_underflow(); }
    	
        if(lhs_fixed && lhs64 == 0 || rhs_fixed && rhs64 == 0)
        	{ return false; }

    	if(!lhs_fixed && !rhs_fixed)
    	{
    		boolean sign = Random.bit();
    		long exponent = DoubleType.MIN_NORMALIZED_EXPONENT;
    		long fraction = Random.int64() & DoubleType.MAX_FRACTION;
    		lhs64 = DoubleType.createDouble(sign? 1 : 0, exponent, fraction);
    	}
    	
    	double fixed = rhs_fixed ? rhs64 : lhs64;
    	double spare = rhs_fixed ? lhs64 : rhs64;
    	
    	int fixedSign = DoubleType.getSign(fixed);
    	long fixedExponent = DoubleType.getExponent(fixed);
    	long fixedFraction = DoubleType.getFraction(fixed);
    	
    	if(fixedExponent == DoubleType.MIN_NORMALIZED_EXPONENT)
    	{
    		int sign = fixedSign == 1? 0 : 1;
    		long exponent = DoubleType.MIN_NORMALIZED_EXPONENT;
    		long fraction = Random.int64() & DoubleType.MAX_FRACTION;

    		if(fraction == fixedFraction)
    			{ fraction = (fraction & 2L) == 0L ? fraction + 1 : fraction - 1; }
    		
    		spare = DoubleType.createDouble(sign, exponent, fraction);

            rhs64 = rhs_fixed ? fixed : spare;
            lhs64 = rhs_fixed ? spare : fixed;

            return true;
    	}
    	
        spare = Random.float64_normalized();
        
        rhs64 = rhs_fixed ? fixed : spare;
        lhs64 = rhs_fixed ? spare : fixed;
        
    	return false;
    }
    
    /**
     * Tries to generate operands for the double-precision addition operation
     * that cause the underflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add64_underflow()
    {
        return add64_underflow(false, false);
    }

    //**********************************************************************************************
    // ADD64 Inexact 
    //**********************************************************************************************

    /**
     * Checks if the double-precision addition operation causes the inexact
     * exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the inexact exception;
     *         <code>false</code> otherwise.
     */
	public static boolean check_add64_inexact(double lhs, double rhs)
    {
		if(!DoubleType.isNormalized(lhs) || !DoubleType.isNormalized(rhs))
			{ return false; }
		
		long explhs = DoubleType.getExponent(lhs);
		long exprhs = DoubleType.getExponent(rhs);
		long fractlhs = DoubleType.getFraction(lhs);
		long fractrhs = DoubleType.getFraction(rhs);
		
		if(fractlhs == 0 || fractrhs == 0)
			{ return false; }
		
		if(explhs > exprhs)
		{
			if(explhs - exprhs >= DoubleType.FRACTION_LENGTH)
				{ return true; }
		}
		else if(explhs < exprhs)
		{
			if(exprhs - explhs >= DoubleType.FRACTION_LENGTH)
				{ return true; }
		}

		return false;
    }

    /**
     * Checks if the double-precision addition operation causes the inexact
     * exception.
     * 
     * @return <code>true</code> if the operation causes the inexact exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_add64_inexact()
    {
        return check_add64_inexact(lhs64, rhs64);
    }

    /**
     * Tries to generate operands for the double-precision addition operation
     * that cause the inexact exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add64_inexact(boolean lhs_fixed, boolean rhs_fixed)
	{
    	if(lhs_fixed && rhs_fixed)
    		{ return check_add64_inexact(); }
    	
        if(lhs_fixed && lhs64 == 0 || rhs_fixed && rhs64 == 0)
        	{ return false; }

    	if(!lhs_fixed && !rhs_fixed)
    	{
    		boolean sign = Random.bit();
    		long exponent = Random.int64_non_negative_less(DoubleType.MAX_NORMALIZED_EXPONENT - DoubleType.FRACTION_LENGTH) + DoubleType.PRECISION;
    		long fraction = Random.int64() & DoubleType.MAX_FRACTION;
    		
    		if(fraction == 0)
    			{ fraction++; }
    		
    		lhs64 = DoubleType.createDouble(sign? 1 : 0, exponent, fraction);
    	}
    	
    	double fixed = rhs_fixed ? rhs64 : lhs64;
    	double spare = rhs_fixed ? lhs64 : rhs64;
    	
    	long fixedFraction = DoubleType.getFraction(fixed);
    	
    	if(DoubleType.isNormalized(fixed) && fixedFraction != 0)
    	{
        	long fixedExponent = DoubleType.getExponent(fixed);
    		boolean sign = Random.bit();
    		long exponent;
    		long fraction = Random.int64() & DoubleType.MAX_FRACTION;
    		
    		if(fraction == 0)
    			{ fraction++; }
    		
    		if(fixedExponent < DoubleType.BIAS)
    		{
    			long delta = fixedExponent + DoubleType.FRACTION_LENGTH;
    			exponent = delta + Random.int64_non_negative_less(DoubleType.MAX_EXPONENT - delta);
    		}
    		else
    		{
    			long delta = fixedExponent - DoubleType.FRACTION_LENGTH;
    			exponent = delta - Random.int64_non_negative_less(delta);
    		}
    		
    		spare = DoubleType.createDouble(sign? 1 : 0, exponent, fraction);
    		
    		rhs64 = rhs_fixed ? fixed : spare;
    		lhs64 = rhs_fixed ? spare : fixed;
    		
    		return true;
    	}

    	spare = Random.float64_normalized();
    	
    	rhs64 = rhs_fixed ? fixed : spare;
    	lhs64 = rhs_fixed ? spare : fixed;
    	
    	return false;
	}

    /**
     * Tries to generate operands for the double-precision addition operation
     * that cause the inexact exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add64_inexact()
    {
        return add64_inexact(false, false);
    }

    //**********************************************************************************************
    // SUB32 Normal 
    //**********************************************************************************************
    
    /**
     * Checks if the single-precision subtraction operation does not cause
     * exceptions.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation does not cause exceptions;
     *         <code>false</code> otherwise.
     */
    public static boolean check_sub32_normal(float lhs, float rhs)
    {
    	return check_add32_normal(lhs, -rhs);
    }
    
    /**
     * Checks if the single-precision subtraction operation does not cause
     * exceptions.
     * 
     * @return <code>true</code> if the operation does not cause exceptions;
     *         <code>false</code> otherwise.
     */
    public static boolean check_sub32_normal()
    {
    	return check_sub32_normal(lhs32, rhs32);
    }
    
    /**
     * Tries to generate operands for the single-precision subtraction operation
     * that do not cause exceptions.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub32_normal(boolean lhs_fixed, boolean rhs_fixed)
    {
    	boolean result = add32_normal(lhs_fixed, rhs_fixed);
    	
    	rhs32 = -rhs32;
    	
    	return result;
    }
    
    /**
     * Tries to generate operands for the single-precision subtraction operation
     * that do not cause exceptions.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub32_normal()
    {
    	return sub32_normal(false, false);
    }

    //**********************************************************************************************
    // SUB32 Overflow 
    //**********************************************************************************************
    
    /**
     * Checks if the single-precision subtraction operation causes the overflow
     * exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the overflow exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_sub32_overflow(float lhs, float rhs)
    {
    	return check_add32_overflow(lhs, -rhs);
    }
    
    /**
     * Checks if the single-precision subtraction operation causes the overflow
     * exception.
     * 
     * @return <code>true</code> if the operation causes the overflow exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_sub32_overflow()
    {
    	return check_sub32_overflow(lhs32, rhs32);
    }
    
    /**
     * Tries to generate operands for the single-precision subtraction operation
     * that cause the overflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub32_overflow(boolean lhs_fixed, boolean rhs_fixed)
    {
    	boolean result = add32_overflow(lhs_fixed, rhs_fixed);
    	
    	rhs32 = -rhs32;
    	
    	return result;
    }
    
    /**
     * Tries to generate operands for the single-precision subtraction operation
     * that cause the overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub32_overflow()
    {
    	return sub32_overflow(false, false);
    }
    
    //**********************************************************************************************
    // SUB32 Underflow 
    //**********************************************************************************************
    
    /**
     * Checks if the single-precision subtraction operation causes the underflow
     * exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the underflow
     *         exception; <code>false</code> otherwise.
     */
    public static boolean check_sub32_underflow(float lhs, float rhs)
    {
    	rhs = -rhs;
    	return check_add32_underflow(lhs, rhs);
    }
    
    /**
     * Checks if the single-precision subtraction operation causes the underflow
     * exception.
     * 
     * @return <code>true</code> if the operation causes the underflow
     *         exception; <code>false</code> otherwise.
     */
    public static boolean check_sub32_underflow()
    {
    	return check_sub32_underflow(lhs32, rhs32);
    }
    
    /**
     * Tries to generate operands for the single-precision subtraction operation
     * that cause the underflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub32_underflow(boolean lhs_fixed, boolean rhs_fixed)
    {
    	boolean result = add32_underflow(lhs_fixed, rhs_fixed);
    	
    	rhs32 = -rhs32;
    	
    	return result;
    }
    
    /**
     * Tries to generate operands for the single-precision subtraction operation
     * that cause the underflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub32_underflow()
    {
    	return sub32_underflow(false, false);
    }
    
    //**********************************************************************************************
    // SUB32 Inexact 
    //**********************************************************************************************

    /**
     * Checks if the single-precision subtraction operation causes the inexact
     * exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the inexact exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_sub32_inexact(float lhs, float rhs)
    {
    	return check_add32_inexact(lhs, rhs);
    }
    
    /**
     * Checks if the single-precision subtraction operation causes the inexact
     * exception.
     * 
     * @return <code>true</code> if the operation causes the inexact exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_sub32_inexact()
    {
    	return check_add32_inexact(lhs32, rhs32);
    }
    
    /**
     * Tries to generate operands for the single-precision subtraction operation
     * that cause the inexact exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub32_inexact(boolean lhs_fixed, boolean rhs_fixed)
    {
    	return add32_inexact(lhs_fixed, rhs_fixed);
    }
    
    /**
     * Tries to generate operands for the single-precision subtraction operation
     * that cause the inexact exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub32_inexact()
    {
    	return add32_inexact(false, false);
    }

    //**********************************************************************************************
    // SUB64 Normal 
    //**********************************************************************************************
    
    /**
     * Checks if the double-precision subtraction operation does not cause
     * exceptions.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation does not cause exceptions;
     *         <code>false</code> otherwise.
     */
    public static boolean check_sub64_normal(double lhs, double rhs)
    {
    	return check_add64_normal(lhs, -rhs);
    }
    
    /**
     * Checks if the double-precision subtraction operation does not cause
     * exceptions.
     * 
     * @return <code>true</code> if the operation does not cause exceptions;
     *         <code>false</code> otherwise.
     */
    public static boolean check_sub64_normal()
    {
    	return check_sub64_normal(lhs64, rhs64);
    }
    
    /**
     * Tries to generate operands for the double-precision subtraction operation
     * that do not cause exceptions.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub64_normal(boolean lhs_fixed, boolean rhs_fixed)
    {
    	boolean result = add64_normal(lhs_fixed, rhs_fixed);
    	
    	rhs64 = -rhs64;
    	
    	return result;
    }
    
    /**
     * Tries to generate operands for the double-precision subtraction operation
     * that do not cause exceptions.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub64_normal()
    {
    	return sub64_normal(false, false);
    }
    
    //**********************************************************************************************
    // SUB64 Overflow 
    //**********************************************************************************************
    
    /**
     * Checks if the double-precision subtraction operation causes the overflow
     * exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the overflow exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_sub64_overflow(double lhs, double rhs)
    {
    	return check_add64_overflow(lhs, -rhs);
    }
    
    /**
     * Checks if the double-precision subtraction operation causes the overflow
     * exception.
     * 
     * @return <code>true</code> if the operation causes the overflow exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_sub64_overflow()
    {
    	return check_sub64_overflow(lhs64, rhs64);
    }
    
    /**
     * Tries to generate operands for the double-precision subtraction operation
     * that cause the overflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub64_overflow(boolean lhs_fixed, boolean rhs_fixed)
    {
    	boolean result = add64_overflow(lhs_fixed, rhs_fixed);
    	
    	rhs64 = -rhs64;
    	
    	return result;
    }
    
    /**
     * Tries to generate operands for the double-precision subtraction operation
     * that cause the overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub64_overflow()
    {
    	return sub64_overflow(false, false);
    }
    
    //**********************************************************************************************
    // SUB64 Underflow 
    //**********************************************************************************************
    
    /**
     * Checks if the double-precision subtraction operation causes the underflow
     * exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the underflow
     *         exception; <code>false</code> otherwise.
     */
    public static boolean check_sub64_underflow(double lhs, double rhs)
    {
    	return check_add64_underflow(lhs, -rhs);
    }
    
    /**
     * Checks if the double-precision subtraction operation causes the underflow
     * exception.
     * 
     * @return <code>true</code> if the operation causes the underflow
     *         exception; <code>false</code> otherwise.
     */
    public static boolean check_sub64_underflow()
    {
    	return check_sub64_underflow(lhs64, rhs64);
    }
    
    /**
     * Tries to generate operands for the double-precision subtraction operation
     * that cause the underflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub64_underflow(boolean lhs_fixed, boolean rhs_fixed)
    {
    	boolean result = add64_underflow(lhs_fixed, rhs_fixed);
    	
    	rhs64 = -rhs64;
    	
    	return result;
    }
    
    /**
     * Tries to generate operands for the double-precision subtraction operation
     * that cause the underflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub64_underflow()
    {
    	return sub64_underflow(false, false);
    }
    
    //**********************************************************************************************
    // SUB64 Inexact 
    //**********************************************************************************************

    /**
     * Checks if the double-precision subtraction operation causes the inexact
     * exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the inexact exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_sub64_inexact(double lhs, double rhs)
    {
    	return check_add64_inexact(lhs, rhs);
    }
    
    /**
     * Checks if the double-precision subtraction operation causes the inexact
     * exception.
     * 
     * @return <code>true</code> if the operation causes the inexact exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_sub64_inexact()
    {
    	return check_add64_inexact(lhs64, rhs64);
    }
    
    /**
     * Tries to generate operands for the double-precision subtraction operation
     * that cause the inexact exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub64_inexact(boolean lhs_fixed, boolean rhs_fixed)
    {
    	return add64_inexact(lhs_fixed, rhs_fixed);
    }
    
    /**
     * Tries to generate operands for the double-precision subtraction operation
     * that cause the inexact exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub64_inexact()
    {
    	return add64_inexact(false, false);
    }
    
    //**********************************************************************************************
    // MUL32 Normal
    //**********************************************************************************************

    /**
     * Checks if the single-precision multiplication operation does not cause
     * exceptions.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation does not cause exceptions;
     *         <code>false</code> otherwise.
     */
    public static boolean check_mul32_normal(float lhs, float rhs)
    {
    	return !check_mul32_overflow(lhs, rhs)
    		&& !check_mul32_underflow(lhs, rhs) && !check_mul32_inexact(lhs, rhs);
    }
    
    /**
     * Checks if the single-precision multiplication operation does not cause
     * exceptions.
     * 
     * @return <code>true</code> if the operation does not cause exceptions;
     *         <code>false</code> otherwise.
     */
    public static boolean check_mul32_normal()
    {
    	return check_mul32_normal(lhs32, rhs32);
    }
    
    /**
     * Tries to generate operands for the single-precision multiplication
     * operation that do not cause exceptions.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean mul32_normal(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(lhs_fixed && rhs_fixed)
        	{ return check_mul32_normal(); }

        if(!lhs_fixed && !rhs_fixed)
        {
        	boolean sign = Random.bit();
        	int exponent = Random.int32_range(SingleType.MIN_NORMALIZED_EXPONENT + 2, SingleType.MAX_NORMALIZED_EXPONENT - 2);
        	int fraction = Random.int32() & SingleType.MAX_FRACTION;
        	
        	lhs32 = SingleType.createSingle(sign? 1 : 0, exponent, fraction);
        }
        
        float fixed = rhs_fixed ? rhs32 : lhs32;
        float spare = rhs_fixed ? lhs32 : rhs32;

        int fixedExponent = SingleType.getExponent(fixed);
        int fixedFraction = SingleType.getFraction(fixed);
        
        if(SingleType.isNormalized(fixed) && fixedExponent > SingleType.MIN_NORMALIZED_EXPONENT + 2 && fixedExponent < SingleType.MAX_NORMALIZED_EXPONENT - 2)
        {
        	boolean sign = Random.bit();
        	int exponent = SingleType.BIAS;
        	int fraction = fixedFraction == 0 ? Random.int32() & SingleType.MAX_FRACTION : 0;
        	
        	spare = SingleType.createSingle(sign ? 1 : 0, exponent, fraction);

        	rhs32 = rhs_fixed ? fixed : spare;
        	lhs32 = rhs_fixed ? spare : fixed;
        	
        	return true;
        }
        
        spare = Random.float32_normalized();
        
        rhs32 = rhs_fixed ? fixed : spare;
        lhs32 = rhs_fixed ? spare : fixed;

    	return false;
    }
    
    /**
     * Tries to generate operands for the single-precision multiplication
     * operation that do not cause exceptions.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean mul32_normal()
    {
    	return mul32_normal(false, false);
    }
    
    //**********************************************************************************************
    // MUL32 Overflow
    //**********************************************************************************************

    /**
     * Checks if the single-precision multiplication operation causes the
     * overflow exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation does not cause the overflow;
     *         exception; <code>false</code> otherwise.
     */
    public static boolean check_mul32_overflow(float lhs, float rhs)
    {
		if(!SingleType.isNormalized(lhs) || !SingleType.isNormalized(rhs))
			{ return false; }

    	int lhsexponent = SingleType.getExponent(lhs);
    	int rhsexponent = SingleType.getExponent(rhs);
    	
    	return lhsexponent + rhsexponent - SingleType.BIAS > SingleType.MAX_NORMALIZED_EXPONENT;
    }
    
    /**
     * Checks if the single-precision multiplication operation causes the
     * overflow exception.
     * 
     * @return <code>true</code> if the operation causes the overflow exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_mul32_overflow()
    {
    	return check_mul32_overflow(lhs32, rhs32);
    }
    
    /**
     * Tries to generate operands for the single-precision multiplication
     * operation that cause the overflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean mul32_overflow(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(lhs_fixed && rhs_fixed)
        	{ return check_mul32_overflow(); }

        if(lhs_fixed && lhs32 == 0 || rhs_fixed && rhs32 == 0)
        	{ return false; }
    
        if(!lhs_fixed && !rhs_fixed)
        {
        	boolean sign = Random.bit();
        	int exponent = Random.int32_range(SingleType.MAX_EXPONENT - SingleType.BIAS, SingleType.MAX_NORMALIZED_EXPONENT);
        	int fraction = Random.int32() & SingleType.MAX_FRACTION;
        	
        	lhs32 = SingleType.createSingle(sign? 1 : 0, exponent, fraction);
        }
        
        float fixed = rhs_fixed ? rhs32 : lhs32;
        float spare = rhs_fixed ? lhs32 : rhs32;

        int fixedExponent = SingleType.getExponent(fixed);
        
        if(SingleType.isNormalized(fixed) && fixedExponent > SingleType.BIAS)
        {
        	boolean sign = Random.bit();
        	int exponent = SingleType.MAX_EXPONENT + SingleType.BIAS - Random.int32_range(SingleType.BIAS + 1, fixedExponent);
        	int fraction = Random.int32() & SingleType.MAX_FRACTION;
        	
        	spare = SingleType.createSingle(sign? 1 : 0, exponent, fraction);
        	
        	rhs32 = rhs_fixed ? fixed : spare;
        	lhs32 = rhs_fixed ? spare : fixed;
        	
        	return true;
        }
        
        spare = Random.float32_normalized();
        
        rhs32 = rhs_fixed ? fixed : spare;
        lhs32 = rhs_fixed ? spare : fixed;

    	return false;
    }
    
    /**
     * Tries to generate operands for the single-precision multiplication
     * operation that cause the overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean mul32_overflow()
    {
    	return mul32_overflow(false, false);
    }
    
    //**********************************************************************************************
    // MUL32 Underflow
    //**********************************************************************************************

    /**
     * Checks if the single-precision multiplication operation causes the
     * underflow exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the underflow;
     *         exception; <code>false</code> otherwise.
     */
    public static boolean check_mul32_underflow(float lhs, float rhs)
    {
		if(!SingleType.isNormalized(lhs) || !SingleType.isNormalized(rhs))
			return false;

    	int lhsexponent = SingleType.getExponent(lhs);
    	int rhsexponent = SingleType.getExponent(rhs);
    	
    	return lhsexponent + rhsexponent < SingleType.MIN_NORMALIZED_EXPONENT + SingleType.BIAS;
    }
    
    /**
     * Checks if the single-precision multiplication operation causes the
     * underflow exception.
     * 
     * @return <code>true</code> if the operation causes the underflow
     *         exception; <code>false</code> otherwise.
     */
    public static boolean check_mul32_underflow()
    {
    	return check_mul32_underflow(lhs32, rhs32);
    }
    
    /**
     * Tries to generate operands for the single-precision multiplication
     * operation that cause the underflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean mul32_underflow(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(lhs_fixed && rhs_fixed)
        	{ return check_mul32_underflow(); }

        if(lhs_fixed && lhs32 == 0 || rhs_fixed && rhs32 == 0)
        	{ return false; }
    
        if(!lhs_fixed && !rhs_fixed)
        {
        	boolean sign = Random.bit();
        	int exponent = Random.int32_range(SingleType.MIN_NORMALIZED_EXPONENT, SingleType.BIAS - 1);
        	int fraction = Random.int32() & SingleType.MAX_FRACTION;
        	
        	lhs32 = SingleType.createSingle(sign? 1 : 0, exponent, fraction);
        }
        
        float fixed = rhs_fixed ? rhs32 : lhs32;
        float spare = rhs_fixed ? lhs32 : rhs32;

        int fixedExponent = SingleType.getExponent(fixed);
        
        if(SingleType.isNormalized(fixed) && fixedExponent < SingleType.BIAS)
        {
        	boolean sign = Random.bit();
        	int exponent = Random.int32_range(SingleType.MIN_NORMALIZED_EXPONENT, SingleType.BIAS - fixedExponent);
        	int fraction = Random.int32() & SingleType.MAX_FRACTION;
        	
        	spare = SingleType.createSingle(sign? 1 : 0, exponent, fraction);
        	
        	rhs32 = rhs_fixed ? fixed : spare;
        	lhs32 = rhs_fixed ? spare : fixed;
        	
        	return true;
        }
        
        spare = Random.float32_normalized();
        
        rhs32 = rhs_fixed ? fixed : spare;
        lhs32 = rhs_fixed ? spare : fixed;

    	return false;
    }
    
    /**
     * Tries to generate operands for the single-precision multiplication
     * operation that cause the underflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean mul32_underflow()
    {
    	return mul32_underflow(false, false);
    }
    
    //**********************************************************************************************
    // MUL32 Inexact
    //**********************************************************************************************

    /**
     * Checks if the single-precision multiplication operation causes the
     * inexact exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the inexact exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_mul32_inexact(float lhs, float rhs)
    {
		if(!SingleType.isNormalized(lhs) || !SingleType.isNormalized(rhs))
			{ return false; }

    	int lhsexponent = SingleType.getExponent(lhs);
    	int rhsexponent = SingleType.getExponent(rhs);
    	int lhsfraction = SingleType.getFraction(lhs);
    	int rhsfraction = SingleType.getFraction(rhs);

    	if(lhsfraction == 0 || rhsfraction == 0)
    		{ return false; }
    	
    	if(lhsexponent > rhsexponent)
    		{ return lhsexponent > rhsexponent + SingleType.FRACTION_LENGTH; }
    	else
    		{ return rhsexponent > lhsexponent + SingleType.FRACTION_LENGTH; }
    }
    
    /**
     * Checks if the single-precision multiplication operation causes the
     * inexact exception.
     * 
     * @return <code>true</code> if the operation causes the inexact exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_mul32_inexact()
    {
    	return check_mul32_inexact(lhs32, rhs32);
    }
    
    /**
     * Tries to generate operands for the single-precision multiplication
     * operation that cause the inexact exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean mul32_inexact(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(lhs_fixed && rhs_fixed)
        	{ return check_mul32_inexact(); }

        if(lhs_fixed && lhs32 == 0 || rhs_fixed && rhs32 == 0)
        	{ return false; }
    
        if(!lhs_fixed && !rhs_fixed)
        {
        	boolean sign = Random.bit();
        	int exponent = Random.int32_range(SingleType.MIN_NORMALIZED_EXPONENT + 1, SingleType.MAX_NORMALIZED_EXPONENT - 1);
        	int fraction = (Random.int32() | 1) & SingleType.MAX_FRACTION;
        	
        	lhs32 = SingleType.createSingle(sign? 1 : 0, exponent, fraction);
        }
        
        float fixed = rhs_fixed ? rhs32 : lhs32;
        float spare = rhs_fixed ? lhs32 : rhs32;

        int fixedExponent = SingleType.getExponent(fixed);
        
        if(SingleType.isNormalized(fixed) && fixedExponent > SingleType.MIN_NORMALIZED_EXPONENT + 1 && fixedExponent < SingleType.MAX_NORMALIZED_EXPONENT - 1)
        {
        	boolean sign = Random.bit();
        	int exponent;
        	int fraction = (Random.int32() | 1) & SingleType.MAX_FRACTION;
        	
        	if(fixedExponent < SingleType.BIAS)
        		{ exponent = Random.int32_range(SingleType.BIAS + 24, SingleType.MAX_NORMALIZED_EXPONENT - 1); }
        	else
        		{ exponent = Random.int32_range(SingleType.MIN_NORMALIZED_EXPONENT + 1, SingleType.BIAS - 24); }
        	
        	spare = SingleType.createSingle(sign? 1 : 0, exponent, fraction);

        	rhs32 = rhs_fixed ? fixed : spare;
        	lhs32 = rhs_fixed ? spare : fixed;
        	
        	return true;
        }
        
        spare = Random.float32_normalized();
        
        rhs32 = rhs_fixed ? fixed : spare;
        lhs32 = rhs_fixed ? spare : fixed;

    	return false;
    }
    
    /**
     * Tries to generate operands for the single-precision multiplication
     * operation that cause the inexact exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean mul32_inexact()
    {
    	return mul32_inexact(false, false);
    }
    
    //**********************************************************************************************
    // MUL64 Normal
    //**********************************************************************************************

    /**
     * Checks if the double-precision multiplication operation does not cause
     * exceptions.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation does not cause exceptions;
     *         <code>false</code> otherwise.
     */
    public static boolean check_mul64_normal(double lhs, double rhs)
    {
    	return !check_mul64_overflow(lhs, rhs)
    		&& !check_mul64_underflow(lhs, rhs) && !check_mul64_inexact(lhs, rhs);
    }
    
    /**
     * Checks if the double-precision multiplication operation does not cause
     * exceptions.
     * 
     * @return <code>true</code> if the operation does not cause exceptions;
     *         <code>false</code> otherwise.
     */
    public static boolean check_mul64_normal()
    {
    	return check_mul64_normal(lhs64, rhs64);
    }
    
    /**
     * Tries to generate operands for the double-precision multiplication
     * operation that do not cause exceptions.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean mul64_normal(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(lhs_fixed && rhs_fixed)
        	{ return check_mul64_normal(); }

        if(!lhs_fixed && !rhs_fixed)
        {
        	boolean sign = Random.bit();
        	long exponent = Random.int64_range(DoubleType.MIN_NORMALIZED_EXPONENT + 2, DoubleType.MAX_NORMALIZED_EXPONENT - 2);
        	long fraction = Random.int64() & DoubleType.MAX_FRACTION;
        	
        	lhs64 = DoubleType.createDouble(sign? 1 : 0, exponent, fraction);
        }
        
        double fixed = rhs_fixed ? rhs64 : lhs64;
        double spare = rhs_fixed ? lhs64 : rhs64;

        long fixedExponent = DoubleType.getExponent(fixed);
        long fixedFraction = DoubleType.getFraction(fixed);
        
        if(DoubleType.isNormalized(fixed) && fixedExponent > DoubleType.MIN_NORMALIZED_EXPONENT + 2 && fixedExponent < DoubleType.MAX_NORMALIZED_EXPONENT - 2)
        {
        	boolean sign = Random.bit();
        	long exponent = DoubleType.BIAS;
        	long fraction = fixedFraction == 0 ? Random.int64() & DoubleType.MAX_FRACTION : 0;
        	
        	spare = DoubleType.createDouble(sign ? 1 : 0, exponent, fraction);

        	rhs64 = rhs_fixed ? fixed : spare;
        	lhs64 = rhs_fixed ? spare : fixed;
        	
        	return true;
        }
        
        spare = Random.float64_normalized();
        
        rhs64 = rhs_fixed ? fixed : spare;
        lhs64 = rhs_fixed ? spare : fixed;

    	return false;
    }
    
    /**
     * Tries to generate operands for the double-precision multiplication
     * operation that do not cause exceptions.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean mul64_normal()
    {
    	return mul64_normal(false, false);
    }
    
    //**********************************************************************************************
    // MUL64 Overflow
    //**********************************************************************************************

    /**
     * Checks if the double-precision multiplication operation causes the
     * overflow exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the overflow exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_mul64_overflow(double lhs, double rhs)
    {
		if(!DoubleType.isNormalized(lhs) || !DoubleType.isNormalized(rhs))
			{ return false; }

    	long lhsexponent = DoubleType.getExponent(lhs);
    	long rhsexponent = DoubleType.getExponent(rhs);
    	
    	return lhsexponent + rhsexponent - DoubleType.BIAS > DoubleType.MAX_NORMALIZED_EXPONENT;
    }
    
    /**
     * Checks if the double-precision multiplication operation causes the
     * overflow exception.
     * 
     * @return <code>true</code> if the operation causes the overflow exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_mul64_overflow()
    {
    	return check_mul64_overflow(lhs64, rhs64);
    }
    
    /**
     * Tries to generate operands for the double-precision multiplication
     * operation that cause the overflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean mul64_overflow(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(lhs_fixed && rhs_fixed)
        	{ return check_mul64_overflow(); }

        if(lhs_fixed && lhs64 == 0 || rhs_fixed && rhs64 == 0)
        	{ return false; }
    
        if(!lhs_fixed && !rhs_fixed)
        {
        	boolean sign = Random.bit();
        	long exponent = Random.int64_range(DoubleType.MAX_EXPONENT - DoubleType.BIAS, DoubleType.MAX_NORMALIZED_EXPONENT);
        	long fraction = Random.int64() & DoubleType.MAX_FRACTION;
        	
        	lhs64 = DoubleType.createDouble(sign? 1 : 0, exponent, fraction);
        }
        
        double fixed = rhs_fixed ? rhs64 : lhs64;
        double spare = rhs_fixed ? lhs64 : rhs64;

        long fixedExponent = DoubleType.getExponent(fixed);
        
        if(DoubleType.isNormalized(fixed) && fixedExponent > DoubleType.BIAS)
        {
        	boolean sign = Random.bit();
        	long exponent = DoubleType.MAX_EXPONENT + DoubleType.BIAS - Random.int64_range(DoubleType.BIAS + 1, fixedExponent);
        	long fraction = Random.int64() & DoubleType.MAX_FRACTION;
        	
        	spare = DoubleType.createDouble(sign? 1 : 0, exponent, fraction);
        	
        	rhs64 = rhs_fixed ? fixed : spare;
        	lhs64 = rhs_fixed ? spare : fixed;
        	
        	return true;
        }
        
        spare = Random.float64_normalized();
        
        rhs64 = rhs_fixed ? fixed : spare;
        lhs64 = rhs_fixed ? spare : fixed;

    	return false;
    }
    
    /**
     * Tries to generate operands for the double-precision multiplication
     * operation that cause the overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean mul64_overflow()
    {
    	return mul64_overflow(false, false);
    }
    
    //**********************************************************************************************
    // MUL64 Underflow
    //**********************************************************************************************

    /**
     * Checks if the double-precision multiplication operation causes the
     * underflow exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the underflow exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_mul64_underflow(double lhs, double rhs)
    {
		if(!DoubleType.isNormalized(lhs) || !DoubleType.isNormalized(rhs))
			{ return false; }

    	long lhsexponent = DoubleType.getExponent(lhs);
    	long rhsexponent = DoubleType.getExponent(rhs);
    	
    	return lhsexponent + rhsexponent < DoubleType.MIN_NORMALIZED_EXPONENT + DoubleType.BIAS;
    }
    
    /**
     * Checks if the double-precision multiplication operation causes the
     * underflow exception.
     * 
     * @return <code>true</code> if the operation causes the underflow exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_mul64_underflow()
    {
    	return check_mul64_underflow(lhs64, rhs64);
    }
    
    /**
     * Tries to generate operands for the double-precision multiplication
     * operation that cause the underflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean mul64_underflow(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(lhs_fixed && rhs_fixed)
        	{ return check_mul64_underflow(); }

        if(lhs_fixed && lhs64 == 0 || rhs_fixed && rhs64 == 0)
        	{ return false; }
    
        if(!lhs_fixed && !rhs_fixed)
        {
        	boolean sign = Random.bit();
        	long exponent = Random.int64_range(DoubleType.MIN_NORMALIZED_EXPONENT, DoubleType.BIAS - 1);
        	long fraction = Random.int64() & DoubleType.MAX_FRACTION;
        	
        	lhs64 = DoubleType.createDouble(sign? 1 : 0, exponent, fraction);
        }
        
        double fixed = rhs_fixed ? rhs64 : lhs64;
        double spare = rhs_fixed ? lhs64 : rhs64;

        long fixedExponent = DoubleType.getExponent(fixed);
        
        if(DoubleType.isNormalized(fixed) && fixedExponent < DoubleType.BIAS)
        {
        	boolean sign = Random.bit();
        	long exponent = Random.int64_range(DoubleType.MIN_NORMALIZED_EXPONENT, DoubleType.BIAS - fixedExponent);
        	long fraction = Random.int64() & DoubleType.MAX_FRACTION;
        	
        	spare = DoubleType.createDouble(sign? 1 : 0, exponent, fraction);
        	
        	rhs64 = rhs_fixed ? fixed : spare;
        	lhs64 = rhs_fixed ? spare : fixed;
        	
        	return true;
        }
        
        spare = Random.float64_normalized();
        
        rhs64 = rhs_fixed ? fixed : spare;
        lhs64 = rhs_fixed ? spare : fixed;

    	return false;
    }
    
    /**
     * Tries to generate operands for the double-precision multiplication
     * operation that cause the underflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean mul64_underflow()
    {
    	return mul64_underflow(false, false);
    }
    
    //**********************************************************************************************
    // MUL64 Inexact
    //**********************************************************************************************

    /**
     * Checks if the double-precision multiplication operation causes the
     * inexact exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the inexact exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_mul64_inexact(double lhs, double rhs)
    {
		if(!DoubleType.isNormalized(lhs) || !DoubleType.isNormalized(rhs))
			{ return false; }

    	long lhsexponent = DoubleType.getExponent(lhs);
    	long rhsexponent = DoubleType.getExponent(rhs);
    	long lhsfraction = DoubleType.getFraction(lhs);
    	long rhsfraction = DoubleType.getFraction(rhs);

    	if(lhsfraction == 0 || rhsfraction == 0)
    		{ return false; }
    	
    	if(lhsexponent > rhsexponent)
    		{ return lhsexponent > rhsexponent + DoubleType.FRACTION_LENGTH; }
    	else
    		{ return rhsexponent > lhsexponent + DoubleType.FRACTION_LENGTH; }
    }
    
    /**
     * Checks if the double-precision multiplication operation causes the
     * inexact exception.
     * 
     * @return <code>true</code> if the operation causes the inexact exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_mul64_inexact()
    {
    	return check_mul64_inexact(lhs64, rhs64);
    }
    
    /**
     * Tries to generate operands for the double-precision multiplication
     * operation that cause the inexact exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean mul64_inexact(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(lhs_fixed && rhs_fixed)
        	{ return check_mul64_inexact(); }

        if(lhs_fixed && lhs64 == 0 || rhs_fixed && rhs64 == 0)
        	{ return false; }
    
        if(!lhs_fixed && !rhs_fixed)
        {
        	boolean sign = Random.bit();
        	long exponent = Random.int64_range(DoubleType.MIN_NORMALIZED_EXPONENT + 1, DoubleType.MAX_NORMALIZED_EXPONENT - 1);
        	long fraction = (Random.int64() | 1) & DoubleType.MAX_FRACTION;
        	
        	lhs64 = DoubleType.createDouble(sign? 1 : 0, exponent, fraction);
        }
        
        double fixed = rhs_fixed ? rhs64 : lhs64;
        double spare = rhs_fixed ? lhs64 : rhs64;

        long fixedExponent = DoubleType.getExponent(fixed);
        
        if(DoubleType.isNormalized(fixed) && fixedExponent > DoubleType.MIN_NORMALIZED_EXPONENT + 1 && fixedExponent < DoubleType.MAX_NORMALIZED_EXPONENT - 1)
        {
        	boolean sign = Random.bit();
        	long exponent;
        	long fraction = (Random.int64() | 1) & DoubleType.MAX_FRACTION;
        	
        	if(fixedExponent < DoubleType.BIAS)
        	    { exponent = Random.int64_range(DoubleType.BIAS + 53, DoubleType.MAX_NORMALIZED_EXPONENT - 1); }
        	else
        		{ exponent = Random.int64_range(DoubleType.MIN_NORMALIZED_EXPONENT + 1, DoubleType.BIAS - 53); }
        	
        	spare = DoubleType.createDouble(sign? 1 : 0, exponent, fraction);

        	rhs64 = rhs_fixed ? fixed : spare;
        	lhs64 = rhs_fixed ? spare : fixed;
        	
        	return true;
        }
        
        spare = Random.float64_normalized();
        
        rhs64 = rhs_fixed ? fixed : spare;
        lhs64 = rhs_fixed ? spare : fixed;

    	return false;
    }
    
    /**
     * Tries to generate operands for the double-precision multiplication
     * operation that cause the inexact exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean mul64_inexact()
    {
    	return mul64_inexact(false, false);
    }
    
    //**********************************************************************************************
    // DIV32 Normal
    //**********************************************************************************************

    /**
     * Checks if the single-precision division operation does not cause
     * exceptions.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation does not cause exceptions;
     *         <code>false</code> otherwise.
     */
    public static boolean check_div32_normal(float lhs, float rhs)
    {
        SoftFloatUtils.float_div(lhs, rhs);
        
        return SoftFloatUtils.get_float_exception_flags() == 0;
    }
    
    /**
     * Checks if the single-precision division operation does not cause
     * exceptions.
     * 
     * @return <code>true</code> if the operation does not cause exceptions;
     *         <code>false</code> otherwise.
     */
    public static boolean check_div32_normal()
    {
    	return check_div32_normal(lhs32, rhs32);
    }
    
    /**
     * Tries to generate operands for the single-precision division operation
     * that do not cause exceptions.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div32_normal(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(lhs_fixed && rhs_fixed)
        	{ return check_div32_normal(); }

        if(rhs_fixed && rhs32 == 0)
        	{ return false; }
        	
        if(!lhs_fixed && !rhs_fixed)
        {
        	boolean sign = Random.bit();
        	int exponent = Random.int32_range(SingleType.MIN_NORMALIZED_EXPONENT + 2, SingleType.MAX_NORMALIZED_EXPONENT - 2);
        	int fraction = Random.int32() & SingleType.MAX_FRACTION;
        	
        	lhs32 = SingleType.createSingle(sign? 1 : 0, exponent, fraction);
        }
        
        float fixed = rhs_fixed ? rhs32 : lhs32;
        float spare = rhs_fixed ? lhs32 : rhs32;

        int fixedExponent = SingleType.getExponent(fixed);
        int fixedFraction = SingleType.getFraction(fixed);
        
        if(SingleType.isNormalized(fixed) && 
           fixedExponent > SingleType.MIN_NORMALIZED_EXPONENT + 2 && 
           fixedExponent < SingleType.MAX_NORMALIZED_EXPONENT - 2 && !(rhs_fixed && fixedFraction == 0))
        {
        	boolean sign = Random.bit();
        	int exponent = fixedExponent;
        	int fraction = rhs_fixed ? Random.int32() & SingleType.MAX_FRACTION : 0;
        	
        	spare = SingleType.createSingle(sign ? 1 : 0, exponent, fraction);

        	rhs32 = rhs_fixed ? fixed : spare;
        	lhs32 = rhs_fixed ? spare : fixed;
        	
        	return true;
        }
        
        spare = Random.float32_normalized();
        
        rhs32 = rhs_fixed ? fixed : spare;
        lhs32 = rhs_fixed ? spare : fixed;

    	return false;
    }
    
    /**
     * Tries to generate operands for the single-precision division operation
     * that do not cause exceptions.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div32_normal()
    {
    	return div32_normal(false, false);
    }
    
    //**********************************************************************************************
    // DIV32 Overflow
    //**********************************************************************************************

    /**
     * Checks if the single-precision division operation causes the overflow
     * exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the overflow exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_div32_overflow(float lhs, float rhs)
    {
        SoftFloatUtils.float_div(lhs, rhs);
        
        return (SoftFloatUtils.get_float_exception_flags() & SoftFloatUtils.FLAG_OVERFLOW) != 0;
    }
    
    /**
     * Checks if the single-precision division operation causes the overflow
     * exception.
     * 
     * @return <code>true</code> if the operation causes the overflow exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_div32_overflow()
    {
    	return check_div32_overflow(lhs32, rhs32);
    }
    
    /**
     * Tries to generate operands for the single-precision division operation
     * that cause the overflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div32_overflow(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(lhs_fixed && rhs_fixed)
        	{ return check_div32_overflow(); }

        if(lhs_fixed && lhs32 == 0 || rhs_fixed && rhs32 == 0)
        	{ return false; }
    
        if(!lhs_fixed && !rhs_fixed)
        {
        	boolean sign = Random.bit();
        	int exponent = Random.int32_range(SingleType.MAX_EXPONENT - SingleType.BIAS + 1 , SingleType.MAX_NORMALIZED_EXPONENT);
        	int fraction = Random.int32() & SingleType.MAX_FRACTION;
        	
        	lhs32 = SingleType.createSingle(sign? 1 : 0, exponent, fraction);
        }
        
        float fixed = rhs_fixed ? rhs32 : lhs32;
        float spare = rhs_fixed ? lhs32 : rhs32;

        int fixedExponent = SingleType.getExponent(fixed);
        
        if(SingleType.isNormalized(fixed) && (rhs_fixed && (fixedExponent < SingleType.BIAS - 1) || !rhs_fixed && (fixedExponent > SingleType.BIAS + 1)))
        {
        	boolean sign = Random.bit();
        	int exponent = 0;
        	int fraction = Random.int32() & SingleType.MAX_FRACTION;
        	
        	if(rhs_fixed)
        		{ exponent = Random.int32_range(SingleType.BIAS + fixedExponent + 1, SingleType.MAX_NORMALIZED_EXPONENT); }
        	else
        		{ exponent = Random.int32_range(1, fixedExponent - SingleType.BIAS - 1); }
        		
        	spare = SingleType.createSingle(sign? 1 : 0, exponent, fraction);
        	
        	rhs32 = rhs_fixed ? fixed : spare;
        	lhs32 = rhs_fixed ? spare : fixed;
        	
        	return true;
        }
        
        spare = Random.float32_normalized();
        
        rhs32 = rhs_fixed ? fixed : spare;
        lhs32 = rhs_fixed ? spare : fixed;

    	return false;
    }
    
    /**
     * Tries to generate operands for the single-precision division operation
     * that cause the overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div32_overflow()
    {
    	return div32_overflow(false, false);
    }
    
    //**********************************************************************************************
    // DIV32 Underflow
    //**********************************************************************************************

    /**
     * Checks if the single-precision division operation causes the underflow
     * exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the underflow
     *         exception; <code>false</code> otherwise.
     */
    public static boolean check_div32_underflow(float lhs, float rhs)
    {
        SoftFloatUtils.float_div(lhs, rhs);
        
        return (SoftFloatUtils.get_float_exception_flags() & SoftFloatUtils.FLAG_UNDERFLOW) != 0;
    }
    
    /**
     * Checks if the single-precision division operation causes the underflow
     * exception.
     * 
     * @return <code>true</code> if the operation causes the underflow
     *         exception; <code>false</code> otherwise.
     */
    public static boolean check_div32_underflow()
    {
    	return check_div32_underflow(lhs32, rhs32);
    }
    
    /**
     * Tries to generate operands for the single-precision division operation
     * that cause the underflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div32_underflow(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(lhs_fixed && rhs_fixed)
        	{ return check_div32_underflow(); }

        if(lhs_fixed && lhs32 == 0 || rhs_fixed && rhs32 == 0)
        	{ return false; }
    
        if(!lhs_fixed && !rhs_fixed)
        {
        	boolean sign = Random.bit();
        	int exponent = Random.int32_range(SingleType.MIN_NORMALIZED_EXPONENT, SingleType.BIAS - 1);
        	int fraction = Random.int32() & SingleType.MAX_FRACTION;
        	
        	lhs32 = SingleType.createSingle(sign? 1 : 0, exponent, fraction);
        }
        
        float fixed = rhs_fixed ? rhs32 : lhs32;
        float spare = rhs_fixed ? lhs32 : rhs32;

        int fixedExponent = SingleType.getExponent(fixed);
        
        if(SingleType.isNormalized(fixed) && (rhs_fixed && fixedExponent > (SingleType.BIAS + 1) || !rhs_fixed && fixedExponent < (SingleType.BIAS - 1)))
        {
        	boolean sign = Random.bit();
        	int exponent;
        	int fraction = Random.int32() & SingleType.MAX_FRACTION;
        	
        	if(rhs_fixed)
        		{ exponent = Random.int32_range(SingleType.MIN_NORMALIZED_EXPONENT, fixedExponent - SingleType.BIAS - 1); }
        	else
        		{ exponent = SingleType.BIAS + Random.int32_range(fixedExponent + 1, SingleType.BIAS); }
        	
        	spare = SingleType.createSingle(sign? 1 : 0, exponent, fraction);
        	
        	rhs32 = rhs_fixed ? fixed : spare;
        	lhs32 = rhs_fixed ? spare : fixed;
        	
        	return true;
        }
        
        spare = Random.float32_normalized();
        
        rhs32 = rhs_fixed ? fixed : spare;
        lhs32 = rhs_fixed ? spare : fixed;

    	return false;
    }
    
    /**
     * Tries to generate operands for the single-precision division operation
     * that cause the underflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div32_underflow()
    {
    	return div32_underflow(false, false);
    }
    
    //**********************************************************************************************
    // DIV32 Inexact
    //**********************************************************************************************

    /**
     * Checks if the single-precision division operation causes the inexact
     * exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the inexact exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_div32_inexact(float lhs, float rhs)
    {
        SoftFloatUtils.float_div(lhs, rhs);
        
    	return (SoftFloatUtils.get_float_exception_flags() & SoftFloatUtils.FLAG_INEXACT) != 0;
    }
    
    /**
     * Checks if the single-precision division operation causes the inexact
     * exception.
     * 
     * @return <code>true</code> if the operation causes the inexact exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_div32_inexact()
    {
    	return check_div32_inexact(lhs32, rhs32);
    }
    
    /**
     * Tries to generate operands for the single-precision division operation
     * that cause the inexact exception in round-to-nearest mode.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div32_inexact_nearest_round(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(lhs_fixed || rhs_fixed)
            { return false; }
        
        if(!div32NearestRoundIterator.hasValue())
            { div32NearestRoundIterator.init(); }
        
        lhs32 = div32NearestRoundIterator.getDividend();
        rhs32 = div32NearestRoundIterator.getDivisor();
        
        div32NearestRoundIterator.next();
        
        return true;
    }
    
    /**
     * Tries to generate operands for the single-precision division operation
     * that cause the inexact exception in round-to-nearest mode.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div32_inexact_nearest_round()
    {
        return div32_inexact_nearest_round(false, false);
    }

    /**
     * Tries to generate operands for the single-precision division operation
     * that cause the inexact exception in direct rounding mode.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div32_inexact_direct_round(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(lhs_fixed || rhs_fixed)
            { return false; }
        
        if(!div32DirectRoundIterator.hasValue())
            { div32DirectRoundIterator.init(); }
        
        lhs32 = div32DirectRoundIterator.getDividend();
        rhs32 = div32DirectRoundIterator.getDivisor();
        
        div32DirectRoundIterator.next();
        
        return true;
    }
    
    /**
     * Tries to generate operands for the single-precision division operation
     * that cause the inexact exception in direct rounding mode.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div32_inexact_direct_round()
    {
        return div32_inexact(false, false);
    }
    
    /**
     * Tries to generate operands for the single-precision division operation
     * that cause the inexact exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div32_inexact(boolean lhs_fixed, boolean rhs_fixed)
    {
        return div64_inexact_nearest_round(lhs_fixed, rhs_fixed);
    }
    
    /**
     * Tries to generate operands for the single-precision division operation
     * that cause the inexact exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div32_inexact()
    {
        return div64_inexact(false, false);
    }

    //**********************************************************************************************
    // DIV64 Normal
    //**********************************************************************************************

    /**
     * Checks if the double-precision division operation does not cause
     * exceptions.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation does not cause exceptions;
     *         <code>false</code> otherwise.
     */
    public static boolean check_div64_normal(double lhs, double rhs)
    {
        SoftFloatUtils.double_div(lhs, rhs);
        
        return SoftFloatUtils.get_float_exception_flags() == 0;
    }
    
    /**
     * Checks if the double-precision division operation does not cause
     * exceptions.
     * 
     * @return <code>true</code> if the operation does not cause exceptions;
     *         <code>false</code> otherwise.
     */
    public static boolean check_div64_normal()
    {
    	return check_div64_normal(lhs64, rhs64);
    }
    
    /**
     * Tries to generate operands for the double-precision division operation
     * that do not cause exceptions.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div64_normal(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(lhs_fixed && rhs_fixed)
        	{ return check_div64_normal(); }

        if(rhs_fixed && rhs64 == 0)
        	{ return false; }
        	
        if(!lhs_fixed && !rhs_fixed)
        {
        	boolean sign = Random.bit();
        	long exponent = Random.int64_range(DoubleType.MIN_NORMALIZED_EXPONENT + 2, DoubleType.MAX_NORMALIZED_EXPONENT - 2);
        	long fraction = Random.int64() & DoubleType.MAX_FRACTION;
        	
        	lhs64 = DoubleType.createDouble(sign? 1 : 0, exponent, fraction);
        }
        
        double fixed = rhs_fixed ? rhs64 : lhs64;
        double spare = rhs_fixed ? lhs64 : rhs64;

        long fixedExponent = DoubleType.getExponent(fixed);
        long fixedFraction = DoubleType.getFraction(fixed);
        
        if(DoubleType.isNormalized(fixed) && 
           fixedExponent > DoubleType.MIN_NORMALIZED_EXPONENT + 2 && 
           fixedExponent < DoubleType.MAX_NORMALIZED_EXPONENT - 2 && !(rhs_fixed && fixedFraction == 0))
        {
        	boolean sign = Random.bit();
        	long exponent = fixedExponent;
        	long fraction = rhs_fixed ? Random.int64() & DoubleType.MAX_FRACTION : 0;
        	
        	spare = DoubleType.createDouble(sign ? 1 : 0, exponent, fraction);

        	rhs64 = rhs_fixed ? fixed : spare;
        	lhs64 = rhs_fixed ? spare : fixed;
        	
        	return true;
        }
        
        spare = Random.float64_normalized();
        
        rhs64 = rhs_fixed ? fixed : spare;
        lhs64 = rhs_fixed ? spare : fixed;

    	return false;
    }
    
    /**
     * Tries to generate operands for the double-precision division operation
     * that do not cause exceptions.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div64_normal()
    {
    	return div64_normal(false, false);
    }
    
    //**********************************************************************************************
    // DIV64 Overflow
    //**********************************************************************************************

    /**
     * Checks if the double-precision division operation causes the overflow
     * exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the overflow exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_div64_overflow(double lhs, double rhs)
    {
        SoftFloatUtils.double_div(lhs, rhs);
        
        return (SoftFloatUtils.get_float_exception_flags() & SoftFloatUtils.FLAG_OVERFLOW) != 0;
    }
    
    /**
     * Checks if the double-precision division operation causes the overflow
     * exception.
     * 
     * @return <code>true</code> if the operation causes the overflow exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_div64_overflow()
    {
    	return check_div64_overflow(lhs64, rhs64);
    }
    
    /**
     * Tries to generate operands for the double-precision division operation
     * that cause the overflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div64_overflow(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(lhs_fixed && rhs_fixed)
        	{ return check_div64_overflow(); }

        if(lhs_fixed && lhs64 == 0 || rhs_fixed && rhs64 == 0)
        	{ return false; }
    
        if(!lhs_fixed && !rhs_fixed)
        {
        	boolean sign = Random.bit();
        	long exponent = Random.int64_range(DoubleType.MAX_EXPONENT - DoubleType.BIAS + 1 , DoubleType.MAX_NORMALIZED_EXPONENT);
        	long fraction = Random.int64() & DoubleType.MAX_FRACTION;
        	
        	lhs64 = DoubleType.createDouble(sign? 1 : 0, exponent, fraction);
        }
        
        double fixed = rhs_fixed ? rhs64 : lhs64;
        double spare = rhs_fixed ? lhs64 : rhs64;

        long fixedExponent = DoubleType.getExponent(fixed);
        
        if(DoubleType.isNormalized(fixed) && (rhs_fixed && (fixedExponent < DoubleType.BIAS - 1) || !rhs_fixed && (fixedExponent > DoubleType.BIAS + 1)))
        {
        	boolean sign = Random.bit();
        	long exponent = 0;
        	long fraction = Random.int64() & DoubleType.MAX_FRACTION;
        	
        	if(rhs_fixed)
        		{ exponent = Random.int64_range(DoubleType.BIAS + 1 + fixedExponent, DoubleType.MAX_NORMALIZED_EXPONENT); }
        	else
        		{ exponent = Random.int64_range(1, fixedExponent - DoubleType.BIAS - 1); }
        		
        	spare = DoubleType.createDouble(sign? 1 : 0, exponent, fraction);
        	
        	rhs64 = rhs_fixed ? fixed : spare;
        	lhs64 = rhs_fixed ? spare : fixed;
        	
        	return true;
        }
        
        spare = Random.float64_normalized();
        
        rhs64 = rhs_fixed ? fixed : spare;
        lhs64 = rhs_fixed ? spare : fixed;

    	return false;
    }
    
    /**
     * Tries to generate operands for the double-precision division operation
     * that cause the overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div64_overflow()
    {
    	return div64_overflow(false, false);
    }
    
    //**********************************************************************************************
    // DIV64 Underflow
    //**********************************************************************************************

    /**
     * Checks if the double-precision division operation causes the underflow
     * exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the underflow
     *         exception; <code>false</code> otherwise.
     */
    public static boolean check_div64_underflow(double lhs, double rhs)
    {
        SoftFloatUtils.double_div(lhs, rhs);
        
        return (SoftFloatUtils.get_float_exception_flags() & SoftFloatUtils.FLAG_UNDERFLOW) != 0;
    }
    
    /**
     * Checks if the double-precision division operation causes the underflow
     * exception.
     * 
     * @return <code>true</code> if the operation causes the underflow
     *         exception; <code>false</code> otherwise.
     */
    public static boolean check_div64_underflow()
    {
    	return check_div64_underflow(lhs64, rhs64);
    }
    
    /**
     * Tries to generate operands for the double-precision division operation
     * that cause the underflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div64_underflow(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(lhs_fixed && rhs_fixed)
        	{ return check_div64_underflow(); }

        if(lhs_fixed && lhs64 == 0 || rhs_fixed && rhs64 == 0)
        	{ return false; }
    
        if(!lhs_fixed && !rhs_fixed)
        {
        	boolean sign = Random.bit();
        	long exponent = Random.int64_range(DoubleType.MIN_NORMALIZED_EXPONENT, DoubleType.BIAS - 1);
        	long fraction = Random.int64() & DoubleType.MAX_FRACTION;
        	
        	lhs64 = DoubleType.createDouble(sign? 1 : 0, exponent, fraction);
        }
        
        double fixed = rhs_fixed ? rhs64 : lhs64;
        double spare = rhs_fixed ? lhs64 : rhs64;

        long fixedExponent = DoubleType.getExponent(fixed);
        
        if(DoubleType.isNormalized(fixed) && (rhs_fixed && fixedExponent > (DoubleType.BIAS + 1) ||
          !rhs_fixed && fixedExponent < (DoubleType.BIAS - 1)))
        {
        	boolean sign = Random.bit();
        	long exponent;
        	long fraction = Random.int64() & DoubleType.MAX_FRACTION;
        	
        	if(rhs_fixed)
        		{ exponent = Random.int64_range(DoubleType.MIN_NORMALIZED_EXPONENT, fixedExponent - DoubleType.BIAS - 1); }
        	else
        		{ exponent = DoubleType.BIAS + Random.int64_range(fixedExponent + 1, DoubleType.BIAS); }
        	
        	spare = DoubleType.createDouble(sign? 1 : 0, exponent, fraction);
        	
        	rhs64 = rhs_fixed ? fixed : spare;
        	lhs64 = rhs_fixed ? spare : fixed;
        	
        	return true;
        }
        
        spare = Random.float64_normalized();
        
        rhs64 = rhs_fixed ? fixed : spare;
        lhs64 = rhs_fixed ? spare : fixed;

    	return false;
    }
    
    /**
     * Tries to generate operands for the double-precision division operation
     * that cause the underflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div64_underflow()
    {
    	return div64_underflow(false, false);
    }
    
    //**********************************************************************************************
    // DIV64 Inexact
    //**********************************************************************************************

    /**
     * Checks if the double-precision division operation causes the inexact
     * exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the inexact exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_div64_inexact(double lhs, double rhs)
    {
        SoftFloatUtils.double_div(lhs, rhs);
        
        return (SoftFloatUtils.get_float_exception_flags() & SoftFloatUtils.FLAG_INEXACT) != 0;
    }
    
    /**
     * Checks if the double-precision division operation causes the inexact
     * exception.
     * 
     * @return <code>true</code> if the operation causes the inexact exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_div64_inexact()
    {
    	return check_div64_inexact(lhs64, rhs64);
    }
    
    /**
     * Tries to generate operands for the double-precision division operation
     * that cause the inexact exception in round-to-nearest mode.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div64_inexact_nearest_round(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(lhs_fixed || rhs_fixed)
            { return false; }
        
        if(!div64NearestRoundIterator.hasValue())
            { div64NearestRoundIterator.init(); }
        
        lhs64 = div64NearestRoundIterator.getDividend();
        rhs64 = div64NearestRoundIterator.getDivisor();
        
        div64NearestRoundIterator.next();
        
        return true;
    }
    
    /**
     * Tries to generate operands for the double-precision division operation
     * that cause the inexact exception in round-to-nearest mode.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div64_inexact_nearest_round()
    {
        return div64_inexact_nearest_round(false, false);
    }

    /**
     * Tries to generate operands for the double-precision division operation
     * that cause the inexact exception in direct rounding mode.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div64_inexact_direct_round(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(lhs_fixed || rhs_fixed)
            { return false; }
        
        if(!div64DirectRoundIterator.hasValue())
            { div64DirectRoundIterator.init(); }
        
        lhs64 = div64DirectRoundIterator.getDividend();
        rhs64 = div64DirectRoundIterator.getDivisor();
        
        div64DirectRoundIterator.next();
        
        return true;
    }
    
    /**
     * Tries to generate operands for the double-precision division operation
     * that cause the inexact exception in direct rounding mode.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div64_inexact_direct_round()
    {
        return div64_inexact(false, false);
    }
    
    /**
     * Tries to generate operands for the double-precision division operation
     * that cause the inexact exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div64_inexact(boolean lhs_fixed, boolean rhs_fixed)
    {
    	return div64_inexact_nearest_round(lhs_fixed, rhs_fixed);
    }
    
    /**
     * Tries to generate operands for the double-precision division operation
     * that cause the inexact exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean div64_inexact()
    {
    	return div64_inexact(false, false);
    }
    
    //**********************************************************************************************
    // SQRT32 Inexact
    //**********************************************************************************************
    
    /**
     * Checks if the single-precision square root operation causes the inexact
     * exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @return <code>true</code> if the operation causes the inexact exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_sqrt32_inexact(float arg)
    {
        SoftFloatUtils.float_sqrt(arg);
        
        return (SoftFloatUtils.get_float_exception_flags() & SoftFloatUtils.FLAG_INEXACT) != 0;
    }
    
    /**
     * Checks if the single-precision square root operation causes the inexact
     * exception.
     * 
     * @return <code>true</code> if the operation causes the inexact exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_sqrt32_inexact()
    {
        return check_sqrt32_inexact(lhs32);
    }
    
    /**
     * Tries to generate operands for the single-precision square root operation
     * that cause the inexact exception in round-to-nearest mode.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sqrt32_inexact_nearest_round(boolean arg_fixed)
    {
        if(arg_fixed)
            { return check_sqrt32_inexact(); }
        
        if(!sqrt32NearestRoundIterator.hasValue())
            { sqrt32NearestRoundIterator.init(); }
        
        lhs32 = sqrt32NearestRoundIterator.singleValue();
        
        sqrt32NearestRoundIterator.next();
        
        return true;
    }
    
    /**
     * Tries to generate operands for the single-precision square root operation
     * that cause the inexact exception in round-to-nearest mode.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sqrt32_inexact_nearest_round()
    {
        return sqrt32_inexact_nearest_round(false);
    }

    /**
     * Tries to generate operands for the single-precision square root operation
     * that cause the inexact exception in direct rounding mode.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sqrt32_inexact_direct_round(boolean arg_fixed)
    {
        if(arg_fixed)
            { return check_sqrt32_inexact(); }
        
        if(!sqrt32DirectRoundIterator.hasValue())
            { sqrt32DirectRoundIterator.init(); }
        
        lhs32 = sqrt32DirectRoundIterator.singleValue();
        
        sqrt32DirectRoundIterator.next();
        
        return true;
    }
    
    /**
     * Tries to generate operands for the single-precision square root operation
     * that cause the inexact exception in direct rounding mode.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sqrt32_inexact_direct_round()
    {
        return sqrt32_inexact_direct_round(false);
    }
    
    /**
     * Tries to generate operands for the single-precision square root operation
     * that cause the inexact exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sqrt32_inexact(boolean arg_fixed)
    {
        return sqrt32_inexact_nearest_round(arg_fixed);
    }
    
    /**
     * Tries to generate operands for the single-precision square root operation
     * that cause the inexact exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sqrt32_inexact()
    {
        return sqrt32_inexact(false);
    }

    //**********************************************************************************************
    // SQRT64 Inexact
    //**********************************************************************************************
    
    /**
     * Checks if the double-precision square root operation causes the inexact
     * exception.
     * 
     * @param  <code>lhs</code> the left-hand operand.
     * 
     * @param  <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the operation causes the inexact exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_sqrt64_inexact(double arg)
    {
        SoftFloatUtils.double_sqrt(arg);
        
        return (SoftFloatUtils.get_float_exception_flags() & SoftFloatUtils.FLAG_INEXACT) != 0;
    }
    
    /**
     * Checks if the double-precision square root operation causes the inexact
     * exception.
     * 
     * @return <code>true</code> if the operation causes the inexact exception;
     *         <code>false</code> otherwise.
     */
    public static boolean check_sqrt64_inexact()
    {
        return check_sqrt64_inexact(lhs64);
    }

    /**
     * Tries to generate operands for the double-precision square root operation
     * that cause the inexact exception in round-to-nearest mode.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sqrt64_inexact_nearest_round(boolean arg_fixed)
    {
        if(arg_fixed)
            { return check_sqrt64_inexact(); }
        
        if(!sqrt64NearestRoundIterator.hasValue())
            { sqrt64NearestRoundIterator.init(); }
        
        lhs64 = sqrt64NearestRoundIterator.doubleValue();
        
        sqrt64NearestRoundIterator.next();
        
        return true;
    }
    
    /**
     * Tries to generate operands for the double-precision square root operation
     * that cause the inexact exception in round-to-nearest mode.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sqrt64_inexact_nearest_round()
    {
        return sqrt64_inexact_nearest_round(false);
    }

    /**
     * Tries to generate operands for the double-precision square root operation
     * that cause the inexact exception in direct rounding mode.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sqrt64_inexact_direct_round(boolean arg_fixed)
    {
        if(arg_fixed)
            { return check_sqrt64_inexact(); }
        
        if(!sqrt64DirectRoundIterator.hasValue())
            { sqrt64DirectRoundIterator.init(); }
        
        lhs64 = sqrt64DirectRoundIterator.doubleValue();
        
        sqrt64DirectRoundIterator.next();
        
        return true;
    }
    
    /**
     * Tries to generate operands for the double-precision square root operation
     * that cause the inexact exception in direct rounding mode.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sqrt64_inexact_direct_round()
    {
        return sqrt64_inexact_direct_round(false);
    }
    
    /**
     * Tries to generate operands for the double-precision square root operation
     * that cause the inexact exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sqrt64_inexact(boolean arg_fixed)
    {
        return sqrt64_inexact_nearest_round(arg_fixed);
    }
    
    /**
     * Tries to generate operands for the double-precision square root operation
     * that cause the inexact exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sqrt64_inexact()
    {
        return sqrt64_inexact(false);
    }
}
