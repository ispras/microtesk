/*
 * Copyright (c) 2012 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MetaAddressingMode.java, Nov 15, 2012 2:47:49 PM Andrei Tatarnikov
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

import java.util.Set;

/**
 * The MetaAddressingMode class holds information on the specified addressing mode.
 * 
 * @author Andrei Tatarnikov
 */

public final class MetaAddressingMode implements MetaData
{
    private final String name;
    private final Set<String> argumentNames; 

    public MetaAddressingMode(String name, Set<String> argumentNames)
    {
        this.name = name;
        this.argumentNames = argumentNames;
    }

    /**
     * Returns the name of the addressing mode.
     * 
     * @return Mode name.
     */

    @Override
    public String getName()
    {
        return name;
    }

    /**
     * Returns the list of addressing mode argument. 
     * 
     * @return Collection of argument names.
     */

    public Iterable<String> getArgumentNames()
    {
        return argumentNames;
    }

    /**
     * Checks whether the addressing mode has an argument with
     * the specified name.
     * 
     * @param name Argument name.
     * @return {@code true} if the argument is defined of {@code false}
     * otherwise.  
     */

    public boolean isArgumentDefined(String name)
    {
        return argumentNames.contains(name);
    }
}
