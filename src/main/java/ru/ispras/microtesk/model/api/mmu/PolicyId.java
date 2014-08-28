/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * PolicyId.java, Aug 28, 2014 1:59:36 PM Tatiana Sergeeva
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

/**
 * This enumeration contains basic data replacement policies.
 *
 * @author <a href="mailto:leonsia@ispras.ru">Tatiana Sergeeva</a>
 */

public enum PolicyId
{
    /**
     * The FIFO (First In - First Out) data replacement policy. 
     */

    FIFO
    {
        @Override
        public Policy newPolicy(int associativity)
        {
            return new PolicyFIFO(associativity);
        }
    },

    /**
     * The LRU (Least Recently Used) data replacement policy. 
     */

    LRU
    {
        @Override
        public Policy newPolicy(int associativity)
        {
            return new PolicyLRU(associativity);
        }
    },

    /**
     * The PLRU (Pseudo Least Recently Used) data replacement policy. 
     */
    
    PLRU
    {
        @Override
        public Policy newPolicy(int associativity)
        {
            return new PolicyPLRU(associativity);
        }
    };

    public abstract Policy newPolicy(int associativity);
}
