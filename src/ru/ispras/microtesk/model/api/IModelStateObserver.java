/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IModelStateObserver.java, Nov 7, 2012 3:22:58 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api;

import ru.ispras.microtesk.model.api.ILocationAccessor;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;

public interface IModelStateObserver
{
    public ILocationAccessor accessLocation(String name) throws ConfigurationException;

    public ILocationAccessor accessLocation(String name, int index) throws ConfigurationException;

    public int getControlTransferStatus();
}
