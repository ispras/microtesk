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

import ru.ispras.microtesk.translator.antlrex.log.ESenderKind;
import ru.ispras.microtesk.translator.antlrex.log.ILogStore;
import ru.ispras.microtesk.translator.antlrex.log.Logger;

import static ru.ispras.microtesk.translator.simnml.ir.primitive.PrimitiveUtils.*;

public final class ShortcutBuilder extends Logger
{
    private final Map<String, Primitive> operations;
    private final PathCounter           pathCounter;
    
    private final Primitive root; 

    public ShortcutBuilder(
        Map<String, Primitive> operations,
        String fileName,
        ILogStore log
        )
    {
        super(ESenderKind.EMITTER, fileName, log);

        this.operations  = operations;
        this.pathCounter = new PathCounter(); 

        List<Primitive> rootList = new ArrayList<Primitive>();
        for (Primitive op : operations.values())
        {
           if (op.isRoot() && !op.isOrRule())
               rootList.add(op);
        }

        this.root = new PrimitiveOR("#root", Primitive.Kind.OP, rootList);
    }

    public void buildShortcuts()
    {
        System.out.println("****************************************");
        System.out.println("BUILDING SHORTCUTS:");

        for (Primitive op : operations.values())
        {
            // Only leafs and junctions: shortcuts for other nodes are redundant.
            if (isLeaf(op) || isJunction(op))
            {    
                final PrimitiveAND target = (PrimitiveAND) op;
                buildShortcut(target, target);
            }
        }
    }

    private int getPathCount(Primitive source, Primitive target)
    {
        return pathCounter.getPathCount(root, target.getName());
    }

    private void buildShortcut(PrimitiveAND entry, PrimitiveAND target)
    {
        final List<String> contextNames = new ArrayList<String>();

        if (entry.isRoot() && getPathCount(root, target) == 1)
        {
            if (entry != target)
                contextNames.add("#root");
        }
        else
        {
            for (Primitive.Reference ref : entry.getParents())
            {
                final int count = getPathCount(ref.getSource(), target);

                if (count > 1)
                    continue;

                if (!isJunction(ref.getSource()))
                    buildShortcut(ref.resolve(), target);
                
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
}
