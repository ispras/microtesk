/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ISituationBuilder.java, Nov 6, 2012 3:30:49 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.instruction;

/**
 * The ISituationBuilder interface allows setting up a test situation by
 * specifying its arguments and attributes.  
 * 
 * @author Andrei Tatarnikov
 */

public interface ISituationBuilder
{
    /**
     * Sets the value of the specified argument of the test situation
     * (input or output value for a constraint). 
     * 
     * @param name Argument name.
     * @param value Textual value of the argument (will be parsed by the model library).
     */
    
    public void setArgument(String name, String value);
    
    /**
     * Sets the value of the specified attribute of the test situation (some value
     * that affects its properties but is an argument. For example, it can specify which
     * solution algorithm should be chosen or set up the probability of the the situation
     * to occur, etc). 
     * 
     * @param name Attribute name.
     * @param value Textual value of the attribute (will be parsed by the model library).
     */
    
    public void setAttribute(String name, String value);
}
