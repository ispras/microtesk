/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * BitRotateRight.java, Nov 30, 2012 7:54:39 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data.operations;

import static ru.ispras.fortress.data.types.bitvector.BitVectorAlgorithm.copy;
import ru.ispras.microtesk.model.api.data.Data;

public class BitRotateRight extends BitRotateShiftBase
{
    /**
     * Rotates the source data to the right and returns the result. 
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
 
        copy(src.getRawData(), 0, result.getRawData(), result.getRawData().getBitSize() - realTo, realTo);
        copy(src.getRawData(), realTo, result.getRawData(), 0, result.getRawData().getBitSize()-realTo);

        return result;
    }
}

