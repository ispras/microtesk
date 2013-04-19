/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprFactoryJavaStatic.java, Jan 24, 2013 6:14:42 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import ru.ispras.microtesk.translator.antlrex.IErrorReporter;
import ru.ispras.microtesk.translator.antlrex.ISemanticError;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.simnml.ir.IR;
import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;

final class ExprFactoryJavaStatic extends ExprFactoryBase implements ExprFactory
{
    public ExprFactoryJavaStatic(IErrorReporter reporter, IR ir)
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
        getReporter().raiseError(w, new NonStaticExpression(location.getText()));
        return null;
    }

    @Override
    public Expr binary(Where w, String opID, Expr arg1, Expr arg2) throws SemanticException
    {
        checkJavaStatic(arg1);
        checkJavaStatic(arg2);

        final ExprFactoryCreator solver =
            new BinaryJavaStaticExprCalculator(this, w, opID, arg1, arg2);

        return solver.create();
    }

    @Override
    public Expr unary(Where w, String opID, Expr arg) throws SemanticException
    {
        checkJavaStatic(arg);

        final ExprFactoryCreator solver =
            new UnaryJavaStaticExprCalculator(this, w, opID, arg);

        return solver.create();
    }

    @Override
    public Expr evaluate(Where w, Expr src) throws SemanticException
    {
        checkJavaStatic(src);
        return src;
    }

    @Override
    public Expr coerce(Where w, Expr src, TypeExpr type) throws SemanticException
    {
        // TODO Auto-generated method stub
        assert false : "ExprFactoryJavaStatic: coerce is not implemented";
        return null;
    }

    private void checkJavaStatic(Expr expr) throws SemanticException
    {
        if (EExprKind.JAVA_STATIC != expr.getKind())
            getReporter().raiseError(new NonStaticExpression(expr.getText()));
    }

    private static final class NonStaticExpression implements ISemanticError
    {
        private final static String FORMAT =
           "The result of the %s expression cannot be statically calculated.";

        private final String exprText;

        public NonStaticExpression(String exprText)
        {
            this.exprText = exprText;
        }

        @Override
        public String getMessage()
        {
            return String.format(FORMAT, exprText);
        }
    }
}
