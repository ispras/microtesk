/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Primitive.java, Jul 9, 2013 11:32:13 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import java.util.Set;
import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;

public class Primitive
{
    public static enum Kind
    {
        /** Addressing mode. */
        MODE,

        /** Operation. */
        OP,

        /** Immediate value. */
        IMM
    }

    public static final class Holder
    {
        private Primitive value;

        public Holder()
           { this.value = null; }

        public Holder(Primitive value)
           { assert value != null; this.value = value; } 

        public void setValue(Primitive value)
            { assert null == this.value : "Aready assigned."; this.value = value; }

        public Primitive getValue()
            { return value; }
    }

    private final String           name;
    private final Kind             kind;
    private final boolean      isOrRule;
    private final TypeExpr   returnType;
    private final Set<String> attrNames;

    Primitive(String name, Kind kind, boolean isOrRule, TypeExpr returnType, Set<String> attrNames)
    {
        this.name       = name;
        this.kind       = kind;
        this.isOrRule   = isOrRule;
        this.returnType = returnType;
        this.attrNames  = attrNames;
    }

    public final String getName()
    {
        if (null != name)
            return name;

        if (Kind.IMM == kind)
            return returnType.getJavaText();

        assert false : "Primitive name is not defined.";
        return null;
    }

    public final Kind getKind()
    {
        return kind;
    }

    public final boolean isOrRule()
    {
        return isOrRule;
    }

    public final TypeExpr getReturnType()
    {
        return returnType;
    }
    
    public final Set<String> getAttrNames()
    {
        return attrNames;
    }
}
