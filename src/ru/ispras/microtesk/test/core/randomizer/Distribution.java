/*
 * Copyright 2013 ISP RAS (http://www.ispras.ru), UniTESK Lab (http://www.unitesk.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.ispras.microtesk.test.core.randomizer;

/**
 * This class represents a discrete probability distribution.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Distribution
{
    private int[] p;
    private int[] v;

    public Distribution(final int[] variants, final int[] weights)
    {
        init(variants, weights);
    }

    public Distribution(final int[] weights)
    {
        init(getNaturalSeries(weights.length), weights);
    }

    public int getMaxWeight()
    {
        return p[p.length - 1];
    }

    public int getWeight(int variant)
    {
        return p[variant] - (variant != 0 ? p[variant - 1] : 0);
    }
    
    public void setWeight(int variant, int weight)
    {
        final int delta = weight - getWeight(variant);

        for(int i = variant; i < p.length; i++)
            { p[i] += delta; }
    }

    public int getLessOrEqualWeight(int variant)
    {
        return p[variant];
    }
    
    public int getVariant(int random_weight)
    {
        final int i = binarySearch(p, 0, p.length - 1, random_weight);

        return v[i];
    }
    
    /**
     * Finds the index <code>i</code> from <code>[a, b]</code> such that
     * <code>x[i-1] <= v && v < x[i]</code>.
     * Note that <code>x[-1]</code> is assumed to be zero.
     *
     * @return i such that <code>x[i-1] <= v && v < x[i]</code>.
     * @param x the ordered array of integer values.
     * @param a the low bound of the array indices.
     * @param b the high bound of the array indices.
     * @param v the value being searched.
     */
    private int binarySearch(int[] x, int a, int b, int v)
    {
        if(a == b)     { return a; }
        if(b == a + 1) { return x[a] <= v ? b : a; }
            
        final int i = (a + b) >> 1;
       
        if(x[i - 1] <= v && v < x[i])
            { return i; }

        if(v < x[i])
            { return binarySearch(x, a, i - 1, v); }
        else    
            { return binarySearch(x, i + 1, b, v); }
    }

    /**
     * Returns the natural series of the size <code>n</code> (0, 1, ... n-1).
     *
     * @return the natural series.
     * @param n the size of the series.
     */
    private int[] getNaturalSeries(int n)
    {
        int[] result = new int[n];

        for(int i = 0; i < n; i++)
            { result[i] = i; }

        return result;
    }

    /**
     * Initializes the probility distribution.
     *
     * @param variants the values of the variate.
     * @param weights  the random biases of the values.
     */
    private void init(final int[] variants, final int[] weights)
    {
        assert variants.length != 0;
        assert variants.length == weights.length;

        p = new int[weights.length];
        v = new int[weights.length];
    
        int m = 0;
        for(int i = 0; i < weights.length; i++)
        {
            assert weights[i] >= 0;

            p[i] = (m += weights[i]);
            v[i] = variants[i];
        }
    }
}

