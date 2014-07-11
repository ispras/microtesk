/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Shortcut.java, Jul 8, 2014 5:44:19 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import java.util.Map;

// TODO: UNDER DESIGN (NEED TO DEFINE THE BOOTOM LINE)
public final class Shortcut
{
    private final String                       name;
    private final String                contextName;
    private final Map<String, Primitive>  arguments;
    private final PrimitiveAND                 root;

    public Shortcut(
        String name,
        String contextName,
        Map<String, Primitive> arguments,
        PrimitiveAND root
        )
    {
        this.name        = name;
        this.contextName = contextName;
        this.arguments   = arguments;
        this.root        = root; 
    }

    public String getName()
    {
        return name;
    }

    public String getContextName()
    {
        return contextName;
    }

    public Map<String, Primitive> getArguments()
    {
        return arguments;
    }

    public PrimitiveAND getRoot()
    {
        return root;
    }
}
