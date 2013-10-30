/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ConstraintBasedSituation.java, May 23, 2013 3:03:08 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.situation.builtin;

import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.exception.config.ConstraintSolverException;
import ru.ispras.formula.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.api.situation.Situation;
import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.solver.api.interfaces.EDataType;
import ru.ispras.solver.api.interfaces.IConstraint;
import ru.ispras.solver.api.interfaces.ISolverResult;
import ru.ispras.solver.api.interfaces.IVariable;
import ru.ispras.solver.api.internal.BitVectorWrapper;

public abstract class ConstraintBasedSituation extends Situation
{
    private final IConstraintFactory constraintFactory;

    public ConstraintBasedSituation(IInfo info, IConstraintFactory constraintFactory)
    {
        super(info);
        this.constraintFactory = constraintFactory;
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
    public final Map<String, Data> solve() throws ConfigurationException
    {
        final IConstraint     constraint = constraintFactory.create();
        final ISolverResult solverResult = constraint.solve();

        checkSolverResult(solverResult);

        final Map<String, Data> result = new HashMap<String, Data>();

        for (IVariable variable : solverResult.getVariables())
        {
            result.put(variable.getName(), variableToData(variable, ETypeID.CARD, 32));
        }

        return result;
    }
    
    private void checkSolverResult(ISolverResult solverResult) throws ConfigurationException
    {
        if (solverResult.isSuccessful())
            return;

        final StringBuilder sb = new StringBuilder();

        sb.append(String.format("Unable to solve the %s test situation. ", getInfo().getName()));
        sb.append("Contraint solver failure. Reason: ");

        for (String error : solverResult.getErrors())
            sb.append(error);

        throw new ConstraintSolverException(sb.toString());
    }

    private Data variableToData(IVariable variable, ETypeID typeID, int size)
    {
        assert null != variable;
        assert variable.getData().getType().getType() == EDataType.BIT_VECTOR;

        final BitVectorWrapper value =
            (BitVectorWrapper)variable.getData().getValue();

        final BitVector rawDataValue =
            BitVector.valueOf(value.getValue().toByteArray(), value.getSize());

        assert 0 < size && size <= rawDataValue.getBitSize();

        final BitVector rawData = (size == rawDataValue.getBitSize()) ?
            rawDataValue : BitVector.createMapping(rawDataValue, 0, size); 

        return new Data(rawData, new Type(typeID, size));
    }
}
