/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * FormatArgument.java, Feb 8, 2013 5:20:39 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.modeop;

import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.translator.simnml.ir.expression2.EExprKind;
import ru.ispras.microtesk.translator.simnml.ir.expression2.Expr;
import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;

public interface FormatArgument
{
    public boolean isConvertibleTo(FormatKind kind);
    public String convertTo(FormatKind kind);
}

final class ExprBasedFormatArgument implements FormatArgument
{
    private final Expr expr;

    public ExprBasedFormatArgument(Expr expr)
    {
        this.expr = expr;
    }

    @Override
    public boolean isConvertibleTo(FormatKind kind)
    {
        if (kind == FormatKind.STRING)
            return false;

        if (EExprKind.MODEL == expr.getKind())
            return isModelConvertibleTo(kind);

        assert (EExprKind.JAVA == expr.getKind() ||
                EExprKind.JAVA_STATIC == expr.getKind());

        return isJavaConvertibleTo(kind);
    }

    private boolean isModelConvertibleTo(FormatKind kind)
    {
        if (kind == FormatKind.BINARY)
            return true;

        assert ((kind == FormatKind.DECIMAL) ||
                (kind == FormatKind.HEXADECIMAL));

        final TypeExpr type = expr.getModelType();

        if (ETypeID.CARD == type.getTypeId() ||
            ETypeID.INT == type.getTypeId())
            return true;

        assert false : "Unsupported model data type.";
        return false;
    }
    
    private boolean isJavaConvertibleTo(FormatKind kind)
    {
        final Class<?> type = expr.getJavaType();

        if (!type.equals(int.class) || !type.equals(Integer.class))
            return false;

        assert ((kind == FormatKind.BINARY)  || 
                (kind == FormatKind.DECIMAL) || 
                (kind == FormatKind.HEXADECIMAL));

        return true;
    }

    @Override
    public String convertTo(FormatKind kind)
    {
        assert isConvertibleTo(kind);

        if (EExprKind.MODEL == expr.getKind())
            return convertModelTo(kind);

        return convertJavaTo(kind);
    }

    private String convertModelTo(FormatKind kind)
    {
        if (kind == FormatKind.BINARY)
            return String.format("%s.getRawData().toBinString()", expr.getText());

        return String.format("%s.getRawData().intValue()", expr.getText());
    }

    private String convertJavaTo(FormatKind kind)
    {
        if (kind == FormatKind.BINARY)
            return String.format("Integer.toBinaryString(%s)", expr.getText());

        return expr.getText();
    }
}

final class AttrCallFormatArgument implements FormatArgument
{
    private final String primitive;
    private final String attribute;

    public AttrCallFormatArgument(String primitive, String attribute)
    {
        this.primitive = primitive;
        this.attribute = attribute;
    }

    private String getCallText()
    {
        return String.format("%s.%s()", primitive, attribute);
    }

    @Override
    public boolean isConvertibleTo(FormatKind kind)
    {
        if (kind == FormatKind.STRING)
            return true;

        if (!attribute.equals("image"))
            return false;

        assert ((kind == FormatKind.BINARY)  || 
                (kind == FormatKind.DECIMAL) || 
                (kind == FormatKind.HEXADECIMAL));

        return true;
    }

    @Override
    public String convertTo(FormatKind kind)
    {
        assert isConvertibleTo(kind);

        if (kind == FormatKind.STRING)
            return getCallText();

        if (kind == FormatKind.BINARY)
            return getCallText();

        return String.format("Integer.valueOf(%s, 2)", getCallText()) ;
    }
}
