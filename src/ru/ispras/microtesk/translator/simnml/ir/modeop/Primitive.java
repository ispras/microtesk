/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Primitive.java, Feb 7, 2013 1:25:20 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.modeop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

abstract class Primitive<OrType extends Primitive<OrType>>
{
    private final String name;
    private final boolean isOrRule;

    private final List<OrType> ors;

    private final Map<String, Argument> args;
    private final Map<String, Attribute> attrs;

    private Primitive(
        String name,
        boolean isOrRule,
        List<OrType> ors,
        Map<String, Argument> args,
        Map<String, Attribute> attrs
        )
    {
        this.name     = name;
        this.isOrRule = isOrRule;
        this.ors      = ors;
        this.args     = args;
        this.attrs    = attrs;
    }

    public Primitive(
        String name,
        List<OrType> ors
        )
    {
        this(
            name,
            true,
            Collections.unmodifiableList(ors),
            null,
            null
        );
    }

    public Primitive(
        String name,
        Map<String, Argument> args,
        Map<String, Attribute> attrs
        )
    {
        this(
            name,
            false,
            null,
            Collections.unmodifiableMap(args),
            Collections.unmodifiableMap(attrs)
        );
    }

    public final String getName()
    {
        return name;
    }

    public final boolean isOrRule()
    {
        return isOrRule;
    }

    public final List<String> getOrNames()
    {
        assert isOrRule && (null != ors) : "This is not an OR-rule!";

        if (null == ors)
            return Collections.emptyList();

        final List<String> names = new ArrayList<String>(); 

        for (OrType o : ors)  
            names.add(o.getName());  

        return names;
    }

    public final List<OrType> getOrs()
    {
        assert isOrRule;
        return ors;
    }

    public final Map<String, Argument> getArgs()
    {
        assert !isOrRule;
        return args;
    }

    public final Map<String, Attribute> getAttrs()
    {
        assert !isOrRule;
        return attrs;
    }
}
