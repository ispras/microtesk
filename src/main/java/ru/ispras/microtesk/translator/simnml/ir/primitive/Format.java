/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Format.java, Jul 25, 2013 1:20:05 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import ru.ispras.microtesk.model.api.type.TypeId;
import ru.ispras.microtesk.translator.simnml.generation.PrinterExpr;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;
import ru.ispras.microtesk.utils.FormatMarker;

public final class Format
{
    public static interface Argument
    {
        public boolean isConvertibleTo(FormatMarker kind);
        public String convertTo(FormatMarker kind);
    }

    public static Argument createArgument(Expr expr)
    {
        return new ExprBasedArgument(expr);
    }

    public static Argument createArgument(StatementAttributeCall call)
    {
        return new AttributeCallBasedArgument(call);
    }
    
    private static final class ExprBasedArgument implements Argument
    {
        private final Expr expr;

        public ExprBasedArgument(Expr expr)
        {
            this.expr = expr;
        }

        @Override
        public boolean isConvertibleTo(FormatMarker marker)
        {
            if (FormatMarker.STR == marker)
                return true;

            if (expr.getValueInfo().isModel())
                return isModelConvertibleTo(marker);

            return isJavaConvertibleTo(marker);
        }

        private boolean isModelConvertibleTo(FormatMarker marker)
        {
            if (FormatMarker.BIN == marker)
                return true;

            assert ((FormatMarker.DEC == marker) || (FormatMarker.HEX == marker));

            final Type type = expr.getValueInfo().getModelType();
            if (TypeId.CARD == type.getTypeId() || TypeId.INT == type.getTypeId())
                return true;

            assert false : "Unsupported model data type.";
            return false;
        }

        private boolean isJavaConvertibleTo(FormatMarker marker)
        {
            final Class<?> type = expr.getValueInfo().getNativeType();

            if (!type.equals(int.class) || !type.equals(Integer.class))
                return false;

            assert ((FormatMarker.BIN == marker) || (FormatMarker.DEC == marker) || (FormatMarker.HEX == marker));
            return true;
        }

        @Override
        public String convertTo(FormatMarker marker)
        {
            assert isConvertibleTo(marker);

            if (expr.getValueInfo().isModel())
                return convertModelTo(marker);

            return convertJavaTo(marker);
        }

        private String convertModelTo(FormatMarker marker)
        {
            final String methodName;

            if (FormatMarker.BIN == marker)
                methodName = "toBinString";
            else if (FormatMarker.STR == marker)
                methodName = "toString";
            else
                methodName = "intValue";
            
            return String.format(
                "%s.getRawData().%s()", new PrinterExpr(expr), methodName);
        }

        private String convertJavaTo(FormatMarker marker)
        {
            final PrinterExpr printer = new PrinterExpr(expr);

            return (FormatMarker.BIN == marker) ?
                String.format("Integer.toBinaryString(%s)", printer) : printer.toString();
        }
    }

    private static final class AttributeCallBasedArgument implements Argument
    {
        private final StatementAttributeCall callInfo;

        public AttributeCallBasedArgument(StatementAttributeCall callInfo)
        {
            assert null != callInfo;
            this.callInfo = callInfo;
        }

        private String getCallText()
        {
            final StringBuilder sb = new StringBuilder();

            if (null != callInfo.getCalleeName())
                sb.append(String.format("%s.", callInfo.getCalleeName()));

            sb.append(String.format("%s()", callInfo.getAttributeName()));
            return sb.toString();
        }

        @Override
        public boolean isConvertibleTo(FormatMarker marker)
        {
            if (FormatMarker.STR == marker)
                return true;

            if (!callInfo.getAttributeName().equals(Attribute.IMAGE_NAME))
                return false;

            assert ((FormatMarker.BIN == marker) || (FormatMarker.DEC == marker) || (FormatMarker.HEX == marker));
            return true;
        }

        @Override
        public String convertTo(FormatMarker marker)
        {
            assert isConvertibleTo(marker);

            if ((FormatMarker.STR == marker) || (FormatMarker.BIN == marker))
                return getCallText();

            return String.format("Integer.valueOf(%s, 2)", getCallText());
        }
    }
}
