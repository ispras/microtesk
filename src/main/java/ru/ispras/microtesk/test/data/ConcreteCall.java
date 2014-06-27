/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ConcreteCall.java, May 9, 2013, 11:00:07 PM PM Andrei Tatarnikov
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

package ru.ispras.microtesk.test.data;

import java.util.Map;
import ru.ispras.microtesk.model.api.instruction.InstructionCall;

public final class ConcreteCall
{
    private final String name;
    private final Map<String, Object> attributes;
    private final InstructionCall executable;

    public ConcreteCall(
        String name,
        Map<String, Object> attributes,
        InstructionCall executable
        )
    {
        this.name = name;
        this.attributes = attributes;
        this.executable = executable;
    }

    public String getName()
    {
        return name;
    }

    public Object getAttribute(String name)
    {
        if (null == attributes)
            return null;

        return attributes.get(name);
    }

    public InstructionCall getExecutable()
    {
        return executable;
    }
}
