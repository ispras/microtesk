/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * $Id: DataType.java, Oct 16, 2012 1:52:52 PM Andrei Tatarnikov Exp $
 */

package ru.ispras.microtesk.model.api.data_old;

import ru.ispras.microtesk.model.api.rawdata.RawData;

/**
 * The DataType interface is used to retrieve information on the type of
 * the specified data object. 
 *
 * @param <T> The actual type of the data object.
 *
 * @author Andrei Tatarnikov
 */

public interface DataType<T extends Data>
{
    /**
     * Returns a constant identifying the data type.
     * 
     * @return Type identifier.
     */

    public EDataTypeID typeId();
    
    /**
     * Returns a data object that stores a copy of the specified data array. The data
     * object type corresponds to the data type.
     * 
     * @param rawData Source binary data array.
     * @return A new data object of the corresponding type.
     */

    public T valueOf(RawData rawData);
    
    /**
     * Creates a new data object of the specified size.
     * 
     * @param bitSize Data size in bits.
     * @return A new data object.
     */
    
    public T newInstance(int bitSize);
}
