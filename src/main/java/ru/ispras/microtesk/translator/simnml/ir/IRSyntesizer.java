/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IRSyntesizer.java, Jul 17, 2014 5:58:18 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.translator.simnml.ir;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.microtesk.translator.antlrex.log.ESenderKind;
import ru.ispras.microtesk.translator.antlrex.log.ILogStore;
import ru.ispras.microtesk.translator.antlrex.log.Logger;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;

public final class IRSyntesizer extends Logger
{
    private final IR ir;
    private boolean isSyntesized;

    public IRSyntesizer(IR ir, String fileName, ILogStore log)
    {
        super(ESenderKind.EMITTER, fileName, log);

        if (null == ir)
            throw new NullPointerException();

        this.ir = ir;
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

        return false;
    }

    private void syntesizeRoots()
    {
        final List<Primitive> roots = new ArrayList<Primitive>();
        for (Primitive op : ir.getOps().values())
        {
           if (op.isRoot() && !op.isOrRule())
               roots.add(op);
        }
        ir.setRoots(roots);
    }
    
    private void syntesizeShortcuts()
    {
        // TODO
    }

    private static final String ALREADY_SYNTHESIZED = 
        "Internal presentation has already been fully synthesized. No action was be performed.";
}
