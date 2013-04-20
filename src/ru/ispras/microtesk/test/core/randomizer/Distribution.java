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
    private int   m;
    private int[] p;

    public Distribution(final int[] weights)
    {
        m = 0;
        p = new int[weights.length];
        
        for(int i = 0; i < weights.length; i++)
        {
            assert weights[i] > 0;

            p[i] = (m += weights[i]);
        }
    }
    
    public int getMaxProbability()
    {
        return m;
    }

    public int getWeight(int variant)
    {
        return p[variant] - (variant != 0 ? p [variant - 1] : 0);
    }
    
    public int getProbability(int variant)
    {
        assert 0 <= variant && variant < p.length;

        return p[variant];
    }
    
    public int getVariant(int probability)
    {
        assert probability < m;
        
        return binarySearch(p, 0, p.length - 1, probability);
    }
    
    /**
     * Finds the index <code>i</code> from [<code>lo</code>, <code>hi</code>]
     * such that <code>array[i-1] <= value && value < array[i]</code>.
     * Note that <code>array[-1]</code> is assumed to be zero.
     *
     * @return i such that <code>array[i-1] <= value && value < array[i]</code>.
     * @param array the ordered array of integer values.
     * @param lo the low bound of array indices.
     * @param hi the high bound of array indices.
     * @param value the value being searched.
     */
    private int binarySearch(int[] array, int lo, int hi, int value)
    {
        if(lo == hi)     { return lo; }
        if(hi == lo + 1) { return value < array[hi] ? lo : hi; }
            
        final int i = (lo + hi) >> 1;
        
        if(value < array[i])
            { return binarySearch(array, lo, i, value); }
        else    
            { return binarySearch(array, i + 1, hi, value); }
    }
}