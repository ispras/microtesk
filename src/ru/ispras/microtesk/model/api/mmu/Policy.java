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
 * This class implements interface of Policy which will be applied to data changing in cache if miss happens. 
 *
 * @author <a href="mailto:leonsia@ispras.ru">Taya Sergeeva</a>
 */

public abstract class Policy
{
    private int associativity;

    public Policy(int associativity)
    {
        this.associativity = associativity;
    }

    /**
     * Returns associativity (number of lines)
     */

    public int getAssociativity()
    {
        return associativity;
    }

    /**
     * Hit happened
     */
    public abstract void accessLine(int index);

    /**
     * Miss happened
     */
    public abstract int chooseVictim();
}