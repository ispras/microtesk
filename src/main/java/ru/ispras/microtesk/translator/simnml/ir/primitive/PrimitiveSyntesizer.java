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
 * AND rules which have no parents. Such operations serve as entry points in
 * composite operations describing instruction calls.<pre></pre>
 * 
 * 2. Shortcuts for leaf (have no child operations) and junction (have more
 * than one child operations) operations that allow addressing (instantiating
 * with all required parent operations) these operation in various contexts.
 * A shortcut can be synthesized if there is an unambiguous way to resolve all
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
     * of all defined operations. For details on building shortcuts,
     * see documentation on the syntesizeShortcuts method.
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
     * Synthesizes shortcuts for leaf and junction operations and adds the them
     * to the corresponding operations. Only leafs (no childs) and junctions
     * (more than one child) are considered interesting because there is no
     * need to create shortcuts for intermediate nodes.<pre></pre>
     * 
     * Implementation details:<pre></pre>
     * 
     * The method iterates over the collection of operations provided by the
     * client and uses the ShortcutBuilder class to build shortcuts for
     * leaf (no childs) and junction (more than one child) primitives. Other
     * operations are ignored as they are considered intermediate (they are not
     * a final point in an unambiguous path). See documentation on the 
     * ShortcutBuilder class for more details. 
     */

    private void syntesizeShortcuts()
    {
        // Fake primitive, root of all roots, needed to provide a common context.
        final PrimitiveOR root =
            new PrimitiveOR(ROOT_ID, Primitive.Kind.OP, roots);

        // Used by ShortcutBuilder, stores all previous results to avoid
        // redundant traversals.
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
 * The ShortcutBuilder class creates all possible shortcuts for the target
 * operation and registers them into the corresponding object.
 * 
 * @author Andrei Tatarnikov
 */

final class ShortcutBuilder
{
    private final PrimitiveOR        root;
    private final PrimitiveAND     target;
    private final PathCounter pathCounter;

    private boolean  canHaveMultiplePaths; 

    /**
     * Constructs a ShortcutBuilder object.
     * 
     * @param root The root primitive, root of all roots, that provides
     * a common topmost starting point for all paths. 
     * @param target Target primitive.
     * @param pathCounter Path counter object that will be used to exclude
     * ambiguous paths.
     * 
     * @throws NullPointerException if any of the parameters equals null.
     */

    public ShortcutBuilder(
        PrimitiveOR       root,
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

        // False, until we meet primitives with multiple parents on our path.
        this.canHaveMultiplePaths = false;
    }

    /**
     * Builds shortcuts for the target primitives and adds them to the list
     * of shortcuts of this primitive.  
     */

    public void build()
    {
        build(target);
    }

    /**
     * Creates shortcuts to the target primitive starting from the entry
     * primitive. Entry is the topmost point of the shortcut path.<pre></pre> 
     * 
     * Algorithm description.<pre></pre>
     * TODO:
     * 
     * @param entry Entry point of the shortcuts to be created.
     */

    private void build(PrimitiveAND entry)
    {
        final ShortcutCreator creator = new ShortcutCreator(entry); 

        if (entry.isRoot() && isSinglePathToTarget(root))
        {
            creator.addShortcutContext(root.getName());
        }
        else
        {
            checkForMultipleParents(entry);
            for (Primitive.Reference ref : entry.getParents())
            {
                if (!isSinglePathToTarget(ref.getSource()))
                    continue;

                if (!isJunction(ref.getSource()))
                    build(ref.resolve());

                creator.addShortcutContext(ref.getSource().getName());
            }
        }

        creator.createAndRegisterShortcut();
    }

    /**
     * Checks if the given Primitive has multiple parents (more than one).
     * If it has, the canHaveMultiplePaths flag is set to <code>true</code>.
     * <pre></pre>
     * This check is performed because  if there are no primitives with
     * multiple parents on the way from the target to the source, there is
     * only one path from the source to the target and there is no need to
     * check for multiple paths. Otherwise, there are multiple paths and we
     * need to look for the point where they start and exclude it from
     * the shortcut path to avoid ambiguities. 
     * 
     * @param entry Primitive to be checked.
     */

    private void checkForMultipleParents(PrimitiveAND entry)
    {
        if (entry.getParentCount() > 1)
            canHaveMultiplePaths = true; 
    }

    /**
     * Checks whether there is only a single path from the source
     * to the target. 
     * 
     * @param source Source primitive.
     * @return <code>true</code> it there is a single path from
     * the source to the target or false otherwise.
     * 
     * @throws IllegalStateException if the number of possible paths from 
     * the source to the target is less than 1. This is an invariant. At least
     * one path always exists (because the build method passes it before
     * checking that there is only one path).   
     */

    private boolean isSinglePathToTarget(Primitive source)
    {
        if (!canHaveMultiplePaths)
            return true;

        final int count = pathCounter.getPathCount(source, target.getName());

        if (count < 1)
            throw new IllegalStateException();

        return count == 1;
    }

    /**
     * The ShortcutCreator class responsible for creating and registering
     * shortcuts that describe paths starting from a common entry primitive.
     * 
     * @author Andrei Tatarnikov
     */

    private final class ShortcutCreator
    {
        private final PrimitiveAND        entry;
        private final List<String> contextNames;

        /**
         * Constructs a shortcut creator for the given entry.
         * 
         * @param entry Entry primitive for the shortcut to be created.
         */

        private ShortcutCreator(PrimitiveAND entry)
        {
            this.entry = entry;
            this.contextNames = new ArrayList<String>();
        }

        /**
         * Add a context in which the shortcut can be used. If the entry
         * refers to the same primitive as the target, no context is 
         * added since there is no need for such a shortcut (in this case,
         * the path consists only of the target). 
         * 
         * @param name Context name. 
         */

        private void addShortcutContext(String name)
        {
            if (entry != target)
                contextNames.add(name);
        }

        /**
         * Creates and registers a shortcut if there are
         * contexts in which it can be used.
         */

        private void createAndRegisterShortcut()
        {
            if (!contextNames.isEmpty())
            {
                final Shortcut shortcut =
                    new Shortcut(entry, target, contextNames);

                target.addShortcut(shortcut);
                System.out.println(shortcut);
            }
        }
    }
}
