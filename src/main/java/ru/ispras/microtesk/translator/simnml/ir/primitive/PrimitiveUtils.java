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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive.Reference;

/**
 * The PrimitiveUtils class provides a range of utilities for working
 * with primitives. The class provides static members only and serves
 * as a namespace. 
 * 
 * @author Andrei Tatarnikov
 */

public final class PrimitiveUtils
{
    private PrimitiveUtils() {} 
    
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
        notNullCheck(source, "source");
        notNullCheck(destination, "destination");

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
        notNullCheck(root, "root");
        notNullCheck(kind, "kind");

        int count = 0;
        for (Primitive p : root.getArguments().values())
            if (p.getKind() == kind)
                count++;

        return count;
    }

    /**
     * Checks whether the given primitive is a leaf primitive.
     * A primitive is considered a leaf it does not have childs
     * (arguments) of the same type. An OR rule cannot be a leaf. 
     * 
     * @param primitive Primitive to be checked.
     * @return true if the primitive is a leaf or false otherwise.
     * 
     * @throws NullPointerException if the parameter equals null.
     */

    public static boolean isLeaf(Primitive primitive)
    {
        notNullCheck(primitive, "primitive");

        if (primitive.isOrRule())
            return false;

        return 0 == getChildCount(
            (PrimitiveAND) primitive, primitive.getKind());
    }

    /**
     * Checks whether the given primitive is a junction.
     * A junction is an AND-rule primitive that has more than one
     * child primitive (argument) of the same type as the junction
     * primitive itself. An OR rule is not a junction. 
     * 
     * @param primitive Primitive to be checked.
     * @return true if the primitive is a junction or false otherwise.
     * 
     * @throws NullPointerException if the parameter equals null.
     */

    public static boolean isJunction(Primitive primitive)
    {
        notNullCheck(primitive, "primitive");

        if (primitive.isOrRule())
            return false;

        return 1 < getChildCount(
            (PrimitiveAND) primitive, primitive.getKind());
    }

    /**
     * Counts non-junction parents of the given primitive.
     * 
     * @param primitive Primitive to be checked.
     * @return number of non-junction parents.
     * 
     * @throws NullPointerException if the parameter equals null.
     */

    public static int countNonJunctionParents(Primitive primitive)
    {
        notNullCheck(primitive, "primitive");

        int nonJunctionParents = 0;

        for (Reference ref : primitive.getParents())
            if (!PrimitiveUtils.isJunction(ref.getSource()))
                nonJunctionParents++;

        return nonJunctionParents;
    }

    /**
     * The PathCounter class helps count the number of possible paths from
     * a source (parent) primitive to a target (child) primitive. It memorizes
     * all previous results to avoid redundant tree traversals.
     * 
     * N.B. The class can be used only for OP primitives. Other primitive
     * kinds are not supported (there is no need for such a facility).
     * 
     * @author Andrei Tatarnikov
     */

    public static final class PathCounter
    {
        /**
         * Holds information on the number of possible paths from
         * some source primitive to target primitives.
         * 
         * @author Andrei Tatarnikov
         */

        private static final class Entry
        {
            /** 
             * Key - target name;
             * Value - number of paths from the source to the target.
             */

            private final Map<String, Integer> targets = new HashMap<String, Integer>();
        }

        /**
         * Key - source name;
         * Value - information on paths from the source to targets (Entry).
         */

        private final Map<String, Entry> entries;

        /**
         * Constructs a path counter object. 
         */

        public PathCounter()
        {
            this.entries = new HashMap<String, Entry>(); 
        }

        /**
         * Saves information on the number of paths between the source 
         * and the target. This information will be used to avoid
         * redundant calculations the next time it is requested.
         * 
         * @param source Source (parent) primitive name. 
         * @param target Target (child) primitive name.
         * @param count Number of paths between the source and the target.
         */

        private void remember(String source, String target, int count)
        {
            final Entry entry;
            if (entries.containsKey(source))
            {
                entry = entries.get(source);
            }
            else
            {
                entry = new Entry();
                entries.put(source, entry);
            }

            entry.targets.put(target, count);
        }

        /**
         * Counts the number of possible paths from the source (parent)
         * primitive to the target (child) primitive. The method recursively
         * traverses all AND- and OR- rules starting from the source primitive
         * searching for child primitives which name equals the target name.
         * If the source is not an OP primitive (operation) 0 is returned.
         * 
         * To avoid redundant traversals and calculations, the method
         * memorizes all previous results. This information is used
         * when it is requested again. This is important as the method 
         * works recursively. That is information on paths from a parent
         * is calculated as a sum of the result of its childs. In this case,
         * there is no need to traverse all the hierarchy again information
         * on child primitives has already been calculated.
         * 
         * @param source Source (parent) primitive. 
         * @param target Target (child) primitive name.
         * @return The number of possible paths from the source to the target.
         * 
         * @throws NullPointerException if any of the parameters equals null.  
         */

        public int getPathCount(Primitive source, String target)
        {
            notNullCheck(source, "from");
            notNullCheck(target, "to");

            if (source.getKind() != Primitive.Kind.OP)
                return 0;

            if (entries.containsKey(source.getName()))
            {
                final Entry entry = entries.get(source.getName());
                if (entry.targets.containsKey(target))
                    return entry.targets.get(target); 
            }

            if (target.equals(source.getName()))
            {   
                remember(source.getName(), target, 1); 
                return 1;
            }

            final Collection<Primitive> childs = source.isOrRule() ?
                ((PrimitiveOR)  source).getORs() : 
                ((PrimitiveAND) source).getArguments().values();

            int count = 0;
            for (Primitive p : childs)
                count += getPathCount(p, target);

            remember(source.getName(), target, count); 
            return count;
        }
    }

    private static void notNullCheck(Object o, String name)
    {
        if (null == o)
            throw new NullPointerException(
                String.format("The %s parameter is null.", name));
    }
}
