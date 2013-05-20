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

import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.rawdata.RawData;
import ru.ispras.microtesk.model.api.situation.ISituation;
import ru.ispras.microtesk.model.api.situation.Situation;
import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.solver.api.DataFactory;
import ru.ispras.solver.api.Environment;
import ru.ispras.solver.api.interfaces.EDataType;
import ru.ispras.solver.api.interfaces.IConstraint;
import ru.ispras.solver.api.interfaces.IDataType;
import ru.ispras.solver.api.interfaces.ISolverResult;
import ru.ispras.solver.api.interfaces.IVariable;
import ru.ispras.solver.api.internal.BitVectorWrapper;
import ru.ispras.solver.core.Constraint;
import ru.ispras.solver.core.solvers.ESolverId;
import ru.ispras.solver.core.syntax.EStandardOperation;
import ru.ispras.solver.core.syntax.Formula;
import ru.ispras.solver.core.syntax.ISyntaxElement;
import ru.ispras.solver.core.syntax.Operation;
import ru.ispras.solver.core.syntax.Syntax;
import ru.ispras.solver.core.syntax.Value;
import ru.ispras.solver.core.syntax.Variable;

public final class AddOverflowSituation extends Situation
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
        super();
    }

    @Override
    public boolean setInput(String name, Data value)
    {
        return false;
    }

    @Override
    public boolean setOutput(String name)
    {
        return false;

    }

    @Override
    public Map<String, Data> solve()
    {
        if (Environment.isUnix())
        {
            Environment.setSolverPath("tools/z3/unix/z3");
        }
        else if(Environment.isWindows())
        {
            Environment.setSolverPath("tools/z3/windows/z3.exe");
        }
        else
        {
            // TODO: add initialization code for other platforms.
            System.out.println(
                "Please set up paths for the external engine. Platform: " + System.getProperty("os.name"));
        }

        final Map<String, Data> result = new HashMap<String, Data>();

        final AddOverflowConstraintBuilder builder = new AddOverflowConstraintBuilder();

        final IConstraint constraint = builder.build();
        final ISolverResult solverResult = constraint.solve();

        if (!solverResult.isSuccessful())
        {
            System.out.println("Failed");
            
            for (String error : solverResult.getErrors())
            {
                System.out.println(error);
            }
            
            return result;
        }
        
        for (IVariable variable : solverResult.getVariables())
        {
            System.out.printf("%s: %s (%s)%n",
                variable.getName(),
                variable.getData().getValue(),
                variable.getData().getType().getType()
                );

            final BitVectorWrapper value = (BitVectorWrapper)variable.getData().getValue();

            // System.out.println(value.getValue().toString(2));
            // System.out.println(value.getSize());

            final RawData rawData = RawData.valueOf(value.getValue().toByteArray(), value.getSize());
            final Data data = new Data(rawData, new Type(ETypeID.CARD, rawData.getBitSize()));

            result.put(variable.getName(), data);
        }

        return result;
    }
}

final class AddOverflowConstraintBuilder
{
    private final int BIT_VECTOR_LENGTH = 34;

    private final IDataType BIT_VECTOR_TYPE; 

    private final Value INT_ZERO;
    private final Value INT_BASE_SIZE;
    private final Operation INT_SIGN_MASK;
    
    public AddOverflowConstraintBuilder()
    {
        BIT_VECTOR_TYPE = DataFactory.createDataType(EDataType.BIT_VECTOR, BIT_VECTOR_LENGTH);

        INT_ZERO = new Value(BIT_VECTOR_TYPE.valueOf("0", 10));
        INT_BASE_SIZE = new Value(BIT_VECTOR_TYPE.valueOf("32", 10));

        INT_SIGN_MASK = new Operation(
           EStandardOperation.BVLSHL,
           new Operation(EStandardOperation.BVNOT, INT_ZERO, null),
           INT_BASE_SIZE
           );
    }

    private Operation IsValidPos(ISyntaxElement arg)
    {
        return new Operation(
            EStandardOperation.EQ,
            new Operation(EStandardOperation.BVAND, arg, INT_SIGN_MASK),
            INT_ZERO
            );
    }

    private Operation IsValidNeg(ISyntaxElement arg)
    {
        return new Operation(
            EStandardOperation.EQ,
            new Operation(EStandardOperation.BVAND, arg, INT_SIGN_MASK),
            INT_SIGN_MASK
            );
    }

    private Operation IsValidSignedInt(ISyntaxElement arg)
    {
        return new Operation(
            EStandardOperation.OR,
            IsValidPos(arg),
            IsValidNeg(arg)
            );
    }
    
    public Constraint build()
    {
        final Constraint constraint = new Constraint();

        constraint.setName("AddOverflow");
        constraint.setDescription("AddOverflow constraint");
        constraint.setSolverId(ESolverId.Z3_TEXT);

        // Unknown variables
        final Variable rs = new Variable(constraint.addVariable("rs", BIT_VECTOR_TYPE));
        final Variable rt = new Variable(constraint.addVariable("rt", BIT_VECTOR_TYPE));

        final Syntax syntax = new Syntax();
        constraint.setSyntax(syntax);

        syntax.addFormula(new Formula(IsValidSignedInt(rs)));
        syntax.addFormula(new Formula(IsValidSignedInt(rt)));

        syntax.addFormula(
            new Formula(
                new Operation(
                    EStandardOperation.NOT,
                        IsValidSignedInt(new Operation(EStandardOperation.BVADD, rs, rt)),
                        null
                        )
                    )
                );

        syntax.addFormula(
            new Formula(
                new Operation(
                    EStandardOperation.NOT,
                    new Operation(EStandardOperation.EQ, rs, rt),
                    null
                    )
                )
            );

        return constraint;
    }
}
