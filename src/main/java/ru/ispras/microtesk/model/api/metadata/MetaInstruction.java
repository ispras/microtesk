/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MetaInstruction.java, Nov 15, 2012 3:02:03 PM Andrei Tatarnikov
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
 * The MetaInstruction class stores information on the given instruction.
 * 
 * @author Andrei Tatarnikov
 */

public final class MetaInstruction implements MetaData
{
    private final String name;
    private final Collection<MetaArgument> args;
    private final Collection<MetaSituation> situations;

    public MetaInstruction(
        String name,
        Collection<MetaArgument> args,
        Collection<MetaSituation> situations
        )
    {
        this.name = name;
        this.args = args;
        this.situations = situations;
    }

    /**
     * Returns the instruction name.
     * 
     * @return The instruction name.
     */

    @Override
    public String getName()
    {
        return name;
    }

    /**
     * Returns an Iterable object for the collection of instruction arguments. 
     * 
     * @return Iterable object.
     */

    public Iterable<MetaArgument> getArguments()
    {
        return args;
    }

    /**
     * Returns an iterator for the collection of test situations.
     * 
     * @return An Iterable object.
     */

    public final Iterable<MetaSituation> getSituations()
    {
        return situations;
    }
}
