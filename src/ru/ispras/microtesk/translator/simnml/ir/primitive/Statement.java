/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Statement.java, Feb 7, 2013 12:09:14 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import java.util.List;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;

public abstract class Statement
{
    public static enum Kind
    {
        CALL,
        ASSIGN,
        COND,
        FORMAT,
        @Deprecated TEXT
    }

    private final Kind kind;

    Statement(Kind kind)
    {
        this.kind = kind;
    }

    public final Kind getKind()
    {
        return kind;
    }

    public abstract String getText();
}

final class FormatStatement extends Statement
{
    public FormatStatement()
    {
        super(Kind.FORMAT);
    }

    @Override
    public String getText()
    {
        return null;
    }
}

class TextStatement extends Statement
{
    private final String text;

    public TextStatement()
    {
        super(Kind.TEXT);
        
        this.text = "//Default code\r\nreturn null";
    }

    public TextStatement(String text)
    {
        super(Kind.TEXT);
        
        this.text = text;
    }

    public String getText()
    {
        return text;
    }
}

class IfElseStatement extends Statement
{
    private static int depth = 0;

    private final Expr condition;
    private final List<Statement> ifSmts;
    private final List<Statement> elseSmts;

    public IfElseStatement(Expr condition, List<Statement> ifSmts, List<Statement> elseSmts)
    {
        super(Kind.TEXT);
        
        this.condition = condition;
        this.ifSmts    = ifSmts;
        this.elseSmts  = elseSmts;
    }

    @Override
    public String getText()
    {
        StringBuffer sb = new StringBuffer();

        //for (int i = 0; i < depth; ++i) sb.append("    ");
        sb.append(String.format("if (%s)\r\n", condition.getText()));

        for (int i = 0; i < depth; ++i) sb.append("    ");
        sb.append("{\r\n");

        for(Statement st : ifSmts)
        {
            ++depth;
            for (int i = 0; i < depth; ++i) sb.append("    ");
            sb.append(st.getText());
            sb.append("\r\n");
            --depth;
        }

        for (int i = 0; i < depth; ++i) sb.append("    ");
        sb.append("}");
        
        if (null != elseSmts)
        {
            sb.append("\r\n");
            for (int i = 0; i < depth; ++i) sb.append("    ");
            sb.append("else");
            sb.append("\r\n");
            for (int i = 0; i < depth; ++i) sb.append("    ");
            sb.append("{");
            sb.append("\r\n");
            
            for(Statement st : elseSmts)
            {
                ++depth;
                for (int i = 0; i < depth; ++i) sb.append("    ");
                sb.append(st.getText());
                sb.append("\r\n");
                --depth;
            }
            
            for (int i = 0; i < depth; ++i) sb.append("    ");
            sb.append("}");
        }
        
        return sb.toString();
    }    
}
