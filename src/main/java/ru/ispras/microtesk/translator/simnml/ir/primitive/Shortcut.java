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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Shortcut class describes a shortcut (a short way) to address
 * a group of operation within the operation tree (some specific path)
 * that describes a composite operation. In most specifications, there
 * are paths in the operation tree can be built in an unambiguous way
 * (there is no need for external information or a decision made by a client).
 * To simplify describing composite instructions calls in test templates,
 * all such paths are saved as shortcuts associated with their target
 * operations (the point there the path ends, most important operation
 * that distinguishes a specific path from other similar paths).
 * 
 * @author Andrei Tatarnikov
 */

public final class Shortcut
{
    private final PrimitiveAND               target;
    private final String                contextName;
    private final PrimitiveAND                entry;
    private final Map<String, Primitive>  arguments;

    /**
     * Constructs a shortcut object. The shortcut object describes how
     * to create the target operation and all other operations it requires
     * from the starting point called entry. The context is the name of
     * the operation that accepts the composite operation built with the
     * help of the shortcut as an argument. The map of arguments is built
     * by traversing the path.   
     * 
     * @param target The target operation of the shortcut.
     * @param contextName The identifier of the context in which the
     * shortcut can be called. 
     * @param entry The entry point where the path starts (the top point).
     * 
     * @throws NullPointerException if any of the parameters equals null.
     */

    public Shortcut(
        PrimitiveAND target,
        String  contextName,
        PrimitiveAND  entry
        )
    {
        notNullCheck(target, "target");
        notNullCheck(contextName, "contextName");
        notNullCheck(entry, "entry");

        if (!(target.getKind() == Primitive.Kind.OP) 
          && (target.getKind() == entry.getKind()))
            throw new IllegalArgumentException();

        this.target      = target;
        this.contextName = contextName;
        this.entry       = entry;
        this.arguments   = new LinkedHashMap<String, Primitive>();

        saveArguments(entry, false);
    }

    private void saveArguments(PrimitiveAND root, boolean reachedTarget)
    {
        for (Map.Entry<String, Primitive> e: root.getArguments().entrySet())
        {
            final String    argName = e.getKey();
            final Primitive argType = e.getValue();

            if (!reachedTarget && (argType.getKind() == Primitive.Kind.OP))
            {
                if (argType.isOrRule())
                    throw new IllegalArgumentException();

                saveArguments((PrimitiveAND) argType, argType == target);
            }
            else
            {
                registerArgument(argName, argType);
            }
        }
    }

    private void registerArgument(String argName, Primitive argType)
    {
        if (arguments.containsKey(argName))
            ;

        arguments.put(argName, argType);
    }

    /**
     * Returns the name of the shortcut. Corresponds to the name
     * of the target operation.
     * 
     * @return Shortcut name.
     */

    public String getName()
    {
        return target.getName();
    }
    
    /**
     * Returns the target operation.
     * 
     * @return Target operation.
     */

    public PrimitiveAND getTarget()
    {
        return target;
    }

    public String getContextName()
    {
        return contextName;
    }

    public PrimitiveAND getEntry()
    {
        return entry;
    }

    public Map<String, Primitive> getArguments()
    {
        return arguments;
    }

    private static void notNullCheck(Object o, String name)
    {
        if (null == o)
            throw new NullPointerException(
                String.format("The %s parameter is null.", name));
    }
}
