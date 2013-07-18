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
import ru.ispras.microtesk.translator.simnml.ir.expression.LocationExpr;

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

    public Statement(Kind kind)
    {
        this.kind = kind;
    }

    public final Kind getKind()
    {
        return kind;
    }

    public abstract String getText();
}

final class AttributeCallStatement extends Statement
{
    private final Attribute        callee;
    private final String    calleeObjName;
    private final Primitive calleeObjType;

    public AttributeCallStatement(
        Attribute callee, String calleeObjName, Primitive calleeObjType)
    {
        super(Kind.CALL);

        this.callee        = callee;
        this.calleeObjName = calleeObjName;
        this.calleeObjType = calleeObjType;
    }

    public Attribute getCallee()
    {
        return callee; 
    }

    public String getCalleeObjectName()
    {
        return calleeObjName;
    }

    public Primitive getCalleeObjectType()
    {
        return calleeObjType;
    }

    @Override
    public String getText()
    {
        // TODO Auto-generated method stub
        return null;
    }
}

final class AssignmentStatement extends Statement
{
    private final LocationExpr left;
    private final Expr right;

    public AssignmentStatement(LocationExpr left, Expr right)
    {
        super(Kind.ASSIGN);

        this.left  = left;
        this.right = right;
    }

    public LocationExpr getLeft()
    {
        return left;
    }
    
    public Expr getRight()
    {
        return right;
    }

    @Override
    public String getText()
    {
        // TODO Auto-generated method stub
        return null;
    }
}

final class ConditionalStatement extends Statement
{
    private final Expr                cond;
    private final List<Statement>   ifSmts;
    private final List<Statement> elseSmts;

    public ConditionalStatement(
        Expr cond, List<Statement> ifSmts, List<Statement> elseSmts)
    {
        super(Kind.COND);
        
        this.cond     = cond;
        this.ifSmts   = ifSmts;
        this.elseSmts = elseSmts;
    }

    public Expr getCondition()
    {
        return cond;
    }

    public List<Statement> getIfStatements()
    {
        return ifSmts;
    }

    public List<Statement> getElseStatements()
    {
        return elseSmts;
    }

    @Override
    public String getText()
    {
        return null;
    }
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
