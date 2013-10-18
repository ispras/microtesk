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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.translator.simnml.generation.utils.ExprPrinter;
import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;
import ru.ispras.microtesk.translator.simnml.ir.expression.ValueKind;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public final class Format
{
    public static final class Marker
    {
        private final static String FORMAT = "[%%][\\d]*[%s]";
        private final String tokenId;

        Marker(String tokenId)
            { this.tokenId = tokenId; }

        Marker(Marker[] markers)
        {
            final StringBuffer sb = new StringBuffer();
            for (Marker m : markers)
            {
                if (0 != sb.length()) sb.append('|');
                sb.append(m.getTokenId());
            }
            this.tokenId = sb.toString();
        }

        public String getTokenId()
            { return tokenId; }

        public String getRegExp()
            { return String.format(FORMAT, tokenId); }
    }
    
    public static interface Argument
    {
        public boolean isConvertibleTo(Marker kind);
        public String convertTo(Marker kind);
    }

    public static final Marker   DEC = new Marker("d");
    public static final Marker   BIN = new Marker("b");
    public static final Marker   HEX = new Marker("x");
    public static final Marker   STR = new Marker("s");

    public static final Marker[] ALL_ARR = {DEC, BIN, HEX, STR};
    public static final Marker   ALL = new Marker(ALL_ARR);

    public static List<Marker> extractMarkers(String format)
    {
        final List<Marker> result = new ArrayList<Marker>();

        final Matcher matcher = Pattern.compile(ALL.getRegExp()).matcher(format);
        while (matcher.find())
        {
            final String token = matcher.group();
            result.add(getFormatMarker(token));
        }

        return result;
    }

    private static Marker getFormatMarker(String token)
    {
        for (Marker m : ALL_ARR)
        {
            final Matcher matcher = Pattern.compile(m.getRegExp()).matcher(token);
            if (matcher.matches())
                return m;
        }

        assert false : "Should not reach here!";
        return null;
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
        public boolean isConvertibleTo(Marker marker)
        {
            if (STR == marker)
                return false;

            if (ValueKind.MODEL == expr.getValueInfo().getValueKind())
                return isModelConvertibleTo(marker);

            assert ValueKind.NATIVE == expr.getValueInfo().getValueKind();
            return isJavaConvertibleTo(marker);
        }

        private boolean isModelConvertibleTo(Marker marker)
        {
            if (BIN == marker)
                return true;

            assert ((DEC == marker) || (HEX == marker));

            final Type type = expr.getValueInfo().getModelType();
            if (ETypeID.CARD == type.getTypeId() || ETypeID.INT == type.getTypeId())
                return true;

            assert false : "Unsupported model data type.";
            return false;
        }

        private boolean isJavaConvertibleTo(Marker marker)
        {
            final Class<?> type = expr.getValueInfo().getNativeType();

            if (!type.equals(int.class) || !type.equals(Integer.class))
                return false;

            assert ((BIN == marker) || (DEC == marker) || (HEX == marker));
            return true;
        }

        @Override
        public String convertTo(Marker marker)
        {
            assert isConvertibleTo(marker);

            if (ValueKind.MODEL == expr.getValueInfo().getValueKind())
                return convertModelTo(marker);

            return convertJavaTo(marker);
        }

        private String convertModelTo(Marker marker)
        {
            return (BIN == marker) ?
                String.format("%s.getRawData().toBinString()", ExprPrinter.toString(expr)) : 
                String.format("%s.getRawData().intValue()", ExprPrinter.toString(expr));
        }

        private String convertJavaTo(Marker marker)
        {
            return (BIN == marker) ?
                String.format("Integer.toBinaryString(%s)", ExprPrinter.toString(expr)) :
                ExprPrinter.toString(expr);
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
        public boolean isConvertibleTo(Marker marker)
        {
            if (STR == marker)
                return true;

            if (!callInfo.getAttributeName().equals(Attribute.IMAGE_NAME))
                return false;

            assert ((BIN == marker) || (DEC == marker) || (HEX == marker));
            return true;
        }

        @Override
        public String convertTo(Marker marker)
        {
            assert isConvertibleTo(marker);

            if ((STR == marker) || (BIN == marker))
                return getCallText();

            return String.format("Integer.valueOf(%s, 2)", getCallText());
        }
    }
}
