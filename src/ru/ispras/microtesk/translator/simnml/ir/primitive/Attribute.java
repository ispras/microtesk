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

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Attribute
{
    public static final String SYNTAX_NAME = "syntax";
    public static final String  IMAGE_NAME = "image";
    public static final String ACTION_NAME = "action";

    public static final Set<String> STANDARD_NAMES = createStandardNames();

    public static enum Kind
    {
        ACTION,
        EXPRESSION
    }

    private final String name;
    private final Kind kind;
    private final List<Statement> stmts;

    Attribute(String name, Kind kind, List<Statement> stmts)
    {
        assert null != name;
        assert null != kind;
        assert null != stmts;
        
        this.name  = name;
        this.kind  = kind;
        this.stmts = Collections.unmodifiableList(stmts);
    }

    public String getName()
    {
        return name;
    }

    public Kind getKind()
    {
        return kind;
    }

    public List<Statement> getStatements()
    {
        return stmts;
    }
    
    private static Set<String> createStandardNames()
    {
        final Set<String> result = new HashSet<String>();

        result.add(SYNTAX_NAME);
        result.add(IMAGE_NAME);
        result.add(ACTION_NAME);

        return Collections.unmodifiableSet(result);
    }
}
