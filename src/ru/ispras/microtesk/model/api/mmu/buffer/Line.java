/*
 * Copyright 2012-2013 ISP RAS (http://www.ispras.ru), UniTESK Lab (http://www.unitesk.com)
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

package ru.ispras.microtesk.model.api.mmu.buffer;

import ru.ispras.microtesk.model.api.mmu.policy.EPolicy;
import ru.ispras.formula.data.types.bitvector.BitVector;

/**
 * This class represents line of a buffer.
 * 
 * @author <a href="mailto:leonsia@ispras.ru">Tatiana Sergeeva</a>
 */
public abstract class Line extends Buffer<Line>
{
    private BitVector data;

    /**
     * Constructs a line with a given size.
     * 
     * @param size the number of sets.
     */
    public Line(int size)
    {
        super(1, 1, EPolicy.FIFO);
        this.data = BitVector.createEmpty(size);
    }

    /**
     * Checks whether there is a buffer hit for the given address.
     * 
     * @return the index in the buffer set.
     */
    public abstract boolean match(final Address address);

    /**
     * Returns the index of the buffer set for the given address.
     * 
     * @return the index in the buffer set.
     */
    public abstract int index(final Address address);

    /**
     * Returns data.
     * 
     * @return data.
     */
    public BitVector getData()
    {
        return data;
    }

    public void setData(BitVector data)
    {
        this.data = data;
    }

    /**
     * Reads data with a given address.
     * 
     * @return data.
     */
    public BitVector read(Address address)
    {
        return getData();
    }

    /**
     * Writes data with a given address and data. If there is a hit returns old
     * data, otherwise it returns null.
     * 
     * @returns data.
     */
    public BitVector write(Address address, BitVector data)
    {
        if (match(address))
        {
            BitVector old = data;

            setData(data);

            return old;
        }

        return null;
    }
}