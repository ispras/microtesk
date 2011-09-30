/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TestBuilder.java,v 1.15 2010/01/13 11:47:07 vorobyev Exp $
 */

package com.unitesk.testfusion.core.config.walker;

import java.util.Collection;
import java.util.Stack;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.core.config.template.TemplateIteratorConfig;
import com.unitesk.testfusion.core.context.GeneratorContext;
import com.unitesk.testfusion.core.dependency.Dependency;
import com.unitesk.testfusion.core.dependency.DependencyType;
import com.unitesk.testfusion.core.engine.GeneratorEngine;
import com.unitesk.testfusion.core.engine.Test;
import com.unitesk.testfusion.core.engine.TestProgramArchivator;
import com.unitesk.testfusion.core.model.Instruction;
import com.unitesk.testfusion.core.model.Processor;
import com.unitesk.testfusion.core.situation.Situation;
import com.unitesk.testfusion.core.situation.SituationEnumerator;
import com.unitesk.testfusion.core.template.DependencyIterator;
import com.unitesk.testfusion.core.template.Section;
import com.unitesk.testfusion.core.template.Template;
import com.unitesk.testfusion.core.template.register.RegisterIterator;
import com.unitesk.testfusion.core.util.Utils;

/**
 * Test builder traverses test configuration and initializes test program
 * generator according to configuration parameters.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class TestBuilder
{
    /**
     * Visitor that implements test builder functionality.
     *   
     * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
     */
    protected static class TestBuilderConfigVisitor extends ConfigEmptyVisitor
    {
        /** Test name. */
        protected String testName;
        
        /** Test program generator. */
        protected GeneratorEngine generator;
        
        /** Stack of template sections. */
        protected Stack<Section> sections;
        
        /** Processing template section. */
        protected Section currentSection;
        
        /** Processing instruction. */
        protected InstructionConfig currentInstruction;
        
        /**
         * Initialization of the visitor.
         * 
         * @param <code>config</config> the configuration to be visitied.
         */
        public void onStart(Config config)
        {
            System.out.println("Building is started");
        }
        
        /**
         * Finalization of the visitor.
         */
        public void onEnd()
        {
            System.out.println("Building is finished\n");
            System.out.flush();
        }

        /**
         * Handler of test configuration.
         * 
         * @param <code>test</code> the test configuration.
         */
        public void onTest(TestConfig test)
        {
            if((generator = test.getGenerator()) == null)
                { return; }
            
            currentSection = new Template();

            Processor processor = generator.getTargetProcessor();
            GeneratorContext context = generator.getTargetContext();
            
            currentSection.setProcessor(processor);
            currentSection.setContext(context);
            
            generator.setTemplate(currentSection);
            
            sections = new Stack<Section>();
            sections.push(currentSection);
            
            System.out.println("Self-checking test generation: " + test.isSelfCheck());
            generator.setSelfCheck(test.isSelfCheck());
            
            System.out.print("Generating test name: ");
            testName = test.generateTestName();

            System.out.println(testName);
            generator.setTestName(testName);
            
            SettingsConfig settings = test.getSettings();
            
            System.out.println("Output directory: " + settings.getOutputDirectory());
            generator.setOutputDirectory(settings.getOutputDirectory());
            
            generator.setTestSize(test.getTestSize());
            
            Test targetTest = generator.getTargetTest();
            String infoFileName = targetTest.getOutputDirectory() + targetTest.getFilePrefix() + ".info";
            test.generateConfigurationInfo(infoFileName);
            System.out.println("Generating info file: " + infoFileName);
            
            System.out.println("Archivation: " + settings.isCompress());
            
            generator.setArchivator(settings.isCompress() ? TestProgramArchivator.ZIP : TestProgramArchivator.NONE);
            generator.setCompressionMethod(settings.getCompressionMethod());
        }
        
        /**
         * Handler of section configuration.
         * 
         * @param <code>section</code> the section configuration.
         */
        public void onSection(SectionConfig section)
        {
            int i, size;

            if(generator == null)
                { return; }

            OptionsConfig optionsConfig = section.getOptions();
            TemplateIteratorConfig templateConfig = optionsConfig.getTemplateIterator();
            
            currentSection = section.isLeaf() ?
                new Section(section.getName(), templateConfig.createTemplateIterator()) :
                new Template(section.getName());

            Processor processor = generator.getTargetProcessor();
            GeneratorContext context = generator.getTargetContext();
            
            currentSection.setProcessor(processor);
            currentSection.setContext(context);
                
            // Register inner section dependencies.
            if(section.isLeaf())
            {
                DependencyListConfig deps = optionsConfig.getDependencies(); 
                
                size = deps.countDependency();
                
                for(i = 0; i < size; i++)
                {
                    DependencyConfig dependencyConfig = deps.getDependency(i);
                    Dependency dependency = dependencyConfig.getDependency();
                    DependencyType dependencyType = dependency.getDependencyType();
                    DependencyIterator dependencyIterator = dependencyConfig.createDependencyIterator(); 
                    
                    System.out.println("Registering dependency: " + dependencyType.getName());

                    if(dependencyIterator.isRegisterDependency())
                    {
                        RegisterIterator registerIterator = (RegisterIterator)dependencyIterator;
                        
                        System.out.println("Read  After Read  (RAR): " + registerIterator.isUseUse());
                        System.out.println("Read  After Write (RAW): " + registerIterator.isDefineUse());
                        System.out.println("Write After Read  (WAR): " + registerIterator.isUseDefine());
                        System.out.println("Write After Write (WAW): " + registerIterator.isDefineDefine());
                    }
                    
                    currentSection.registerDependencyIterator(dependencyIterator);
                }
            }

            Template template = (Template)sections.peek();
            template.registerSection(currentSection);
            
            if(!section.isLeaf())
            {
                // If section is not leaf push current section into the stack.
                sections.push(currentSection);
            }
            else
            {
                SectionListConfig parent = (SectionListConfig)section.getParent();

                // If section is last leaf section of the parent, pop the top of the stack.
                if(parent.getSection(parent.countSection() - 1) == section)
                    { sections.pop(); }
            }
        }
        
        /**
         * Handler of instruction configuration.
         * 
         * @param <code>instruction</code> the instruction configuration.
         */
        public void onInstruction(InstructionConfig instruction)
        {
            Instruction selectedInstruction = instruction.getInstruction();
            
            selectedInstruction.setSituation(null);
            
            this.currentInstruction = instruction;
        }
        
        /**
         * Handler of test situation configuration.
         * 
         * @param <code>situation</code> the test situation configuration.
         */
        public void onSituation(SituationConfig situation)
        {
            // Generator is not configured
            if(generator == null)
                { return; }
            
            if(situation.isSelected())
            {
                String equivalenceClass = currentInstruction.getEquivalenceClass();
                
                Instruction selectedInstruction = currentInstruction.getInstruction();
                Situation selectedSituation = situation.getSituation();
                
                String className;
                
                // Generate name of the equivalence class
                if(Utils.isNullOrEmpty(equivalenceClass))
                    { className = selectedInstruction.getName(); }
                else
                    { className = equivalenceClass; }
                
                Collection<Integer> positions = currentInstruction.getPositions();
                
                // Compose the test situation
                Situation currentSituation = selectedInstruction.getSituation();
                
                if(currentSituation == null)
                {
                    currentSituation = selectedSituation;
                }
                else
                {
                    if(currentSituation instanceof SituationEnumerator)
                    {
                        SituationEnumerator situationEnumerator = (SituationEnumerator)currentSituation;
                        
                        situationEnumerator.registerSituation(selectedSituation);
                    }
                    else
                    {
                        SituationEnumerator situationEnumerator = new SituationEnumerator();
                        
                        situationEnumerator.registerSituation(currentSituation);
                        situationEnumerator.registerSituation(selectedSituation);
                        
                        currentSituation = situationEnumerator;
                    }
                }
                    
                // Register the instruction
                if(Utils.isNullOrEmpty(positions))
                {
                    System.out.println("Registering instruction: " + selectedInstruction.getName() + "\tclass: " + className + "");
                    currentSection.registerInstruction(className, selectedInstruction, currentSituation);
                }
                else
                {
                    System.out.println("Registering instruction: " + selectedInstruction.getName() + "\tclass: " + className + "\tpositions: " + positions);
                    currentSection.registerInstruction(positions, className, selectedInstruction, currentSituation);
                }
            }
        }
    }
    
    /**
     * Initializes test program generator according to the test configuration.
     * 
     * @param <code>test</code> the test configuration.
     */
    public static void build(TestConfig test)
    {
        ConfigWalker walker = new ConfigWalker(test, new TestBuilderConfigVisitor());
        
        walker.process();
    }
    
    /**
     * Runs test program generation.
     * 
     * @param <code>test</code> the test configuration.
     */
    public static void run(TestConfig test)
    {
        GeneratorEngine generator = test.getGenerator();
        
        if(generator != null)
            { generator.generate(); }
    }
    
    /**
     * Initializes test program generator according to the test configuration
     * and runs test program generation.
     * 
     * @param <code>test</code> the test configuration.
     */
    public static void buildAndRun(TestConfig test)
    {
        build(test);
        run(test);
    }
}
