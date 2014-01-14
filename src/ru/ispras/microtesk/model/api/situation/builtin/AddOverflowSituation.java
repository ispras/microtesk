/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IntegerOverflowSituation.java, May 20, 2013 11:46:39 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.situation.builtin;

import ru.ispras.fortress.expression.NodeExpr;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.solver.constraint.Constraint;
import ru.ispras.fortress.solver.constraint.ConstraintBuilder;
import ru.ispras.fortress.solver.constraint.ConstraintKind;
import ru.ispras.fortress.solver.constraint.Formulas;
import ru.ispras.microtesk.model.api.situation.ISituation;

public final class AddOverflowSituation extends ConstraintBasedSituation
{
    private static final   String    NAME = "overflow";
    private static final IFactory FACTORY = new IFactory()
    {
        @Override
        public ISituation create() { return new AddOverflowSituation(); }
    };

    public static final IInfo INFO = new Info(NAME, FACTORY);

    public AddOverflowSituation()
    {
        super(
            INFO,
            new AddOverflowConstraintBuilder()
        );
    }
}

final class AddOverflowConstraintBuilder extends OverflowConstraintFactory
{
    public Constraint create()
    {
        final ConstraintBuilder builder = new ConstraintBuilder();

        builder.setName("AddOverflow");
        builder.setKind(ConstraintKind.FORMULA_BASED);
        builder.setDescription("AddOverflow constraint");

        // Unknown variables
        final NodeVariable rs = new NodeVariable(builder.addVariable("src2", BIT_VECTOR_TYPE));
        final NodeVariable rt = new NodeVariable(builder.addVariable("src3", BIT_VECTOR_TYPE));

        final Formulas formulas = new Formulas();
        builder.setInnerRep(formulas);

        formulas.add(IsValidSignedInt(rs));
        formulas.add(IsValidSignedInt(rt));

        formulas.add(isNot(IsValidSignedInt(new NodeExpr(StandardOperation.BVADD, rs, rt))));
        formulas.add(isNotEqual(rs, rt));

        return builder.build();
    }
}
