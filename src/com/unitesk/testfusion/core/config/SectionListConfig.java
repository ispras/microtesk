/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SectionListConfig.java,v 1.11 2008/12/13 12:04:16 kozlov Exp $
 */

package com.unitesk.testfusion.core.config;

/**
 * Abstract class that represents list of section configurations.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class SectionListConfig extends Config
{
    /** List of subsection configurations. */
    protected ConfigList<SectionConfig> sections = new ConfigList<SectionConfig>();
    
    /** Options configuration. */
    protected OptionsConfig options = new OptionsConfig();
    
    /** Default constructor. */ 
    public SectionListConfig()
    {
        super();
        
        options.setParent(this);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>deps</code> the dependency list configuration.
     */
    public SectionListConfig(DependencyListConfig deps)
    {
        super();
        
        options = new OptionsConfig(deps);
        options.setParent(this);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of the configuration.
     */
    public SectionListConfig(String name)
    {
        super(name);
        
        options.setParent(this);        
    }

    /**
     * Basic constructor.
     * 
     * @param <code>name</code> the name of the configuration.
     * 
     * @param <code>deps</code> the dependency list configuration.
     */
    public SectionListConfig(String name, DependencyListConfig deps)
    {
        super(name);
        
        options = new OptionsConfig(deps);
        options.setParent(this);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to configuration object.
     */
    protected SectionListConfig(SectionListConfig r)
    {
        super(r);
     
        sections = r.sections.clone(this);
        options = r.options.clone();
        
        options.setParent(this);
    }
    
    /**
     * Returns the index of the section in the configuration.
     * 
     * @return the index of the section in the configuration if it exists;
     *         <code>-1</code>otherwise.
     */
    public int getIndex(SectionConfig section)
    {
        return sections.getIndex(section);
    }
    
    /**
     * Returns the number of sections in the configuration.
     * 
     * @return the number of sections in the configuration.
     */
    public int countSection()
    {
        return sections.size();
    }
    
    /**
     * Returns the <code>i</code>-th section of the configuration.
     * 
     * @param  <code>i</code> the index of the section configuration.
     * 
     * @return the <code>i</code>-th section of the configuration.
     */
    public SectionConfig getSection(int i)
    {
        return sections.getConfig(i);
    }
    
    /**
     * Finds the section configuration with the given name.
     * 
     * @param  <code>name</code> the name of the section configuration to be
     *         found.
     * 
     * @return the section configuration with name <code>name</code> if it
     *         exists in the configuration; <code>null</code> otherwise.
     */
    public SectionConfig getSection(String name)
    {
        return sections.getConfig(name);
    }

    /**
     * Returns the first leaf configuration.
     * 
     * @return the first leaf configuration.
     */
    public SectionListConfig getFirstLeaf()
    {
        if(sections.isEmpty())
            { return this; }

        return getSection(0).getFirstLeaf();
    }
    
    /**
     * Adds the section to the configuration.
     * 
     * @param <code>section</code> the section configuration to be added.
     */
    public void registerSection(SectionConfig section)
    {
        section.setParent(this);
        sections.addConfig(section);
    }

    /**
     * Adds the section to the configuration at the specified position.
     * 
     * @param <code>i</code> the section position.
     * 
     * @param <code>section</code> the section configuration to be added.
     */
    public void registerSection(int i, SectionConfig section)
    {
        section.setParent(this);
        sections.addConfig(i, section);
    }

    /**
     * Removes the section from the configuration.
     * 
     * @param <code>section</code> the section to be removed. 
     */
    public void removeSection(SectionConfig section)
    {
       sections.removeConfig(section);
    }
    
    /**
     * Returns the options configuration.
     * 
     * @return the options configuration.
     */
    public OptionsConfig getOptions()
    {
        return options;
    }
    
    /**
     * Sets the options configuration.
     * 
     * @param <code>options</code> the options configuration.
     */
    public void setOptions(OptionsConfig options)
    {
        this.options = options;
        options.setParent(this);
    }
    
    /**
     * Search daughter section configuration, not necessarily immediate, by 
     * specified full name. Recursive metod.
     *  
     * @param <code>fullName</code> ful name of section configuration to be 
     *        searched.
     *        
     * @return the daughter section configuration with specified full name or
     *         null if such configuration doesn't exist.
     */
    public SectionConfig searchSectionByFullName(String fullName)
    {
        if (fullName.contains("."))
        {
            // recursive branch
            int pos = fullName.indexOf('.');
            
            String firstSectionName = fullName.substring(0, pos);
            String end = fullName.substring(pos + 1);
            
            SectionConfig firstSection = getSection(firstSectionName);
            
            if (firstSection != null)
                { return firstSection.searchSectionByFullName(end); }
            else 
                { return null; }
        }
        else 
        {
            // unrecursive branch
            return getSection(fullName);
        }
    }
}
