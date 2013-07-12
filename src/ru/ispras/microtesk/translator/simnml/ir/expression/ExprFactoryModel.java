/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprFactoryModel.java, Jan 24, 2013 6:14:50 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

import ru.ispras.microtesk.translator.antlrex.IErrorReporter;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.simnml.ir.IR;
import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;

final class ExprFactoryModel extends ExprFactoryBase implements ExprFactory
{
    public ExprFactoryModel(IErrorReporter reporter, IR ir)
    {
        super(reporter, ir);
    }

    @Override
    public Expr namedConst(Where w, String name) throws SemanticException
    {
        final ExprFactoryCreator creator = 
            new NamedConstBasedExprCreator(this, w, name);

        return creator.create();
    }

    @Override
    public Expr intConst(Where w, String text, int radix) throws SemanticException
    {
        final ExprFactoryCreator creator = 
            new IntegerValueBasedExprCreator(this, w, text, radix); 

        return creator.create();
    }

    @Override
    public Expr location(Where w, LocationExpr location) throws SemanticException 
    {
        final ExprFactoryCreator creator =
            new LocationBasedExprCreator(this, location);

        return creator.create();
    }

    private boolean isKindOfBoth(EExprKind kind, Expr arg1, Expr arg2)
    {
        return (arg1.getKind() == kind) && (arg2.getKind() == kind);
    }

    private boolean isKindOfOne(EExprKind kind, Expr arg1, Expr arg2)
    {
        return (arg1.getKind() == kind) || (arg2.getKind() == kind);
    }

    @Override
    public Expr binary(Where w, String opID, Expr arg1, Expr arg2) throws SemanticException
    {
        final ExprFactoryCreator creator;
        
        if (isKindOfBoth(EExprKind.MODEL, arg1, arg2))
        {
            creator = new BinaryModelExprCalculator(this, w, opID, arg1, arg2);
        }
        else if (isKindOfOne(EExprKind.MODEL, arg1, arg2))
        {
            final Expr convertedArg1, convertedArg2;

            if (JavaToModelConverter.isConversionNeeded(arg1))
            {
                convertedArg1 = new JavaToModelConverter(this, w, arg1, arg2.getModelType()).create();
                convertedArg2 = arg2;
            }
            else
            {
                convertedArg1 = arg1;
                convertedArg2 = new JavaToModelConverter(this, w, arg2, arg1.getModelType()).create();
            }

            creator = new BinaryModelExprCalculator(
                this, w, opID, convertedArg1, convertedArg2);
        }
        else if (isKindOfBoth(EExprKind.JAVA_STATIC, arg1, arg2))
        {
            creator = new BinaryJavaStaticExprCalculator(this, w, opID, arg1, arg2);
        }
        else
        {
            creator = new BinaryJavaExprCalculator(this, w, opID, arg1, arg2);
        }

        return creator.create();
    }

    @Override
    public Expr unary(Where w, String opID, Expr arg) throws SemanticException
    {
        final ExprFactoryCreator creator;

        switch(arg.getKind())
        {
        case JAVA:
            creator = new UnaryJavaExprCalculator(this, w, opID, arg);
            break;

        case JAVA_STATIC:
            creator = new UnaryJavaStaticExprCalculator(this, w, opID, arg);
            break;

        case MODEL:
            creator = new UnaryModelExprCalculator(this, w, opID, arg);
            break;

        default:
            creator = null;
            break;
        }

        assert null != creator;
        return creator.create();
    }

    @Override
    public Expr evaluate(Where w, Expr src) throws SemanticException
    {
        final ExprFactoryCreator converter = 
            new JavaToModelConverter(this, w, src, null);

        return converter.create();
    }

    @Override
    public Expr coerce(Where w, Expr src, TypeExpr type) throws SemanticException
    {
        final ExprFactoryCreator coercer = 
            new ModelExprTypeCoercer(this, w, src, type);

        return coercer.create();
    }
}
