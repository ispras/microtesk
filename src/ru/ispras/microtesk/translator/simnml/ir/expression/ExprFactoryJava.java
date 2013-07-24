/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprFactoryJava.java, Jan 24, 2013 6:08:38 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.simnml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;

final class ExprFactoryJava extends WalkerFactoryBase implements ExprFactory
{
    public ExprFactoryJava(WalkerContext context)
    {
        super(context);
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

        final ExprFactoryCreator converter = 
            new ModelToJavaConverter(this, w, creator.create());

        return converter.create();
    }

    @Override
    public Expr binary(
        Where w, String opID, Expr arg1, Expr arg2) throws SemanticException
    {
        final ExprFactoryCreator converterArg1 = 
            new ModelToJavaConverter(this, w, arg1);

        final ExprFactoryCreator converterArg2 = 
            new ModelToJavaConverter(this, w, arg2);

        final ExprFactoryCreator calculator =
            new BinaryJavaExprCalculator(this, w, opID, converterArg1.create(), converterArg2.create());

        return calculator.create();
    }

    @Override
    public Expr unary(Where w, String opID, Expr arg) throws SemanticException
    {
        final ExprFactoryCreator converter = 
            new ModelToJavaConverter(this, w, arg);

        final ExprFactoryCreator calculator =
            new UnaryJavaExprCalculator(this, w, opID, converter.create());

        return calculator.create();
    }

    @Override
    public Expr evaluate(Where w, Expr src) throws SemanticException
    {
        final ExprFactoryCreator converter = 
            new ModelToJavaConverter(this, w, src);

        return converter.create();
    }

    @Override
    public Expr coerce(Where w, Expr src, TypeExpr type) throws SemanticException
    {
        final ExprFactoryCreator coercer = 
                new ModelExprTypeCoercer(this, w, src, type);

            return coercer.create();
        
        // TODO Auto-generated method stub
        //assert false : "ExprFactoryJava: coerce is not implemented";
        //return null;
    }
}
