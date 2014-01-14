/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * BitShiftRight.java, Nov 30, 2012 7:37:32 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data.operations;

import ru.ispras.microtesk.model.api.data.Data;

import static ru.ispras.fortress.data.types.bitvector.BitVectorAlgorithm.copy;

public final class BitShiftRight extends BitRotateShiftBase
{
    /**
     * Shifts the source data to the right and returns the result. 
     * 
     * @param src Source data.
     * @param to Shift distance. 
     * @return Shift result.
     */
    
    @Override
    protected Data doRotateShift(Data src, int to)
    {
        if (0 == to)
            return src;
 
        final Data result = new Data(src.getType());

        if (to < src.getRawData().getBitSize())
            copy(src.getRawData(), to, result.getRawData(), 0, result.getRawData().getBitSize()-to);

        return result;
    }
}
