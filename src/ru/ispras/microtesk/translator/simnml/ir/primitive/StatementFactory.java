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

import ru.ispras.microtesk.model.api.state.Status;
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
    private static final String UNDEFINED_ARG =
        "The %s argument is not defined.";

    private static final String IMM_HAVE_NO_ATTR =
        "The immediate value %s does not provide any callable attributes.";

    private static final String ONLY_STANDARD_ATTR =
        "Only standard attributes can be called for the %s object.";

    private static final String WRONG_FORMAT_ARG_SPEC =
        "Incorrect format specification. The number of arguments specified in the format string (%d) " +
        "does not match to the number of provided argumens (%d).";

    private static final String UNDEFINED_ATTR =
        "The %s arrtibute is not defined for the %s primitive.";

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

        return new StatementAttributeCall(null, attributeName);
    }

    public Statement createAttributeCall(Where where, String calleeName, String attributeName) throws SemanticException
    {
        assert null != attributeName;
        assert null != calleeName;

        if (!getThisArgs().containsKey(calleeName))
            raiseError(where, String.format(UNDEFINED_ARG, calleeName));

        final Primitive callee = getThisArgs().get(calleeName);

        if (Primitive.Kind.IMM == callee.getKind())
            raiseError(where, String.format(IMM_HAVE_NO_ATTR, calleeName));

        if (!Attribute.STANDARD_NAMES.contains(attributeName))
            raiseError(where, String.format(ONLY_STANDARD_ATTR, calleeName));

        if (!callee.getAttrNames().contains(attributeName))
            raiseError(where, String.format(UNDEFINED_ATTR, attributeName, callee.getName())); 

        return new StatementAttributeCall(calleeName, attributeName);
    }

    public Statement createControlTransfer(int index)
    {
        return new StatementStatus(Status.CTRL_TRANSFER, index);
    }

    public Statement createFormat(Where where, String format, List<Format.Argument> args) throws SemanticException
    {
        if (null == args)
            return new StatementFormat(format, null, null);

        final List<Format.Marker> markers = Format.extractMarkers(format);

        if (markers.size() != args.size())
            raiseError(where, String.format(WRONG_FORMAT_ARG_SPEC, markers.size(), args.size()));

        return new StatementFormat(format, markers, args);
     }
}
