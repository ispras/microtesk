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
import java.util.List;
import java.util.Map;

import org.stringtemplate.v4.ST;

import ru.ispras.microtesk.translator.generation.ITemplateBuilder;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.simnml.ir.primitive.AttributeFactory;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.simnml.ir.primitive.StatementAssignment;
import ru.ispras.microtesk.translator.simnml.ir.primitive.StatementCondition;

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
    
    protected static void addStatement(ST attrST, Statement stmt)
    {
        new StatementBuilder(attrST).build(stmt);
    }
}

final class StatementBuilder
{
    private static final String SINDENT = "    ";
    
    private final ST sequenceST;
    private int indent;

    StatementBuilder(ST sequenceST)
    {
        this.sequenceST = sequenceST;
        this.indent = 0;
    }

    private void increaseIndent()
    {
        indent++;
    }

    private void decreaseIndent()
    {
        assert indent > 0;
        if (indent > 0) indent--;
    }

    public void build(Statement stmt)
    {
        addStatement(stmt);
    }

    private void addStatement(Statement stmt)
    {
        switch (stmt.getKind()) 
        {
            case TEXT:
                addStatement(stmt.getText());
                break;

            case ASSIGN:
                addStatement((StatementAssignment) stmt);
                break;
                
            case COND:
                addStatement((StatementCondition) stmt);
                break;

            default:
                assert false : String.format("Unsupported statement type: %s.", stmt.getKind());
                addStatement("// Error! Unknown statement!");
                break;
        }
    }

    private void addStatement(String stmt)
    {
        final StringBuilder sb = new StringBuilder(indent * SINDENT.length() + stmt.length());
        
        for (int index = 0; index < indent; ++index)
            sb.append(SINDENT);
        sb.append(stmt);

        sequenceST.add("stmts", sb.toString());
    }

    private void addStatement(StatementAssignment stmt)
    {
        addStatement(String.format("%s.store(%s);", stmt.getLeft().getText(), stmt.getRight().getText()));
    }
    
    private void addStatement(StatementCondition stmt)
    {
        addStatement(String.format("if (%s)", stmt.getCondition().getText()));
        addStatementBlock(stmt.getIfStatements());
        
        if (null != stmt.getElseStatements() && !stmt.getElseStatements().isEmpty())
        {
            addStatement("else");
            addStatementBlock(stmt.getElseStatements());
        }
    }
    
    private void addStatementBlock(List<Statement> stmts)
    {
        addStatement("{");
        increaseIndent();

        for (Statement stmt : stmts)
            addStatement(stmt);

        decreaseIndent();
        addStatement("}");
    }
}
