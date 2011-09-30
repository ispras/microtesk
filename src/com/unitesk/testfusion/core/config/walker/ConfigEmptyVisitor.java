/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ConfigEmptyVisitor.java,v 1.4 2009/07/08 08:25:57 kamkin Exp $
 */

package com.unitesk.testfusion.core.config.walker;

import com.unitesk.testfusion.core.config.*;

/**
 * Empty visitor of MicroTESK configuration.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ConfigEmptyVisitor implements ConfigVisitor
{
    /**
     * Empty initialization of the visitor.
     * 
     * @param <code>config</config> the configuration to be visitied.
     */
    public void onStart(Config config) {}
    
    /**
     * Empty handler of test suite configuration.
     * 
     * @param <code>test</code> the test configuration.
     */
    public void onTestSuite(TestSuiteConfig testSuite) {}
    
    /**
     * Empty handler of test configuration.
     * 
     * @param <code>test</code> the test configuration.
     */
    public void onTest(TestConfig test) {}
    
    /**
     * Empty handler of section configuration.
     * 
     * @param <code>section</code> the section configuration.
     */
    public void onSection(SectionConfig section) {}
    
    /**
     * Empty handler of processor configuration.
     * 
     * @param <code>processor</code> the processor configuration.
     */
    public void onProcessor(ProcessorConfig processor) {}
    
    /**
     * Empty handler of group configuration.
     * 
     * @param <code>group</code> the group configuration.
     */
    public void onGroup(GroupConfig group) {}
    
    /**
     * Empty handler of instruction configuration.
     * 
     * @param <code>instruction</code> the instruction configuration.
     */
    public void onInstruction(InstructionConfig instruction) {}
    
    /**
     * Empty handler of test situation configuration.
     * 
     * @param <code>situation</code> the test situation configuration.
     */
    public void onSituation(SituationConfig situation) {}
    
    /**
     * Empty finalization of the visitor. 
     */
    public void onEnd() {}
}
