/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprFactoryCreator.java, Feb 1, 2013 11:33:13 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

import java.math.BigInteger;

import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.translator.antlrex.ISemanticError;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.errors.UndefinedConstant;
import ru.ispras.microtesk.translator.simnml.errors.ValueParsingFailure;
import ru.ispras.microtesk.translator.simnml.generation.utils.LocationPrinter;
import ru.ispras.microtesk.translator.simnml.ir.shared.LetExpr;
import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;

interface ExprFactoryCreator
{
    public Expr create() throws SemanticException;
}

final class LocationBasedExprCreator extends WalkerFactoryBase implements ExprFactoryCreator
{
    private final Location location;

    public LocationBasedExprCreator(
        WalkerFactoryBase context,
        Location location
        )
    {
        super(context);
        this.location = location;
    }

    public Expr create()
    {
        final String LOCATION_TO_DATA_FORMAT = "%s.load()";
        
        assert null != location;
        return new ExprClass(
            EExprKind.MODEL,
            String.format(LOCATION_TO_DATA_FORMAT, LocationPrinter.toString(location)),
            null,
            location.getType(),
            null,
            null,
            location
            );
    }
}

final class IntegerValueBasedExprCreator extends WalkerFactoryBase implements ExprFactoryCreator
{
    private static final int BIN_RADIX = 2;
    private static final int DEC_RADIX = 10;
    private static final int HEX_RADIX = 16;

    private static final int BITS_IN_HEX_CHAR = 4;
    private static final String HEX_PREFIX = "0x";

    private final Where w;
    private final String text;
    private final int radix;

    public IntegerValueBasedExprCreator(
        WalkerFactoryBase context,
        Where w,
        String text,
        int radix
        )
    {
        super(context);
        
        this.w = w;
        this.text = text;
        this.radix = radix;
    }

    @Override
    public Expr create() throws SemanticException
    {
        final boolean isHex = (HEX_RADIX == radix);

        try
        {           
            final BigInteger bi = new BigInteger(text, radix);
            if (bi.bitLength() > Integer.SIZE)
                throw new NumberFormatException("For input string: \"" + text + "\"");

            final Integer v = bi.intValue();
            return new ExprClass(
                EExprKind.JAVA_STATIC,
                isHex ? HEX_PREFIX + Integer.toHexString(v) : Integer.toString(v, DEC_RADIX),
                int.class,
                createModelType(),
                null,
                v,
                null
                );
        }
        catch (NumberFormatException e) {}

        getReporter().raiseError(w, new ValueParsingFailure(text, int.class.getSimpleName()));
        return null;
    }
/*
    private int getBitSizeForInt(int value)
    {
        final int BITS_IN_BYTE = 8;

        final int bitSize =
            Integer.SIZE - Integer.numberOfLeadingZeros(value);

        if (0 == bitSize)
            return BITS_IN_BYTE;

        return (0 == bitSize % BITS_IN_BYTE) ?
            bitSize : 
            bitSize + (BITS_IN_BYTE - bitSize % BITS_IN_BYTE);
    }
*/
    private TypeExpr createModelType() throws SemanticException
    {
        final ETypeID typeId;
        final int bitSize;

        if (BIN_RADIX == radix)
        {
            typeId  = ETypeID.CARD;
            bitSize = text.length();
        }
        else if (HEX_RADIX == radix)
        {
            typeId  = ETypeID.CARD;
            bitSize = text.length() * BITS_IN_HEX_CHAR;
        }
        else
        {
            // TODO: Temporary assumption. A decimal integer literal is always 32-bit int.
            typeId  = ETypeID.INT;
            bitSize = Integer.SIZE; // getBitSizeForInt(Integer.parseInt(text, radix));
        }

        return new TypeExpr(
            typeId,
            ExprClass.createConstant(bitSize, Integer.toString(bitSize))
            );
    }
}

final class NamedConstBasedExprCreator extends WalkerFactoryBase implements ExprFactoryCreator
{
    private final Where w;
    private final String name;

    public NamedConstBasedExprCreator(
        WalkerFactoryBase context,
        Where w,
        String name
        )
    {
        super(context);

        this.w = w;
        this.name = name;
    }

    @Override
    public Expr create() throws SemanticException
    {
        if (!getIR().getLets().containsKey(name))
            getReporter().raiseError(w, new UndefinedConstant(name));

        final LetExpr letExpr = getIR().getLets().get(name);
        return new ExprClass(
            EExprKind.JAVA_STATIC,
            name,
            letExpr.getJavaType(),
            createModelTypeForJavaType(letExpr.getJavaType()),
            null,
            letExpr.getValue(),
            null
            );
    }

    private TypeExpr createModelTypeForJavaType(Class<?> javaType) throws SemanticException
    {
        if (javaType.equals(int.class) || javaType.equals(Integer.class))
        {
            return new TypeExpr(
                ETypeID.INT,
                ExprClass.createConstant(Integer.SIZE, Integer.toString(Integer.SIZE)) 
            );
        }

        assert false : "Not supported";
        return null;
    }
}

final class ModelToJavaConverter extends WalkerFactoryBase implements ExprFactoryCreator
{
    private final String TO_JAVA_INT_FORMAT = "DataEngine.intValue(%s)";

    private final Where w;
    private final Expr expr;

    public ModelToJavaConverter(
        WalkerFactoryBase context,
        Where w,
        Expr expr
        )
    {
        super(context);

        assert null != expr;

        this.w = w; 
        this.expr = expr;
    }

    @Override
    public Expr create() throws SemanticException
    {
        if (!isConversionNeeded())
           return expr;

        assert null != expr.getModelType();
        final TypeExpr modelType = expr.getModelType(); 

        if ((modelType.getTypeId() != ETypeID.CARD) &&
            (modelType.getTypeId() != ETypeID.INT))
        {
            getReporter().raiseError(
                w, new UnsupportedModelType(expr.getModelType().getTypeId()));
        }

        assert modelType.getBitSize().getJavaType().equals(int.class);
        final int dataSize = (Integer) expr.getModelType().getBitSize().getValue();

        if (dataSize > Integer.SIZE)
        {
            getReporter().raiseError(
                w, new TooLargeDataSize(expr.getText(), dataSize));
        }

        return new ExprClass(
            EExprKind.JAVA,
            String.format(TO_JAVA_INT_FORMAT, expr.getText()),
            int.class,
            expr.getModelType(),
            null,
            null,
            null
        );
    }

    private boolean isConversionNeeded()
    {
        final boolean isModel = EExprKind.MODEL == expr.getKind();

        if (!isModel)
        {
            assert expr.getKind() == EExprKind.JAVA_STATIC||
                   expr.getKind() == EExprKind.JAVA;
        }

        return isModel;
    }

    private final static class UnsupportedModelType implements ISemanticError
    {
        private final static String FORMAT =
            "The %s data type is not supported in expressions of this kind.";

        private final ETypeID typeID;

        public UnsupportedModelType(ETypeID typeID)
        {
            this.typeID = typeID;
        }

        @Override
        public String getMessage()
        {
            return String.format(FORMAT, typeID.name());
        }
    }
    
    private final static class TooLargeDataSize implements ISemanticError
    {
        private static final String FORMAT =
            "The data buffer returned by the %s expression is too large (%d bits) " +
            "to be converted to a corresponsing Java type.";

        private final String exprText;
        private final    int dataSize;

        public TooLargeDataSize(String exprText, int dataSize)
        {
            this.exprText = exprText;
            this.dataSize = dataSize;
        }

        @Override
        public String getMessage()
        {
            return String.format(FORMAT, exprText, dataSize);
        }
    }
}

final class JavaToModelConverter extends WalkerFactoryBase implements ExprFactoryCreator
{
    private final String TO_MODEL_FORMAT = "DataEngine.valueOf(%s, %s)";

    @SuppressWarnings("unused")
    private final Where w; // TODO: temporary unused (will be used by error checks)
    private final Expr expr;
    private final TypeExpr targetType;

    public JavaToModelConverter(WalkerFactoryBase context, Where w, Expr expr, TypeExpr targetType)
    {
        super(context);

        this.w = w;
        this.expr = expr;
        this.targetType = targetType;
    }
    
    private final String getTypeCode(TypeExpr type)
    {
        if (null != type.getRefName())
            return type.getRefName();

        return String.format(
            "new Type(%s.%s, %s)",
            ETypeID.class.getSimpleName(),
            type.getTypeId().name(),
            type.getBitSize().getText()
            );
    }
    
    private TypeExpr createModelTypeForJavaType(Class<?> javaType) throws SemanticException
    {
        if (javaType.equals(int.class) || javaType.equals(Integer.class))
        {
            return new TypeExpr(
                ETypeID.INT,
                ExprClass.createConstant(Integer.SIZE, Integer.toString(Integer.SIZE))
            );
        }

        assert false : "Not supported";
        return null;
    }

    @Override
    public Expr create() throws SemanticException
    {
        if (!isConversionNeeded())
            return expr;

        final TypeExpr newType;

        if (null != targetType)
        {
            newType = targetType;
        }
        else if (null != expr.getModelType())
        {
            newType = expr.getModelType();
        }
        else
        {
            newType = createModelTypeForJavaType(expr.getJavaType());
        }

        return new ExprClass(
            EExprKind.MODEL,
            String.format(TO_MODEL_FORMAT, getTypeCode(newType), expr.getText()),
            expr.getJavaType(),
            newType,
            null,
            null,
            null
            );
    }
    
    private boolean isConversionNeeded()
    {
        return isConversionNeeded(expr);
    }
    
    public static boolean isConversionNeeded(Expr expr)
    {
        final boolean isModel = EExprKind.MODEL == expr.getKind();

        if (!isModel)
        {
            assert expr.getKind() == EExprKind.JAVA_STATIC ||
                   expr.getKind() == EExprKind.JAVA;
        }

        return !isModel;
    }
}

abstract class ExprCalculatorBase extends WalkerFactoryBase
{
    private final Where w;
    private final String opID;

    public ExprCalculatorBase(
        WalkerFactoryBase context,
        Where w,
        String opID
        )
    {
        super(context);

        this.w = w;
        this.opID = opID;
    }

    protected final Where getWhere()
    { 
        return w;
    }

    protected final String getOpID()
    { 
        return opID;
    }

    protected abstract ExprOperator getOperator();
    
    protected final JavaTypeRules.Cast getCast(Class<?> type1, Class<?> type2) throws SemanticException 
    {
        final JavaTypeRules.Cast result = JavaTypeRules.getCast(type1, type2);

        if (null == result)
        {
            getReporter().raiseError(
                getWhere(),
                new IncompatibleTypes(type1.getSimpleName(), type2.getSimpleName())
            );
        }

        return result;
    }
    
    protected final TypeExpr getCast(TypeExpr type1, TypeExpr type2) throws SemanticException 
    {
        // TODO: CHECK SIZE (SHOULD BE EQUAL)
        // System.out.println(type1.getBitSize().getValue());
        // System.out.println(type2.getBitSize().getValue());
        // assert type1.getBitSize().getValue().equals(type2.getBitSize().getValue());

        final ETypeID resultTypeId = 
            ModelTypeRules.getCastType(type1.getTypeId(), type2.getTypeId());

        if (null == resultTypeId)
        {
            getReporter().raiseError(
                getWhere(),
                new IncompatibleTypes(type1.getTypeId().name(), type2.getTypeId().name())
            );
        }
        
        return new TypeExpr(resultTypeId, type1.getBitSize());
    }

    protected final void checkSupported(Class<?> javaType) throws SemanticException
    {
        if (!getOperator().isSupported(javaType))
        {
            getReporter().raiseError(
                getWhere(),
                new UnsupportedOperator(getOpID(), javaType.getSimpleName())
            );
        }
    }

    protected final void checkSupported(ETypeID modelType) throws SemanticException
    {
        if (!getOperator().isSupported(modelType))
        {
            getReporter().raiseError(
                getWhere(),
                new UnsupportedOperator(getOpID(), modelType.name())
            );
        }
    }
    
    protected final boolean needBrackets(Expr arg)
    {
        if (null == arg.getOperator())
            return false;

        return arg.getOperator().getPriority() < getOperator().getPriority();
    }

    protected final String getArgText(Expr arg)
    {
        return needBrackets(arg) ?
            String.format("(%s)", arg.getText()) : arg.getText();
    }

    private class UnsupportedOperator implements ISemanticError
    {
        private static final String FORMAT =
            "The %s operator is not supported for the %s type.";

        private final String opID;
        private final String typeName;

        public UnsupportedOperator(String opID, String typeName)
        {
            this.opID = opID;
            this.typeName = typeName;
        }

        @Override
        public String getMessage()
        {
            return String.format(FORMAT, opID, typeName);
        }
    }
    
    private class IncompatibleTypes implements ISemanticError
    {
        private static final String FORMAT =
            "The %s and %s are incompatible and cannot be used together as operator arguments.";

        private final String typeName1;
        private final String typeName2;

        public IncompatibleTypes(String typeName1, String typeName2)
        {
            this.typeName1 = typeName1;
            this.typeName2 = typeName2;
        }

        @Override
        public String getMessage()
        {
            return String.format(FORMAT, typeName1, typeName2);
        }
    }
}

abstract class UnaryExprCalculatorBase extends ExprCalculatorBase
{
    protected final Expr arg;

    public UnaryExprCalculatorBase(
        WalkerFactoryBase context,
        Where w,
        String opID,
        Expr arg,
        final EExprKind expectedKind
        )
    {
        super(context, w, opID);

        assert arg.getKind() == expectedKind;

        this.arg = arg;
    }

    protected final ExprOperatorUnary getOperator()
    {
        assert ExprUnaryOperators.get().isSupported(getOpID());
        return ExprUnaryOperators.get().getOperator(getOpID());
    }
}

abstract class BinaryExprCalculatorBase extends ExprCalculatorBase
{
    protected final Expr arg1;
    protected final Expr arg2;

    public BinaryExprCalculatorBase(
        WalkerFactoryBase context,
        Where w,
        String opID,
        Expr arg1,
        Expr arg2,
        final EExprKind expectedKind
        )
    {
        super(context, w, opID);

 //     TODO: a different check java and java static are compatible if we
 //     don't calculate the value.

 //     assert arg1.getKind() == expectedKind;  
 //     assert arg2.getKind() == expectedKind; 

        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    protected final ExprOperatorBinary getOperator()
    {
        assert ExprBinaryOperators.get().isSupported(getOpID()) : 
            String.format("The '%s' operator is not supported", getOpID());

        return ExprBinaryOperators.get().getOperator(getOpID());
    }
}

final class UnaryJavaExprCalculator extends UnaryExprCalculatorBase implements ExprFactoryCreator
{
    public UnaryJavaExprCalculator(
        WalkerFactoryBase context,
        Where w,
        String opID,
        Expr arg
        )
    {
        super(
            context,
            w,
            opID,
            arg,
            EExprKind.JAVA
        );
    }

    @Override
    public Expr create() throws SemanticException
    {
        final ExprOperatorUnary op = getOperator();

        final Class<?> javaType = arg.getJavaType();
        checkSupported(javaType);

        final String newText = op.translate(javaType, getArgText(arg));

        return new ExprClass(
            EExprKind.JAVA,
            newText,
            javaType,
            arg.getModelType(),
            op,
            null,
            null
            );
    }
}

final class BinaryJavaExprCalculator extends BinaryExprCalculatorBase implements ExprFactoryCreator
{
    public BinaryJavaExprCalculator(
        WalkerFactoryBase context,
        Where w,
        String opID,
        Expr arg1,
        Expr arg2
        )
    {
        super(
            context,
            w,
            opID,
            arg1,
            arg2,
            EExprKind.JAVA
        );
    }

    @Override
    public Expr create() throws SemanticException
    {
        final ExprOperatorBinary op = getOperator();

        final JavaTypeRules.Cast cast =
            getCast(arg1.getJavaType(), arg2.getJavaType());

        final Class<?> javaType = cast.getTargetType();
        checkSupported(javaType);

        final String newText =
            op.translate(javaType, getArgText(arg1), getArgText(arg2));

        return new ExprClass(
            EExprKind.JAVA,
            newText,
            javaType,
            null, // TODO:
            op,
            null,
            null
            );
    }
}

final class UnaryJavaStaticExprCalculator extends UnaryExprCalculatorBase implements ExprFactoryCreator
{
    public UnaryJavaStaticExprCalculator(
         WalkerFactoryBase context,
         Where w,
         String opID,
         Expr arg
         )
    {
        super(
            context,
            w,
            opID,
            arg,
            EExprKind.JAVA_STATIC
        );    
    }

    @Override
    public Expr create() throws SemanticException
    {
        final ExprOperatorUnary op = getOperator();

        final Class<?> javaType = arg.getJavaType();
        checkSupported(javaType);

        final Object newValue = op.calculate(javaType, arg.getValue());
        final String newText = op.translate(javaType, getArgText(arg));

        return new ExprClass(
            EExprKind.JAVA_STATIC,
            newText,
            javaType,
            arg.getModelType(),
            op,
            newValue,
            null
            );
    }
}

final class BinaryJavaStaticExprCalculator extends BinaryExprCalculatorBase implements ExprFactoryCreator
{
    public BinaryJavaStaticExprCalculator(
        WalkerFactoryBase context,
        Where w,
        String opID,
        Expr arg1,
        Expr arg2
        )
    {
        super(
            context,
            w,
            opID,
            arg1,
            arg2,
            EExprKind.JAVA_STATIC
        );
    }

    @Override
    public Expr create() throws SemanticException 
    {
        final ExprOperatorBinary op = getOperator();

        final JavaTypeRules.Cast cast =
            getCast(arg1.getJavaType(), arg2.getJavaType());

        final Class<?> javaType = cast.getTargetType();
        checkSupported(javaType);

        final Object newValue =
            op.calculate(javaType, cast.cast(arg1.getValue()), cast.cast(arg2.getValue()));

        final String newText =
            op.translate(javaType, getArgText(arg1), getArgText(arg2));

        return new ExprClass(
            EExprKind.JAVA_STATIC,
            newText,
            javaType,
            null, // TODO
            op,
            newValue,
            null
            );
    }
}

final class UnaryModelExprCalculator extends UnaryExprCalculatorBase implements ExprFactoryCreator
{
    public UnaryModelExprCalculator(
        WalkerFactoryBase context,
        Where w,
        String opID,
        Expr arg
        )
    {
        super(
            context,
            w,
            opID,
            arg,
            EExprKind.MODEL
        );    
    }

    @Override
    public Expr create() throws SemanticException
    {
        final ExprOperatorUnary op = getOperator();

        final TypeExpr modelType = arg.getModelType();
        checkSupported(modelType.getTypeId());

        final String newText = op.translate(modelType.getTypeId(), getArgText(arg));

        return new ExprClass(
            EExprKind.MODEL,
            newText,
            null,
            modelType,
            op,
            null,
            null
            );
    }
}

final class BinaryModelExprCalculator extends BinaryExprCalculatorBase implements ExprFactoryCreator
{
    public BinaryModelExprCalculator(
        WalkerFactoryBase context,
        Where w,
        String opID,
        Expr arg1,
        Expr arg2
        )
    {
        super(
            context,
            w,
            opID,
            arg1,
            arg2,
            EExprKind.MODEL
        );
    }

    @Override
    public Expr create() throws SemanticException
    {
        final ExprOperatorBinary op = getOperator();
        
        final TypeExpr modelType = getCast(arg1.getModelType(), arg2.getModelType());
        checkSupported(modelType.getTypeId());

        final String newText =
            op.translate(modelType.getTypeId(), getArgText(arg1), getArgText(arg2));

        return new ExprClass(
            EExprKind.MODEL,
            newText,
            null,
            modelType,
            op,
            null,
            null
            );
    }
}

final class ModelExprTypeCoercer extends WalkerFactoryBase implements ExprFactoryCreator
{
    private static final String COERCE_FORMAT = "DataEngine.coerce(%s, %s)"; 

    @SuppressWarnings("unused")
    private final Where w; // TODO: TEMPORARILY UNUSED
    private final Expr src;
    private final TypeExpr type;

    public ModelExprTypeCoercer(
        WalkerFactoryBase context,
        Where w,
        Expr src,
        TypeExpr type
        )
    {
        super(context);

        this.w = w;
        this.src = src;
        this.type = type;
    }

    @Override
    public Expr create() throws SemanticException
    {
        return new ExprClass(
            EExprKind.MODEL,
            String.format(COERCE_FORMAT, getTypeCode(), src.getText()),
            null,
            type,
            null,
            null,
            null
            );
    }

    private final String getTypeCode()
    {
        if (null != type.getRefName())
            return type.getRefName();

        return String.format(
            "new Type(%s.%s, %s)",
            ETypeID.class.getSimpleName(),
            type.getTypeId().name(),
            type.getBitSize().getText()
            );
    }
}
