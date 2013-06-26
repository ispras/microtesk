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

import ru.ispras.microtesk.model.api.mmu.EPolicy;
import ru.ispras.microtesk.model.api.rawdata.RawData;
import ru.ispras.microtesk.model.api.rawdata.RawDataStore;

/**
 * This class represents line of a buffer.
 * 
 * @author <a href="mailto:leonsia@ispras.ru">Tatiana Sergeeva</a>
 */
public abstract class Line extends Buffer<Line>
{
    private RawData data;

    /**
     * Constructs a line with a given size.
     * 
     * @param size the number of sets.
     */
    public Line(int size)
    {
        super(1, 1, EPolicy.FIFO);
        this.data = new RawDataStore(size);
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
    public RawData getData()
    {
        return data;
    }

    public void setData(RawData data)
    {
        this.data = data;
    }

    /**
     * Reads data with a given address.
     * 
     * @return data.
     */
    public RawData read(Address address)
    {
        return getData();
    }

    /**
     * Writes data with a given address and data. If there is a hit returns old
     * data, otherwise it returns null.
     * 
     * @returns data.
     */
    public RawData write(Address address, RawData data)
    {
        if (match(address))
        {
            RawData old = data;

            setData(data);

            return old;
        }

        return null;
    }
}