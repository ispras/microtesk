/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * BitRotateLeft.java, Nov 30, 2012 7:48:19 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data.operations;

import ru.ispras.microtesk.model.api.data.Data;

import static ru.ispras.formula.data.types.bitvector.BitVectorAlgorithm.copy;

public final class BitRotateLeft extends BitRotateShiftBase
{
    /**
     * Rotates the source data to the left and returns the result. 
     * 
     * @param src Source data.
     * @param to Rotation distance. 
     * @return Rotation result.
     */

    @Override
    protected Data doRotateShift(Data src, int to)
    {
        if (0 == to)
            return src;

        final Data result = new Data(src.getType());
        final int realTo = to % result.getRawData().getBitSize();
        
        copy(src.getRawData(), 0, result.getRawData(), realTo, result.getRawData().getBitSize() - realTo);
        copy(src.getRawData(), src.getRawData().getBitSize()-realTo, result.getRawData(), 0, realTo);

        return result;
    }
}
