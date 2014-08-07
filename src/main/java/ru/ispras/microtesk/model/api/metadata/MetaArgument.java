/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MetaArgument.java, Nov 15, 2012 2:57:18 PM Andrei Tatarnikov
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

import java.util.Collection;

/**
 * The MetaArgument class describes instruction arguments.
 * 
 * @author Andrei Tatarnikov
 */

public final class MetaArgument implements MetaData
{
    private final String name;
    private final Collection<String> typeNames; 

    public MetaArgument(String name, Collection<String> typeNames)
    {
        this.name  = name;
        this.typeNames = typeNames;
    }

    /**
     * Returns the name of the argument.
     * 
     * @return Argument name.
     */

    @Override
    public String getName()
    {
        return name;
    }

    /**
     * Returns an iterator for the collection of type names associated with the argument.  
     * 
     * @return An Iterable object that refers to the collection of type names
     *         (e.g. addressing mode names).
     */

    public Iterable<String> getTypeNames()
    {
        return typeNames;
    }
}
