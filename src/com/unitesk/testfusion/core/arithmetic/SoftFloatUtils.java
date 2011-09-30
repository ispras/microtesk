/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SoftFloatUtils.java,v 1.11 2009/07/08 08:27:47 vorobyev Exp $
 */

package com.unitesk.testfusion.core.arithmetic;

/**
 * Class contains methods for accessing SoftFloat variables and calling its
 * functions via JNI (Java Native Interface).
 * 
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class SoftFloatUtils 
{
	/** Underflow tininess-detection mode: after rounding. */
	public static final char TININESS_AFTER_ROUNDING = 0;
	
	/** Underflow tininess-detection mode: before rounding. */
	public static final char TININESS_BEFORE_ROUNDING = 1;
	
	/** Get underflow tininess-detection mode. */
	public static native char get_float_detect_tininess();
	
	/** Set underflow tininess-detection mode. */
	public static native void set_float_detect_tininess( char float_detect_tininess );
	
	/** Rounding to nearest. */
	public static final char ROUND_NEAREST_EVEN = 0;
	
	/** Rounding towards negative infinity. */
	public static final char ROUND_DOWN = 1;
	
	/** Rounding towards positive infinity. */
	public static final char ROUND_UP = 2;
	
	/** Rounding towards zero. */
	public static final char ROUND_TO_ZERO = 3;
	
	/** Get floating-point rounding mode. */
	public static native char get_floating_rounding_mode();
	
	/** Set floating-point rounding mode. */
	public static native void set_floating_rounding_mode( char floating_rounding_mode );
	
	/** Invalid operation exception flag. */
    public static final char FLAG_INVALID = 1;

    /** Division by zero exception flag. */
    public static final char FLAG_DIVBYZERO = 4;
    
    /** Overflow exception flag. */
    public static final char FLAG_OVERFLOW = 8;
    
    /** Underflow exception flag. */
    public static final char FLAG_UNDERFLOW = 16;
    
    /** Inexact exception flag. */
    public static final char FLAG_INEXACT = 32;
	
	/** Get floating-point exception flags. */
    public static native char get_float_exception_flags();
	
	//*************************************************************************
	// Software IEC/IEEE integer-to-floating-point conversion routines.
	//*************************************************************************
	
	/** 
	 * Integer-to-single-precision conversion routine. 
     * 
	 * @param  <code>arg0</code> the number to be converted.
     * 
	 * @return result of converting the 32-bit two's complement integer 
	 *         <code>arg0</code> to the single-precision floating-point format.
	 */
    public static native int  int32_to_float32( int arg0 );

	/** 
	 * Integer-to-double-precision conversion routine. 
	 * 
	 * @param  <code>arg0</code> the number to be converted.
	 * 
	 * @return the result of converting the 32-bit two's complement integer 
	 *         <code>arg0</code> to the double-precision floating-point format.
	 */
    public static native long int32_to_float64( int arg0 );
	
	/** 
	 * Integer-to-single-precision conversion routine. 
	 * 
	 * @param  <code>arg0</code> the number to be converted.
	 * 
	 * @return the result of converting the 64-bit two's complement integer 
	 *         <code>arg0</code> to the single-precision floating-point format.
	 */
    public static native int  int64_to_float32( long arg0 );
	
	/** 
	 * Integer-to-double-precision conversion routine. 
	 * 
	 * @param  <code>arg0</code> the number to be converted.
	 * 
	 * @return the result of converting the 64-bit two's complement integer 
	 *         <code>arg0</code> to the double-precision floating-point format.
	 */
    public static native long int64_to_float64( long arg0 );

	//*************************************************************************
	// Software IEC/IEEE single-precision conversion routines.
	//*************************************************************************

	/** 
	 * Single-precision-to-integer conversion routine. 
	 * 
	 * @param  <code>arg0</code> the number to be converted in bit integer 
	 *         number representation.
	 * 
	 * @return the result of converting the single-precision floating-point 
	 * 		   value <code>arg0</code> to the 32-bit two's complement integer 
	 *         format. If <code>arg0</code> is a NaN, the largest positive 
	 *         integer is returned.  Otherwise, if the conversion overflows, 
	 *         the  largest integer with the same sign as <code>arg0</code> 
	 *         is returned.
	 */
    public static native int  float32_to_int32( int arg0 );
	
	/**
	 * Single-precision-to-integer with rounding to zero conversion routine.
	 * The conversion is performed according to the IEC/IEEE Standard for 
	 * Binary Floating-Point Arithmetic, except that the conversion is 
	 * always rounded toward zero. 
	 * 
	 * @param  <code>arg0</code> the number to be converted in bit integer 
	 *         number representation.
	 * 
	 * @return the result of converting the single-precision floating-point 
	 *         value <code>arg0</code> to the 32-bit two's complement integer 
	 *         format. If <code>arg0</code> is a NaN, the largest positive 
	 *         integer is returned. Otherwise, if the conversion overflows, 
	 *         the largest integer with the same sign as <code>arg0</code> 
	 *         is returned.
	 */
    public static native int  float32_to_int32_round_to_zero( int arg0 );
	
	/** 
	 * Single-precision-to-integer conversion routine. 
	 * 
	 * @param  <code>arg0</code> the number to be converted in bit integer 
	 *         number representation.
	 * 
	 * @return the result of converting the single-precision floating-point 
	 *         value <code>arg0</code> to the 64-bit two's complement integer
	 *         format.  If <code>arg0</code> is a NaN, the largest positive 
	 *         integer is returned.  Otherwise, if the conversion overflows, 
	 *         the largest integer with the same sign as <code>arg0</code> 
	 *         is returned.
	 */
    public static native long float32_to_int64( int arg0 );
	
    /**
     * Single-precision-to-integer with rounding to zero conversion routine.
     * The conversion is performed according to the IEC/IEEE Standard for 
     * Binary Floating-Point Arithmetic, except that the conversion is always 
     * rounded toward zero.
	 * 
	 * @param  <code>arg0</code> the number to be converted in bit integer 
	 *         number representation.
	 * 
	 * @return the result of converting the single-precision floating-point 
	 *         value <code>arg0</code> to the 64-bit two's complement integer
	 *         format. If <code>arg0</code> is a NaN, the largest positive 
	 *         integer is returned.  Otherwise, if the conversion overflows,
	 *         the largest integer with the same sign as <code>arg0</code> 
	 *         is returned.
	 */
    public static native long float32_to_int64_round_to_zero( int arg0 );
    
    /**
     * Single-precision-to-double-precision with rounding to zero conversion 
     * routine. 
	 * 
	 * @param  <code>arg0</code> the number to be converted in bit integer 
	 *         number representation.
	 * 
	 * @return the result of converting the single-precision floating-point
	 *         value <code>arg0</code> to the double-precision floating-point
	 *         format. 
	 */
    public static native long float32_to_float64( int arg0 );
	
	//*************************************************************************
	// Software IEC/IEEE single-precision operations.
	//*************************************************************************
	
    /**
     * Rounds the single-precision floating-point value <code>arg0</code> 
     * to an integer.
	 * 
	 * @param  <code>arg0</code> the operand in bit integer number 
	 *         representation.
	 * 
	 * @return the result as a single-precision floating-point value.
	 */
    public static native int  float32_round_to_int( int arg0 );
	
    /** 
     * Single-precision add operation. 
     * 
	 * @param  <code>arg0</code> the first operand of add operation in bit 
	 *         integer number representation.
  	 * 
  	 * @param  <code>arg1</code> the second operand of add operation in bit 
  	 *         integer number representation.
  	 *         
	 * @return the result of adding the single-precision floating-point values
	 *         <code>arg0</code> and <code>arg1</code> 
	 */
    public static native int  float32_add( int arg0, int arg1 );
	
    /** 
     * Single-precision sub operation. 
	 * 
	 * @param  <code>arg0</code> the first operand of sub operation in bit 
	 *         integer number representation.
  	 * 
  	 * @param  <code>arg1</code> the second operand of sub operation in bit 
  	 *         integer number representation.
	 * 
	 * @return the result of subtracting the single-precision floating-point 
	 *         values <code>arg0</code> and <code>arg1</code>. 
	 */
    public static native int  float32_sub( int arg0, int arg1 );
	
    /**
     * Single-precision mul operation. 
     * 
	 * @param  <code>arg0</code> the first operand of mul operation in bit 
	 *         integer number representation.
	 * 
  	 * @param  <code>arg1</code> the second operand of mul operation in bit 
  	 *         integer number representation.
  	 * 
	 * @return the result of multiplying the single-precision floating-point 
	 *         values <code>arg0</code> and <code>arg1</code>. 
	 */
    public static native int  float32_mul( int arg0, int arg1 );

    /**
     * Single-precision div operation. 
     * 
	 * @param  <code>arg0</code> the first operand of div operation in bit 
	 *         integer number representation.
	 * 
  	 * @param  <code>arg1</code> the second operand of div operation in bit 
  	 *         integer number representation.
  	 * 
	 * @return the result of dividing the single-precision floating-point value
	 *         <code>arg0</code> by the corresponding value <code>arg1</code>. 
	 */
    public static native int  float32_div( int arg0, int arg1 );
	
    /**
     * Single-precision rem operation.
     * 
	 * @param  <code>arg0</code> the first operand of rem operation in bit 
	 *         integer number representation.
	 * 
  	 * @param  <code>arg1</code> the second operand of rem operation in bit
  	 *         integer number representation.
  	 * 
	 * @return the remainder of the single-precision floating-point value 
	 *         <code>arg0</code> with respect to the corresponding value 
	 *         <code>arg1</code>. 
	 */
    public static native int  float32_rem( int arg0, int arg1 );

    /** 
     * Single-precision sqrt operation. 
	 * 
	 * @param  <code>arg0</code> the operand of sqrt operation in bit integer
	 *         number representation.
	 * 
	 * @return the square root of the single-precision floating-point value 
	 *         <code>arg0</code>.
	 */
    public static native int  float32_sqrt( int arg0 );
	
    /**
     * Single-precision eq operation.
     *  
	 * @param  <code>arg0</code> the first operand of eq operation in bit 
	 *         integer number representation.
	 * 
	 * @param  <code>arg1</code> the second operand of eq operation in bit  
	 *         integer number representation.
	 * 
	 * @return <code>1</code> if the single-precision floating-point value 
	 *         <code>arg0</code> is equal to the corresponding value 
	 *         <code>arg1</code>;
	 *         
	 *         <code>0</code> otherwise.
	 */
    public static native char float32_eq( int arg0, int arg1 );
	
    /** 
     * Single-precision le operation. 
	 *
	 * @param  <code>arg0</code> the first operand of le operation in bit 
	 *         integer number representation.
	 * 
	 * @param  <code>arg1</code> the second operand of le operation in bit
	 *         integer number representation.
	 * 
	 * @return <code>1</code> if the single-precision floating-point value 
	 *         <code>arg0</code> is less than or equal to the corresponding 
	 *         value <code>arg1</code>;
	 * 
     *         <code>0</code> otherwise.
	 */
    public static native char float32_le( int arg0, int arg1 );
	
    /** 
     * Single-precision lt operation.
     *  
	 * @param  <code>arg0</code> the first operand of lt operation in bit 
	 *         integer number representation.
	 * 
	 * @param  <code>arg1</code> the second operand of lt operation in bit 
	 *         integer number representation.
	 * 
	 * @return <code>1</code> if the single-precision floating-point value 
	 *         <code>arg0</code> is less than the corresponding value 
	 *         <code>arg1</code>;
	 * 
     *         <code>0</code> otherwise.
	 */
    public static native char float32_lt( int arg0, int arg1 );
	
    /** 
     * Single-precision eq operation. The invalid exception is raised if 
     * either operand is a NaN.  Otherwise, the comparison is performed 
     * according to the IEC/IEEE Standard for Binary Floating-Point Arithmetic.
	 * 
	 * @param  <code>arg0</code> the first operand of eq operation in bit 
	 *         integer number representation.
	 * 
	 * @param  <code>arg1</code> the second operand of eq operation in bit
	 *         integer number representation.
	 * 
	 * @return <code>1</code> if the single-precision floating-point value 
	 *         <code>arg0</code> is equal to the corresponding value 
	 *         <code>arg1</code>;
	 * 
     *         <code>0</code> otherwise.
	 */
    public static native char float32_eq_signaling( int arg0, int arg1 );
	
    /** 
     * Single-precision le operation. Quiet NaNs do not cause an exception.  
     * Otherwise, the comparison is performed according to the IEC/IEEE 
     * Standard for Binary Floating-Point Arithmetic.
     * 
	 * @param  <code>arg0</code> the first operand of eq operation in bit 
	 *         integer number representation.
	 * 
	 * @param  <code>arg1</code> the second operand of eq operation in bit 
	 *         integer number representation.
	 * 
	 * @return <code>1</code> if the single-precision floating-point value 
	 *         <code>arg0</code> is less than or equal to the corresponding 
	 *         value <code>arg1</code>;
	 *         
     *         <code>0</code> otherwise.
	 */
    public static native char float32_le_quiet( int arg0, int arg1 );
	
    /** 
     * Single-precision lt operation. Quiet NaNs do not cause an exception.  
     * Otherwise, the comparison is performed according to the IEC/IEEE 
     * Standard for Binary Floating-Point Arithmetic.
     * 
	 * @param  <code>arg0</code> the first operand of eq operation in bit 
	 *         integer number representation.
	 * 
	 * @param  <code>arg1</code> the second operand of eq operation in bit 
	 *         integer number representation.
	 * 
	 * @return <code>1</code> if the single-precision floating-point value 
	 *         <code>arg0</code> is less than or equal to the corresponding
	 *         value <code>arg1</code>;
	 * 
     *         <code>0</code> otherwise.
	 */
    public static native char float32_lt_quiet( int arg0, int arg1 );
	
    /**
    * Method test whether a floating-point value is a signaling NaN.
    * 
    * @param  <code>arg0</code> the operand in bit integer number
    *         representation.
    * 
    * @return <code>1</code> if the operand is a signaling NaN;
    * 
    *         <code>0</code> otherwise.
    */
    public static native char float32_is_signaling_nan( int arg0 );

	//*************************************************************************
    // Software IEC/IEEE double-precision conversion routines.
 	//*************************************************************************
	
    /**
     * Double-precision-to-integer conversion routine.
     * 
     * @param  <code>arg0</code> the operand in bit integer number 
     *         representation.
     *  
     * @return the result of converting the double-precision floating-point 
     *         value <code>arg0</code> to the single-precision floating-point
     *         format. If <code>arg0</code> is a NaN, the largest positive 
     *         integer is returned. Otherwise, if the conversion overflows, 
     *         the largest integer with the same sign as <code>arg0</code> 
     *         is returned.
     */
    public static native int  float64_to_int32( long arg0);
	
    /**
     * Double-precision-to-integer conversion routine.
     * The conversion is performed according to the IEC/IEEE Standard for 
     * Binary Floating-Point Arithmetic, except that the conversion is always
     * rounded toward zero.
     * 
     * @param  <code>arg0</code> the operand in bit integer number 
     *         representation. 
     * 
     * @return the result of converting the double-precision floating-point 
     *         value <code>arg0</code> to the single-precision floating-point
     *         format. If <code>arg0</code> is a NaN, the largest positive 
     *         integer is returned. Otherwise, if the conversion overflows, 
     *         the largest integer with the same sign as <code>arg0</code> 
     *         is returned.
     */
    public static native int  float64_to_int32_round_to_zero( long arg0 );
    
    /**
     * Double-precision-to-integer conversion routine.
     * The conversion is performed according to the IEC/IEEE Standard for 
     * Binary Floating-Point Arithmetic which means in particular that 
     * the conversion is rounded according to the current rounding mode.
     * 
     * @param  <code>arg0</code> the operand in bit integer number 
     *         representation.
     * 
     * @return the result of converting the double-precision floating-point 
     *         value <code>arg0</code> to the 64-bit two's complement integer 
     *         format. If <code>arg0</code> is a NaN, the largest positive 
     *         integer is returned.  Otherwise, if the conversion overflows,
     *         the largest integer with the same sign as <code>arg0</code> 
     *         is returned.
     */
	public static native long float64_to_int64( long arg0 );
	
	/**
	 * Double-precision-to-integer conversion routine.
	 * The conversion is performed according to the IEC/IEEE Standard for 
	 * Binary Floating-Point Arithmetic, except that the conversion is always 
	 * rounded toward zero.
	 * 
	 * @param  <code>arg0</code> the operand in bit integer number 
	 *         representation.
	 * 
	 * @return the result of converting the double-precision floating-point 
	 *         value <code>arg0</code> to the 64-bit two's complement integer
	 *         format. If <code>arg0</code> is a NaN, the largest positive 
	 *         integer is returned. Otherwise, if the conversion overflows, 
	 *         the largest integer with the same sign as <code>arg0</code> 
	 *         is returned.
	 */
	public static native long float64_to_int64_round_to_zero( long arg0 );
	
	/**
	 * Double-precision-to-single-precision conversion routine.
	 * 
	 * @param  <code>arg0</code> the operand in bit integer number 
	 *         representation.
	 * 
	 * @return the result of converting the double-precision floating-point 
	 *         value <code>arg0</code> to the single-precision floating-point 
	 *         format.
	 */
	public static native int  float64_to_float32( long arg0 );

	//*************************************************************************
    // Software IEC/IEEE double-precision operations.
  	//*************************************************************************

	/**
	 * Rounds the double-precision floating-point value <code>arg0</code> 
	 * to an integer. 
	 * 
	 * @param  <code>arg0</code> the operand in bit integer number 
	 *         representation.
	 * 
	 * @return the result as a double-precision floating-point value.
	 */
	public static native int  float64_round_to_int( long arg0);
	
	/**
	 * Double-precision add operation.
	 * 
	 * @param  <code>arg0</code> the first operand of add operation in bit 
	 *         integer number representation.
	 * 
  	 * @param  <code>arg1</code> the second operand of add operation in bit 
  	 *         integer number representation.
  	 * 
	 * @return the result of adding the double-precision floating-point values
	 *         <code>arg0</code> and <code>arg1</code>.
	 */
	public static native long float64_add( long arg0, long arg1 );

	/**
	 * Double-precision sub operation.
	 * 
	 * @param  <code>arg0</code> the first operand of sub operation in bit 
	 *         integer number representation.
	 * 
  	 * @param  <code>arg1</code> the second operand of sub operation in bit 
  	 *         integer number representation.
  	 * 
  	 * @return the result of subtracting the double-precision floating-point 
  	 *         values <code>arg0</code> and <code>arg1</code>.
	 */
	public static native long float64_sub( long arg0, long arg1 );
	

	/**
	 * Double-precision mul operation.
	 * 
	 * @param  <code>arg0</code> the first operand of mul operation in bit 
	 *         integer number representation.
	 * 
  	 * @param  <code>arg1</code> the second operand of mul operation in bit 
  	 *         integer number representation.
  	 * 
  	 * @return the result of multiplying the double-precision floating-point 
  	 *         values <code>arg0</code> and <code>arg1</code>.
	 */
	public static native long float64_mul( long arg0, long arg1 );
	
	/**
	 * Double-precision div operation.
	 * 
	 * @param  <code>arg0</code> the first operand of div operation in bit 
	 *         integer number representation.
	 * 
  	 * @param  <code>arg1</code> the second operand of div operation in bit
  	 *         integer number representation.
  	 * 
  	 * @return the result of dividing the double-precision floating-point 
  	 *         value <code>arg0</code> by the corresponding value 
  	 *         <code>arg1</code>.
	 */
	public static native long float64_div( long arg0, long arg1 );
	
	/**
	 * Double-precision rem operation.
	 * 
	 * @param  <code>arg0</code> the first operand of rem operation in bit 
	 *         integer number representation.
	 * 
  	 * @param  <code>arg1</code> the second operand of rem operation in bit 
  	 *         integer number representation.
  	 * 
	 * @return the remainder of the double-precision floating-point value 
	 *         <code>arg0</code> with respect to the corresponding value 
	 *         <code>arg1</code>.
	 */
	public static native long float64_rem( long arg0, long arg1 );
	
	/**
	 * Double-precision sqrt operation.
	 * 
	 * @param  <code>arg0</code> the first operand of rem operation in bit 
	 *         integer number representation.
	 * 
	 * @return the square root of the double-precision floating-point value
	 *         <code>arg0</code>.
	 */
	public static native long float64_sqrt( long arg0 );
	
	/**
	 * Double-precision eq operation.
	 * 
	 * @param  <code>arg0</code> the first operand of eq operation in bit 
	 *         integer number representation.
	 * 
  	 * @param  <code>arg1</code> the second operand of eq operation in bit 
  	 *         integer number representation.
  	 * 
	 * @return <code>1</code> if the double-precision floating-point value 
	 *         <code>arg0</code> is equal to the corresponding value 
	 *         <code>arg1</code>;
	 *         
     *         <code>0</code> otherwise.
	 */
	public static native char float64_eq( long arg0, long arg1 );
	
	/**
	 * Double-precision le operation.
	 * 
	 * @param  <code>arg0</code> the first operand of le operation in bit 
	 *         integer number representation.
	 * 
  	 * @param  <code>arg1</code> the second operand of le operation in bit 
  	 *         integer number representation.
  	 * 
	 * @return <code>1</code> if the double-precision floating-point value 
	 *         <code>arg0</code> is less than or equal to the corresponding 
	 *         value <code>arg1</code>;
	 *         
     *         <code>0</code> otherwise.
	 */
	public static native char float64_le( long arg0, long arg1 );
	
	/**
	 * Double-precision lt operation.
	 * 
	 * @param  <code>arg0</code> the first operand of lt operation in bit 
	 *         integer number representation.
	 * 
  	 * @param  <code>arg1</code> the second operand of lt operation in bit
  	 *         integer number representation.
  	 * 
	 * @return <code>1</code> if the double-precision floating-point value 
	 *         <code>arg0</code> is less than the corresponding value 
	 *         <code>arg1</code>;
	 *         
     *         <code>0</code> otherwise.
	 */
	public static native char float64_lt( long arg0, long arg1 );
	
	/**
	 * Double-precision eq operation. The invalid exception is raised if 
	 * either operand is a NaN. Otherwise, the comparison is performed 
	 * according to the IEC/IEEE Standard for Binary Floating-Point Arithmetic.
	 * 
	 * @param  <code>arg0</code> the first operand of eq operation in bit 
	 *         integer number representation.
	 * 
  	 * @param  <code>arg1</code> the second operand of eq operation in bit 
  	 *         integer number representation.
  	 * 
	 * @return <code>1</code> if the double-precision floating-point value 
	 *         <code>arg0</code> is equal to the corresponding value 
	 *         <code>arg1</code>;
	 *         
     *         <code>0</code> otherwise.
	 */
	public static native char float64_eq_signaling( long arg0, long arg1 );
	
	/**
	 * Double-precision le operation. Quiet NaNs do not cause an exception.  
	 * Otherwise, the comparison is performed according to the IEC/IEEE 
	 * Standard for Binary Floating-Point Arithmetic. 
	 * 
	 * @param  <code>arg0</code> the first operand of le operation in bit
	 *         integer number representation.
	 * 
  	 * @param  <code>arg1</code> the second operand of le operation in bit 
  	 *         integer number representation.
  	 * 
	 * @return <code>1</code> if the double-precision floating-point value 
	 *         <code>arg0</code> is less than or equal to the corresponding 
	 *         value <code>arg1</code>;
	 *         
     *         <code>0</code> otherwise.
	 */
	public static native char float64_le_quiet( long arg0, long arg1 );
	
	/**
	 * Double-precision lt operation. Quiet NaNs do not cause an exception.  
	 * Otherwise, the comparison is performed according to the IEC/IEEE 
	 * Standard for Binary Floating-Point Arithmetic. 
	 * 
	 * @param  <code>arg0</code> the first operand of lt operation in bit 
	 *         integer number representation.
	 * 
  	 * @param  <code>arg1</code> the second operand of lt operation in bit
  	 *         integer number representation.
  	 * 
	 * @return <code>1</code> if the double-precision floating-point value
	 *         <code>arg0</code> is less than the corresponding value 
	 *         <code>arg1</code>;
	 *         
     *         <code>0</code> otherwise.
	 */
	public static native char float64_lt_quiet( long arg0, long arg1 );
	
	/**
     * Method test whether a double-precision value is a signaling NaN.
     * 
     * @param  <code>arg0</code> the operand in bit integer number 
     *         representation.
     * 
     * @return <code>1</code> if the operand is a signaling NaN;
     * 
     *         <code>0</code> otherwise.
	 */
	public static native char float64_is_signaling_nan( long arg0 );

	static
	{
	    System.loadLibrary("SoftFloatLibrary");
	}
	
	//*************************************************************************
    // Wrappers for integer-to-floating-point conversion routines.
 	//*************************************************************************
	
	/** 
	 * Wrapper method for <code>int32_to_float32()</code> conversion routine. 
	 * 
	 * @param  <code>arg0</code> the number to be converted.
	 * 
	 * @return converted float number.
	 * 
	 * @see    #int32_to_float32(int).
	 */
	public static float int_to_float(int arg0)
	{
		return Float.intBitsToFloat(int32_to_float32(arg0));
	}
	
	/** 
	 * Wrapper method for <code>int32_to_float64()</code> conversion routine. 
	 * 
	 * @param  <code>arg0</code> the number to be converted.
	 * 
	 * @return converted double number.
	 * 
	 * @see    #int32_to_float64(int).
	 */
	public static double int_to_double(int arg0)
	{
		return Double.longBitsToDouble(int32_to_float64(arg0));
	}
	
	/** 
	 * Wrapper method for <code>int64_to_float32()</code> conversion routine.
	 *  
	 * @param  <code>arg0</code> the number to be converted.
	 * 
	 * @return converted float number.
	 * 
	 * @see    #int64_to_float32(long).
	 */
	public static float long_to_float(long arg0)
	{
		return Float.intBitsToFloat(int64_to_float32(arg0));
	}

	/** 
	 * Wrapper method for <code>int64_to_float64()</code> conversion routine. 
	 * 
	 * @param  <code>arg0</code> the number to be converted.
	 * 
	 * @return converted double number.
	 * 
	 * @see    #int64_to_float64(long).
	 */
	public static double long_to_double(long arg0)
	{
		return Double.longBitsToDouble(int64_to_float64(arg0));	
	} 
	
	//*************************************************************************
    // Wrappers for floating-point-to-integer conversion routines.
 	//*************************************************************************

	/** 
	 * Wrapper method for <code>float32_to_int32()</code> conversion routine. 
	 *
	 * @param  <code>arg0</code> the number to be converted.
	 *
	 * @return converted integer number.
	 * 
	 * @see    #float32_to_int32(int)
	 */
	public static int float_to_int(float arg0)
	{
		return float32_to_int32(Float.floatToRawIntBits(arg0));	
	}
	
	/** 
	 * Wrapper method for <code>float32_to_int32_round_to_zero()</code> 
	 * conversion routine. 
	 * 
	 * @param  <code>arg0</code> the number to be converted.
	 * 
	 * @return converted integer number.
	 * 
	 * @see    #float32_to_int32_round_to_zero(int).
	 */
	public static int float_to_int_round_to_zero(float arg0)
	{
		return float32_to_int32_round_to_zero(Float.floatToRawIntBits(arg0));	
	}
	
	/** 
	 * Wrapper method for <code>float32_to_int64()</code> conversion routine.
	 * 
	 * @param  <code>arg0</code> the number to be converted.
	 * 
	 * @return converted long number.
	 * 
	 * @see    #float32_to_int64(int).
	 */
	public static long float_to_long(float arg0)
	{
		return float32_to_int64(Float.floatToRawIntBits(arg0));	
	}

	/** 
	 * Wrapper method for <code>float32_to_int64_round_to_zero()</code> 
	 * conversion routine. 
	 *
	 * @param  <code>arg0</code> the number to be converted.
	 *
	 * @return converted long number.
	 * 
	 * @see    #float32_to_int64_round_to_zero(int).
	 */
	public static long float_to_long_round_to_zero(float arg0)
	{
		return float32_to_int64_round_to_zero(Float.floatToRawIntBits(arg0));	
	}

	/** 
	 * Wrapper method for <code>float32_to_float64()</code> conversion routine.
	 * 
	 * @param  <code>arg0</code> the number to be converted.
	 * 
	 * @return converted double number.
	 * 
	 * @see    #float32_to_float64(int).
	 */
	public static double float_to_double(float arg0)
	{
		return Double.longBitsToDouble(float32_to_float64(Float.floatToRawIntBits(arg0)));
	}

	//*************************************************************************
    // Wrappers for single-precision operations.
 	//*************************************************************************
	
	/** 
	 * Wrapper method for <code>float32_round_to_int()</code> single-precision 
	 * operation. 
	 * 
	 * @param  <code>arg0</code> the number to be rounded.
	 * 
	 * @return rounded integer number
	 * 
	 * @see    #float32_round_to_int(int).
	 */
	public static int float_round_to_int(float arg0)
	{
	    return float32_round_to_int(Float.floatToRawIntBits(arg0));	
	}
	
	/** 
	 * Wrapper method for <code>float32_add()</code> single-precision 
	 * operation. 
	 * 
	 * @param  <code>arg0</code> the first operand of add operation.
	 * 
	 * @param  <code>arg1</code> the second operand of add operation.
	 * 
	 * @return the result of add operation.
	 * 
	 * @see    #float32_add(int, int).
	 */
	public static float float_add(float arg0, float arg1)
	{
		return Float.intBitsToFloat(float32_add(Float.floatToRawIntBits(arg0), 
				                                Float.floatToRawIntBits(arg1)) );
	}

	/** 
	 * Wrapper method for <code>float32_sub()</code> single-precision 
	 * operation. 
	 *
	 * @param  <code>arg0</code> the first operand of sub operation.
     *
	 * @param  <code>arg1</code> the second operand of sub operation.
	 * 
	 * @return the result of sub operation.
	 * 
	 * @see    #float32_sub(int, int).
	 */
	public static float float_sub(float arg0, float arg1)
	{
		return Float.intBitsToFloat(float32_sub(Float.floatToRawIntBits(arg0), 
				                                Float.floatToRawIntBits(arg1)) );	
	}

	/** 
	 * Wrapper method for <code>float32_mul()</code> single-precision 
	 * operation. 
	 * 
	 * @param  <code>arg0</code> the first operand of mul operation.
	 * 
	 * @param  <code>arg1</code> the second operand of mul operation.
	 * 
	 * @return the result of mul operation.
	 * 
	 * @see    #float32_mul(int, int).
	 */
	public static float float_mul(float arg0, float arg1)
	{
		return Float.intBitsToFloat(float32_mul(Float.floatToRawIntBits(arg0), 
				                                Float.floatToRawIntBits(arg1)) );	
	}

	/** 
	 * Wrapper method for <code>float32_div()</code> single-precision 
	 * operation.
	 * 
	 * @param  <code>arg0</code> the first operand of div operation.
	 * 
	 * @param  <code>arg1</code> the second operand of div operation.
	 * 
	 * @return the result of div operation.
	 * 
	 * @see    #float32_div(int, int).
	 */
	public static float float_div(float arg0, float arg1)
	{
		return Float.intBitsToFloat(float32_div(Float.floatToRawIntBits(arg0), 
				                                Float.floatToRawIntBits(arg1)) );	
	}

	/** 
	 * Wrapper method for <code>float32_rem()</code> single-precision 
	 * operation. 
	 * 
	 * @param  <code>arg0</code> the first operand of rem operation.
	 * 
	 * @param  <code>arg1</code> the second operand of rem operation.
	 * 
	 * @return the result of rem operation.
	 * 
	 * @see    #float32_rem(int, int).
	 */
	public static float float_rem(float arg0, float arg1)
	{
		return Float.intBitsToFloat(float32_rem(Float.floatToRawIntBits(arg0), 
				                                Float.floatToRawIntBits(arg1)) );	
	}
	
	/** 
	 * Wrapper method for <code>float32_sqrt()</code> single-precision
	 * operation. 
	 * 
	 * @param  <code>arg0</code> the first operand of sqrt operation.
	 * 
	 * @return the result of rem operation.
	 * 
	 * @see    #float32_sqrt(int).
	 */
	public static float float_sqrt(float arg0)
	{
		return Float.intBitsToFloat(float32_sqrt(Float.floatToRawIntBits(arg0)));	
	}
	
	/** 
	 * Wrapper method for <code>float32_sqrt()</code> single-precision 
	 * operation.
	 *  
	 * @param  <code>arg0</code> the first operand of eq operation.
	 * 
	 * @param  <code>arg1</code> the second operand of eq operation
	 * 
	 * @return the result of eq operation
	 * 
	 * @see    #float32_eq(int, int)
	 */
	public static boolean float_eq(float arg0, float arg1)
	{
		return float32_eq(Float.floatToRawIntBits(arg0), Float.floatToRawIntBits(arg1)) == 1;	
	}
	
	/** 
	 * Wrapper method for <code>float32_le()</code> single-precision 
	 * operation.
	 *  
	 * @param  <code>arg0</code> the first operand of le operation
	 * 
	 * @param  <code>arg0</code> the second operand of le operation
	 * 
	 * @return the result of eq operation.
	 * 
	 * @see    #float32_le(int, int).
	 */
	public static boolean float_le(float arg0, float arg1)
	{
		return float32_le(Float.floatToRawIntBits(arg0), Float.floatToRawIntBits(arg1)) == 1;	
	}

	/** 
	 * Wrapper method for <code>float32_lt()</code> single-precision 
	 * operation.
	 *  
	 * @param  <code>arg0</code> the first operand of lt operation.
	 * 
	 * @param  <code>arg1</code> the second operand of lt operation.
	 * 
	 * @return the result of eq operation.
	 * 
	 * @see    #float32_lt(int, int).
	 */
	public static boolean float_lt(float arg0, float arg1)
	{
		return float32_lt(Float.floatToRawIntBits(arg0), Float.floatToRawIntBits(arg1)) == 1;	
	}
	
	/** 
	 * Wrapper method for <code>float32_eq_signaling()</code> single-precision 
	 * operation.
	 * 
	 * @param  <code>arg0</code> the first operand of eq signaling operation.
	 * 
	 * @param  <code>arg1</code> the second operand of eq signaling operation.
	 * 
	 * @return the result of eq signaling operation.
	 * 
	 * @see    #float32_lt(int, int).
	 */
	public static boolean float_eq_signaling(float arg0, float arg1)
	{
		return float32_eq_signaling(Float.floatToRawIntBits(arg0), 
				                    Float.floatToRawIntBits(arg1) ) == 1;	
	}

	/** 
	 * Wrapper method for <code>float32_le_quiet()</code> single-precision 
	 * operation. 
	 * 
	 * @param  <code>arg0</code> the first operand of le quiet operation.
	 * 
	 * @param  <code>arg1</code> the second operand of le quiet operation.
	 * 
	 * @return the result of le quiet operation.
	 * 
	 * @see    #float32_le_quiet(int, int).
	 */
	public static boolean float_le_quiet(float arg0, float arg1)
	{
		return float32_le_quiet(Float.floatToRawIntBits(arg0), 
				                Float.floatToRawIntBits(arg1) ) == 1;	
	}
	
	/** 
	 * Wrapper method for <code>float32_lt_quiet()</code> single-precision 
	 * operation. 
	 * 
	 * @param  <code>arg0</code> the first operand of lt quiet operation.
	 * 
	 * @param  <code>arg1</code> the second operand of lt quiet operation.
	 * 
	 * @return the result of lt quiet operation.
	 * 
	 * @see    #float32_lt_quiet(int, int).
	 */
	public static boolean float_lt_quiet(float arg0, float arg1)
	{
		return float32_lt_quiet(Float.floatToRawIntBits(arg0), 
				                Float.floatToRawIntBits(arg1) ) == 1;	
	}	
	
	/**
	 * Wrapper method for <code>float32_is_signaling_nan()</code> 
	 * single-precision operation. 
	 * 
	 * @param arg0 the first operand of lt quiet operation.
	 * 
	 * @return <code>true</code> if argument is signaling NaN;
	 * 
     *         <code>false</code> otherwise.
     *         
	 * @see    #float32_is_signaling_nan(int).
	 */
	public static boolean float_is_signaling_nan(float arg0)
	{
		return float32_is_signaling_nan(Float.floatToRawIntBits(arg0) ) == 1;	
	}
	
	//*************************************************************************
    // Wrappers for double-precision conversion routines.
 	//*************************************************************************
	
	/**
	 * Wrapper method for <code>float64_to_int32()</code> double-precision 
	 * conversion operation. 
	 * 
	 * @param  <code>arg0</code> the number to be converted.
	 * 
	 * @return the result of conversion.
	 * 
	 * @see    #float64_to_int32(long).
	 */
	public static int double_to_int(double arg0)
	{
		return float64_to_int32(Double.doubleToRawLongBits(arg0));
	}
	
	/** 
	 * Wrapper method for <code>float64_to_int32_round_to_zero()</code> 
	 * double-precision conversion operation. 
	 * 
	 * @param  <code>arg0</code> the number to be converted.
	 * 
	 * @return the result of conversion.
	 * 
	 * @see    #float64_to_int32_round_to_zero(long).
	 */
	public static int double_to_int_round_to_zero(double arg0)
	{
		return float64_to_int32_round_to_zero(Double.doubleToRawLongBits(arg0) );
	}

	/** 
	 * Wrapper method for <code>float64_to_int64()</code> double-precision
	 * conversion operation.
	 * 
	 * @param  <code>arg0</code> the number to be converted.
	 * 
	 * @return the result of conversion.
	 * 
	 * @see    #float64_to_int64(long).
	 */
	public static long double_to_long(double arg0)
	{
		return float64_to_int64(Double.doubleToRawLongBits(arg0) );
	}

	/**
	 * Wrapper method for <code>float64_to_int64_round_to_zero()</code> 
	 * double-precision conversion operation.
	 * 
	 * @param arg0 the number to be rounded
	 * 
	 * @return the result of round operation 
	 * 
	 * @see    #float64_to_int64_round_to_zero(long).
	 */
	public static long double_to_long_round_to_zero(double arg0)
	{
		return float64_to_int64_round_to_zero(Double.doubleToRawLongBits(arg0) );
	}

	/** 
	 * Wrapper method for <code>float64_to_float32()</code> double-precision 
	 * conversion operation. 
	 * 
	 * @param  <code>arg0</code> the number to be converted.
	 * 
	 * @return the result of round operation.
	 * 
	 * @see    #float64_to_float32(long).
	 */
	public static float double_to_float(double arg0)
	{
		return Float.intBitsToFloat(float64_to_float32(Double.doubleToRawLongBits(arg0)) );
	}
	
	//*************************************************************************
    // Wrappers for double-precision operations.
 	//*************************************************************************
	
	/**
	 * Wrapper method for <code>float64_round_to_int()</code> double-precision
	 * operation. 
	 * 
	 * @param  <code>arg0</code> the operand of round operation.
	 * 
	 * @return the result of round operation.
	 * 
	 * @see    #float64_round_to_int(long).
	 */
	public static int double_round_to_int(double arg0)
	{
		return float64_round_to_int(Double.doubleToRawLongBits(arg0));
	}

	/** 
	 * Wrapper method for <code>float64_add()</code> double-precision 
	 * operation. 
	 * 
	 * @param  <code>arg0</code> the first operand of add operation.
	 * 
	 * @param  <code>arg1</code> the second operand of add operation.
	 * 
	 * @return the result of add operation 
	 * 
	 * @see    #float64_add(long, long).
	 */
	public static double double_add(double arg0, double arg1)
	{
		return Double.longBitsToDouble(float64_add(Double.doubleToRawLongBits(arg0), 
				                                   Double.doubleToRawLongBits(arg1)) );
	}

	/** 
	 * Wrapper method for <code>float64_sub()</code> double-precision 
	 * operation. 
	 * 
	 * @param  <code>arg0</code> the first operand of sub operation.
	 * 
	 * @param  <code>arg1</code> the second operand of sub operation.
	 * 
	 * @return the result of sub operation.
	 * 
	 * @see    #float64_sub(long, long).
	 */
	public static double double_sub(double arg0, double arg1)
	{
		return Double.longBitsToDouble(float64_sub(Double.doubleToRawLongBits(arg0), 
				                                   Double.doubleToRawLongBits(arg1)) );
	}
	
	/**
	 * Wrapper method for <code>float64_mul()</code> double-precision
	 * operation.
	 *  
	 * @param  <code>arg0</code> the first operand of mul operation.
	 * 
	 * @param  <code>arg1</code> the second operand of mul operation.
	 * 
	 * @return the result of mul operation.
	 * 
	 * @see    #float64_mul(long, long).
	 */
	public static double double_mul(double arg0, double arg1)
	{
		return Double.longBitsToDouble(float64_mul(Double.doubleToRawLongBits(arg0), 
				                                   Double.doubleToRawLongBits(arg1)) );
	}

	/** 
	 * Wrapper method for <code>float64_div()</code> double-precision 
	 * operation. 
	 * 
	 * @param  <code>arg0</code> the first operand of div operation.
	 * 
	 * @param  <code>arg1</code> the second operand of div operation.
	 * 
	 * @return the result of div operation.
	 * 
	 * @see    #float64_div(long, long).
	 */
	public static double double_div(double arg0, double arg1)
	{
		return Double.longBitsToDouble(float64_div(Double.doubleToRawLongBits(arg0), 
				                                   Double.doubleToRawLongBits(arg1)) );
	}

	/**
	 * Wrapper method for <code>float64_rem()</code> double-precision 
	 * operation. 
	 * 
	 * @param  <code>arg0</code> the first operand of rem operation.
	 * 
	 * @param  <code>arg1</code> the second operand of rem operation.
	 * 
	 * @return the result of rem operation.
	 * 
	 * @see    #float64_rem(long, long).
	 */
	public static double double_rem(double arg0, double arg1)
	{
		return Double.longBitsToDouble(float64_rem(Double.doubleToRawLongBits(arg0), 
				                                   Double.doubleToRawLongBits(arg1)) );
	}

	/**
	 * Wrapper method for <code>float64_sqrt()</code> double-precision 
	 * operation.
	 * 
	 * @param  <code>arg0</code> the operand of sqrt operation.
	 * 
	 * @return the result of sqrt operation.
	 * 
	 * @see    #float64_sqrt(long).
	 */
	public static double double_sqrt(double arg0)
	{
		return Double.longBitsToDouble(float64_sqrt(Double.doubleToRawLongBits(arg0)));
	}
	
	/**
	 * Wrapper method for <code>float64_eq()</code> double-precision 
	 * operation.
	 * 
	 * @param  <code>arg0</code> the first operand of eq operation.
	 * 
	 * @param  <code>arg1</code> the second operand of eq operation.
	 * 
	 * @return the result of eq operation 
	 * 
	 * @see    #float64_eq(long, long).
	 */
	public static boolean double_eq(double arg0, double arg1)
	{
		return float64_eq(Double.doubleToRawLongBits(arg0), 
				          Double.doubleToRawLongBits(arg1) ) == 1;
	}

	/** 
	 * Wrapper method for <code>float64_le()</code> double-precision 
	 * operation. 
	 * 
	 * @param  <code>arg0</code> the first operand of le operation.
	 * 
	 * @param  <code>arg1</code> the second operand of le operation.
	 * 
	 * @return the result of le operation.
	 * 
	 * @see    #float64_le(long, long).
	 */
	public static boolean double_le(double arg0, double arg1)
	{
		return float64_le(Double.doubleToRawLongBits(arg0), 
				          Double.doubleToRawLongBits(arg1) ) == 1;
	}

	/** 
	 * Wrapper method for <code>float64_lt()</code> double-precision 
	 * operation.
	 *  
	 * @param  <code>arg0</code> the first operand of lt operation.
	 * 
	 * @param  <code>arg1</code> the second operand of lt operation.
	 * 
	 * @return the result of lt operation 
	 * 
	 * @see    #float64_lt(long, long).
	 */
	public static boolean double_lt(double arg0, double arg1)
	{
		return float64_lt(Double.doubleToRawLongBits(arg0), 
				          Double.doubleToRawLongBits(arg1) ) == 1;
	}

	/** 
	 * Wrapper method for <code>float64_eq_signaling()</code> double-precision
	 * operation.
	 *  
	 * @param  <code>arg0</code> the first operand of eq signaling operation.
	 * 
	 * @param  <code>arg1</code> the second operand of eq signaling operation.
	 * 
	 * @return the result of eq signaling operation.
	 *  
	 * @see    #float64_eq_signaling(long, long).
	 */
	public static boolean double_eq_signaling(double arg0, double arg1)
	{
		return float64_eq_signaling(Double.doubleToRawLongBits(arg0), 
				                    Double.doubleToRawLongBits(arg1) ) == 1;
	}

	/** 
	 * Wrapper method for <code>float64_le_quiet()</code> double-precision 
	 * operation.
	 *  
	 * @param  <code>arg0</code> the first operand of le quiet operation.
	 * 
	 * @param  <code>arg1</code> the second operand of le quiet operation.
	 * 
	 * @return the result of le quiet operation.
	 *  
	 * @see    #float64_le_quiet(long, long).
	 */
	public static boolean double_le_quiet(double arg0, double arg1)
	{
		return float64_le_quiet(Double.doubleToRawLongBits(arg0), 
				                Double.doubleToRawLongBits(arg1) ) == 1;
	}

	/** 
	 * Wrapper method for <code>float64_lt_quiet()</code> double-precision 
	 * operation. 
	 * 
	 * @param  <code>arg0</code> the first operand of lt quiet operation.
	 * 
	 * @param  <code>arg1</code> the second operand of lt quiet operation.
	 * 
	 * @return the result of lt quiet operation.
	 * 
	 * @see    #float64_lt_quiet(long, long).
	 */
	public static boolean double_lt_quiet(double arg0, double arg1)
	{
		return float64_lt_quiet(Double.doubleToRawLongBits(arg0), 
				                Double.doubleToRawLongBits(arg1) ) == 1;
	}

	/**
	 * Wrapper method for <code>float64_is_signaling_nan()</code> 
	 * single-precision operation. 
	 * 
	 * @param  <code>arg0</code> the first operand of lt quiet operation.
	 * 
	 * @return <code>true</code> if argument is signaling NaN;
	 * 
     *         <code>false</code> otherwise.
	 * 
	 * @see    #float64_is_signaling_nan(long).
	 */
	public static boolean double_is_signaling_nan(double arg0)
	{
		return float64_is_signaling_nan(Double.doubleToRawLongBits(arg0)) == 1;
	}
}