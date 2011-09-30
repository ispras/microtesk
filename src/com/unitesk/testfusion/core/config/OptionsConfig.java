/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: OptionsConfig.java,v 1.25 2009/07/09 14:48:05 kamkin Exp $
 */

package com.unitesk.testfusion.core.config;

import com.unitesk.testfusion.core.config.template.*;

/**
 * Options, represented by this class, belongs to leaf section list
 * configuration that is defined in class <code>SectionListConfig</code>. 
 * In contrast to settings, which are defined in class 
 * <code>SettingsConfig</code>, options have an influence on generated test
 * programs, but they do not specify in which particular directory generated
 * test programs should be saved, etc.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class OptionsConfig extends IndexConfig
{
    /** Index of the product template iterator. */
    public static final int PRODUCT_TEMPLATE_ITERATOR = 0;

    /** Index of the branch template iterator. */
    public static final int BRANCH_TEMPLATE_ITERATOR = 1;
    
    /** Index of the set template iterator. */
    public static final int SET_TEMPLATE_ITERATOR = 2;

    /** Index of the multiset template iterator. */
    public static final int MULTISET_TEMPLATE_ITERATOR = 3;

    /** Index of the sequence template iterator. */
    public static final int SEQUENCE_TEMPLATE_ITERATOR = 4;

    /** Index of the single template iterator. */
    public static final int SINGLE_TEMPLATE_ITERATOR = 5;
    
    /** Number of template iterators. */
    public static final int TEMPLATE_ITERATOR_NUMBER = 6;
    
    /** List of dependency configurations. */
    protected DependencyListConfig deps = new DependencyListConfig();
    
    /** List of cross dependency configurations. */
    protected CrossDependencyListConfig crossDeps;

    /** Array of template iterator configurations. */
    protected TemplateIteratorConfig[] templateIterators = new TemplateIteratorConfig[]
    {
        new ProductTemplateIteratorConfig(),
        new BranchTemplateIteratorConfig(),
        new SetTemplateIteratorConfig(),
        new MultisetTemplateIteratorConfig(),
        new SequenceTemplateIteratorConfig(),
        new SingleTemplateIteratorConfig()
    };
    
    /** Default constructor. */
    public OptionsConfig()
    {
        super(TEMPLATE_ITERATOR_NUMBER);
        
        deps.setParent(this);
        
        for (TemplateIteratorConfig config : templateIterators)
            { config.setParent(this); }
        
        crossDeps = new CrossDependencyListConfig(deps);
        crossDeps.setParent(this);
    }
    
    /**
     * Basic constructor.
     * 
     * @param <code>deps</code> the dependency list configuration.
     */
    public OptionsConfig(DependencyListConfig deps)
    {
        super(TEMPLATE_ITERATOR_NUMBER);
        
        this.deps = deps;
        this.deps.setParent(this);
        
        for (TemplateIteratorConfig config : templateIterators)
            { config.setParent(this); }
        
        crossDeps = new CrossDependencyListConfig(deps);
        crossDeps.setParent(this);
    }

    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to options object.
     */
    protected OptionsConfig(OptionsConfig r)
    {
        super(r);
        
        deps = r.deps.clone();
        deps.setParent(this);
        
        templateIterators = new TemplateIteratorConfig[TEMPLATE_ITERATOR_NUMBER];

        for(int i = 0; i < TEMPLATE_ITERATOR_NUMBER; i++)
        {
            templateIterators[i] = (TemplateIteratorConfig)r.templateIterators[i].clone();
            templateIterators[i].setParent(this);
        }
        
        crossDeps = r.crossDeps.clone();
        crossDeps.setParent(this);
    }
    
    /**
     * Returns the template iterator configuration by index.
     * 
     * @param  <code>index</code> the value of index.
     * @return the template iterator configuration.
     */
    public TemplateIteratorConfig getTemplateIterator(int index)
    {
        return templateIterators[index];
    }

    /**
     * Returns the template iterator configuration corresponding to the current
     * value of index.
     * 
     * @return the choosen template iterator configuration.
     */
    public TemplateIteratorConfig getTemplateIterator()
    {
        return getTemplateIterator(getIndex());
    }
    
    /**
     * Returns the dependecy list configuration.
     * 
     * @return the dependecy list configuration.
     */
    public DependencyListConfig getDependencies()
    {
        return deps;
    }
    
    /**
     * Sets the dependecy list configuration.
     * 
     * @param <code>deps</code> the dependecy list configuration.
     */
    public void setDependencies(DependencyListConfig deps)
    {
        this.deps = deps;
        
        this.deps.setParent(this);
    }
    
    public CrossDependencyListConfig getCrossDependencies()
    {
        return crossDeps;
    }
    
    /**
     * Returns the full name of the options configuration.
     * 
     * @return the full name of the configuration.
     */
    public String getFullName()
    {
        return parent.getFullName() + "." + "options";
    }
    
    /**
     * Returns a copy of the options.
     *
     * @return a copy of the options.
     */
    public OptionsConfig clone()
    {
        return new OptionsConfig(this);
    }
}
