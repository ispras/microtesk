/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ContentDependencyConfig.java,v 1.1 2008/09/03 09:38:46 kamkin Exp $
 */

package com.unitesk.testfusion.core.config;

import com.unitesk.testfusion.core.dependency.Dependency;
import com.unitesk.testfusion.core.template.DependencyIterator;
import com.unitesk.testfusion.core.template.content.ContentIterator;

/**
 * Configuration of content dependency.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ContentDependencyConfig extends DependencyConfig
{
    /** Flag that indicates if the dependencies are enabled or not. */ 
    protected boolean enabled;
    
    /**
     * Constructor.
     * 
     * @param <code>dependency</code> the content dependency.
     */
    public ContentDependencyConfig(Dependency dependency)
    {
        super(dependency, 0);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference content dependency config.
     */
    protected ContentDependencyConfig(ContentDependencyConfig r)
    {
        super(r);
        
        enabled = r.enabled;
    }

    /**
     * Checks if the iterator is enabled or not.
     * 
     * @return <code>true</code> if the iterator is enabled;
     *         <code>false</code> otherwise.
     */
    public boolean isEnabled()
    {
        return enabled;
    }
    
    /**
     * Enables/disables the iterator.
     * 
     * @param <code>enabled</code> the enabling status.
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
    
    /**
     * Creates dependency iterator according to the configuration.
     * 
     * @return the created dependency iterator.
     */
    public DependencyIterator createDependencyIterator()
    {
        return new ContentIterator(dependency, enabled);
    }
    
    /**
     * Returns a copy of the configuration.
     *
     * @return a copy of the configuration.
     */
    public ContentDependencyConfig clone()
    {
        return new ContentDependencyConfig(this);
    }
}
