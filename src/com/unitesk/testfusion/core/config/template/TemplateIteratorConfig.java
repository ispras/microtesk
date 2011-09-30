/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TemplateIteratorConfig.java,v 1.8 2008/11/20 13:06:28 kozlov Exp $
 */

package com.unitesk.testfusion.core.config.template;

import com.unitesk.testfusion.core.config.Config;
import com.unitesk.testfusion.core.template.iterator.TemplateIterator;
import com.unitesk.testfusion.core.util.Utils;

/**
 * Abstract class that represents template iterator configuration.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class TemplateIteratorConfig extends Config
{
	/** Maximum value for test template size. */ 
	public static final int MAX_TEMPLATE_SIZE = Integer.MAX_VALUE;

    /**
     * Basic constructor.
     * 
     * @param <code>name</code> the name of the configuration.
     */
    public TemplateIteratorConfig(String name)
    {
        super(name);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the 
     */
    protected TemplateIteratorConfig(TemplateIteratorConfig r)
    {
        super(r);
    }
    
    /**
     * Returns a string representation of the configuration without blanks.
     * 
     * @return a string representation of the configuration without blanks.
     */
    public String toString()
    {
        return Utils.removeBlanks(getName());
    }
    
    /**
     * Creates template iterator according to the configuration.
     * 
     * @return the created template iterator.
     */
    public abstract TemplateIterator createTemplateIterator();
}
