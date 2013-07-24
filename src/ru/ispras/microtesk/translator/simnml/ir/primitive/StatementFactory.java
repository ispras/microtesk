/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * StatementFactory.java, Jul 19, 2013 11:40:51 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import java.util.List;

import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.antlrex.symbols.ISymbol;
import ru.ispras.microtesk.translator.simnml.ESymbolKind;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.errors.UndefinedPrimitive;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.expression.LocationExpr;

public final class StatementFactory extends WalkerFactoryBase
{
    private static final String ERR_UNDEFINED_ARG =
        "The %s argument is not defined.";

    private static final String ERR_IMM_HAVE_NO_ATTR =
        "The immediate value %s does not provide any callable attributes.";

    private static final String ERR_ONLY_STANDARD_ATTR =
        "Only standard attributes can be called for the %s object.";

    public StatementFactory(WalkerContext context)
    {
        super(context);
    }

    public Statement createAssignment(LocationExpr left, Expr right)
    {
        return new StatementAssignment(left, right);
    }

    public Statement createCondition(Expr cond, List<Statement> isSmts, List<Statement> elseSmts)
    {
        return new StatementCondition(cond, isSmts, elseSmts);
    }

    public Statement createAttributeCall(Where where, String attributeName) throws SemanticException
    {
        assert null != attributeName;

        final ISymbol<ESymbolKind> symbol = 
              getSymbols().resolveMember(attributeName);

        if ((null == symbol) || (symbol.getKind() != ESymbolKind.ATTRIBUTE))
            raiseError(where, new UndefinedPrimitive(attributeName, ESymbolKind.ATTRIBUTE));

        return new StatementAttributeCall(getThis(), null, attributeName);
    }

    public Statement createAttributeCall(Where where, String calleeName, String attributeName) throws SemanticException
    {
        assert null != attributeName;
        assert null != calleeName;

        if (!getThisArgs().containsKey(calleeName))
            raiseError(where, String.format(ERR_UNDEFINED_ARG, calleeName));

        final Primitive callee = getThisArgs().get(calleeName);

        if (Primitive.Kind.IMM == callee.getKind())
            raiseError(where, String.format(ERR_IMM_HAVE_NO_ATTR, calleeName));

        if (!Attribute.STANDARD_NAMES.contains(attributeName))
            raiseError(where, String.format(ERR_ONLY_STANDARD_ATTR, calleeName));

        return new StatementAttributeCall(callee, calleeName, attributeName);
    }

    public Statement createComment(String text)
    {
        return new StatementText(String.format("// %s", text));
    }
}
