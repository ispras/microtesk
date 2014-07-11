/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * PrimitiveUtils.java, Jul 11, 2014 2:42:55 PM Andrei Tatarnikov
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

import java.util.List;

public class PrimitiveUtils
{
    /**
     * Saves all AND primitives associated with the current primitive by using
     * OR rules to a list. Nested OR rules are resolved recursively. If the
     * current primitive is an AND rule, it is places to the list and the method
     * returns. 
     * 
     * @param source A primitives that serves as a source.
     * @param destination The list to which AND rules are to be saved.
     * 
     * @throws NullPointerException if any of the parameters equals null. 
     */

    public static void saveAllOrsToList(Primitive source, List<PrimitiveAND> destination)
    {
        if (null == source)
            throw new NullPointerException();
        
        if (null == destination)
            throw new NullPointerException();
        
        if (!source.isOrRule())
        {
            destination.add((PrimitiveAND) source);
            return;
        }

        for (Primitive o : ((PrimitiveOR) source).getORs())
           saveAllOrsToList(o, destination);
    }

    /**
     * Counts the number of childs (arguments) that have a specific type 
     * for the given primitive.
     * 
     * @param root Root primitive. 
     * @param kind Type of child primitives to be counted.
     * @return Number of childs of the given type.
     * 
     * @throws NullPointerException if any of the parameters equals null.
     */

    public static int getChildCount(PrimitiveAND root, Primitive.Kind kind)
    {
        if (null == root)
            throw new NullPointerException();

        if (null == kind)
            throw new NullPointerException();

        int count = 0;
        for (Primitive p : root.getArguments().values())
            if (p.getKind() == kind)
                count++;

        return count;
    }
}
