/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprTreeVisitor.java, Dec 17, 2013 12:32:29 PM Andrei Tatarnikov
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.model.api.mmu;

import java.util.LinkedList;

/**
 * This enumeration contains basic data replacement policies.
 *
 * @author <a href="mailto:leonsia@ispras.ru">Tatiana Sergeeva</a>
 */

public enum Policy
{
    /**
     * The FIFO (First In - First Out) data replacement policy. 
     */
    FIFO
    {
        /**
         * Keeps line indexes in the order of their usage.
         */
        private LinkedList<Integer> fifo;

        @Override
        public void initialize(int associativity)
        {
            fifo = new LinkedList<Integer>();

            for (int i = 0; i < associativity; i++)
            {
                fifo.add(i);
            }
        }

        @Override
        public void accessLine(int index)
        {
            for (int i = 0; i < fifo.size(); i++)
            {
                if (fifo.get(i) == index)
                {
                    fifo.remove(i);
                    fifo.add(index);

                    return;
                }
            }

            assert false : "Incorrect state: index cannot be found";
        }

        @Override
        public int chooseVictim()
        {
            return fifo.peek();
        }
    },

    /**
     * The LRU (Least Recently Used) data replacement policy. 
     */
    LRU
    {
        @Override
        public void initialize(int associativity)
        {
            // TODO:
        }

        @Override
        public void accessLine(int index)
        {
            // TODO:
        }

        @Override
        public int chooseVictim()
        {
            // TODO:
            return 0;
        }
    },

    /**
     * The PLRU (Pseudo Least Recently Used) data replacement policy. 
     */
    PLRU
    {
        /**
         * The associativity.
         */
        private int associativity;

        /**
         * The PLRU bits.
         */
        private int bits;

        /**
         * {@inheritDoc}
         *
         * The associativity should not exceed 32.
         */
        @Override
        public void initialize(int associativity)
        {
            this.associativity = associativity;
        }

        @Override
        public void accessLine(int index)
        {
            setBit(index);
        }

        @Override
        public int chooseVictim()
        {
            for (int i = 0; i < associativity; i++)
            {
                if ((bits & (1 << i)) == 0)
                {
                    setBit(i);
                    return i;
                }
            }
  
            assert false : "Incorrect state: all bits are set to 1";
            return -1;
        }
        
        private void setBit(int i)
        {
            final int mask = (1 << i);

            bits |= mask;

            if (bits == ((1 << associativity) - 1))
                bits = mask;
        }
    };

    /**
     * Sets the buffer associativity.
     * 
     * @param associativity the buffer associativity.
     */
    public abstract void initialize(int associativity);

    /**
     * Handles a buffer hit.
     * 
     * @param index the line being hit.
     */
    public abstract void accessLine(int index);

    /**
     * Handles a buffer miss.
     * 
     * @return the line to be replaced.
     */
    public abstract int chooseVictim();
}