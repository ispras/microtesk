/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SelectionConfig.java,v 1.11 2009/07/08 08:25:55 kamkin Exp $
 */

package com.unitesk.testfusion.core.config;

import com.unitesk.testfusion.core.config.walker.*;

/**
 * Abstract class for representing MicroTESK configuration or its part, which
 * has property "selected". 
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class SelectionConfig extends Config
{
    /** Selection status of the configuration. */
    protected boolean selected;

    /** Default constructor. */
    public SelectionConfig()
    {
        super();
    }
    
    /**
     * Basic constructor.
     * 
     * @param <code>name</code> the name of configuration.
     */
    public SelectionConfig(String name)
    {
        super(name);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to configuration object to be copied.
     */
    protected SelectionConfig(SelectionConfig r)
    {
        super(r);
        
        selected = r.selected;
    }
    
    /**
     * Returns the selection status of the configuration.
     * 
     * @return the selection status of the configuration.
     */
    public boolean isSelected()
    {
        return selected;
    }
    
    /**
     * Sets the selection status of the configuration.
     * 
     * @param <code>selected</code> the selection status of the configuration.
     */
    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }

    /**
     * Sets the selection status of the configuration to <code>true</code>.
     */
    public void setSelected()
    {
        setSelected(true);
    }

    /**
     * Propagates the selection status of the configuration recursively to its
     * descendants.
     * 
     * @param <code>selected</code> the selection status of the configuration.
     */
    protected void propagateDown(boolean selected)
    {
        ConfigWalker walker = new ConfigWalker(this, new ConfigSelector(selected));
        
        walker.process();
    }
    
    /**
     * Propagates the selection status of the configuration to its ancestors.
     * 
     * @param <code>selected</code> the selection status of the configuration.
     */
    protected void propagateUp(boolean selected)
    {
        if(isRoot() || !(parent instanceof SelectionConfig))
            { return; }
        
        SelectionConfig r = (SelectionConfig)parent;
        
        if(selected)
            { r.setSelected(selected); r.propagateUp(selected); return; }
        
        ConfigCounter counter = new ConfigCounter();
        ConfigWalker walker = new ConfigWalker(r, counter, ConfigWalker.VISIT_IMMEDIATE_CHILDREN);
        
        walker.process();
        
        r.setSelected(selected = counter.isSelected());
        r.propagateUp(selected);
    }
    
    /**
     * Sets the selection status of the configuration and propagates it to
     * descendants and ancestors.
     * 
     * @param <code>selected</code> the selection status of the configuration.
     */
    public void setSelectedWithPropagation(boolean selected)
    {
        setSelected(selected);
        
        propagateDown(selected);
        propagateUp(selected);
    }
    
    /**
     * Sets the selection status of the configuration to <code>true</code> and
     * propagates it to descendants and ancestors.
     */
    public void setSelectedWithPropagation()
    {
        setSelectedWithPropagation(true);
    }
}
