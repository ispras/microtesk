/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * AttributeFactory.java, Feb 7, 2013 1:00:30 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.modeop;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.translator.antlrex.IErrorReporter;
import ru.ispras.microtesk.translator.antlrex.symbols.SymbolTable;
import ru.ispras.microtesk.translator.simnml.ESymbolKind;
import ru.ispras.microtesk.translator.simnml.ir.IR;
import ru.ispras.microtesk.translator.simnml.ir.expression2.Expr;
import ru.ispras.microtesk.translator.simnml.ir.expression2.LocationExpr;
import ru.ispras.microtesk.translator.simnml.ir.modeop.Attribute.EKind;

public final class AttributeFactory
{
    public static final String SYNTAX_NAME = "syntax";
    public static final String  IMAGE_NAME = "image";
    public static final String ACTION_NAME = "action";

    // TODO: Members below are temporary unused. 
    // They will be used in future when I implement error diagnostics.
  
    @SuppressWarnings("unused")
    private final IErrorReporter reporter;
    @SuppressWarnings("unused")
    private final SymbolTable<ESymbolKind> symbols;
    @SuppressWarnings("unused")
    private final IR ir;

    private final Map<String, Attribute> defaultAttrs;

    public AttributeFactory(
        IErrorReporter reporter,
        SymbolTable<ESymbolKind> symbols,
        IR ir
        )
    {
        this.reporter = reporter;
        this.symbols = symbols;
        this.ir = ir;
        this.defaultAttrs = createDefaultAttributes();
    }

    private final Map<String, Attribute> createDefaultAttributes()
    {
        final Map<String, Attribute> result = new LinkedHashMap<String, Attribute>();

        result.put(SYNTAX_NAME, createDefaultExprAttribute(SYNTAX_NAME));
        result.put(IMAGE_NAME,  createDefaultExprAttribute(IMAGE_NAME));
        result.put(ACTION_NAME, createDefaultActionAttribute(ACTION_NAME));

        return Collections.unmodifiableMap(result);
    }

    public Attribute createDefaultExprAttribute(String name)
    {
        final String DEFAULT_EXPR_CODE =
             "// Default code. The attribute was not defined.\r\nreturn null;";

        return new Attribute(
             name,
             EKind.EXPRESSION,
             Collections.singletonList((Statement)new TextStatement(DEFAULT_EXPR_CODE))
             );
    }

    public Attribute createDefaultActionAttribute(String name)
    {
        final String DEFAULT_ACTION_CODE =
            "// Default code. The attribute was not defined.";

        return new Attribute(
            name,
            EKind.ACTION,
            Collections.singletonList((Statement)new TextStatement(DEFAULT_ACTION_CODE))
            );
    }

    public Map<String, Attribute> addDefaultAttributes(Map<String, Attribute> attrs)
    {
        final String DEFAULT_KEY_FRMT = "default#%s";

        for (Attribute defAttr: defaultAttrs.values())
        {
            if (!attrs.containsKey(defAttr.getName()))
                attrs.put(String.format(DEFAULT_KEY_FRMT, defAttr.getName()), defAttr);
        }

        return attrs;
    }

    public Attribute createAction(String name, List<Statement> stmts)
    {
        return new Attribute(
             name,
             EKind.ACTION,
             stmts
             );
    }

    public Attribute createFormatExpression(String name, Statement exprStatement)
    {
        return new Attribute(
            name,
            EKind.EXPRESSION,
            Collections.singletonList(
                (Statement)new TextStatement(
                    String.format("return %s", exprStatement.getText()))));
    }

    public Attribute syntax()
    {
        // TODO: create a non-default attribute here.
        return defaultAttrs.get(SYNTAX_NAME);
    }

    public Attribute image()
    {
        // TODO: create a non-default attribute here.
        return defaultAttrs.get(IMAGE_NAME);
    }

    public Attribute action()
    {
        // TODO: create a non-default attribute here.
        return defaultAttrs.get(ACTION_NAME);
    }

    public Statement createCommentStatement(String text)
    {
        return new TextStatement(String.format("// %s", text));
    }

    public Statement createAttributeCallStatement(String primitive, String attribute)
    {
        return new TextStatement(
            String.format("%s.%s();", primitive, attribute));
    }

    public Statement createAttributeCallStatement(String attribute)
    {
        return new TextStatement(
            String.format("%s();", attribute));
    }

    public Statement createAssignmentStatement(LocationExpr left, Expr right)
    {
        return new TextStatement(
            String.format("%s.store(%s);", left.getText(), right.getText()));
    }
    
    public Statement createIfElseStatement(
        Expr condition, List<Statement> isSmts, List<Statement> elseSmts)
    {
        return new IfElseStatement(condition, isSmts, elseSmts);
    }

    public Statement createTextLiteralStatement(String text)
    {
        return new TextStatement(String.format("\"%s\";", text));
    }
    
    public Statement createFormatStatement(String format, List<FormatArgument> args) 
    {
        assert null != args;

        final FormatAnalyzer formatAnalyzer = new FormatAnalyzer(format);
        final List<FormatKind> specKinds = formatAnalyzer.getFormatKinds();

        if (specKinds.size() > args.size())
            assert false; // TODO: Error is needed here!

        final StringBuffer sbArgs = new StringBuffer();
        for(int index = 0; index < specKinds.size(); ++index)
        {
            sbArgs.append(", ");
            sbArgs.append(args.get(index).convertTo(specKinds.get(index)));
        }

        final String text = String.format(
            "String.format(\"%s\"%s);", format, sbArgs.toString());

        return new TextStatement(text);
    }

    public FormatArgument createAttrCallFormatArgument(String primitive, String attribute)
    {
        return new AttrCallFormatArgument(primitive, attribute);
    }

    public FormatArgument createExprBasedFormatArgument(Expr expr)
    {
        return new ExprBasedFormatArgument(expr); 
    }
}
