/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * BitShiftLeft.java, Nov 30, 2012 7:12:51 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.data.operations;

import ru.ispras.microtesk.model.api.data.Data;

import static ru.ispras.microtesk.model.api.rawdata.RawDataAlgorithm.copy;

public final class BitShiftLeft extends BitRotateShiftBase
{
    /**
     * Shifts the source data to the left and returns the result. 
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
            copy(src.getRawData(), 0, result.getRawData(), to, result.getRawData().getBitSize()-to);

        return result;
    }
}
