/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * $Id: ITemplateBuilder.java, Jul 10, 2012 11:58:57 AM Andrei Tatarnikov Exp $
 */

package ru.ispras.microtesk.translator.generation;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

/**
 * The ITemplateBuilder interface is a base interface for all objects
 * that are responsible for initialization of class file templates. 
 * 
 * @author Andrei Tatarnikov
 */

public interface ITemplateBuilder
{
    /**
     * Performs initialization of the template of the target class based
     * on templates described in the corresponding template group and
     * information extracted from the intermediate representation of
     * the target classes.     
     * 
     * @param group A template group that stores templates required to
     * build generated representation.
     *    
     * @return Fully initialized template object. 
     */

    public ST build(STGroup group);
}
