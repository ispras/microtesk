/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * AddNormalSituation.java, May 20, 2013 11:50:19 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.situation.builtin;

import ru.ispras.microtesk.model.api.situation.ISituation;
import ru.ispras.solver.api.interfaces.IConstraint;
import ru.ispras.solver.core.Constraint;
import ru.ispras.solver.core.solvers.ESolverId;
import ru.ispras.solver.core.syntax.EStandardOperation;
import ru.ispras.solver.core.syntax.Formula;
import ru.ispras.solver.core.syntax.Operation;
import ru.ispras.solver.core.syntax.Syntax;
import ru.ispras.solver.core.syntax.Variable;

public final class AddNormalSituation extends ConstraintBasedSituation
{
    private static final   String    NAME = "normal";
    private static final IFactory FACTORY = new IFactory()
    {
        @Override
        public ISituation create() { return new AddNormalSituation(); }
    };

    public static final IInfo INFO = new Info(NAME, FACTORY); 

    public AddNormalSituation()
    {
        super(
           INFO,
           new AddNormalConstraintBuilder()
        );
    }
}

final class AddNormalConstraintBuilder extends OverflowConstraintFactory
{
    public IConstraint create()
    {
        final Constraint constraint = new Constraint();

        constraint.setName("AddNormal");
        constraint.setDescription("AddNormal constraint");
        constraint.setSolverId(ESolverId.Z3_TEXT);

        // Unknown variables
        final Variable rs = new Variable(constraint.addVariable("src2", BIT_VECTOR_TYPE));
        final Variable rt = new Variable(constraint.addVariable("src3", BIT_VECTOR_TYPE));

        final Syntax syntax = new Syntax();
        constraint.setSyntax(syntax);

        syntax.addFormula(new Formula(IsValidSignedInt(rs)));
        syntax.addFormula(new Formula(IsValidSignedInt(rt)));

        syntax.addFormula(new Formula(IsValidSignedInt(new Operation(EStandardOperation.BVADD, rs, rt))));
        syntax.addFormula(new Formula(isNotEqual(rs, rt)));

        return constraint;
    }
}
