/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Expr.java, Jan 22, 2013 3:07:07 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.translator.simnml.ir.expression;

import ru.ispras.microtesk.translator.simnml.ir.shared.TypeExpr;

public interface Expr
{
    public EExprKind getKind();
    public String getText();

    public Class<?> getJavaType();
    public TypeExpr getModelType();

    public ExprOperator getOperator();
    public Object getValue();

    public LocationExpr getLocation();
}
