/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IModel.java, Nov 8, 2012 2:13:34 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api;

import ru.ispras.microtesk.model.api.metadata.IMetaModel;
import ru.ispras.microtesk.model.api.monitor.IModelStateMonitor;

/**
 * The IModel interface is main interface that should be implemented by 
 * a model. It provides method for accessing the model from the outside. 
 * 
 * @author Andrei Tatarnikov
 */

public interface IModel
{
    /**
     * Returns a meta description of the model.
     * 
     * @return An meta data object (provides access to model's meta data). 
     */
    
    public IMetaModel getMetaData();
    
    /**
     * Returns a model state monitor object that allows getting information
     * on the current state of the microprocessor mode (current register values,
     * value in memory locations, etc)   
     */

    public IModelStateMonitor getModelStateMonitor(); 
    
    /**
     * Returns an object that provides access to the internal state of the model
     * (instruction set and resources). 
     * 
     * @return A simulator object (provides access to the model's state).
     */
    
    public ISimulator getSimulator();
}
