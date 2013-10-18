/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprNodeCoercion.java, Sep 25, 2013 12:07:03 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

/**
 * The ExprNodeCoercion class describes cast of a source expression to the specified type.
 * 
 * @author Andrei Tatarnikov
 */

public final class ExprNodeCoercion extends ExprAbstract 
{
    private final Expr source;

    /**
     * Creates a cast of source expression to the specified Model API type.  
     * 
     * @param source Source expression.
     * @param type Target Model API type.
     */

    ExprNodeCoercion(Expr source, Type type)
    {
        super(NodeKind.COERCION, ValueInfo.createModel(type));

        assert null != source;
        this.source = source;
    }

    /**
     * Creates a cast of source expression to the specified native Java type.
     * 
     * @param source Source expression.
     * @param type Target native Java type.
     */

    ExprNodeCoercion(Expr source, Class<?> type)
    {
        super(NodeKind.COERCION, nativeCast(source.getValueInfo(), type));

        assert null != source;
        this.source = source;
    }
    
    private static ValueInfo nativeCast(ValueInfo source, Class<?> type)
    {
        if (!source.isConstant())
            return ValueInfo.createNativeType(type);

        final Object newValue = 
            NativeTypeCastRules.castTo(type, source.getNativeValue());

        assert null != newValue;
        return ValueInfo.createNative(newValue);
    }

    /**
     * Returns source expression. 
     * 
     * @return Source expression.
     */

    public Expr getSource()
    {
        return source;
    }
}
