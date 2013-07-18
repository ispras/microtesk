/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * PrimitiveBaseSTBuilder.java, Jul 18, 2013 11:36:48 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.generation.builders;

import java.util.EnumMap;
import java.util.Map;

import ru.ispras.microtesk.translator.generation.ITemplateBuilder;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.simnml.ir.primitive.AttributeFactory;

public abstract class PrimitiveBaseSTBuilder implements ITemplateBuilder
{
    private static final Map<Attribute.Kind, String> RET_TYPE_MAP =
        new EnumMap<Attribute.Kind, String>(Attribute.Kind.class);

    static
    {
        RET_TYPE_MAP.put(Attribute.Kind.ACTION,       "void");
        RET_TYPE_MAP.put(Attribute.Kind.EXPRESSION, "String");
    }

    protected final String getRetTypeName(Attribute.Kind kind)
    {
       return RET_TYPE_MAP.get(kind);
    }
    
    private static final String[] STANDARD_ATTRIBUTES =
    {
        AttributeFactory.IMAGE_NAME,
        AttributeFactory.SYNTAX_NAME,
        AttributeFactory.ACTION_NAME
    };

    protected final boolean isStandardAttribute(String name)
    {
        for (String standardName : STANDARD_ATTRIBUTES)
        {
            if (standardName.equals(name))
               return true;
        }

        return false;
    }
}
