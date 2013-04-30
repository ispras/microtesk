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

package ru.ispras.microtesk.model.api.mmu;

/**
 * This class implements Pseudo Least Recently Used (PLRU) algorithm.
 *
 * @author <a href="mailto:leonsia@ispras.ru">Taya Sergeeva</a>
 */

public class PLRU extends Policy
{
    public int bits;

    public PLRU(int associativity)
    {
        super(associativity);

        assert associativity > 0;
        assert associativity <= Integer.SIZE - 1;

        resetBits();
    }

    private void resetBits()
    {
        this.bits = 0;
    }

    private void setBit(int bitIndex)
    {
        bits |= (1 << bitIndex);

        if (bits == ((1 << getAssociativity()) - 1))
            resetBits();
    }
    
    /**
     * {@inheritDoc}
     * 
     * If hit occurs:
     * if bit accesses to i_th cell, then i_th bit is set to 1;
     * if all bits equal to 1, then all of them are set to zero.
     */
    
    @Override
    public void accessLine(int index)
    {
        setBit(index);
    }

    /**
     * {@inheritDoc}
     * If miss happened the index of first nonzero bit is look for.
     */
    
    @Override
    public int chooseVictim()
    {
        for (int index = 0; index < getAssociativity(); ++index)
        {
            if ((bits & (1 << index)) == 0)
            {
                setBit(index);
                return index;
            }
        }

        assert false : "Incorrect state: all bits are set to 1";
        return -1;
    }
}
