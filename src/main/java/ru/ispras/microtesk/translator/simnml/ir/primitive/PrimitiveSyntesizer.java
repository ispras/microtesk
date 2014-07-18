/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * PrimitiveSyntesizer.java, Jul 17, 2014 7:35:31 PM Andrei Tatarnikov
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.ispras.microtesk.translator.antlrex.log.ESenderKind;
import ru.ispras.microtesk.translator.antlrex.log.ILogStore;
import ru.ispras.microtesk.translator.antlrex.log.Logger;

import static ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveUtils.*;

/**
 * The PrimitiveSyntesizer class provides facilities to analyze information
 * on relations between operations and to synthesize on its basis the following
 * elements:<pre></pre>
 * 
 * 1. The list of root operations that includes all operations described by
 * AND rules which have no parents. Such operations serves as entry points in
 * composite operations describing instruction calls.<pre></pre>
 * 
 * 2. Shortcuts for leaf (have no child operations) and junction (have more
 * than one child operations) operations that allow addressing (instantiating
 * with all required parent operations) these operation in various contexts.
 * Shortcut is created when there is an unambiguous way to resolve all
 * dependencies of parent operations on the way from an entry operation to
 * a target operation. Shortcuts are added to IR of corresponding target 
 * operations.<pre></pre>
 * 
 * @author Andrei Tatarnikov
 */

public final class PrimitiveSyntesizer extends Logger
{
    /**
     * Name for the fake operation (OR rule) that unites all root operations
     * described in the specification (AND rules that have no parents).
     * This identifier is used a context name when a shortcut is addressed
     * from the topmost level of operation nesting in test templates. 
     */

    private static final String ROOT_ID = "#root"; 

    private final Collection<Primitive> operations;
    private final List<Primitive> roots;
    private boolean isSyntesized;

    /**
     * Constructs a PrimitiveSyntesizer object.
     * 
     * @param operations A list of operation IR objects to be analyzed.
     * @param fileName Specification file name. 
     * @param log log Log object that stores information about events
     * and issues that may occur.
     * 
     * @throws NullPointerException if any of the parameters equals null.
     */

    public PrimitiveSyntesizer(
        Collection<Primitive> operations,
        String fileName,
        ILogStore log
        )
    {
        super(ESenderKind.EMITTER, fileName, log);

        if (null == operations)
            throw new NullPointerException();

        this.operations   = operations;
        this.roots        = new ArrayList<Primitive>();
        this.isSyntesized = false;
    }

    /**
     * Synthesizes the list of root operations and shortcuts for all
     * "interesting" operations (leafs and junctions) based on the list
     * of all defined operations. Details on building shortcuts are
     * in the class description.
     * 
     * @return <code>true</code> if the information was successfully
     * synthesized or <code>false</code> otherwise.
     */

    public boolean syntesize()
    {
        if (isSyntesized)
        {
            reportWarning(ALREADY_SYNTHESIZED);
            return true;
        }

        if (operations.isEmpty())
        {
            reportError(NO_OPERATIONS);
            return false;
        }

        if (!syntesizeRoots())
            return false;

        syntesizeShortcuts();

        isSyntesized = true;
        return true;
    }

    /**
     * Checks whether the information was successfully synthesized.
     * 
     * @return <code>true</code> if the information was successfully
     * synthesized or <code>false</code> otherwise.  
     */

    public boolean isSyntesized() 
    {
        return isSyntesized;
    }

    /**
     * Returns the list of root operations. A root operation is an AND rule
     * that has no parents. The list is synthesized.
     * 
     * @return List of root operations.
     */

    public List<Primitive> getRoots()
    {
        return roots;
    }

    /**
     * Synthesizes the list of root operations (saves all root operations
     * in the list). A root operation is considered to be an AND-rule
     * operation that has no parents. If the method fails, the list is
     * cleared. 
     * 
     * @return <code>true</code> if the list of root operations was
     * successfully synthesized or <code>false</code> otherwise.
     */

    private boolean syntesizeRoots()
    {
        for (Primitive op : operations)
        {
            if (op.getKind() != Primitive.Kind.OP)
            {
                roots.clear();
                reportError(String.format(NOT_OPERATION, op.getName()));
                return false;
            }

            if (op.isRoot() && !op.isOrRule())
                roots.add(op);
        }

        return true;
    }
    
    /**
     * 
     */

    private void syntesizeShortcuts()
    {
        final Primitive root = new PrimitiveOR(ROOT_ID, Primitive.Kind.OP, roots);
        final PathCounter pathCounter = new PathCounter();

        for (Primitive op : operations)
        {
            // Only leafs and junctions: shortcuts for other nodes are redundant.
            if (isLeaf(op) || isJunction(op))
            {
                final PrimitiveAND target = (PrimitiveAND) op;
                new ShortcutBuilder(root, target, pathCounter).build();
            }
        }
    }

    private static final String NO_OPERATIONS =
         "The operation list is empty. No information to be analyzed.";

    private static final String ALREADY_SYNTHESIZED = 
        "Internal presentation has already been fully synthesized. No action was be performed.";

    private static final String NOT_OPERATION =
        "Wring input data. The %s primitive is not an operation.";
}

/**
 * 
 * @author Andrei Tatarnikov
 */

final class ShortcutBuilder
{
    private final Primitive          root;
    private final PrimitiveAND     target;
    private final PathCounter pathCounter;

    /**
     * 
     * @param root
     * @param target
     * @param pathCounter
     * 
     *  @throws NullPointerException if any of the parameters equals null.
     */

    public ShortcutBuilder(
        Primitive         root,
        PrimitiveAND    target,
        PathCounter pathCounter
        )
    {
        if (null == root)
            throw new NullPointerException();

        if (null == target)
            throw new NullPointerException();

        if (null == pathCounter)
            throw new NullPointerException();

        this.root        = root;
        this.target      = target;
        this.pathCounter = pathCounter;
    }

    public void build()
    {
        build(target);
    }

    private void build(PrimitiveAND entry)
    {
        final List<String> contextNames = new ArrayList<String>();

        if (entry.isRoot() && getPathCount(root, target) == 1)
        {
            if (entry != target)
                contextNames.add(root.getName());
        }
        else
        {
            for (Primitive.Reference ref : entry.getParents())
            {
                final int count = getPathCount(ref.getSource(), target);

                if (count > 1)
                    continue;

                if (!isJunction(ref.getSource()))
                    build(ref.resolve());
                
                if (entry != target)
                    contextNames.add(ref.getSource().getName());
            }
        }

        if (!contextNames.isEmpty())
        {
            final Shortcut shortcut =
                new Shortcut(entry, target, contextNames);

            target.addShortcut(shortcut);
            System.out.println(shortcut);
        }
    }
    
    private int getPathCount(Primitive src, Primitive targ)
    {
        return pathCounter.getPathCount(src, targ.getName());
    }
}
