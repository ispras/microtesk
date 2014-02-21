/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * BitRotateShiftBase.java, Nov 30, 2012 6:13:29 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data.operations;

import java.util.EnumSet;
import java.util.Set;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.IBinaryOperator;
import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.model.api.type.Type;

public abstract class BitRotateShiftBase implements IBinaryOperator
{
    private final static Set<ETypeID> SUPPORTED_TYPES = EnumSet.of(
        ETypeID.INT,
        ETypeID.CARD
    );

    /**
     * Abstract method to be implemented in concrete operation classes. It is 
     * responsible for performing data transformation according to the concrete
     * action type (left shift, right rotation, etc.)
     * 
     * @param src Source data.
     * @param to Rotation/shift distance. 
     * @return Operation result.
     */

    protected abstract Data doRotateShift(Data src, int to);

    @Override
    public final Data execute(Data left, Data right)
    {
        final int distanceTo = right.getRawData().intValue();
        return doRotateShift(left, distanceTo);
    }

    @Override
    public final boolean supports(Type left, Type right)
    {
        if (!SUPPORTED_TYPES.contains(left.getTypeID()))
            return false;

        if (!SUPPORTED_TYPES.contains(right.getTypeID()))
            return false;

        // Rotate and shift operations make sense on when 
        // the second operand (that specifies rotation/shift 
        // distance) is represented by an unsigned integer
        // value (in Sim-nML, it is CARD).

        //if (right.getTypeID() != ETypeID.CARD)
        //    return false;

        // The right operand is too big to be a distance. Distance
        // will be converted to int. If it exceeds the size of int,
        // it will be truncated and we will receive incorrect results.

        if (right.getBitSize() > Integer.SIZE)
            return false;

        return true;
    }
}
