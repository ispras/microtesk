/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ShortcutBuilder.java, Jul 8, 2014 8:21:28 PM Andrei Tatarnikov
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

import ru.ispras.microtesk.translator.antlrex.log.ELogEntryKind;
import ru.ispras.microtesk.translator.antlrex.log.ESenderKind;
import ru.ispras.microtesk.translator.antlrex.log.ILogStore;
import ru.ispras.microtesk.translator.antlrex.log.LogEntry;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive.Reference;

public final class ShortcutBuilder
{
    /** The name of the model entry point (root operation). */
    public static final String ENTRY_POINT = "instruction";

    private final Map<String, Primitive> operations;

    private final String fileName;
    private final ILogStore   log;

    public ShortcutBuilder(
        Map<String, Primitive> operations,
        String fileName,
        ILogStore log
        )
    {
        this.operations = operations;
        this.fileName   = fileName;
        this.log        = log;
    }

    private void report(ELogEntryKind kind, String message)
    {
        log.append(
            new LogEntry(
                kind, ESenderKind.EMITTER, fileName, 0, 0, message));
    }

    private void reportError(String message)
    {
        report(ELogEntryKind.ERROR, message);
    }

    private void reportWarning(String message)
    {
        report(ELogEntryKind.WARNING, message);
    }

    public void buildShortcuts()
    {
        System.out.println("****************************************");
        System.out.println("BUILDING SHORTCUTS:");

        for (Primitive op : operations.values())
            buildShortcuts(op);
    }

    private void buildShortcuts(Primitive op)
    {
        // Only leafs and junctions: shortcuts for other nodes are redundant.
        if (!PrimitiveUtils.isLeaf(op) && !PrimitiveUtils.isJunction(op))
            return;

        // If all parents are junctions, shortcuts make no sense.
        if (0 == PrimitiveUtils.countNonJunctionParents(op))
            return;

        final PrimitiveAND target = (PrimitiveAND) op; 
        for (Reference ref : target.getParents())
        {
            if (!PrimitiveUtils.isJunction(ref.getSource()))
            {
                buildShortcut(ref, target);
            }
        }
    }

    private void buildShortcut(Reference refToParent, PrimitiveAND target)
    {
        final PrimitiveAND entry = refToParent.resolve();

        if (entry.isRoot())
        {
            System.out.printf(
                "Target: %s, Entry: %s, Context: %s%n", target.getName(), entry.getName(), "#root");
            
            target.addShortcut(new Shortcut(entry, target, "#root"));
            
            return;
        }

        for (Reference ref : entry.getParents())
        {
            if (PrimitiveUtils.isJunction(ref.getSource()))
            {
                System.out.printf("Target: %s, Entry: %s, Context: %s%n",
                    target.getName(), entry.getName(), ref.getSource().getName());

                target.addShortcut(new Shortcut(entry, target, ref.getSource().getName()));
            }
            else
            {
                buildShortcut(ref, target);
            }
        }
    }
}
