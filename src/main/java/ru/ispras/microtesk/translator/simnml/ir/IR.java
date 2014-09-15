/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IR.java, Dec 11, 2012 1:57:58 PM Andrei Tatarnikov
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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.translator.simnml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetConstant;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetLabel;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetString;
import ru.ispras.microtesk.translator.simnml.ir.shared.MemoryExpr;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public final class IR
{
    private final Map<String, LetConstant> consts;
    private final Map<String, LetString>  strings;
    private final Map<String, LetLabel>    labels;
    private final Map<String, Type>         types;
    private final Map<String, MemoryExpr>  memory;
    private final Map<String, Primitive>    modes;
    private final Map<String, Primitive>      ops;
    private final Map<String, Initializer>  inits;
    private List<Primitive>                 roots;

    public IR()
    {
        this.consts  = new LinkedHashMap<String, LetConstant>();
        this.strings = new LinkedHashMap<String, LetString>();

        this.labels  = new LinkedHashMap<String, LetLabel>();
        this.types   = new LinkedHashMap<String, Type>();
        this.memory  = new LinkedHashMap<String, MemoryExpr>();

        this.modes   = new LinkedHashMap<String, Primitive>();
        this.ops     = new LinkedHashMap<String, Primitive>();

        this.inits   = new LinkedHashMap<String, Initializer>();

        this.roots        = Collections.<Primitive>emptyList();
    }

    public void add(String name, LetConstant value)
    {
        consts.put(name, value);
    }

    public void add(String name, LetString value)
    {
        strings.put(name, value);
    }

    public void add(String name, LetLabel value)
    {
        labels.put(name, value);
    }

    public void add(String name, Type value)
    {
        types.put(name, value);
    }

    public void add(String name, MemoryExpr value)
    {
        memory.put(name, value);
    }

    public void add(String name, Primitive value)
    {
        if (Primitive.Kind.MODE == value.getKind())
            modes.put(name, value);
        else if (Primitive.Kind.OP == value.getKind())
            ops.put(name, value);
        else
            assert false : String.format("Incorrect primitive kind: %s.", value.getKind());
    }

    public void add(String name, Initializer value)
    {
        inits.put(name, value);
    }

    public Map<String, LetConstant> getConstants()
    {
        return Collections.unmodifiableMap(consts);
    }

    public Map<String, LetString> getStrings()
    {
        return Collections.unmodifiableMap(strings);
    }

    public Map<String, LetLabel> getLabels()
    {
        return Collections.unmodifiableMap(labels);
    }

    public Map<String, Type> getTypes()
    {
        return Collections.unmodifiableMap(types);
    }

    public Map<String, MemoryExpr> getMemory()
    {
        return Collections.unmodifiableMap(memory);
    }

    public Map<String, Primitive> getModes()
    {
        return Collections.unmodifiableMap(modes);
    }

    public Map<String, Primitive> getOps()
    {
        return Collections.unmodifiableMap(ops);
    }

    public Map<String, Initializer> getInitializers()
    {
        return Collections.unmodifiableMap(inits);
    }

    public List<Primitive> getRoots()
    {
        return roots;
    }

    public void setRoots(List<Primitive> value)
    {
        assert null != value;
        assert roots.isEmpty();

        roots = Collections.unmodifiableList(roots);
    }
}
