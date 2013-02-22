/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * LocationExprFactory.java, Jan 23, 2013 3:44:43 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression2;

import java.util.Map;

import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.antlrex.exception.SemanticException;
import ru.ispras.microtesk.translator.simnml.ir.modeop.ArgumentTypeExpr;

public interface LocationExprFactory
{
    public LocationExpr location(
         Where w, String name, Map<String, ArgumentTypeExpr> globalArgTypes) throws SemanticException;

    public LocationExpr location(
         Where w, String name, Expr index) throws SemanticException;

    public LocationExpr bitfield(
         Where w, LocationExpr loc, Expr start, Expr end) throws SemanticException;

    public LocationExpr concat(
         Where w, LocationExpr loc1, LocationExpr loc2) throws SemanticException;
}
