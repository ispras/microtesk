/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IAddressingMode.java, Nov 8, 2012 2:30:42 PM Andrei Tatarnikov
 */ 

package ru.ispras.microtesk.model.api.simnml.instruction;

import java.util.Collection;
import java.util.Map;

import ru.ispras.microtesk.model.api.metadata.IMetaAddressingMode;
import ru.ispras.microtesk.model.api.memory.Location;
import ru.ispras.microtesk.model.api.data.Data;

/**
 * The IAddressingMode interface is the base interface for addressing mode objects
 * or OR rules that group addressing mode objects. It extends the IInstructionPrimitive
 * interface allowing to access the location the mode object points to and to trace
 * attempts to access the location. Also, it includes enclosed interfaces that are base
 * interfaces for different parts of addressing mode objects.    
 * 
 * @author Andrei Tatarnikov
 */

public interface IAddressingMode extends IPrimitive
{   
    /**
     * Sends a notification to trace an attempt to load data from memory storage. 
     */

    public void onBeforeLoad();

    /**
     * Sends a notification to trace an attempt to write data to memory storage.
     */

    public void onBeforeStore();

    /**
     * Returns the location the addressing mode object points to (when initialized with
     * specific parameters).
     *
     * @return The memory location.
     */

    public Location access();
    
    /**
     * The IAddressingMode.IFactory interfaces is a base interface for
     * factories that create concrete types of addressing mode implementations
     * and initialize them with the parameter list.  
     * 
     * @author Andrei Tatarnikov
     */
    
    public interface IFactory
    {
        /**
         * Creates an addressing mode object.
         * 
         * @param args A table of arguments (key is the argument name, value is the argument value).  
         * @return The addressing mode object.
         */
        
        public IAddressingMode create(Map<String, Data> args);
    }
    
    /**
     * The IInfo interface provides information on an addressing mode object or
     * a group of addressing mode object united by an OR rule. This information
     * is needed to instantiate a concrete addressing mode object at runtime
     * depending on the selected builder.
     * 
     * @author Andrei Tatarnikov
     */
    
    public interface IInfo
    {
        /**
         * Returns the name of the mode or the name of the OR rule used for grouping
         * addressing modes.
         * 
         * @return The mode name.
         */
        
        public String getName();
        
        /**
         * Returns a table of builder for the addressing mode (or the group of
         * addressing modes) described by the current info object.
         * 
         * @return A table of addressing mode builders (key is the mode name,
         * value is the builder). 
         */

        public Map<String, IAddressingModeBuilderEx> createBuilders();

        /**
         * Returns a collection of meta data objects describing the addressing
         * mode (or the group of addressing modes) the info object refers to.
         * In the case, when there is a single addressing mode, the collection
         * will contain only one item.   
         * 
         * @return A collection of meta data objects for an addressing mode or a group of 
         * addressing modes.  
         */
        
        public Collection<IMetaAddressingMode> getMetaData();
        
        /**
         * Checks if the current addressing mode (or group of addressing modes)
         * implements (or contains) the specified addressing mode. This method 
         * is used in runtime checks to make sure that the object composition
         * in the model is valid.   
         * 
         * @param mode An addressing mode object.
         * @return true if the mode is supported or false otherwise. 
         */

        public boolean isSupported(IAddressingMode mode); 
    }
}
