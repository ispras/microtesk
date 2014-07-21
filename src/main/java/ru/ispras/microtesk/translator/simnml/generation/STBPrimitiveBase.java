/*
 * Copyright (c) 2013 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * STBPrimitiveBase.java, Jul 18, 2013 11:36:48 AM Andrei Tatarnikov
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

package ru.ispras.microtesk.translator.simnml.generation;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.stringtemplate.v4.ST;

import ru.ispras.microtesk.translator.generation.ITemplateBuilder;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Format;
import ru.ispras.microtesk.translator.simnml.ir.primitive.Statement;
import ru.ispras.microtesk.translator.simnml.ir.primitive.StatementAssignment;
import ru.ispras.microtesk.translator.simnml.ir.primitive.StatementAttributeCall;
import ru.ispras.microtesk.translator.simnml.ir.primitive.StatementCondition;
import ru.ispras.microtesk.translator.simnml.ir.primitive.StatementFormat;
import ru.ispras.microtesk.translator.simnml.ir.primitive.StatementStatus;

abstract class STBPrimitiveBase implements ITemplateBuilder
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
 
    protected final boolean isStandardAttribute(String name)
    {
        return Attribute.STANDARD_NAMES.contains(name);
    }
    
    protected static void addStatement(ST attrST, Statement stmt, boolean isReturn)
    {
        new StatementBuilder(attrST, isReturn).build(stmt);
    }
}

final class StatementBuilder
{
    private static final String SINDENT = "    ";
    
    private final ST sequenceST;
    private final boolean isReturn;
    private int indent;

    StatementBuilder(ST sequenceST, boolean isReturn)
    {
        this.sequenceST = sequenceST;
        this.isReturn   = isReturn;
        this.indent     = 0;
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
        if (null == stmt)
        {
            if (isReturn)
                addStatement("null;");

            return;
        }
                    
        switch (stmt.getKind()) 
        {
            case ASSIGN:
                addStatement((StatementAssignment) stmt);
                break;

            case COND:
                addStatement((StatementCondition) stmt);
                break;

            case CALL:
                addStatement((StatementAttributeCall) stmt);
                break;
                
            case STATUS:
                addStatement((StatementStatus) stmt);
                break;
                
            case FORMAT:
                addStatement((StatementFormat) stmt);
                break;

            default:
                assert false : String.format("Unsupported statement type: %s.", stmt.getKind());
                addStatement("// Error! Unknown statement!");
                break;
        }
    }

    private void addStatement(String stmt)
    {
        final StringBuilder sb = new StringBuilder();

        if (isReturn)
            sb.append("return ");

        
        for (int index = 0; index < indent; ++index)
            sb.append(SINDENT);
        sb.append(stmt);

        sequenceST.add("stmts", sb.toString());
    }

    private void addStatement(StatementAssignment stmt)
    {
        addStatement(String.format("%s.store(%s);",
            LocationPrinter.toString(stmt.getLeft()), new ExprPrinter(stmt.getRight())));
    }

    private void addStatement(StatementCondition stmt)
    {
        final int FIRST = 0;
        final int  LAST = stmt.getBlockCount() - 1;

        for(int index = FIRST; index <= LAST; ++index)
        {
            final StatementCondition.Block block = stmt.getBlock(index);

            if (FIRST == index)
            {
                addStatement(String.format("if (%s)", new ExprPrinter(block.getCondition())));
            }
            else if (LAST == index && block.isElseBlock())
            {
                addStatement("else");
            }
            else 
            {
                addStatement(String.format("else if (%s)", new ExprPrinter(block.getCondition())));
            }

            addStatementBlock(block.getStatements());
        }
    }

    private void addStatement(StatementAttributeCall stmt)
    {
        if (null != stmt.getCalleeName())
            addStatement(String.format("%s.%s();", stmt.getCalleeName(), stmt.getAttributeName()));
        else
            addStatement(String.format("%s();", stmt.getAttributeName()));
    }
    
    private void addStatement(StatementFormat stmt)
    {
        if (null == stmt.getArguments())
        {
            addStatement(String.format("\"%s\";", stmt.getFormat()));
            return;
        }

        final StringBuffer sb = new StringBuffer();
        for(int index = 0; index < stmt.getArguments().size(); ++index)
        {
            sb.append(", ");

            final Format.Argument argument = stmt.getArguments().get(index);
            final Format.Marker marker = stmt.getMarkers().get(index);

            sb.append(argument.convertTo(marker));
        }

        addStatement(String.format("String.format(\"%s\"%s);", stmt.getFormat(), sb.toString()));
    }

    private void addStatement(StatementStatus stmt)
    {
        addStatement(
            String.format("%s.set(%d);", stmt.getStatus().getName(), stmt.getNewValue()));
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
