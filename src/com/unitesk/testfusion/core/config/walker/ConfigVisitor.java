/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ConfigVisitor.java,v 1.8 2008/08/19 11:54:25 vorobyev Exp $
 */

package com.unitesk.testfusion.core.config.walker;

import com.unitesk.testfusion.core.config.*;

/**
 * Interface of configuration visitor.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface ConfigVisitor
{
    /**
     * Initialization of the visitor.
     * 
     * @param <code>config</config> the configuration to be visitied.
     */
    public void onStart(Config config);
    
    /**
     * Handler of test suite configuration.
     * 
     * @param <code>test</code> the test configuration.
     */
    public void onTestSuite(TestSuiteConfig testSuite);
    
    /**
     * Handler of test configuration.
     * 
     * @param <code>test</code> the test configuration.
     */
    public void onTest(TestConfig test);
    
    /**
     * Handler of section configuration.
     * 
     * @param <code>section</code> the section configuration.
     */
    public void onSection(SectionConfig section);
    
    /**
     * Handler of processor configuration.
     * 
     * @param <code>processor</code> the processor configuration.
     */
    public void onProcessor(ProcessorConfig processor);
    
    /**
     * Handler of group configuration.
     * 
     * @param <code>group</code> the group configuration.
     */
    public void onGroup(GroupConfig group);
    
    /**
     * Handler of instruction configuration.
     * 
     * @param <code>instruction</code> the instruction configuration.
     */
    public void onInstruction(InstructionConfig instruction);

    /**
     * Handler of test situation configuration.
     * 
     * @param <code>situation</code> the test situation configuration.
     */
    public void onSituation(SituationConfig situation);
    
    /**
     * Finalization of the visitor.
     */
    public void onEnd();
}
