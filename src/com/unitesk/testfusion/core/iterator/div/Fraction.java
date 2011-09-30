/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Fraction.java,v 1.2 2009/01/30 10:40:23 kozlov Exp $
 */

package com.unitesk.testfusion.core.iterator.div;

/**
 * Class <code>Fraction</code> represents a fraction.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Fraction
{
    /** Numerator. */
	public long n;
    
    /** Denominator. */
	public long d;

    /**
     * Constructor.
     * 
     * @param <code>n</code> the numerator.
     * @param <code>d</code> the denominator.
     */
	public Fraction(long n, long d)
	{
		this.n = n;
		this.d = d;
	}
	
    /** Default constructor. */
    public Fraction()
    {
        this(0, 0);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the fraction
     */
    protected Fraction(Fraction r)
    {
        n = r.n;
        d = r.d;
    }
    
    /**
     * Returns the numerator of the fraction.
     * 
     * @return the numberator of the fraction.
     */
	public long getNumerator()
	{
		return n;
	}
	
    /**
     * Sets the numerator of the fraction.
     * 
     * @param <code>n</code> the numberator of the fraction.
     */
    public void setNumerator(long n)
    {
        this.n = n;
    }
    
    /**
     * Returns the denominator of the fraction.
     * 
     * @return the denominator of the fraction.
     */
    public long getDenominator()
	{
		return d;
	}
	
    /**
     * Sets the denominator of the fraction.
     * 
     * @param <code>d</code> the denominator of the fraction.
     */
	public void setDenominator(long d)
	{
		this.d = d;
	}

    /**
     * Returns the greatest common divisor (GCD) of the numerator and the
     * denominator.
     * 
     * @return the greatest common divisor of the numerator and the denominator.
     */
	public long getGreatestCommonDivisor()
	{
	    long n = this.n;
	    long d = this.d;

	    long k = n;

	    if(n < d)
	        { k = d; d = n; }

	    while(d >= 1 && (n = k) != d)
	        { d = (n % (k = d)); }

	    return (d != 0) ? d : k;
	}

    /**
     * Checks if the fraction is irreducible, i.e. GCD(n, d) = 1.
     * 
     * @return <code>true</code> if the fraction is irreducible;
     *         <code>false</code> otherwise.
     */
	public boolean isIrreducible()
	{
	    return getGreatestCommonDivisor() == 1;
	}

    /** Reduces the fraction. */
	public void reduce()
	{
	    long gcd = getGreatestCommonDivisor();

	    if(gcd != 1)
	        { n /= gcd; d /= gcd; }
	}
    
    /**
     * Checks if this fraction is simpler than the other fraction.
     * 
     * @param  <code>rhs</code> the fraction.
     * 
     * @return <code>true</code> if this fraction is simpler than the other
     *         fraction; <code>false</code> otherwise.
     */
	public boolean isSimplerThan(Fraction rhs)
	{
	    return n <= rhs.n && d <= rhs.d && !equals(rhs);
	}

    /**
     * Returns the mediant of the fractions.
     * 
     * @param  <code>rhs</code> the fraction.
     * 
     * @return the mediant of the fractions.
     */
	public Fraction getMediant(Fraction rhs)
	{
	    return new Fraction(n + rhs.n, d + rhs.d);
	}

    /**
     * Calculates parent partition of the fraction.
     * 
     * @param <code>sp</code> the simplest parent (output).
     * @param <code>cp</code> the common parent (output).
     */
	public void parentPartition(Fraction sp, Fraction cp)
	{
	    long q_n_1, q_n_2;
	    long p_n_1, p_n_2;

	    long n = this.n;
	    long d = this.d;

	    long a, b;

	    boolean reverse = n > d;

	    if(reverse)
	        { a = d; d = n; n = a; }

	    a = d / n;
	    b = d % n;

	    d = n;
	    n = b;

	    q_n_1 = a;
	    q_n_2 = 1;

	    p_n_1 = 1;
	    p_n_2 = 0;

	    while(n != 1)
	    {
	        a = d / n;
	        b = d % n;

	        d = n;
	        n = b;

	        q_n_1 = a * (b = q_n_1) + q_n_2;
	        q_n_2 = b;

	        p_n_1 = a * (b = p_n_1) + p_n_2;
	        p_n_2 = b;
	    }

	    if(reverse)
	        { b = p_n_1; p_n_1 = q_n_1; q_n_1 = b; }

	    if(a != 1) 
	    {
	        sp.n = p_n_1;
	        sp.d = q_n_1;

	        cp.n = this.n - p_n_1;
	        cp.d = this.d - q_n_1;
	    }
	    else
	    {
	        cp.n = p_n_1;
	        cp.d = q_n_1;

	        sp.n = this.n - p_n_1;
	        sp.d = this.d - q_n_1;
	    }
	}
    
    /**
     * Compares two fractions.
     * 
     * @return <code>true</code> if the fractions are equal; <code>false</code>
     *         otherwise.
     */
    public boolean equals(Object o)
    {
        if(!(o instanceof Fraction))
            { return false; }
        
        Fraction r = (Fraction)o;

        return n == r.n && d == r.d;
    }
    
    /**
     * Returns a hash code value for the object.
     */
    public int hashCode()
    {
        Long num = new Long(n);
        Long den = new Long(d);
        
        return (int)(31 * num.hashCode() + den.hashCode());
    }
    
    /**
     * Returns a copy of the fraction.
     * 
     * @return a copy of the fraction.
     */
    public Fraction clone()
    {
        return new Fraction(this);
    }
}