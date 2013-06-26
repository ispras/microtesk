/*
 * Copyright 2013 ISP RAS (http://www.ispras.ru), UniTESK Lab (http://www.unitesk.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
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
 * This class is a basic data replacement policy which is applied 
 * when a cache miss happens.
 * 
 * @author <a href="mailto:leonsia@ispras.ru">Taya Sergeeva</a>
 */

public class Policy
{
    // / Number of sets
    private int associativity;

    public Policy(int associativity)
    {
        this.associativity = associativity;
    }

    /**
     * Returns associativity.
     * 
     * @return associativity.
     */
    public int getAssociativity()
    {
        return associativity;
    }

    /**
     * Hit happens.
     */
    public void accessLine(int index)
    {
    }

    /**
     * Miss happened.
     */
    public int chooseVictim()
    {
        return 0;
    }
}