/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Attribute.java, Jan 2, 2013, 11:04:14 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.modeop;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class Attribute
{
    public enum EKind
    {
        ACTION,
        EXPRESSION
    }

    private static final Map<EKind, String> RET_TYPE_MAP = new EnumMap<EKind, String>(EKind.class);
    static
    {
        RET_TYPE_MAP.put(EKind.ACTION, "void");
        RET_TYPE_MAP.put(EKind.EXPRESSION, "String");
    }

    private final String name;
    private final EKind  kind;
    private final List<Statement> stmts;

    public Attribute(String name, EKind kind, List<Statement> stmts)
    {
        this.name  = name;
        this.kind  = kind;
        this.stmts = Collections.unmodifiableList(stmts);
    }

    public String getName()
    {
        return name;
    }

    public EKind getKind()
    {
        return kind;
    }

    public String getRetTypeName()
    {
        return RET_TYPE_MAP.get(getKind());
    }

    public List<Statement> getStatements()
    {
        return stmts;
    }
}
