/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: IntegerArithmetic.java,v 1.11 2009/08/18 14:52:36 vorobyev Exp $
 */

package com.unitesk.testfusion.core.generator;

import com.unitesk.testfusion.core.type.IntegerType;

/**
 * Random generator for integer arithmetic operations.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class IntegerArithmetic
{
    /** 32-bit left-hand value. */
    public static int lhs32;

    /** 32-bit right-hand value. */
    public static int rhs32;

    /** 64-bit left-hand value. */
    public static long lhs64;

    /** 64-bit right-hand value. */
    public static long rhs64;
    
    /** 12-bit right-hand value. */
    public static int rhs12;

    /** 16-bit right-hand value. */
    public static short rhs16;
    
    /** 18-bit right-hand value. */
    public static int rhs18;
    
    //**********************************************************************************************
    // ADD32 Normal 
    //**********************************************************************************************

    /**
     * Checks if the 32-bit addition operation does not cause exceptions.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 32-bit addition operation does not cause
     *         exceptions; <code>false</code> otherwise.
     */
    public static boolean check_add32_normal(int lhs, int rhs)
    {
        return !check_add32_overflow(lhs, rhs);
    }

    /**
     * Checks if the 32-bit addition operation does not cause exceptions.
     * 
     * @return <code>true</code> if the 32-bit addition operation does not cause
     *         exceptions; <code>false</code> otherwise.
     */
    public static boolean check_add32_normal()
    {
        return check_add32_normal(lhs32, rhs32);
    }

    /**
     * Tries to generate operands for the 32-bit addition operation that do not
     * cause exceptions.
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

        if(!lhs_fixed && !rhs_fixed)
            { lhs32 = Random.int32(); }
            
        int fixed = rhs_fixed ? rhs32 : lhs32;
        int spare = rhs_fixed ? lhs32 : rhs32;
        
        if(fixed >= 0)
            { spare = Random.int32_range(Integer.MIN_VALUE, Integer.MAX_VALUE - fixed); }
        else
            { spare = Random.int32_range(Integer.MIN_VALUE - fixed, Integer.MAX_VALUE); }
        
        rhs32 = rhs_fixed ? fixed : spare;
        lhs32 = rhs_fixed ? spare : fixed;
        
        return true;
	}

    /**
     * Tries to generate operands for the 32-bit addition operation that do not
     * cause exceptions.
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
     * Checks if the 32-bit addition operation causes the integer overflow
     * exception.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 32-bit addition operation causes the
     *         integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_add32_overflow(int lhs, int rhs)
    {
        if(lhs > 0 && rhs > 0)
            { return rhs > Integer.MAX_VALUE - lhs; }
        
        if(lhs < 0 && rhs < 0)
            { return rhs < Integer.MIN_VALUE - lhs; }
        
        return false;
    }
	
    /**
     * Checks if the 32-bit addition operation causes the integer overflow
     * exception.
     * 
     * @return <code>true</code> if the 32-bit addition operation causes the
     *         integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_add32_overflow()
    {
        return check_add32_overflow(lhs32, rhs32);
    }

    /**
     * Tries to generate operands for the 32-bit addition operation that causes
     * the integer overflow exception.
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
            { if((lhs32 = Random.int32()) == 0) lhs32++; }
            
        int fixed = rhs_fixed ? rhs32 : lhs32;
        int spare = rhs_fixed ? lhs32 : rhs32;

        if(fixed > 0)
            { spare = Random.int32_range((Integer.MAX_VALUE - fixed) + 1, Integer.MAX_VALUE); }
        else
            { spare = Random.int32_range(Integer.MIN_VALUE, (Integer.MIN_VALUE - fixed) - 1); }
        
        rhs32 = rhs_fixed ? fixed : spare;
        lhs32 = rhs_fixed ? spare : fixed;
            
	    return true;
	}

    /**
     * Tries to generate operands for the 32-bit addition operation that causes
     * the integer overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add32_overflow()
    {
        return add32_overflow(false, false);
    }
    
    //**********************************************************************************************
    // ADD64 Normal 
    //**********************************************************************************************

    /**
     * Checks if the 64-bit addition operation does not cause exceptions.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 64-bit addition operation does not cause
     *         exceptions; <code>false</code> otherwise.
     */
    public static boolean check_add64_normal(long lhs, long rhs)
    {
        return !check_add64_overflow(lhs, rhs);
    }	
	
    /**
     * Checks if the 64-bit addition operation does not cause exceptions.
     * 
     * @return <code>true</code> if the 64-bit addition operation does not cause
     *         exceptions; <code>false</code> otherwise.
     */
    public static boolean check_add64_normal()
    {
        return check_add64_normal(lhs64, rhs64);
    }   

    /**
     * Tries to generate operands for the 64-bit addition operation that do not
     * cause exceptions.
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
            { return check_add64_normal(); }
    
        if(!lhs_fixed && !rhs_fixed)
            { lhs64 = Random.int64(); }
            
        long fixed = rhs_fixed ? rhs64 : lhs64;
        long spare = rhs_fixed ? lhs64 : rhs64;
        
        if(fixed >= 0)
            { spare = Random.int64_range(Long.MIN_VALUE, Long.MAX_VALUE - fixed); }
        else
            { spare = Random.int64_range(Long.MIN_VALUE - fixed, Long.MAX_VALUE); }
        
        rhs64 = rhs_fixed ? fixed : spare;
        lhs64 = rhs_fixed ? spare : fixed;
        
        return true;
	}

    /**
     * Tries to generate operands for the 64-bit addition operation that do not
     * cause exceptions.
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
     * Checks if the 64-bit addition operation causes the integer overflow
     * exception.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 64-bit addition operation causes the
     *         integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_add64_overflow(long lhs, long rhs)
    {
        if(lhs > 0 && rhs > 0)
            { return rhs > Long.MAX_VALUE - lhs; }
        
        if(lhs < 0 && rhs < 0)
            { return rhs < Long.MIN_VALUE - lhs; }
        
        return false;
    }
	
    /**
     * Checks if the 64-bit addition operation causes the integer overflow
     * exception.
     * 
     * @return <code>true</code> if the 64-bit addition operation causes the
     *         integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_add64_overflow()
    {
        return check_add64_overflow(lhs64, rhs64);
    }
    
    /**
     * Tries to generate operands for the 64-bit addition operation that causes
     * the integer overflow exception.
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
            { if((lhs64 = Random.int64()) == 0) lhs64++; }
            
        long fixed = rhs_fixed ? rhs64 : lhs64;
        long spare = rhs_fixed ? lhs64 : rhs64;
    
        if(fixed > 0)
            { spare = Random.int64_range((Long.MAX_VALUE - fixed) + 1, Long.MAX_VALUE); }
        else
            { spare = Random.int64_range(Long.MIN_VALUE, (Long.MIN_VALUE - fixed) - 1); }
        
        rhs64 = rhs_fixed ? fixed : spare;
        lhs64 = rhs_fixed ? spare : fixed;
            
        return true;
	}
	
    /**
     * Tries to generate operands for the 64-bit addition operation that causes
     * the integer overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
	public static boolean add64_overflow()
	{
	    return add64_overflow(false, false);
	}

    //**********************************************************************************************
    // ADD32x12 Normal 
    //**********************************************************************************************

    /**
     * Checks if the 32x12-bit addition operation does not cause exceptions.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 32x12-bit addition operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
	public static boolean check_add32x12_normal(int lhs, short rhs)
	{
	    return check_add32_normal(lhs, rhs);
	}

    /**
     * Checks if the 32x12-bit addition operation does not cause exceptions.
     * 
     * @return <code>true</code> if the 32x12-bit addition operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_add32x12_normal()
    {
        return check_add32_normal(lhs32, rhs12);
    }
		
    /**
     * Tries to generate operands for the 32x12-bit addition operation that do
     * not cause exceptions.
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
	public static boolean add32x12_normal(boolean lhs_fixed, boolean rhs_fixed)
	{
	    if(!lhs_fixed)
	        { lhs32 = Random.int32(); }

	    if(rhs_fixed)
	    	{ return check_add32x12_normal(); }
	    
	    if(lhs32 >= 0)
            { rhs12 = (short)Random.int32_range(IntegerType.MIN_VALUE(12), Math.min(Integer.MAX_VALUE - lhs32, IntegerType.MAX_VALUE(12))); }
        else
            { rhs12 = (short)Random.int32_range(Math.max(Integer.MIN_VALUE - lhs32, IntegerType.MIN_VALUE(12)), IntegerType.MAX_VALUE(12)); }
        
        return true;
	}
	
    /**
     * Tries to generate operands for the 32x12-bit addition operation that do
     * not cause exceptions.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add32x12_normal(boolean lhs_fixed)
    {
    	return add32x12_normal(lhs_fixed, false);
    }
	
    public static boolean add32x12_normal()
    {
        return add32x12_normal(false, false);
    }
	
	//**********************************************************************************************
	// ADD32x12 Overflow 
	//**********************************************************************************************

    /**
     * Checks if the 32x12-bit addition operation causes the integer overflow
     * exception.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 32x12-bit addition operation causes the
     *         integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_add32x12_overflow(int lhs, short rhs)
    {
        return check_add32_overflow(lhs, rhs);
    }

    /**
     * Checks if the 32x12-bit addition operation causes the integer overflow
     * exception.
     * 
     * @return <code>true</code> if the 32x12-bit addition operation causes the
     *         integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_add32x12_overflow()
    {
        return check_add32_overflow(lhs32, rhs12);
    }
        
    /**
     * Tries to generate operands for the 32x12-bit addition operation that
     * causes the integer overflow exception.
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
	public static boolean add32x12_overflow(boolean lhs_fixed, boolean rhs_fixed)
	{
	    final int max = Integer.MAX_VALUE - IntegerType.MAX_VALUE(12);
	    final int min = Integer.MIN_VALUE - IntegerType.MIN_VALUE(12);
	        
        if(lhs_fixed)
        {
            if(lhs32 >= 0 && lhs32 <= max || lhs32 < 0 && lhs32 >= min)
                { return false; }
        }
        
        if(!rhs_fixed)
        {
            if((lhs32 = Random.int32()) >= 0)
                { lhs32 = (max + lhs32 % IntegerType.MAX_VALUE(12)) + 1; }
            else
                { lhs32 = (min - -lhs32 % -IntegerType.MIN_VALUE(12)) - 1; }
        }
        else
        {
        	if(rhs12 > 0)
        		{ lhs32 = Random.int32_range(Integer.MAX_VALUE - rhs12 + 1, Integer.MAX_VALUE);	}
        	else if(rhs12 < 0)
        		{ lhs32 = Random.int32_range(Integer.MIN_VALUE, Integer.MIN_VALUE - rhs12 - 1);	}
        	else
        		{ return false;	}
        }

        if(!rhs_fixed)
        {
	        if(lhs32 >= 0)
	            { rhs12 = (short)Random.int32_range((Integer.MAX_VALUE - lhs32) + 1, IntegerType.MAX_VALUE(12)); }
	        else
	            { rhs12 = (short)Random.int32_range(IntegerType.MIN_VALUE(12), (Integer.MIN_VALUE - lhs32) - 1); }
        }
	        
	    return true;
	}

    /**
     * Tries to generate operands for the 32x12-bit addition operation that
     * causes the integer overflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add32x12_overflow(boolean lhs_fixed)
    {
    	return add32x12_overflow(lhs_fixed, false);
    }
	
    /**
     * Tries to generate operands for the 32x12-bit addition operation that
     * causes the integer overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add32x12_overflow()
    {
        return add32x12_overflow(false, false);
    }
    
    //**********************************************************************************************
    // ADD32x16 Normal 
    //**********************************************************************************************

    /**
     * Checks if the 32x16-bit addition operation does not cause exceptions.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 32x16-bit addition operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_add32x16_normal(int lhs, short rhs)
	{
	    return check_add32_normal(lhs, rhs);
	}

    /**
     * Checks if the 32x16-bit addition operation does not cause exceptions.
     * 
     * @return <code>true</code> if the 32x16-bit addition operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_add32x16_normal()
    {
        return check_add32_normal(lhs32, rhs16);
    }
	
    /**
     * Tries to generate operands for the 32x16-bit addition operation that do
     * not cause exceptions.
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
	public static boolean add32x16_normal(boolean lhs_fixed, boolean rhs_fixed)
	{
	    if(!lhs_fixed)
	        { lhs32 = Random.int32(); }

	    if(rhs_fixed)
	    	{ return check_add32x16_normal(); }
	    
	    if(lhs32 >= 0)
            { rhs16 = (short)Random.int32_range(Short.MIN_VALUE, Math.min(Integer.MAX_VALUE - lhs32, Short.MAX_VALUE)); }
        else
            { rhs16 = (short)Random.int32_range(Math.max(Integer.MIN_VALUE - lhs32, Short.MIN_VALUE), Short.MAX_VALUE); }
        
        return true;
	}
	
    /**
     * Tries to generate operands for the 32x16-bit addition operation that do
     * not cause exceptions.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     *
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add32x16_normal(boolean lhs_fixed)
    {
    	return add32x16_normal(lhs_fixed, false);
    }
	
    /**
     * Tries to generate operands for the 32x16-bit addition operation that do
     * not cause exceptions.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add32x16_normal()
    {
        return add32x16_normal(false, false);
    }
	
    //**********************************************************************************************
    // ADD32x16 Overflow 
    //**********************************************************************************************

    /**
     * Checks if the 32x16-bit addition operation causes the integer overflow
     * exception.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 32x16-bit addition operation causes the
     *         integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_add32x16_overflow(int lhs, short rhs)
    {
        return check_add32_overflow(lhs, rhs);
    }

    /**
     * Checks if the 32x16-bit addition operation causes the integer overflow
     * exception.
     * 
     * @return <code>true</code> if the 32x16-bit addition operation causes the
     *         integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_add32x16_overflow()
    {
        return check_add32_overflow(lhs32, rhs16);
    }
        
    /**
     * Tries to generate operands for the 32x16-bit addition operation that
     * causes the integer overflow exception.
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
	public static boolean add32x16_overflow(boolean lhs_fixed, boolean rhs_fixed)
	{
	    final int max = Integer.MAX_VALUE - Short.MAX_VALUE;
	    final int min = Integer.MIN_VALUE - Short.MIN_VALUE;
	        
        if(lhs_fixed)
        {
            if(lhs32 >= 0 && lhs32 <= max || lhs32 < 0 && lhs32 >= min)
                { return false; }
        }
        
        if(!lhs_fixed)
        {
        	if(!rhs_fixed)
        	{
	        	if((lhs32 = Random.int32()) >= 0)
	                { lhs32 = (max + lhs32 % Short.MAX_VALUE) + 1; }
	            else
	                { lhs32 = (min - -lhs32 % -Short.MIN_VALUE) - 1; }
        	}
        	else
        	{
            	if(rhs16 > 0)
        			{ lhs32 = Random.int32_range(Integer.MAX_VALUE - rhs16 + 1, Integer.MAX_VALUE);	}
            	else if(rhs16 < 0)
        			{ lhs32 = Random.int32_range(Integer.MIN_VALUE, Integer.MIN_VALUE - rhs16 - 1);	}
            	else
        			{ return false;	}
        	}
        }
     
        if(!rhs_fixed)
        {
	        if(lhs32 >= 0)
	            { rhs16 = (short)Random.int32_range((Integer.MAX_VALUE - lhs32) + 1, Short.MAX_VALUE); }
	        else
	            { rhs16 = (short)Random.int32_range(Short.MIN_VALUE, (Integer.MIN_VALUE - lhs32) - 1); }
        }
        
	    return true;
	}

    /**
     * Tries to generate operands for the 32x16-bit addition operation that
     * causes the integer overflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add32x16_overflow(boolean lhs_fixed)
    {
    	return add32x16_overflow(lhs_fixed, false);
    }
	
    /**
     * Tries to generate operands for the 32x16-bit addition operation that
     * causes the integer overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add32x16_overflow()
    {
        return add32x16_overflow(false, false);
    }

    //**********************************************************************************************
    // ADD32x18 Normal 
    //**********************************************************************************************

    /**
     * Checks if the 32x18-bit addition operation does not cause exceptions.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 32x18-bit addition operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_add32x18_normal(int lhs, int rhs)
	{
	    return check_add32_normal(lhs, rhs);
	}

    /**
     * Checks if the 32x18-bit addition operation does not cause exceptions.
     * 
     * @return <code>true</code> if the 32x18-bit addition operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_add32x18_normal()
    {
        return check_add32_normal(lhs32, rhs18);
    }
	
    /**
     * Tries to generate operands for the 32x18-bit addition operation that do
     * not cause exceptions.
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
	public static boolean add32x18_normal(boolean lhs_fixed, boolean rhs_fixed)
	{
	    if(!lhs_fixed)
	        { lhs32 = Random.int32(); }

	    if(rhs_fixed)
	    	{ return check_add32x18_normal(); }
	    
	    if(lhs32 >= 0)
            { rhs18 = Random.int32_range(IntegerType.MIN_VALUE(18), Math.min(Integer.MAX_VALUE - lhs32, IntegerType.MAX_VALUE(18))); }
        else
            { rhs18 = Random.int32_range(Math.max(Integer.MIN_VALUE - lhs32, IntegerType.MIN_VALUE(18)), IntegerType.MAX_VALUE(18)); }
        
        return true;
	}
	
    /**
     * Tries to generate operands for the 32x18-bit addition operation that do
     * not cause exceptions.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add32x18_normal(boolean lhs_fixed)
    {
    	return add32x18_normal(lhs_fixed, false);
    }
	
    /**
     * Tries to generate operands for the 32x18-bit addition operation that do
     * not cause exceptions.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add32x18_normal()
    {
        return add32x18_normal(false, false);
    }
	
    
    //**********************************************************************************************
    // ADD32x18 Overflow 
    //**********************************************************************************************

    /**
     * Checks if the 32x18-bit addition operation causes the integer overflow
     * exception.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 32x18-bit addition operation causes the
     *         integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_add32x18_overflow(int lhs, int rhs)
    {
        return check_add32_overflow(lhs, rhs);
    }

    /**
     * Checks if the 32x18-bit addition operation causes the integer overflow
     * exception.
     * 
     * @return <code>true</code> if the 32x18-bit addition operation causes the
     *         integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_add32x18_overflow()
    {
        return check_add32_overflow(lhs32, rhs18);
    }
        
    /**
     * Tries to generate operands for the 32x18-bit addition operation that
     * causes the integer overflow exception.
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
	public static boolean add32x18_overflow(boolean lhs_fixed, boolean rhs_fixed)
	{
	    final int max = Integer.MAX_VALUE - IntegerType.MAX_VALUE(18);
	    final int min = Integer.MIN_VALUE - IntegerType.MIN_VALUE(18);
	        
        if(lhs_fixed)
        {
            if(lhs32 >= 0 && lhs32 <= max || lhs32 < 0 && lhs32 >= min)
                { return false; }
        }
        
        if(!lhs_fixed)
        {
        	if(!rhs_fixed)
        	{
	            if((lhs32 = Random.int32()) >= 0)
	                { lhs32 = (max + lhs32 % IntegerType.MAX_VALUE(18)) + 1; }
	            else
	                { lhs32 = (min - -lhs32 % -IntegerType.MIN_VALUE(18)) - 1; }
        	}
        	else
        	{
            	if(rhs18 > 0)
    				{ lhs32 = Random.int32_range(Integer.MAX_VALUE - rhs18 + 1, Integer.MAX_VALUE);	}
            	else if(rhs18 < 0)
    				{ lhs32 = Random.int32_range(Integer.MIN_VALUE, Integer.MIN_VALUE - rhs18 - 1);	}
            	else
    				{ return false;	}
        	}
        }
     
        if(!rhs_fixed)
        {
	        if(lhs32 >= 0)
	            { rhs18 = Random.int32_range((Integer.MAX_VALUE - lhs32) + 1, IntegerType.MAX_VALUE(18)); }
	        else
	            { rhs18 = Random.int32_range(IntegerType.MIN_VALUE(18), (Integer.MIN_VALUE - lhs32) - 1); }
        }
        
	    return true;
	}

    /**
     * Tries to generate operands for the 32x18-bit addition operation that
     * causes the integer overflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add32x18_overflow(boolean lhs_fixed)
    {
    	return add32x18_overflow(lhs_fixed, false);
    }
	
    /**
     * Tries to generate operands for the 32x18-bit addition operation that
     * causes the integer overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add32x18_overflow()
    {
        return add32x18_overflow(false, false);
    }
    
    //**********************************************************************************************
    // ADD64x12 Normal 
    //**********************************************************************************************	

    /**
     * Checks if the 64x12-bit addition operation does not cause exceptions.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 64x12-bit addition operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_add64x12_normal(long lhs, short rhs)
    {
        return check_add64_normal(lhs, rhs);
    }

    /**
     * Checks if the 64x12-bit addition operation does not cause exceptions.
     * 
     * @return <code>true</code> if the 64x12-bit addition operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_add64x12_normal()
    {
        return check_add64_normal(lhs64, rhs12);
    }
        
    /**
     * Tries to generate operands for the 64x12-bit addition operation that do
     * not cause exceptions.
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
    public static boolean add64x12_normal(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(!lhs_fixed)
            { lhs64 = Random.int64(); }
	    
        if(rhs_fixed)
    		{ return check_add64x12_normal(); }
        
        if(lhs64 >= 0)
            { rhs12 = (short)Random.int64_range(IntegerType.MIN_VALUE(12), Math.min(Long.MAX_VALUE - lhs64, IntegerType.MAX_VALUE(12))); }
        else
            { rhs12 = (short)Random.int64_range(Math.max(Long.MIN_VALUE - lhs64, IntegerType.MIN_VALUE(12)), IntegerType.MAX_VALUE(12)); }
        
        return true;
    }
    
    /**
     * Tries to generate operands for the 64x12-bit addition operation that do
     * not cause exceptions.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add64x12_normal(boolean lhs_fixed)
    {
        return add64x12_normal(lhs_fixed, false);
    }
    
    /**
     * Tries to generate operands for the 64x12-bit addition operation that do
     * not cause exceptions.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add64x12_normal()
    {
        return add64x12_normal(false, false);
    }
    
    //**********************************************************************************************
    // ADD64x12 Overflow 
    //**********************************************************************************************    

    /**
     * Checks if the 64x12-bit addition operation causes the integer overflow
     * exception.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 64x12-bit addition operation causes the
     *         integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_add64x12_overflow(long lhs, short rhs)
    {
        return check_add64_overflow(lhs, rhs);
    }

    /**
     * Checks if the 64x12-bit addition operation causes the integer overflow
     * exception.
     * 
     * @return <code>true</code> if the 64x12-bit addition operation causes the
     *         integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_add64x12_overflow()
    {
        return check_add64_overflow(lhs64, rhs12);
    }
    
    /**
     * Tries to generate operands for the 64x12-bit addition operation that
     * causes the integer overflow exception.
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
    public static boolean add64x12_overflow(boolean lhs_fixed, boolean rhs_fixed)
    {
        final long max = Long.MAX_VALUE - IntegerType.MAX_VALUE(12);
        final long min = Long.MIN_VALUE - IntegerType.MIN_VALUE(12);
            
        if(lhs_fixed)
        {
            if(lhs64 >= 0 && lhs64 <= max || lhs64 < 0 && lhs64 >= min)
                { return false; }
        }
        
        if(!lhs_fixed)
        {
        	if(!rhs_fixed)
        	{
	            if((lhs64 = Random.int64()) >= 0)
	                { lhs64 = (max + lhs64 % IntegerType.MAX_VALUE(12)) + 1; }
	            else
	                { lhs64 = (min - -lhs64 % -IntegerType.MIN_VALUE(12)) - 1; }
        	}
        	else
        	{
            	if(rhs12 > 0)
    				{ lhs64 = Random.int64_range(Long.MAX_VALUE - rhs12 + 1, Long.MAX_VALUE); }
            	else if(rhs12 < 0)
    				{ lhs64 = Random.int64_range(Long.MIN_VALUE, Long.MIN_VALUE - rhs12 - 1); }
            	else
    				{ return false;	}
        	}
        }
        
        if(!rhs_fixed)
        {
	        if(lhs64 >= 0)
	            { rhs12 = (short)Random.int64_range((Long.MAX_VALUE - lhs64) + 1, IntegerType.MAX_VALUE(12)); }
	        else
	            { rhs12 = (short)Random.int64_range(IntegerType.MIN_VALUE(12), (Long.MIN_VALUE - lhs64) - 1); }
        }
        
        return true;
    }
    
    /**
     * Tries to generate operands for the 64x12-bit addition operation that
     * causes the integer overflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add64x12_overflow(boolean lhs_fixed)
    {
    	return add64x12_overflow(lhs_fixed, false);
    }
    
    /**
     * Tries to generate operands for the 64x12-bit addition operation that
     * causes the integer overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add64x12_overflow()
    {
        return add64x12_overflow(false, false);
    }
    
    //**********************************************************************************************
    // ADD64x16 Normal 
    //**********************************************************************************************	

    /**
     * Checks if the 64x16-bit addition operation does not cause exceptions.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 64x16-bit addition operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_add64x16_normal(long lhs, short rhs)
    {
        return check_add64_normal(lhs, rhs);
    }

    /**
     * Checks if the 64x16-bit addition operation does not cause exceptions.
     * 
     * @return <code>true</code> if the 64x16-bit addition operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_add64x16_normal()
    {
        return check_add64_normal(lhs64, rhs16);
    }
        
    /**
     * Tries to generate operands for the 64x16-bit addition operation that do
     * not cause exceptions.
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
    public static boolean add64x16_normal(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(!lhs_fixed)
            { lhs64 = Random.int64(); }

        if(rhs_fixed)
        	{ return check_add64x16_normal(); }
        
        if(lhs64 >= 0)
            { rhs16 = (short)Random.int64_range(Short.MIN_VALUE, Math.min(Long.MAX_VALUE - lhs64, Short.MAX_VALUE)); }
        else
            { rhs16 = (short)Random.int64_range(Math.max(Long.MIN_VALUE - lhs64, Short.MIN_VALUE), Short.MAX_VALUE); }
        
        return true;
    }
    
    /**
     * Tries to generate operands for the 64x16-bit addition operation that do
     * not cause exceptions.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add64x16_normal(boolean lhs_fixed)
    {
    	return add64x16_normal(lhs_fixed, false);
    }
    
    /**
     * Tries to generate operands for the 64x16-bit addition operation that do
     * not cause exceptions.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add64x16_normal()
    {
        return add64x16_normal(false, false);
    }

    //**********************************************************************************************
    // ADD64x16 Overflow 
    //**********************************************************************************************    

    /**
     * Checks if the 64x16-bit addition operation causes the integer overflow
     * exception.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 64x16-bit addition operation causes the
     *         integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_add64x16_overflow(long lhs, short rhs)
    {
        return check_add64_overflow(lhs, rhs);
    }

    /**
     * Checks if the 64x16-bit addition operation causes the integer overflow
     * exception.
     * 
     * @return <code>true</code> if the 64x16-bit addition operation causes the
     *         integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_add64x16_overflow()
    {
        return check_add64_overflow(lhs64, rhs16);
    }
    
    /**
     * Tries to generate operands for the 64x16-bit addition operation that
     * causes the integer overflow exception.
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
    public static boolean add64x16_overflow(boolean lhs_fixed, boolean rhs_fixed)
    {
        final long max = Long.MAX_VALUE - Short.MAX_VALUE;
        final long min = Long.MIN_VALUE - Short.MIN_VALUE;
            
        if(lhs_fixed)
        {
            if(lhs64 >= 0 && lhs64 <= max || lhs64 < 0 && lhs64 >= min)
                { return false; }
        }
        else
        {
            if(!rhs_fixed)
            {
	        	if((lhs64 = Random.int64()) >= 0)
	                { lhs64 = (max + lhs64 % Short.MAX_VALUE) + 1; }
	            else
	                { lhs64 = (min - -lhs64 % -Short.MIN_VALUE) - 1; }
            }
            else
            {
            	if(rhs16 > 0)
					{ lhs64 = Random.int64_range(Long.MAX_VALUE - rhs16 + 1, Long.MAX_VALUE); }
            	else if(rhs16 < 0)
					{ lhs64 = Random.int64_range(Long.MIN_VALUE, Long.MIN_VALUE - rhs16 - 1); }
            	else
					{ return false;	}
            }
        }
        
        if(!rhs_fixed)
        {
	        if(lhs64 >= 0)
	            { rhs16 = (short)Random.int64_range((Long.MAX_VALUE - lhs64) + 1, Short.MAX_VALUE); }
	        else
	            { rhs16 = (short)Random.int64_range(Short.MIN_VALUE, (Long.MIN_VALUE - lhs64) - 1); }
        }

        return true;
    }
    
    /**
     * Tries to generate operands for the 64x16-bit addition operation that
     * causes the integer overflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add64x16_overflow(boolean lhs_fixed)
    {
    	return add64x16_overflow(lhs_fixed, false);
    }
    
    /**
     * Tries to generate operands for the 64x16-bit addition operation that
     * causes the integer overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add64x16_overflow()
    {
        return add64x16_overflow(false, false);
    }

    //**********************************************************************************************
    // ADD64x18 Normal 
    //**********************************************************************************************	

    /**
     * Checks if the 64x18-bit addition operation does not cause exceptions.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 64x18-bit addition operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_add64x18_normal(long lhs, int rhs)
    {
        return check_add64_normal(lhs, rhs);
    }

    /**
     * Checks if the 64x18-bit addition operation does not cause exceptions.
     * 
     * @return <code>true</code> if the 64x18-bit addition operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_add64x18_normal()
    {
        return check_add64_normal(lhs64, rhs18);
    }
        
    /**
     * Tries to generate operands for the 64x18-bit addition operation that do
     * not cause exceptions.
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
    public static boolean add64x18_normal(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(!lhs_fixed)
            { lhs64 = Random.int64(); }

        if(rhs_fixed)
        	{ return check_add64x18_normal(); }
        
        if(lhs64 >= 0)
            { rhs18 = (int)Random.int64_range(IntegerType.MIN_VALUE(18), Math.min(Long.MAX_VALUE - lhs64, IntegerType.MAX_VALUE(18))); }
        else
            { rhs18 = (int)Random.int64_range(Math.max(Long.MIN_VALUE - lhs64, IntegerType.MIN_VALUE(18)), IntegerType.MAX_VALUE(18)); }
        
        return true;
    }
    
    /**
     * Tries to generate operands for the 64x18-bit addition operation that do
     * not cause exceptions.
     * 
     * @param  <code>rhs_fixed</code> indicates if the right-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add64x18_normal(boolean lhs_fixed)
    {
    	return add64x18_normal(lhs_fixed, false);
    }
    
    /**
     * Tries to generate operands for the 64x18-bit addition operation that do
     * not cause exceptions.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add64x18_normal()
    {
        return add64x18_normal(false, false);
    }

    //**********************************************************************************************
    // ADD64x18 Overflow 
    //**********************************************************************************************    

    /**
     * Checks if the 64x18-bit addition operation causes the integer overflow
     * exception.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 64x18-bit addition operation causes the
     *         integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_add64x18_overflow(long lhs, int rhs)
    {
        return check_add64_overflow(lhs, rhs);
    }

    /**
     * Checks if the 64x18-bit addition operation causes the integer overflow
     * exception.
     * 
     * @return <code>true</code> if the 64x18-bit addition operation causes the
     *         integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_add64x18_overflow()
    {
        return check_add64_overflow(lhs64, rhs18);
    }
    
    /**
     * Tries to generate operands for the 64x18-bit addition operation that
     * causes the integer overflow exception.
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
    public static boolean add64x18_overflow(boolean lhs_fixed, boolean rhs_fixed)
    {
        final long max = Long.MAX_VALUE - IntegerType.MAX_VALUE(18);
        final long min = Long.MIN_VALUE - IntegerType.MIN_VALUE(18);
            
        if(lhs_fixed)
        {
            if(lhs64 >= 0 && lhs64 <= max || lhs64 < 0 && lhs64 >= min)
                { return false; }
        }
        else
        {
        	if(!rhs_fixed)
        	{
	            if((lhs64 = Random.int64()) >= 0)
	                { lhs64 = (max + lhs64 % IntegerType.MAX_VALUE(18)) + 1; }
	            else
	                { lhs64 = (min - -lhs64 % -IntegerType.MIN_VALUE(18)) - 1; }
        	}
        	else
        	{
            	if(rhs18 > 0)
					{ lhs64 = Random.int64_range(Long.MAX_VALUE - rhs18 + 1, Long.MAX_VALUE); }
            	else if(rhs18 < 0)
					{ lhs64 = Random.int64_range(Long.MIN_VALUE, Long.MIN_VALUE - rhs18 - 1); }
            	else
					{ return false;	}
        	}
        }
        
        if(!rhs_fixed)
        {
	        if(lhs64 >= 0)
	            { rhs18 = (short)Random.int64_range((Long.MAX_VALUE - lhs64) + 1, IntegerType.MAX_VALUE(18)); }
	        else
	            { rhs18 = (short)Random.int64_range(IntegerType.MIN_VALUE(18), (Long.MIN_VALUE - lhs64) - 1); }
        }
        
        return true;
    }
    
    /**
     * Tries to generate operands for the 64x18-bit addition operation that
     * causes the integer overflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add64x18_overflow(boolean lhs_fixed)
    {
    	return add64x18_overflow(lhs_fixed, false);
    }
    
    /**
     * Tries to generate operands for the 64x18-bit addition operation that
     * causes the integer overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean add64x18_overflow()
    {
        return add64x18_overflow(false, false);
    }

    //**********************************************************************************************
    // SUB32 Normal 
    //**********************************************************************************************    

    /**
     * Checks if the 32-bit subtraction operation does not cause exceptions.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 32-bit subtraction operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_sub32_normal(int lhs, int rhs)
    {
        return !check_sub32_overflow(lhs, rhs);
    }

    /**
     * Checks if the 32-bit subtraction operation does not cause exceptions.
     * 
     * @return <code>true</code> if the 32-bit subtraction operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_sub32_normal()
    {
        return check_sub32_normal(lhs32, rhs32);
    }
    
    /**
     * Tries to generate operands for the 32-bit subtraction operation that do
     * not cause exceptions.
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
        if(lhs_fixed && rhs_fixed)
            { return check_sub32_normal(); }

        if(!lhs_fixed && !rhs_fixed)
            { lhs32 = Random.int32(); }
        
        if(!rhs_fixed)
        {
            if(lhs32 >= 0)
                { rhs32 = Random.int32_range((Integer.MIN_VALUE + lhs32) + 1, Integer.MAX_VALUE); }
            else
                { rhs32 = Random.int32_range(Integer.MIN_VALUE, (Integer.MAX_VALUE + lhs32) + 1); }
        }
        else
        {
            if(rhs32 >= 0)
                { lhs32 = Random.int32_range(Integer.MIN_VALUE + rhs32, Integer.MAX_VALUE); }
            else
                { lhs32 = Random.int32_range(Integer.MIN_VALUE, Integer.MAX_VALUE + rhs32); }
        }
        
        return true;
    }

    /**
     * Tries to generate operands for the 32-bit subtraction operation that do
     * not cause exceptions.
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
     * Checks if the 32-bit subtraction operation causes the integer overflow
     * exception.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 32-bit subtraction operation causes the
     *         integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_sub32_overflow(int lhs, int rhs)
    {
        if(rhs == Integer.MIN_VALUE)
            { return lhs >= 0; }
        
        if(lhs == -1)
            { return false; }
        
        return check_add32_overflow(lhs, -rhs);
    }

    /**
     * Checks if the 32-bit subtraction operation causes the integer overflow
     * exception.
     * 
     * @return <code>true</code> if the 32-bit subtraction operation causes the
     *         integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_sub32_overflow()
    {
        return check_sub32_overflow(lhs32, rhs32);
    }
    
    /**
     * Tries to generate operands for the 32-bit subtraction operation that
     * causes the integer overflow exception.
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
        if(lhs_fixed && rhs_fixed)
            { return check_sub32_overflow(); }

        if(lhs_fixed && lhs32 == -1 || rhs_fixed && rhs32 == 0)
            { return false; }

        if(!lhs_fixed && !rhs_fixed)
            { if((lhs32 = Random.int32()) == -1) lhs32++; }
            
        if(!rhs_fixed)
        {
            if(lhs32 >= 0)
                { rhs32 = Random.int32_range(Integer.MIN_VALUE, Integer.MIN_VALUE + lhs32); }
            else
                { rhs32 = Random.int32_range((Integer.MAX_VALUE + lhs32) + 1, Integer.MAX_VALUE); }
        }
        else
        {
            if(rhs32 > 0)
                { lhs32 = Random.int32_range(Integer.MIN_VALUE, (Integer.MIN_VALUE + rhs32) - 1); }
            else
                { lhs32 = Random.int32_range((Integer.MAX_VALUE + rhs32) + 1, Integer.MAX_VALUE); }
        }
                
        return true;
    }
    
    /**
     * Tries to generate operands for the 32-bit subtraction operation that
     * causes the integer overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub32_overflow()
    {
        return sub32_overflow(false, false);
    }

    //**********************************************************************************************
    // SUB64 Normal 
    //**********************************************************************************************    

    /**
     * Checks if the 64-bit subtraction operation does not cause exceptions.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 64-bit subtraction operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_sub64_normal(long lhs, long rhs)
    {
        return !check_sub64_overflow(lhs, rhs);
    }

    /**
     * Checks if the 64-bit subtraction operation does not cause exceptions.
     * 
     * @return <code>true</code> if the 64-bit subtraction operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_sub64_normal()
    {
        return check_sub64_normal(lhs64, rhs64);
    }
        
    /**
     * Tries to generate operands for the 64-bit subtraction operation that do
     * not cause exceptions.
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
        if(lhs_fixed && rhs_fixed)
            { return check_sub64_normal(); }
    
        if(!lhs_fixed && !rhs_fixed)
            { lhs64 = Random.int64(); }
    
        if(!rhs_fixed)
        {
            if(lhs64 >= 0)
                { rhs64 = Random.int64_range((Long.MIN_VALUE + lhs64) + 1, Long.MAX_VALUE); }
            else
                { rhs64 = Random.int64_range(Long.MIN_VALUE, (Long.MAX_VALUE + lhs64) + 1); }
        }
        else
        {
            if(rhs64 >= 0)
                { lhs64 = Random.int64_range(Long.MIN_VALUE + rhs64, Long.MAX_VALUE); }
            else
                { lhs64 = Random.int64_range(Long.MIN_VALUE, Long.MAX_VALUE + rhs64); }
        }
        
        return true;
    }
    
    /**
     * Tries to generate operands for the 64-bit subtraction operation that do
     * not cause exceptions.
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
     * Checks if the 64-bit subtraction operation causes the integer overflow
     * exception.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 64-bit subtraction operation causes the
     *         integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_sub64_overflow(long lhs, long rhs)
    {
        if(rhs == Long.MIN_VALUE)
            { return lhs >= 0; }
        
        if(lhs == -1)
            { return false; }
        
        return check_add64_overflow(lhs, -rhs);
    }

    /**
     * Checks if the 64-bit subtraction operation causes the integer overflow
     * exception.
     * 
     * @return <code>true</code> if the 64-bit subtraction operation causes the
     *         integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_sub64_overflow()
    {
        return check_sub64_overflow(lhs64, rhs64);
    }
        
    /**
     * Tries to generate operands for the 64-bit subtraction operation that
     * causes the integer overflow exception.
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
        if(lhs_fixed && rhs_fixed)
            { return check_sub64_overflow(); }
    
        if(lhs_fixed && lhs64 == -1 || rhs_fixed && rhs64 == 0)
            { return false; }
    
        if(!lhs_fixed && !rhs_fixed)
            { if((lhs64 = Random.int64()) == -1) lhs64++; }
            
        if(!rhs_fixed)
        {
            if(lhs64 >= 0)
                { rhs64 = Random.int64_range(Long.MIN_VALUE, Long.MIN_VALUE + lhs64); }
            else
                { rhs64 = Random.int64_range((Long.MAX_VALUE + lhs64) + 1, Long.MAX_VALUE); }
        }
        else
        {
            if(rhs64 > 0)
                { lhs64 = Random.int64_range(Long.MIN_VALUE, (Long.MIN_VALUE + rhs64) - 1); }
            else
                { lhs64 = Random.int64_range((Long.MAX_VALUE + rhs64) + 1, Long.MAX_VALUE); }
        }
                
        return true;
    }
    
    /**
     * Tries to generate operands for the 64-bit subtraction operation that
     * causes the integer overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub64_overflow()
    {
        return sub64_overflow(false, false);
    }

    //**********************************************************************************************
    // SUB32x12 Normal 
    //**********************************************************************************************    

    /**
     * Checks if the 32x12-bit subtraction operation does not cause exceptions.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 32x12-bit subtraction operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_sub32x12_normal(int lhs, int rhs)
    {
        return check_sub32_normal(lhs, rhs);
    }

    /**
     * Checks if the 32x12-bit subtraction operation does not cause exceptions.
     * 
     * @return <code>true</code> if the 32x12-bit subtraction operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_sub32x12_normal()
    {
        return check_sub32x12_normal(lhs32, rhs12);
    }
        
    /**
     * Tries to generate operands for the 32x12-bit subtraction operation that
     * do not cause exceptions.
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
    public static boolean sub32x12_normal(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(!lhs_fixed)
            { lhs32 = Random.int32(); }
        
	    if(rhs_fixed)
    		{ return check_sub32x12_normal(); }
	    
        if(lhs32 >= 0)
            { rhs12 = (short)Random.int32_range(Math.max(Integer.MIN_VALUE + lhs32 + 1, IntegerType.MIN_VALUE(12)), IntegerType.MAX_VALUE(12)); }
        else
            { rhs12 = (short)Random.int32_range(IntegerType.MIN_VALUE(12), Math.min(Integer.MAX_VALUE + lhs32 + 1, IntegerType.MAX_VALUE(12))); }
    
        return true;
    }

    /**
     * Tries to generate operands for the 32x12-bit subtraction operation that
     * do not cause exceptions.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub32x12_normal(boolean lhs_fixed)
    {
        return sub32x12_normal(lhs_fixed, false);
    }
    
    /**
     * Tries to generate operands for the 32x12-bit subtraction operation that
     * do not cause exceptions.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub32x12_normal()
    {
        return sub32x12_normal(false, false);
    }
    
    //**********************************************************************************************
    // SUB32x12 Overflow 
    //**********************************************************************************************    

    /**
     * Checks if the 32x12-bit subtraction operation causes the integer overflow
     * exception.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 32x12-bit subtraction operation causes
     *         the integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_sub32x12_overflow(int lhs, int rhs)
    {
        return check_sub32_overflow(lhs, rhs);
    }

    /**
     * Checks if the 32x12-bit subtraction operation causes the integer overflow
     * exception.
     * 
     * @return <code>true</code> if the 32x12-bit subtraction operation causes
     *         the integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_sub32x12_overflow()
    {
        return check_sub32x12_overflow(lhs32, rhs12);
    }

    /**
     * Tries to generate operands for the 32x12-bit subtraction operation that
     * causes the integer overflow exception.
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
    public static boolean sub32x12_overflow(boolean lhs_fixed, boolean rhs_fixed)
    {
        final int max = Integer.MAX_VALUE + IntegerType.MIN_VALUE(12);
        final int min = Integer.MIN_VALUE + IntegerType.MAX_VALUE(12);
            
        if(lhs_fixed)
        {
            if(lhs32 >= 0 && lhs32 <= max || lhs32 < 0 && lhs32 >= min)
                { return false; }
        }
        
        if(!lhs_fixed)
        {
        	if(!rhs_fixed)
        	{
	            if((lhs32 = Random.int32()) >= 0)
	                { lhs32 = (max + lhs32 % -IntegerType.MIN_VALUE(12)) + 1; }
	            else
	                { lhs32 = (min - -lhs32 % IntegerType.MAX_VALUE(12)) - 1; }
        	}
        	else
        	{
            	if(rhs12 < 0)
        			{ lhs32 = Random.int32_range(Integer.MAX_VALUE + rhs12 + 1, Integer.MAX_VALUE); }
            	else if(rhs12 > 0)
        			{ lhs32 = Random.int32_range(Integer.MIN_VALUE, Integer.MIN_VALUE + rhs12 - 1); }
            	else
        			{ return false;	}
        	}
        }

        if(!rhs_fixed)
        {
	        if(lhs32 >= 0)
	            { rhs12 = (short)Random.int32_range(IntegerType.MIN_VALUE(12), Math.min(Integer.MIN_VALUE + lhs32, IntegerType.MAX_VALUE(12))); }
	        else
	            { rhs12 = (short)Random.int32_range(Math.max(IntegerType.MIN_VALUE(12), (Integer.MAX_VALUE + lhs32) + 1), IntegerType.MAX_VALUE(12)); }
        }
	        
        return true;
    }

    /**
     * Tries to generate operands for the 32x12-bit subtraction operation that
     * causes the integer overflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub32x12_overflow(boolean lhs_fixed)
    {
        return sub32x12_overflow(lhs_fixed, false);
    }
    
    /**
     * Tries to generate operands for the 32x12-bit subtraction operation that
     * causes the integer overflow exception.
     *
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub32x12_overflow()
    {
        return sub32x12_overflow(false, false);
    }
    
    //**********************************************************************************************
    // SUB32x16 Normal 
    //**********************************************************************************************    

    /**
     * Checks if the 32x16-bit subtraction operation does not cause exceptions.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 32x16-bit subtraction operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_sub32x16_normal(int lhs, short rhs)
    {
        return check_sub32_normal(lhs, rhs);
    }

    /**
     * Checks if the 32x16-bit subtraction operation does not cause exceptions.
     * 
     * @return <code>true</code> if the 32x16-bit subtraction operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_sub32x16_normal()
    {
        return check_sub32x16_normal(lhs32, rhs16);
    }
        
    /**
     * Tries to generate operands for the 32x16-bit subtraction operation that
     * do not cause exceptions.
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
    public static boolean sub32x16_normal(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(!lhs_fixed)
            { lhs32 = Random.int32(); }

        if(rhs_fixed)
        	{ return check_sub32x16_normal(); }
        
        if(lhs32 >= 0)
            { rhs16 = (short)Random.int32_range(Math.max(Integer.MIN_VALUE + lhs32 + 1, Short.MIN_VALUE), Short.MAX_VALUE); }
        else
            { rhs16 = (short)Random.int32_range(Short.MIN_VALUE, Math.min(Integer.MAX_VALUE + lhs32 + 1, Short.MAX_VALUE)); }
    
        return true;
    }

    /**
     * Tries to generate operands for the 32x16-bit subtraction operation that
     * do not cause exceptions.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub32x16_normal(boolean lhs_fixed)
    {
        return sub32x16_normal(lhs_fixed, false);
    }
    
    /**
     * Tries to generate operands for the 32x16-bit subtraction operation that
     * do not cause exceptions.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub32x16_normal()
    {
        return sub32x16_normal(false, false);
    }

    //**********************************************************************************************
    // SUB32x16 Overflow 
    //**********************************************************************************************    

    /**
     * Checks if the 32x16-bit subtraction operation causes the integer overflow
     * exception.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 32x16-bit subtraction operation causes
     *         the integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_sub32x16_overflow(int lhs, short rhs)
    {
        return check_sub32_overflow(lhs, rhs);
    }

    /**
     * Checks if the 32x16-bit subtraction operation causes the integer overflow
     * exception.
     * 
     * @return <code>true</code> if the 32x16-bit subtraction operation causes
     *         the integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_sub32x16_overflow()
    {
        return check_sub32x16_overflow(lhs32, rhs16);
    }

    /**
     * Tries to generate operands for the 32x16-bit subtraction operation that
     * causes the integer overflow exception.
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
    public static boolean sub32x16_overflow(boolean lhs_fixed, boolean rhs_fixed)
    {
        final int max = Integer.MAX_VALUE + Short.MIN_VALUE;
        final int min = Integer.MIN_VALUE + Short.MAX_VALUE;
            
        if(lhs_fixed)
        {
            if(lhs32 >= 0 && lhs32 <= max || lhs32 < 0 && lhs32 >= min)
                { return false; }
        }
        
        if(!lhs_fixed)
        {
        	if(!rhs_fixed)
        	{
	            if((lhs32 = Random.int32()) >= 0)
	                { lhs32 = (max + lhs32 % -Short.MIN_VALUE) + 1; }
	            else
	                { lhs32 = (min - -lhs32 % Short.MAX_VALUE) - 1; }
        	}
        	else
        	{
            	if(rhs16 < 0)
        			{ lhs32 = Random.int32_range(Integer.MAX_VALUE + rhs16 + 1, Integer.MAX_VALUE); }
            	else if(rhs16 > 0)
        			{ lhs32 = Random.int32_range(Integer.MIN_VALUE, Integer.MIN_VALUE + rhs16 - 1); }
            	else
        			{ return false;	}
        	}
        }

        if(!rhs_fixed)
        {
	        if(lhs32 >= 0)
	            { rhs16 = (short)Random.int32_range(Short.MIN_VALUE, Math.min(Integer.MIN_VALUE + lhs32, Short.MAX_VALUE)); }
	        else
	            { rhs16 = (short)Random.int32_range(Math.max(Short.MIN_VALUE, (Integer.MAX_VALUE + lhs32) + 1), Short.MAX_VALUE); }
        }
        
        return true;
    }

    /**
     * Tries to generate operands for the 32x16-bit subtraction operation that
     * causes the integer overflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub32x16_overflow(boolean lhs_fixed)
    {
    	return sub32x16_overflow(lhs_fixed, false);
    }
    
    /**
     * Tries to generate operands for the 32x16-bit subtraction operation that
     * causes the integer overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub32x16_overflow()
    {
        return sub32x16_overflow(false, false);
    }

    //**********************************************************************************************
    // SUB32x18 Normal 
    //**********************************************************************************************    

    /**
     * Checks if the 32x18-bit subtraction operation does not cause exceptions.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 32x18-bit subtraction operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_sub32x18_normal(int lhs, int rhs)
    {
        return check_sub32_normal(lhs, rhs);
    }

    /**
     * Checks if the 32x18-bit subtraction operation does not cause exceptions.
     * 
     * @return <code>true</code> if the 32x18-bit subtraction operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_sub32x18_normal()
    {
        return check_sub32x18_normal(lhs32, rhs18);
    }
        
    /**
     * Tries to generate operands for the 32x18-bit subtraction operation that
     * do not cause exceptions.
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
    public static boolean sub32x18_normal(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(!lhs_fixed)
            { lhs32 = Random.int32(); }

        if(rhs_fixed)
        	{ return check_sub32x18_normal(); }
        
        if(lhs32 >= 0)
            { rhs18 = Random.int32_range(Math.max(Integer.MIN_VALUE + lhs32 + 1, IntegerType.MIN_VALUE(18)), IntegerType.MAX_VALUE(18)); }
        else
            { rhs18 = Random.int32_range(IntegerType.MIN_VALUE(18), Math.min(Integer.MAX_VALUE + lhs32 + 1, IntegerType.MAX_VALUE(18))); }
    
        return true;
    }

    /**
     * Tries to generate operands for the 32x18-bit subtraction operation that
     * do not cause exceptions.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub32x18_normal(boolean lhs_fixed)
    {
        return sub32x18_normal(lhs_fixed, false);
    }
    
    /**
     * Tries to generate operands for the 32x18-bit subtraction operation that
     * do not cause exceptions.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub32x18_normal()
    {
        return sub32x18_normal(false, false);
    }

    //**********************************************************************************************
    // SUB32x18 Overflow 
    //**********************************************************************************************    

    /**
     * Checks if the 32x18-bit subtraction operation causes the integer overflow
     * exception.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 32x18-bit subtraction operation causes
     *         the integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_sub32x18_overflow(int lhs, int rhs)
    {
        return check_sub32_overflow(lhs, rhs);
    }

    /**
     * Checks if the 32x18-bit subtraction operation causes the integer overflow
     * exception.
     * 
     * @return <code>true</code> if the 32x18-bit subtraction operation causes
     *         the integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_sub32x18_overflow()
    {
        return check_sub32x18_overflow(lhs32, rhs18);
    }

    /**
     * Tries to generate operands for the 32x18-bit subtraction operation that
     * causes the integer overflow exception.
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
    public static boolean sub32x18_overflow(boolean lhs_fixed, boolean rhs_fixed)
    {
        final int max = Integer.MAX_VALUE + IntegerType.MIN_VALUE(18);
        final int min = Integer.MIN_VALUE + IntegerType.MAX_VALUE(18);
        
        if(lhs_fixed)
        {
            if(lhs32 >= 0 && lhs32 <= max || lhs32 < 0 && lhs32 >= min)
                { return false; }
        }
        
        if(!lhs_fixed)
        {
            if(!rhs_fixed)
            {
	        	if((lhs32 = Random.int32()) >= 0)
	                { lhs32 = (max + lhs32 % -IntegerType.MIN_VALUE(18)) + 1; }
	            else
	                { lhs32 = (min - -lhs32 % IntegerType.MAX_VALUE(18)) - 1; }
            }
            else
            {
            	if(rhs18 < 0)
            		{ lhs32 = Random.int32_range(Integer.MAX_VALUE + rhs18 + 1, Integer.MAX_VALUE); }
            	else if(rhs18 > 0)
            		{ lhs32 = Random.int32_range(Integer.MIN_VALUE, Integer.MIN_VALUE + rhs18 - 1); }
            	else
            		{ return false;	}
            }
        }
        
        if(!rhs_fixed)
        {
            if(lhs32 >= 0)
        		{ rhs18 = Random.int32_range(IntegerType.MIN_VALUE(18), Math.min(Integer.MIN_VALUE + lhs32, IntegerType.MAX_VALUE(18))); }
            else
        		{ rhs18 = Random.int32_range(Math.max(IntegerType.MIN_VALUE(18), (Integer.MAX_VALUE + lhs32) + 1), IntegerType.MAX_VALUE(18)); }
        }

        return true;
    }

    /**
     * Tries to generate operands for the 32x18-bit subtraction operation that
     * causes the integer overflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub32x18_overflow(boolean lhs_fixed)
    {
    	return sub32x18_overflow(lhs_fixed, false);
    }
    
    /**
     * Tries to generate operands for the 32x18-bit subtraction operation that
     * causes the integer overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub32x18_overflow()
    {
        return sub32x18_overflow(false, false);
    }
    
    //**********************************************************************************************
    // SUB64x12 Normal 
    //**********************************************************************************************    

    /**
     * Checks if the 64x12-bit subtraction operation does not cause exceptions.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 64x12-bit subtraction operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_sub64x12_normal(long lhs, int rhs)
    {
        return check_sub64_normal(lhs, rhs);
    }

    /**
     * Checks if the 64x12-bit subtraction operation does not cause exceptions.
     * 
     * @return <code>true</code> if the 64x12-bit subtraction operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_sub64x12_normal()
    {
        return check_sub64x12_normal(lhs64, rhs12);
    }
        
    /**
     * Tries to generate operands for the 64x12-bit subtraction operation that
     * do not cause exceptions.
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
    public static boolean sub64x12_normal(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(!lhs_fixed)
            { lhs64 = Random.int64(); }
    
	    if(rhs_fixed)
    		{ return check_add64x12_normal(); }
        
        if(lhs64 >= 0)
            { rhs12 = (short)Random.int64_range(Math.max(Long.MIN_VALUE + lhs64 + 1, IntegerType.MIN_VALUE(12)), IntegerType.MAX_VALUE(12)); }
        else
            { rhs12 = (short)Random.int64_range(IntegerType.MIN_VALUE(12), Math.min(Long.MAX_VALUE + lhs64 + 1, IntegerType.MAX_VALUE(12))); }
    
        return true;
    }

    /**
     * Tries to generate operands for the 64x12-bit subtraction operation that
     * do not cause exceptions.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub64x12_normal(boolean lhs_fixed)
    {
        return sub64x12_normal(lhs_fixed, false);
    }
    
    /**
     * Tries to generate operands for the 64x12-bit subtraction operation that
     * do not cause exceptions.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub64x12_normal()
    {
        return sub64x12_normal(false, false);
    }

    //**********************************************************************************************
    // SUB64x12 Overflow 
    //**********************************************************************************************    

    /**
     * Checks if the 64x12-bit subtraction operation causes the integer overflow
     * exception.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 64x12-bit subtraction operation causes
     *         the integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_sub64x12_overflow(long lhs, int rhs)
    {
        return check_sub64_overflow(lhs, rhs);
    }

    /**
     * Checks if the 64x12-bit subtraction operation causes the integer overflow
     * exception.
     * 
     * @return <code>true</code> if the 64x12-bit subtraction operation causes
     *         the integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_sub64x12_overflow()
    {
        return check_sub64x12_overflow(lhs64, rhs12);
    }
        
    /**
     * Tries to generate operands for the 64x12-bit subtraction operation that
     * causes the integer overflow exception.
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
    public static boolean sub64x12_overflow(boolean lhs_fixed, boolean rhs_fixed)
    {
        final long max = Long.MAX_VALUE + IntegerType.MIN_VALUE(12);
        final long min = Long.MIN_VALUE + IntegerType.MAX_VALUE(12);
            
        if(lhs_fixed)
        {
            if(lhs64 >= 0 && lhs64 <= max || lhs64 < 0 && lhs64 >= min)
                { return false; }
        }
        
        if(!lhs_fixed)
        {
        	if(!rhs_fixed)
        	{
	            if((lhs64 = Random.int64()) >= 0)
	                { lhs64 = (max + lhs64 % -IntegerType.MIN_VALUE(12)) + 1; }
	            else
	                { lhs64 = (min - -lhs64 % IntegerType.MAX_VALUE(12)) - 1; }
        	}
        	else
        	{
            	if(rhs12 < 0)
        			{ lhs64 = Random.int64_range(Long.MAX_VALUE + rhs12 + 1, Long.MAX_VALUE); }
            	else if(rhs12 > 0)
        			{ lhs64 = Random.int64_range(Long.MIN_VALUE, Long.MIN_VALUE + rhs12 - 1); }
            	else
        			{ return false;	}
        	}
        }
        
        if(!rhs_fixed)
        {
	        if(lhs64 >= 0)
	            { rhs12 = (short)Random.int64_range(IntegerType.MIN_VALUE(12), Math.min(Long.MIN_VALUE + lhs64, IntegerType.MAX_VALUE(12))); }
	        else
	            { rhs12 = (short)Random.int64_range(Math.max(IntegerType.MIN_VALUE(12), (Long.MAX_VALUE + lhs64) + 1), IntegerType.MAX_VALUE(12)); }
        }
        
        return true;
    }

    /**
     * Tries to generate operands for the 64x12-bit subtraction operation that
     * causes the integer overflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub64x12_overflow(boolean lhs_fixed)
    {
        return sub64x12_overflow(lhs_fixed, false);
    }
    
    /**
     * Tries to generate operands for the 64x12-bit subtraction operation that
     * causes the integer overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub64x12_overflow()
    {
        return sub64x12_overflow(false, false);
    }
    
    //**********************************************************************************************
    // SUB64x16 Normal 
    //**********************************************************************************************    

    /**
     * Checks if the 64x16-bit subtraction operation does not cause exceptions.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 64x16-bit subtraction operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_sub64x16_normal(long lhs, short rhs)
    {
        return check_sub64_normal(lhs, rhs);
    }

    /**
     * Checks if the 64x16-bit subtraction operation does not cause exceptions.
     * 
     * @return <code>true</code> if the 64x16-bit subtraction operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_sub64x16_normal()
    {
        return check_sub64x16_normal(lhs64, rhs16);
    }
        
    /**
     * Tries to generate operands for the 64x16-bit subtraction operation that
     * do not cause exceptions.
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
    public static boolean sub64x16_normal(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(!lhs_fixed)
            { lhs64 = Random.int64(); }
    
        if(rhs_fixed)
        	{ return check_sub64x16_normal(); }
        
        if(lhs64 >= 0)
            { rhs16 = (short)Random.int64_range(Math.max(Long.MIN_VALUE + lhs64 + 1, Short.MIN_VALUE), Short.MAX_VALUE); }
        else
            { rhs16 = (short)Random.int64_range(Short.MIN_VALUE, Math.min(Long.MAX_VALUE + lhs64 + 1, Short.MAX_VALUE)); }
    
        return true;
    }

    /**
     * Tries to generate operands for the 64x16-bit subtraction operation that
     * do not cause exceptions.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub64x16_normal(boolean lhs_fixed)
    {
        return sub64x16_normal(lhs_fixed, false);
    }
    
    /**
     * Tries to generate operands for the 64x16-bit subtraction operation that
     * do not cause exceptions.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub64x16_normal()
    {
        return sub64x16_normal(false, false);
    }

    //**********************************************************************************************
    // SUB64x16 Overflow 
    //**********************************************************************************************    

    /**
     * Checks if the 64x16-bit subtraction operation causes the integer overflow
     * exception.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 64x16-bit subtraction operation causes
     *         the integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_sub64x16_overflow(long lhs, short rhs)
    {
        return check_sub64_overflow(lhs, rhs);
    }

    /**
     * Checks if the 64x16-bit subtraction operation causes the integer overflow
     * exception.
     * 
     * @return <code>true</code> if the 64x16-bit subtraction operation causes
     *         the integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_sub64x16_overflow()
    {
        return check_sub64x16_overflow(lhs64, rhs16);
    }
        
    /**
     * Tries to generate operands for the 64x16-bit subtraction operation that
     * causes the integer overflow exception.
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
    public static boolean sub64x16_overflow(boolean lhs_fixed, boolean rhs_fixed)
    {
        final long max = Long.MAX_VALUE + Short.MIN_VALUE;
        final long min = Long.MIN_VALUE + Short.MAX_VALUE;
            
        if(lhs_fixed)
        {
            if(lhs64 >= 0 && lhs64 <= max || lhs64 < 0 && lhs64 >= min)
                { return false; }
        }
        else
        {
        	if(!rhs_fixed)
        	{
	            if((lhs64 = Random.int64()) >= 0)
	                { lhs64 = (max + lhs64 % -Short.MIN_VALUE) + 1; }
	            else
	                { lhs64 = (min - -lhs64 % Short.MAX_VALUE) - 1; }
        	}
        	else
        	{
            	if(rhs16 < 0)
    				{ lhs64 = Random.int64_range(Long.MAX_VALUE + rhs16 + 1, Long.MAX_VALUE); }
            	else if(rhs12 > 0)
    				{ lhs64 = Random.int64_range(Long.MIN_VALUE, Long.MIN_VALUE + rhs16 - 1); }
            	else
    				{ return false;	}
        	}
        }
        
        if(!rhs_fixed)
        {
	        if(lhs64 >= 0)
	            { rhs16 = (short)Random.int64_range(Short.MIN_VALUE, Math.min(Long.MIN_VALUE + lhs64, Short.MAX_VALUE)); }
	        else
	            { rhs16 = (short)Random.int64_range(Math.max(Short.MIN_VALUE, (Long.MAX_VALUE + lhs64) + 1), Short.MAX_VALUE); }
        }
        
        return true;
    }

    /**
     * Tries to generate operands for the 64x16-bit subtraction operation that
     * causes the integer overflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub64x16_overflow(boolean lhs_fixed)
    {
        return sub64x16_overflow(lhs_fixed, false);
    }
    
    /**
     * Tries to generate operands for the 64x16-bit subtraction operation that
     * causes the integer overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub64x16_overflow()
    {
        return sub64x16_overflow(false, false);
    }
    
    //**********************************************************************************************
    // SUB64x18 Normal 
    //**********************************************************************************************    

    /**
     * Checks if the 64x18-bit subtraction operation does not cause exceptions.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 64x18-bit subtraction operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_sub64x18_normal(long lhs, int rhs)
    {
        return check_sub64_normal(lhs, rhs);
    }

    /**
     * Checks if the 64x18-bit subtraction operation does not cause exceptions.
     * 
     * @return <code>true</code> if the 64x18-bit subtraction operation does not
     *         cause exceptions; <code>false</code> otherwise.
     */
    public static boolean check_sub64x18_normal()
    {
        return check_sub64x18_normal(lhs64, rhs18);
    }
        
    /**
     * Tries to generate operands for the 64x18-bit subtraction operation that
     * do not cause exceptions.
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
    public static boolean sub64x18_normal(boolean lhs_fixed, boolean rhs_fixed)
    {
        if(!lhs_fixed)
            { lhs64 = Random.int64(); }
    
        if(rhs_fixed)
        	{ return check_sub64x18_normal(); }
        
        if(lhs64 >= 0)
            { rhs18 = (int)Random.int64_range(Math.max(Long.MIN_VALUE + lhs64 + 1, IntegerType.MIN_VALUE(18)), IntegerType.MAX_VALUE(18)); }
        else
            { rhs18 = (int)Random.int64_range(IntegerType.MIN_VALUE(18), Math.min(Long.MAX_VALUE + lhs64 + 1, IntegerType.MAX_VALUE(18))); }
    
        return true;
    }

    /**
     * Tries to generate operands for the 64x18-bit subtraction operation that
     * do not cause exceptions.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub64x18_normal(boolean lhs_fixed)
    {
        return sub64x18_normal(lhs_fixed, false);
    }
    
    /**
     * Tries to generate operands for the 64x18-bit subtraction operation that
     * do not cause exceptions.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub64x18_normal()
    {
        return sub64x18_normal(false, false);
    }

    //**********************************************************************************************
    // SUB64x18 Overflow 
    //**********************************************************************************************    

    /**
     * Checks if the 64x18-bit subtraction operation causes the integer overflow
     * exception.
     * 
     * @param <code>lhs</code> the left-hand operand.
     * 
     * @param <code>rhs</code> the right-hand operand.
     * 
     * @return <code>true</code> if the 64x18-bit subtraction operation causes
     *         the integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_sub64x18_overflow(long lhs, int rhs)
    {
        return check_sub64_overflow(lhs, rhs);
    }

    /**
     * Checks if the 64x18-bit subtraction operation causes the integer overflow
     * exception.
     * 
     * @return <code>true</code> if the 64x18-bit subtraction operation causes
     *         the integer overflow exception; <code>false</code> otherwise.
     */
    public static boolean check_sub64x18_overflow()
    {
        return check_sub64x18_overflow(lhs64, rhs18);
    }
        
    /**
     * Tries to generate operands for the 64x18-bit subtraction operation that
     * causes the integer overflow exception.
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
    public static boolean sub64x18_overflow(boolean lhs_fixed, boolean rhs_fixed)
    {
        final long max = Long.MAX_VALUE + IntegerType.MIN_VALUE(18);
        final long min = Long.MIN_VALUE + IntegerType.MAX_VALUE(18);
            
        if(lhs_fixed)
        {
            if(lhs64 >= 0 && lhs64 <= max || lhs64 < 0 && lhs64 >= min)
                { return false; }
        }
        else
        {
        	if(!rhs_fixed)
        	{
	            if((lhs64 = Random.int64()) >= 0)
	                { lhs64 = (max + lhs64 % -IntegerType.MIN_VALUE(18)) + 1; }
	            else
	                { lhs64 = (min - -lhs64 % IntegerType.MAX_VALUE(18)) - 1; }
        	}
        	else
        	{
            	if(rhs18 < 0)
					{ lhs64 = Random.int64_range(Long.MAX_VALUE + rhs18 + 1, Long.MAX_VALUE); }
            	else if(rhs18 > 0)
					{ lhs64 = Random.int64_range(Long.MIN_VALUE, Long.MIN_VALUE + rhs18 - 1); }
            	else
					{ return false;	}
        	}
        }
        
        if(!rhs_fixed)
        {
	        if(lhs64 >= 0)
	            { rhs18 = (int)Random.int64_range(IntegerType.MIN_VALUE(18), Math.min(Long.MIN_VALUE + lhs64, IntegerType.MAX_VALUE(18))); }
	        else
	            { rhs18 = (int)Random.int64_range(Math.max(IntegerType.MIN_VALUE(18), (Long.MAX_VALUE + lhs64) + 1), IntegerType.MAX_VALUE(18)); }
        }

        return true;
    }

    /**
     * Tries to generate operands for the 64x18-bit subtraction operation that
     * causes the integer overflow exception.
     * 
     * @param  <code>lhs_fixed</code> indicates if the left-hand operand is
     *         fixed.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub64x18_overflow(boolean lhs_fixed)
    {
        return sub64x18_overflow(lhs_fixed, false);
    }
    
    /**
     * Tries to generate operands for the 64x18-bit subtraction operation that
     * causes the integer overflow exception.
     * 
     * @return <code>true</code> if operands are successfully generated;
     *         <code>false</code> otherwise.
     */
    public static boolean sub64x18_overflow()
    {
        return sub64x18_overflow(false, false);
    }
}
