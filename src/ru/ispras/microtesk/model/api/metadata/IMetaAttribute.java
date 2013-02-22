/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IMetaAttribute.java, Nov 15, 2012 1:54:56 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.metadata;

public interface IMetaAttribute
{
    /**
     * Returns the name of the attribute.
     * 
     * @return Attribute name.
     */
    
    public String getName();
    
    /**
     * Returns the type of the attribute.
     * 
     * @return Attribute type.
     */
    
    public EMetaAttributeType getType();
}
