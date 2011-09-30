/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ConfigWriter.java,v 1.34 2009/06/08 12:59:29 kamkin Exp $
 */

package com.unitesk.testfusion.gui.parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.AbstractMap.SimpleEntry;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.core.config.register.RegisterIteratorConfig;
import com.unitesk.testfusion.core.config.template.*;
import com.unitesk.testfusion.core.util.Utils;

import com.unitesk.testfusion.gui.GUISettings;
import com.unitesk.testfusion.gui.GUI;

import static com.unitesk.testfusion.gui.parser.Constants.*;

/**
 * Class for write test configuration in XML document.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class ConfigWriter
{
    /** This value indicates that a file has been successfully written. */  
	public static int FILE_HAS_BEEN_WRITTEN     = 0;
    
    /** This value indicates that a file wasn't written. */
	public static int FILE_HAS_NOT_BEEN_WRITTEN = 1;
    
    /** The default value of shift size beetwen two taken up elements. */
    public static final int DEFAULT_SHIFT_SIZE = 2;
    
    /** GUI frame. */
    protected GUI frame;
	
	/** Value of current shift of a paragraph in XML document. */  
	protected int shift;
    
    /** File writer. */
	protected FileWriter writer;	
    
    /**
     * Base constructor.
     * 
     * @param <code>frame</code> the GUI frame.
     */
    public ConfigWriter(GUI frame)
    {
        this.frame = frame;
    }
    
    /**
     * Writes test's configuration into specified file in XML format.
     * If file writing failed, than function shows error message dialog.
     * 
     * @param <code>file</code> the file for writing configuration.
     * 
     * @return an integer that indicates whether the file has been written or not.
     * 
     * @see #FILE_HAS_BEEN_WRITTEN FILE_HAS_BEEN_WRITTEN
     * 
     * @see #FILE_HAS_NOT_BEEN_WRITTEN FILE_HAS_NOT_BEEN_WRITTEN
     */
    public int writeConfig(File file)
    {
        try
        {
            writer = new FileWriter(file);
            
            writeShiftedString(HEADER + "\n");
            
            TestConfig config = frame.getConfig();
            GUISettings guiSettings = frame.getSettings();
            String processorName = config.getProcessor().getName();
            
            ArrayList<SimpleEntry<String,String>> atts =
                new ArrayList<SimpleEntry<String,String>>();
            
            SimpleEntry<String,String> firstAtt = 
                new SimpleEntry<String,String>(
                        TEST_ELEM_PROCESSOR_ATTR, processorName); 
            
            SimpleEntry<String,String> secondAtt = 
                new SimpleEntry<String,String>(
                        TEST_ELEM_SIZE_ATTR,
                        config.getTestSize() + "");
            
            SimpleEntry<String,String> thirdAtt = 
                new SimpleEntry<String,String>(
                        TEST_ELEM_SELF_CHECK_ATTR,
                        config.isSelfCheck() + "");
            
            
            atts.add(firstAtt);
            atts.add(secondAtt);
            atts.add(thirdAtt);
            
            writeStartTag(TEST_ELEM, atts, false);
            
            shift += DEFAULT_SHIFT_SIZE;
            
            // write element's content
            for (int i=0; i < config.countSection(); i++)
            	writeSection(config.getSection(i));
            
            writeTestOptions(config.getOptions()); 
            
            writeSettings(config.getSettings(), guiSettings);
            
            shift -= DEFAULT_SHIFT_SIZE;
            
            writeEndTag(TEST_ELEM, false);
            
            // set test configuration name like file name
            config.setName(
                    Utils.removeFileExtention(file.getName()));
            
            writer.flush();
            writer.close();
            return FILE_HAS_BEEN_WRITTEN;
        }
        catch(IOException e)
        {
        	frame.showWarningMessage("Failure writting configation into " +
                    file.getName(), "Error");
            return FILE_HAS_NOT_BEEN_WRITTEN;
        }
    }
    
    /**
     * Writes XML element corresponding to specified section's
     * configuration into file.
     * 
     * @param <code>config</code> the section configuration.
     */
    protected void writeSection(SectionConfig config)
    {
        ArrayList<SimpleEntry<String,String>> atts =
            new ArrayList<SimpleEntry<String,String>>();
        
        SimpleEntry<String,String> firstAtt = 
            new SimpleEntry<String,String>(
                    NAME, config.getName()); 
        
        atts.add(firstAtt);
        
        writeStartTag(SECTION_ELEM, atts, false);
            
        shift += DEFAULT_SHIFT_SIZE;
        
        // write element's content
        if (config.isLeaf())
        {
            atts.clear();
            
            SimpleEntry<String,String> procFirstAtt = 
                new SimpleEntry<String,String>(
                        SELECTED, 
                        config.getProcessor().isSelected() + "");
            
            atts.add(procFirstAtt);
            
            writeStartTag(PROCESSOR_ELEM, atts, false);
            
            shift += DEFAULT_SHIFT_SIZE;
            
            // writes element's content
            for(int i = 0; i < config.getProcessor().countGroup(); i++)
                writeGroup(config.getProcessor().getGroup(i));
            
            shift -= DEFAULT_SHIFT_SIZE;
            
            writeEndTag(PROCESSOR_ELEM, false);
            
            writeLeafOptions(config.getOptions());
        }
        else
        {
            for(int i = 0; i < config.countSection(); i++)
                writeSection(config.getSection(i));
            
            writeNonLeafOptions(config.getOptions());
        }
                
        shift -=DEFAULT_SHIFT_SIZE;
        
        writeEndTag(SECTION_ELEM, false);
    }
    
    /**
     * Writes XML element corresponding to specified group's
     * configuration into file.
     * 
     * @param <code>config</code> the group's configuration.
     */
    protected void writeGroup(GroupConfig config)
    {
        ArrayList<SimpleEntry<String,String>> atts =
            new ArrayList<SimpleEntry<String,String>>();

        SimpleEntry<String,String> firstAtt = 
            new SimpleEntry<String,String>(
                    NAME, config.getName());
        
        SimpleEntry<String,String> secondAtt = 
            new SimpleEntry<String,String>(
                    SELECTED, config.isSelected() + "");
        
        atts.add(firstAtt);
        atts.add(secondAtt);
        
        writeStartTag(GROUP_ELEM, atts, false);
        
        shift += DEFAULT_SHIFT_SIZE;

        for(int i = 0; i < config.countGroup(); i++)                
            { writeGroup(config.getGroup(i)); }

        for(int i = 0; i < config.countInstruction(); i++)
            { writeInstruction(config.getInstruction(i)); }
        
        shift -= DEFAULT_SHIFT_SIZE;

        writeEndTag(GROUP_ELEM, false);
    }
    
    /**
     * Writes XML element corresponding to specified instruction's
     * configuration into file.
     * 
     * @param <code>config</code> the instruction's configuration.
     */
    protected void writeInstruction(InstructionConfig config)
    {
        String eqClass = config.getEquivalenceClass();
        HashSet<Integer> positions = config.getPositions();
        
        ArrayList<SimpleEntry<String,String>> atts =
            new ArrayList<SimpleEntry<String,String>>();

        SimpleEntry<String,String> firstAtt = 
            new SimpleEntry<String,String>(
                    NAME, config.getName());
        
        SimpleEntry<String,String> secondAtt = 
            new SimpleEntry<String,String>(
                    INSTRUCTION_ELEM_EQ_CLASS_ATTR,
                    eqClass);
        
        SimpleEntry<String,String> thirdAtt = 
            new SimpleEntry<String,String>(
                    SELECTED, config.isSelected() + "");

        SimpleEntry<String,String> fourthAtt = 
            new SimpleEntry<String,String>(
                    POSITIONS, positions + "");
        
        atts.add(firstAtt);
        if (!Utils.isNullOrEmpty(eqClass)) 
            { atts.add(secondAtt); }
        atts.add(thirdAtt);
        if(!Utils.isNullOrEmpty(positions))
        	{ atts.add(fourthAtt); }
        	
        writeStartTag(INSTRUCTION_ELEM, atts, false);
                    
        shift += DEFAULT_SHIFT_SIZE;
        
        // write element's content
        for(int i = 0; i < config.countSituation(); i++)            
            writeSituation(config.getSituation(i));
        
        shift -= DEFAULT_SHIFT_SIZE;
        
        writeEndTag(INSTRUCTION_ELEM, false);
        
    }
    
    /**
     * Writes XML element corresponding to specified situation's
     * configuration into file.
     * 
     * @param <code>config</code> the situation's configuration.
     */
    protected void writeSituation(SituationConfig config)
    {
        ArrayList<SimpleEntry<String,String>> atts =
            new ArrayList<SimpleEntry<String,String>>();

        SimpleEntry<String,String> firstAtt = 
            new SimpleEntry<String,String>(
                    NAME, config.getName());
        
        SimpleEntry<String,String> secondAtt = 
            new SimpleEntry<String,String>(
                    SELECTED, config.isSelected() + "");
        
        atts.add(firstAtt);
        atts.add(secondAtt);
        
        writeEmptyContentElement(SITUATION_ELEM, atts); 
    }
    
    /**
     * Writes XML element corresponding to specified options
     * configuration from test configuration into file.
     * 
     * @param <code>config</code> the options' configuration.
     */
    protected void writeTestOptions(OptionsConfig config)
    {
        ArrayList<SimpleEntry<String,String>> atts =
            new ArrayList<SimpleEntry<String,String>>();
        
        writeStartTag(OPTIONS_ELEM, atts, false);
        
        shift += DEFAULT_SHIFT_SIZE;
        
        // write options content
        writeDependencies(config.getDependencies());
        
        shift -= DEFAULT_SHIFT_SIZE;
        writeEndTag(OPTIONS_ELEM, false);
    }
    
    /**
     * Writes XML element corresponding to specified options
     * configuration from leaf section configuration into file.
     * 
     * @param <code>config</code> the options' configuration.
     */
    protected void writeLeafOptions(OptionsConfig config)
    {
        ArrayList<SimpleEntry<String,String>> atts =
            new ArrayList<SimpleEntry<String,String>>();
        
    	writeStartTag(OPTIONS_ELEM, atts, false);
        
    	shift += DEFAULT_SHIFT_SIZE;
    	
        // write options content
        
    	writeTemplateIterator(config.getTemplateIterator());
        
        writeDependencies(config.getDependencies());
        
        writeCrossDependencies(config.getCrossDependencies());
        
    	shift -= DEFAULT_SHIFT_SIZE;
        writeEndTag(OPTIONS_ELEM, false);
    }
    
    /**
     * Writes XML element corresponding to specified options
     * configuration from non-leaf section configuration into file.
     * 
     * @param <code>config</code> the options' configuration.
     */
    protected void writeNonLeafOptions(OptionsConfig config)
    {
        ArrayList<SimpleEntry<String,String>> atts =
            new ArrayList<SimpleEntry<String,String>>();
        
        writeStartTag(OPTIONS_ELEM, atts, false);
        
        shift += DEFAULT_SHIFT_SIZE;
        
        // write options content
        
        writeDependencies(config.getDependencies());
        
        writeCrossDependencies(config.getCrossDependencies());
        
        shift -= DEFAULT_SHIFT_SIZE;
        writeEndTag(OPTIONS_ELEM, false);
    }
    
    /**
     * Writes XML element corresponding to specified dependency list
     * configuration into file.
     * 
     * @param <code>deps</code> the dependency list configuration.
     */
    protected void writeDependencies(DependencyListConfig deps)
    {
        ArrayList<SimpleEntry<String,String>> atts =
            new ArrayList<SimpleEntry<String,String>>();
        
        writeStartTag(ALL_DEPEND_ELEM, atts, false);
        shift += DEFAULT_SHIFT_SIZE;
        
        // write dependencies' content
        atts.clear();
        writeStartTag(ALL_REG_DEPEND_ELEM, atts, false);
        shift += DEFAULT_SHIFT_SIZE;
        
        // write register dependencies' content
        for (int i=0; i < deps.countDependency(); i++)
            if (deps.getDependency(i).isRegisterDependency())
                writeRegisterDependency(
                        (RegisterDependencyConfig)deps.getDependency(i));
        
        shift -= DEFAULT_SHIFT_SIZE;
        writeEndTag(ALL_REG_DEPEND_ELEM, false);
        
        atts.clear();
        writeStartTag(ALL_CUSTOM_DEPEND_ELEM, atts, false);
        shift += DEFAULT_SHIFT_SIZE;
        
        // write custom dependencies' content 
        for (int i=0; i < deps.countDependency(); i++)
            if (!deps.getDependency(i).isRegisterDependency())
                writeCustomDependency(
                        (ContentDependencyConfig)deps.getDependency(i));
        
        shift -= DEFAULT_SHIFT_SIZE;
        writeEndTag(ALL_CUSTOM_DEPEND_ELEM, false);
        
        shift -= DEFAULT_SHIFT_SIZE;
        writeEndTag(ALL_DEPEND_ELEM, false);
    }
    
    /**
     * Writes XML element corresponding to specified cross dependency
     * list configuration into file.
     * 
     * @param <code>cross</code> the cross dependency list configuration.
     */
    protected void writeCrossDependencies(CrossDependencyListConfig cross)
    {
        ArrayList<SimpleEntry<String,String>> atts =
            new ArrayList<SimpleEntry<String,String>>();
        
        writeStartTag(ALL_CROSS_DEPEND_ELEM, atts, false);
        shift += DEFAULT_SHIFT_SIZE;
        
        // write cross-dependencies content
        for (int i = 0; i < cross.countCrossDependencies(); i++)
            { writeCrossDependency(cross.getCrossDependency(i)); }
        
        shift -= DEFAULT_SHIFT_SIZE;
        writeEndTag(ALL_CROSS_DEPEND_ELEM, false);
    }
    
    /**
     * Writes XML element corresponding to specified cross dependency
     * configuration into file.
     * 
     * @param <code>cross</code> the cross dependency configuration.
     */
    protected void writeCrossDependency(CrossDependencyConfig cross)
    {
        ArrayList<SimpleEntry<String,String>> atts =
            new ArrayList<SimpleEntry<String,String>>();
        
        SimpleEntry<String,String> firstSectionName = 
            new SimpleEntry<String,String>(
                    DEPEND_ON_SECTION_CROSS_DEPEND_PARAM,
                    cross.getDependsOnSection().getFullName());
        atts.add(firstSectionName);
        
        writeStartTag(ONE_CROSS_DEPEND_ELEM, atts, false);
        shift += DEFAULT_SHIFT_SIZE;
        
        // write cross-dependencies content
        writeDependencies(cross.getDependencies());
        
        shift -= DEFAULT_SHIFT_SIZE;
        writeEndTag(ONE_CROSS_DEPEND_ELEM, false);
    }
    
    /**
     * Writes XML element corresponding to specified template iterator's
     * configuration into file.
     * 
     * @param <code>iterConfig</code> the template iterator's configuration.
     */
    protected void writeTemplateIterator(TemplateIteratorConfig iterConfig)
    {
        ArrayList<SimpleEntry<String,String>> params =
            new ArrayList<SimpleEntry<String,String>>();
        
    	if (iterConfig instanceof ProductTemplateIteratorConfig)
        {
            ProductTemplateIteratorConfig productIter = 
                (ProductTemplateIteratorConfig)iterConfig; 
            
            SimpleEntry<String,String> firstParam = 
                new SimpleEntry<String,String>(
                        PRODUCT_ITER_SIZE,
                        productIter.getTemplateSize() + ""); 
            
            params.add(firstParam);
        }
    	else if (iterConfig instanceof SetTemplateIteratorConfig)
    	{
    		SetTemplateIteratorConfig setIter = 
                (SetTemplateIteratorConfig)iterConfig;
            
            SimpleEntry<String,String> firstParam = 
                new SimpleEntry<String,String>(
                        SET_ITER_MIN_SIZE,
                        setIter.getMinTemplateSize() + "");
            
            SimpleEntry<String,String> secondParam = 
                new SimpleEntry<String,String>(
                        SET_ITER_MAX_SIZE,
                        setIter.getMaxTemplateSize() + "");
            
            params.add(firstParam);
            params.add(secondParam);
    	}
    	else if (iterConfig instanceof MultisetTemplateIteratorConfig)
    	{
    		MultisetTemplateIteratorConfig multisetIter = 
                (MultisetTemplateIteratorConfig)iterConfig;
            
            SimpleEntry<String,String> firstParam = 
                new SimpleEntry<String,String>(
                        MULTISET_ITER_MIN_SIZE,
                        multisetIter.getMinTemplateSize() + "");
            
            SimpleEntry<String,String> secondParam = 
                new SimpleEntry<String,String>(
                        MULTISET_ITER_MAX_SIZE,
                        multisetIter.getMaxTemplateSize() + "");
            
            SimpleEntry<String,String> thirdParam = 
                new SimpleEntry<String,String>(
                        MULTISET_ITER_MAX_REPETITION_SIZE,
                        multisetIter.getMaxRepetition() + "");
            
            params.add(firstParam);
            params.add(secondParam);
            params.add(thirdParam);
    	}
    	else if (iterConfig instanceof SequenceTemplateIteratorConfig)
    	{
            SequenceTemplateIteratorConfig seqIter = 
                (SequenceTemplateIteratorConfig)iterConfig; 
            
            SimpleEntry<String,String> firstParam = 
                new SimpleEntry<String,String>(
                        SEQUENCE_ITER_SIZE,
                        seqIter.getTemplateSize() + ""); 
            
            params.add(firstParam);
    	}
    	else if (iterConfig instanceof SingleTemplateIteratorConfig)
    	{
            SingleTemplateIteratorConfig singleIter = 
                (SingleTemplateIteratorConfig)iterConfig; 
            
            SimpleEntry<String,String> firstParam = 
                new SimpleEntry<String,String>(
                        SINGLE_ITER_SIZE,
                        singleIter.getTemplateSize() + ""); 
            
            params.add(firstParam);
    	}
        
        writeElementWithParameters(TEMPLATE_ITER_ELEM, 
                iterConfig.toString(), params);
    }
    
    /**
     * Writes XML element corresponding to specified register dependency's
     * configuration into file.
     * 
     * @param <code>regDep</code> the register dependency's configuration.
     */
    protected void writeRegisterDependency(RegisterDependencyConfig regDep)
    {
        ArrayList<SimpleEntry<String,String>> atts =
            new ArrayList<SimpleEntry<String,String>>();

        SimpleEntry<String,String> firstAtt = 
            new SimpleEntry<String,String>(
                    NAME, regDep.getName());
        
        atts.add(firstAtt);
        
        writeStartTag(ONE_REG_DEPEND_ELEM, atts, false);
        
        shift += DEFAULT_SHIFT_SIZE;
        
        //write elements body
        writeRegisterDependencyIterator(regDep.getRegisterIterator());
        
        shift -= DEFAULT_SHIFT_SIZE;
        
        writeEndTag(ONE_REG_DEPEND_ELEM, false);
    }
    
    /**
     * Writes XML element corresponding to specified register
     * iterator's configuration into file.
     * 
     * @param <code>iter</code> the register iterator's configuration.
     */
    protected void writeRegisterDependencyIterator(RegisterIteratorConfig iter)
    {
        ArrayList<SimpleEntry<String,String>> params =
            new ArrayList<SimpleEntry<String,String>>();

        SimpleEntry<String,String> firstParam = 
            new SimpleEntry<String,String>(
                    REG_ITERATOR_ELEM_USE_USE_PARAM,
                    iter.isUseUse() + "");
        
        SimpleEntry<String,String> secondParam = 
            new SimpleEntry<String,String>(
                    REG_ITERATOR_ELEM_DEF_USE_PARAM,
                    iter.isDefineUse() + "");

        SimpleEntry<String,String> thirdParam = 
            new SimpleEntry<String,String>(
                    REG_ITERATOR_ELEM_USE_DEF_PARAM,
                    iter.isUseDefine() + "");
        
        SimpleEntry<String,String> fourthParam = 
            new SimpleEntry<String,String>(
                    REG_ITERATOR_ELEM_DEF_DEF_PARAM,
                    iter.isDefineDefine() + "");

        int minNumber = iter.getMinNumber();
        int maxNumber = iter.getMaxNumber();
        
        String min = (minNumber == Integer.MIN_VALUE) ? 
                "" : minNumber + ""; 
        
        String max = (maxNumber == Integer.MAX_VALUE) ? 
                "" : maxNumber + "";

        SimpleEntry<String,String> fifthParam = 
            new SimpleEntry<String,String>(
                    REG_ITERATOR_ELEM_MIN_NUMBER_PARAM, min);
        
        SimpleEntry<String,String> sixthParam = 
            new SimpleEntry<String,String>(
                    REG_ITERATOR_ELEM_MAX_NUMBER_PARAM, max);
        
        params.add(firstParam);
        params.add(secondParam);
        params.add(thirdParam);
        params.add(fourthParam);
        params.add(fifthParam);
        params.add(sixthParam);
        
        writeElementWithParameters(REG_ITERATOR_ELEM, iter.toString(), params);
    }
    
    /**
     * Writes XML element corresponding to specified custom dependency's
     * configuration into file.
     * 
     * @param <code>customDep</code> the custom dependency's configuration.
     */
    protected void writeCustomDependency(ContentDependencyConfig customDep)
    {
        ArrayList<SimpleEntry<String,String>> atts =
            new ArrayList<SimpleEntry<String,String>>();

        SimpleEntry<String,String> firstAtt = 
            new SimpleEntry<String,String>(
                    NAME, customDep.getName());
        
        SimpleEntry<String,String> secondAtt = 
            new SimpleEntry<String,String>(
                    SELECTED, customDep.isEnabled() + "");
        
        atts.add(firstAtt);
        atts.add(secondAtt);
        
        writeEmptyContentElement(ONE_CUSTOM_DEPEND_ELEM, atts);
    }
    
    /**
     * Writes XML element corresponding to specified settings'
     * configuration and GUI's settings into file.
     * 
     * @param <code>config</code> the settings' configuration.
     * 
     * @param <code>guiSettings</code> the GUI's settings.
     */
    protected void writeSettings(SettingsConfig config, GUISettings guiSettings)
    {
        ArrayList<SimpleEntry<String,String>> atts =
            new ArrayList<SimpleEntry<String,String>>();
        
    	writeStartTag(SETTINGS_ELEM, atts, false);
        
    	shift +=DEFAULT_SHIFT_SIZE;
        
        // write element's content
        
        ArrayList<SimpleEntry<String,String>> params =
            new ArrayList<SimpleEntry<String,String>>();
        
        int strategy = config.getTestNameStrategy();
        String strategyName = config.toString();
        
        if (strategy == SettingsConfig.SPECIFIED_NAME)
        {
            SimpleEntry<String,String> firstParam = 
                new SimpleEntry<String,String>(
                        TEST_NAMING_STRATEGY_ELEM_TEST_NAME_PARAM, 
                        config.getTestName());
            
            params.add(firstParam);
        }
        
        
        String outputDir = config.getOutputDirectory();
        if (!Utils.isNullOrEmpty(outputDir))
        {
            SimpleEntry<String,String> secondParam = 
                new SimpleEntry<String,String>(
                        TEST_NAMING_STRATEGY_ELEM_OUTPUT_DIR_PARAM, 
                        outputDir);
            
            params.add(secondParam);
        }
        
        if (strategy != SettingsConfig.SPECIFIED_NAME)
        {
            SimpleEntry<String,String> thirdParam = 
                new SimpleEntry<String,String>(
                        TEST_NAMING_STRATEGY_ELEM_FULL_NAME_PARAM,
                        config.isFullName() + "");
            
            params.add(thirdParam);
        }
        
        writeElementWithParameters(TEST_NAMING_STRATEGY_ELEM,
                strategyName, params);
        
    	shift -=DEFAULT_SHIFT_SIZE;
        
        writeEndTag(SETTINGS_ELEM, false);
    }
    
    //*************************************************************************
    // Supporting methods
    //*************************************************************************

    /**
     * Writes start tag of XML element to file.
     * 
     * @param <code>elemName</code> the element name.
     * 
     * @param <code>atts</code> the array with element's attributes. Array 
     *        contains pairs with attribute's name and attribute's value.
     * 
     * @param <code>isPlain</code> the boolean value, wich shows is element has
     *        plain form (start tag, content, end tag place in one string)
     *        or not.
     */
    protected void writeStartTag(String elemName, 
            ArrayList<SimpleEntry<String,String>> atts, boolean isPlain)
    {
        writeShiftedString("<" + elemName);
        for (SimpleEntry<String,String> entry : atts)
        {
            writeString(" " + entry.getKey() + "=" +
                    Utils.quote(entry.getValue()));
        }
        writeString(">");
        
        if (!isPlain)
            { writeString("\n"); }
    }
    
    /**
     * Writes end tag of XML element to file.
     * 
     * @param <code>elemName</code> the element name.
     * 
     * @param <code>isPlain</code> the boolean value, wich shows is element has
     *        plain form (start tag, content, end tag place in one string)
     *        or not.
     */
    protected void writeEndTag(String elemName, boolean isPlain)
    {
        if (isPlain)
            { writeString("</" + elemName + ">\n"); } 
        else
            { writeShiftedString("</" + elemName + ">\n"); }
        
    }
    
    /**
     * Writes XML element with empty content to file. 
     * 
     * @param <code>elemName</code> the element's name.
     * 
     * @param <code>atts</code> the array with element's attributes. Array 
     *        contains pairs with attribute's name and attribute's value.
     */
    protected void writeEmptyContentElement(String elemName, 
            ArrayList<SimpleEntry<String,String>> atts)
    {
        writeShiftedString("<" + elemName);
        for (SimpleEntry<String,String> entry : atts)
        {
            writeString(" " + entry.getKey() + "=" +
                    Utils.quote(entry.getValue()));
        }
        writeString(" />\n");
    }
    
    /**
     * Writes XML element wich has one attibute and has content with parameter
     * elements. 
     * 
     * @param <code>element</code> the element's name.
     * 
     * @param <code>name</code> the value of element's attribute.
     * 
     * @param <code>params</code> the element's parameter. 
     */
    protected void writeElementWithParameters(String element, String name,
            ArrayList<SimpleEntry<String,String>> params)
    {
        ArrayList<SimpleEntry<String,String>> atts =
            new ArrayList<SimpleEntry<String,String>>();
        
        SimpleEntry<String,String> firstAtt = 
            new SimpleEntry<String,String>(
                    NAME, name);
        
        atts.add(firstAtt);
        
        writeStartTag(element, atts, false);
        
        shift +=DEFAULT_SHIFT_SIZE;
        
        // writes element's content
        for(SimpleEntry<String,String> entry : params)
            { writeParameterElement(entry.getKey(), entry.getValue()); }
        
        shift -=DEFAULT_SHIFT_SIZE;
        
        writeEndTag(element, false);
    }
    
    /**
     * Writes XML element with one parameter in special structure. 
     * 
     * @param <code>paramName</code> the parameter's name.
     * 
     * @param <code>paramValue</code> the parameter's value. 
     */
    protected void writeParameterElement(String paramName, String paramValue)
    {
        /*
         * element structure is
         * structure<param name="parameterName">value<param> 
         */ 
        
        ArrayList<SimpleEntry<String,String>> atts =
            new ArrayList<SimpleEntry<String,String>>();
        
        SimpleEntry<String,String> firstAtt = 
            new SimpleEntry<String,String>(NAME, paramName);
        
        atts.add(firstAtt);
        
        writeStartTag(PARAM_ELEM, atts, true);
        
        // writes element's content
        writeString(paramValue);
        
        writeEndTag(PARAM_ELEM, true);
    }
    
    /**
     * Writes specified string to file with current paragraph's shift.
     * 
     * @param <code>s</code> the string.
     */
    protected void writeShiftedString(String s)
    {
        try
        {
        	for (int i=0; i < shift; i++) writer.write(" ");
            writer.write(s);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
	
    /**
     * Writes specified string to file.
     * 
     * @param <code>s</code> the string.
     */
    protected void writeString(String s)
    {
        try
        {
            writer.write(s);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
