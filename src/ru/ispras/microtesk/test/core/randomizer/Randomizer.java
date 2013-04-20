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

import ru.ispras.microtesk.model.api.rawdata.RawData;

/**
 * This class is a wrapper around a random number generator. It is responsible
 * for generating random objects (numbers, strings, etc.) and filling storages
 * (arrays, collections, etc.) with random data.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class Randomizer
{
    /// The random number generator being used by the randomizer.
    private IRandomGenerator generator = new ModifiedLaggedFibonacci();
    
    /**
     * Returns the current random number generator.
     *
     * @return the random number generator.
     */     
    public IRandomGenerator getGenerator()
    {
        return generator;
    }

    /**
     * Sets the current random number generator.
     *
     * @param generator the random number generator to be set.
     */
    public void setGenerator(final IRandomGenerator generator)
    {
        this.generator = generator;
    }
    
    /**
     * Sets the new seed of the random number generator.
     *
     * @param seed the seed to be set.
     */
    public void setSeed(int seed)
    {
        generator.seed(seed);
    }
    
    /// @return A random byte.
    public byte nextByte() { return (byte)next(); }
    /// @return A random char.
    public char nextChar() { return (char)next(); }
    /// @return A random int.
    public int  nextInt()  { return ( int)next(); }
    /// @return A random long.
    public long nextLong() { return (long)next(); }

    /**
     * Fills the byte array with random data.
     *
     * @param data the array to be randomized.
     */
    public void fillByteArray(byte[] data)
    {
        for(int i = 0; i < data.length; i++)
            { data[i] = nextByte(); }
    }
    
    /**
     * Fills the char array with random data.
     *
     * @param data the array to be randomized.
     */
    public void fillCharArray(char[] data)
    {
        for(int i = 0; i < data.length; i++)
            { data[i] = nextChar(); }
    }

    /**
     * Fills the int array with random data.
     *
     * @param data the array to be randomized.
     */
    public void fillIntArray(int[] data)
    {
        for(int i = 0; i < data.length; i++)
            { data[i] = nextInt(); }
    }
    
    /**
     * Fills the long array with random data.
     *
     * @param data the array to be randomized.
     */
    public void fillLongArray(long[] data)
    {
        for(int i = 0; i < data.length; i++)
            { data[i] = nextLong(); }
    }
    
    /**
     * Fills the raw data storage with random data.
     *
     * @param data the raw data storage to be randomized.
     */
    public void fillRawData(RawData data)
    {
        final int s = data.getBitSize() / RawData.BITS_IN_BYTE +
                     (data.getBitSize() % RawData.BITS_IN_BYTE != 0 ? 1 : 0);
        
        for(int i = 0; i < s; i++)
            { data.setByte(i, nextChar()); }
    }

    private int next()
    {
        return generator.next();
    }
}