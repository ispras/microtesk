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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.translator.antlrex.log.ELogEntryKind;
import ru.ispras.microtesk.translator.antlrex.log.ESenderKind;
import ru.ispras.microtesk.translator.antlrex.log.ILogStore;
import ru.ispras.microtesk.translator.antlrex.log.LogEntry;

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
        {
            if (!op.isRoot() && !op.isOrRule())
                buildShortcuts(op);
        }
    }
    
    private void buildShortcuts(Primitive op)
    {
        System.out.println(op.getName());
    }

    private void traverse(String contextName, Primitive entryPoint)
    {
        final List<PrimitiveAND> roots = new ArrayList<PrimitiveAND>();
        PrimitiveUtils.saveAllOrsToList(entryPoint, roots);

        for (PrimitiveAND root : roots)
            traverseAND(contextName, root);
    }

    private void traverseAND(String contextName, PrimitiveAND root)
    {
        final int childOpCount = 
            PrimitiveUtils.getChildCount(root, Primitive.Kind.OP);

        if (childOpCount == 0)
            return;

        if (childOpCount > 1)
        {
            traverseAllChildOps(root);
            return;
        }

        if (childOpCount == 1)
        {
            startBuildingShortcut(contextName, root);
            return;
        }

        assert false : "Unexpected value: " + childOpCount;
    }

    private void traverseAllChildOps(PrimitiveAND root)
    {
        for (Primitive child : root.getArguments().values())
            if (child.getKind() == Primitive.Kind.OP)
                traverse(root.getName(), child);
    }
    
    private void startBuildingShortcut(String contextName, PrimitiveAND root)
    {
        System.out.printf("Context: %s, Root: %s%n", contextName, root.getName());
    }

/*
    private void traverse(String contextName, PrimitiveAND root)
    {
        final int childOpCount = PrimitiveUtils.getChildOpCount(root);

        System.out.printf("Context: %s, Root: %s, Child Ops: %d%n",
            contextName, root.getName(), childOpCount);

        if (childOpCount > 1)
        {
            for (Primitive child : root.getArguments().values())
                traverse(root.getName(), child);
        }
            
    }
*/
}
