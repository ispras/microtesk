/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MetaSituation.java, Nov 15, 2012 3:28:10 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.api.metadata;

/**
 * The MetaSituation class describes test situations.
 * 
 * @author Andrei Tatarnikov
 */

public final class MetaSituation implements MetaData
{
    private final String name;

    public MetaSituation(String name)
    {
        this.name  = name;
    }

    /**
     * Returns the name of the test situation.
     *  
     * @return Situation name.
     */

    @Override
    public String getName()
    {
        return name;
    }
}
