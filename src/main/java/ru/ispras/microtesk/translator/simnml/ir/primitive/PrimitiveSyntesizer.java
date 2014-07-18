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

public final class PrimitiveSyntesizer extends Logger
{
    private static final String ROOT_ID = "#root"; 
    
    private final Collection<Primitive> operations;
    private final List<Primitive> roots;

    private boolean isSyntesized;

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

    public boolean syntesize()
    {
        if (isSyntesized)
        {
            reportWarning(ALREADY_SYNTHESIZED);
            return true;
        }

        syntesizeRoots();
        syntesizeShortcuts();

        isSyntesized = true;
        return true;
    }

    public boolean isSyntesized()
    {
        return isSyntesized;
    }
    
    public List<Primitive> getRoots()
    {
        return roots;
    }

    private void syntesizeRoots()
    {
        for (Primitive op : operations)
        {
           if (op.isRoot() && !op.isOrRule())
               roots.add(op);
        }
    }
    
    private void syntesizeShortcuts()
    {
        System.out.println("****************************************");
        System.out.println("SYNTESIZING SHORTCUTS:");
        
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

    private static final String ALREADY_SYNTHESIZED = 
        "Internal presentation has already been fully synthesized. No action was be performed.";
}

final class ShortcutBuilder
{
    private final Primitive          root;
    private final PrimitiveAND     target;
    private final PathCounter pathCounter;

    public ShortcutBuilder(
        Primitive root,
        PrimitiveAND target,
        PathCounter pathCounter
        )
    {
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
