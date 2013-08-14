/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ExprFactory.java, Jan 22, 2013 3:07:49 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.simnml.ir.shared.Type;

public interface ExprFactory
{
    public Expr namedConst(Where w, String name) throws SemanticException;
    public Expr intConst(Where w, String text, int radix) throws SemanticException;

    public Expr location(Where w, Location location) throws SemanticException;

    public Expr binary(Where w, String opID, Expr arg1, Expr arg2) throws SemanticException;
    public Expr unary(Where w, String opID, Expr arg) throws SemanticException;

    public Expr evaluate(Where w, Expr src) throws SemanticException;
    public Expr coerce(Where w, Expr src, Type type) throws SemanticException;
}
