/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TestSuiteConfig.java,v 1.11 2008/08/19 10:59:23 kamkin Exp $
 */

package com.unitesk.testfusion.core.config;

/**
 * Test suite configuration.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class TestSuiteConfig extends Config
{
    /** List of test configurations. */
    protected ConfigList<TestConfig> tests = new ConfigList<TestConfig>();
    
    /** Default constructor. */
    public TestSuiteConfig()
    {
        super();
    }
    
    /**
     * Basic constructor.
     * 
     * @param <code>name</code> the test suite name (directory where XML files
     *        with test configurations are located).
     */
    public TestSuiteConfig(String name)
    {
        super(name);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to test suite configuration object.
     */
    protected TestSuiteConfig(TestSuiteConfig r)
    {
        super(r);
        
        tests = r.tests.clone();
    }
    
    /**
     * Returns the number of tests in the configuration.
     * 
     * @return the number of tests in the configuration.
     */
    public int countTest()
    {
        return tests.size();
    }
    
    /**
     * Returns the <code>i</code>-th tests of the configuration.
     * 
     * @param  <code>i</code> the index of the test configuration.
     * 
     * @return the <code>i</code>-th test of the configuration.
     */
    public TestConfig getTest(int i)
    {
        return tests.getConfig(i);
    }
    
    /**
     * Finds the test configuration with the given name.
     * 
     * @param  <code>name</code> the name of the test configuration to be
     *         found.
     * 
     * @return the test configuration with name <code>name</code> if it
     *         exists in the configuration; <code>null</code> otherwise.
     */
    public TestConfig getTest(String name)
    {
        return tests.getConfig(name);
    }

    /**
     * Adds the test to the configuration.
     * 
     * @param <code>test</code> the test configuration to be added.
     */
    public void registerTest(TestConfig test)
    {
        tests.addConfig(test);
    }

    /**
     * Removes the test from the configuration.
     * 
     * @param <code>test</code> the test to be removed. 
     */
    public void removeTest(TestConfig test)
    {
        tests.removeConfig(test);
    }
    
    
    /**
     * Returns a copy of the test suite configuration.
     *
     * @return a copy of the test suite configuration.
     */
    public TestSuiteConfig clone()
    {
        return new TestSuiteConfig(this);
    }
}