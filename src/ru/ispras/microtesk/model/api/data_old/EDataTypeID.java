/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * $Id: EDataTypeID.java, Oct 8, 2012 12:21:36 PM Andrei Tatarnikov Exp $
 */

package ru.ispras.microtesk.model.api.data_old;

/**
 * The EDataType enumeration stores the list of data types supported by the model.
 * The data types are taken from the Sim-nML language. 
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public enum EDataTypeID
{
    INT,
    CARD,
    FLOAT,
    FIX,
    RANGE,
    ENUM,
    BOOL
}
