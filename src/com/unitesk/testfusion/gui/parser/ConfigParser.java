/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ConfigParser.java,v 1.48 2009/06/08 12:59:29 kamkin Exp $
 */

package com.unitesk.testfusion.gui.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.AbstractMap.SimpleEntry;

import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;

import com.unitesk.testfusion.core.config.*;

import com.unitesk.testfusion.core.config.register.*;
import com.unitesk.testfusion.core.config.template.*;

import com.unitesk.testfusion.core.util.Utils;

import com.unitesk.testfusion.gui.GUI;

import com.unitesk.testfusion.gui.dialog.WarningsDialog;

import static com.unitesk.testfusion.gui.parser.Constants.*;

/**
 * Parser of XML files with MicroTESK test configuration.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class ConfigParser extends DefaultHandler
{
    /**
     * Class for configuration parser exceptions.
     * 
     * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
     */
    protected class ConfigParserException extends SAXException
    {
        public static final long serialVersionUID = 0;
        
        /**
         * Constructor.
         */
        public ConfigParserException() {}
        
        /**
         * Constructor.
         * 
         * @param <code>message</code> the message of the exception.
         */
        public ConfigParserException(String message)
        {
            super(message);
        }
       
        /**
         * Constructor for exception, when attribute {@link Constants#NAME}}
         * of some element is missing.
         * 
         * @param <code>elem</code> the element's name.
         * 
         * @param <code>comment</code> the comment.
         */
        public ConfigParserException(String elem, String comment)
        {
            this("attribute " + Utils.quote(NAME) + 
            " for element " + Utils.quote(elem) + " (" + comment + ")" + 
            " is missing.");
        }
    }
    
	/** This value indicates that a file has been successfully read. */
	public static int FILE_HAS_BEEN_SUCCESSFULLY_READ  = 0;
    
    /** This value indicates that a file has been read with some warnings. */
	public static int FILE_HAS_BEEN_READ_WITH_WARNINGS = 1;
    
    /** This value indicates that a file has not been read. */
	public static int FILE_HAS_NOT_BEEN_READ           = 2;
    
    private static final String ATTR = "Attribute";
    
    private static final String PARAM = "Parameter";
    
    /** GUI frame. */
    protected GUI frame; 
    
    /** Current test configuration, where data from XML file are saved. */
    protected TestConfig curTestConfig;
    
    /** 
     * Stack with pairs, where key is the XML element's name
     * and value is configuration corresponding to this element. 
     */
    protected Stack<SimpleEntry<String,Config>> stack = 
        new Stack<SimpleEntry<String,Config>>();
    
    /** The last delivered XML element's content. */
    protected StringBuffer lastValue;
    
    /** Vector with parser's warnings messages. */
    protected Vector<String> warningMessages = new Vector<String>();
    
    /** 
     * List keeps data from the elements with name #PARAM_ELEM.
     * List contains pairs, where the key is a value of attribute with name
     * #NAME, and the value is element's content.
     * List contains data only from that elements, wich have one parent 
     * element.
     */ 
    protected LinkedList<SimpleEntry<String,String>>params =
       new LinkedList<SimpleEntry<String,String>>();
    
    /**
     * Base constructor.
     * 
     * @param <code>frame</code> the GUI frame.
     */
    public ConfigParser(GUI frame)
    {
        super();
        
        this.frame =frame;
    }

    /**
     * Parses specified XML file.
     *  
     * @param <code>file<code> the file to be parsed.
     *  
     * @return an integer that indicates whether the file has been parsed or not.
     * 
     * @see #FILE_HAS_BEEN_SUCCESSFULLY_READ FILE_HAS_BEEN_SUCCESSFULLY_READ
     * 
     * @see #FILE_HAS_BEEN_READ_WITH_WARNINGS FILE_HAS_BEEN_READ_WITH_WARNINGS
     * 
     * @see #FILE_HAS_NOT_BEEN_READ FILE_HAS_NOT_BEEN_READ
     */
    public int parseFile(File file)
    {
        try
        {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(this);
            xr.setErrorHandler(this);
            
            curTestConfig = frame.getDefaultConfig().clone();
            
            FileReader r = new FileReader(file);
            
            xr.parse(new InputSource(r));
            
            // file has been successfully parsed
            frame.setConfig(curTestConfig);
            
            frame.getConfig().setName(
                    Utils.removeFileExtention(file.getName()));
            
            if(warningMessages.size() == 0)
            {
            	frame.showInformationMessage(
                        "Configuration has been successfully loaded from "
                        + file.getName(), "File has been opened");
                
            	return  FILE_HAS_BEEN_SUCCESSFULLY_READ;
            }
            else
            {
            	WarningsDialog dialog = 
                    new WarningsDialog(frame, warningMessages);
                
            	dialog.setVisible(true);
            	return  FILE_HAS_BEEN_READ_WITH_WARNINGS;
            }
        }
        catch (FileNotFoundException e)
        {
            frame.showWarningMessage(
                    "File " + file.getName() + " not found", "Error");
            
            return  FILE_HAS_NOT_BEEN_READ;
        }
        catch (IOException e)
        {
            frame.showWarningMessage("File " + file.getName() + 
                    " has incorrect format: it is not valid XML document",
                    "Error");
            
            return  FILE_HAS_NOT_BEEN_READ;
        }
        catch (ConfigParserException e)
        {
            frame.showWarningMessage("File " + file.getName() +
                    " has incorrect format: " + e.getMessage(), "Error");
            
            return  FILE_HAS_NOT_BEEN_READ;
        }
        catch (SAXException e)
        {
            frame.showWarningMessage("File " + file.getName() + 
                    " has incorrect format: it is not valid XML document",
                    "Error");
            
            return  FILE_HAS_NOT_BEEN_READ;
        }
    }
    
    //*************************************************************************
    // Overrided functions from DefaultHandler class.
    //*************************************************************************
    public void startElement(String uri, String name, String qName,
            Attributes atts) throws SAXException
    {
        lastValue = new StringBuffer();
        
        if (stack.isEmpty())
            { parseRootElementStartTag(name, atts); }
        
        else if (stack.peek() != null)
        {
            SimpleEntry<String,Config> entry = stack.peek(); 
            
            if (entry.getValue() instanceof TestConfig)
                { parseTestContent(name, atts); }
            
            else if (entry.getValue() instanceof SectionConfig)
                { parseSectionContent(name, atts); }
            
            else if (entry.getValue() instanceof ProcessorConfig)
                { parseProcessorContent(name, atts); }
            
            else if (entry.getValue() instanceof GroupConfig)
                { parseGroupContent(name, atts); }
            
            else if (entry.getValue() instanceof InstructionConfig)
                { parseInstructionContent(name, atts); }
        
            else if (entry.getValue() instanceof OptionsConfig)
                { parseOptionsContent(name, atts); }
            
            else if (entry.getKey().equals(TEMPLATE_ITER_ELEM))
                { parseParameters(name, atts); }
            
            else if (entry.getValue() instanceof DependencyListConfig)
                { parseDependeciesContent(name); }
            
            else if (entry.getKey().equals(ALL_REG_DEPEND_ELEM))
                { parseRegisterDependeciesContent(name, atts); }
            
            else if (entry.getKey().equals(ALL_CUSTOM_DEPEND_ELEM))
                { parseCustomDependeciesContent(name, atts); }
        
            else if (entry.getValue() instanceof RegisterDependencyConfig)
                { parseRegDepContent(name, atts); }
            
            else if (entry.getKey().equals(REG_ITERATOR_ELEM))
                { parseParameters(name, atts); }
            
            else if (entry.getValue() instanceof CrossDependencyListConfig)
                { parseCrossDependeciesContent(name, atts); }
            
            else if (entry.getValue() instanceof CrossDependencyConfig)
                { parseCrossDependecyContent(name); }
            
            else if (entry.getValue() instanceof SettingsConfig)
                { parseSettingsContent(name, atts); }
            
            else if (entry.getKey().equals(
                    TEST_NAMING_STRATEGY_ELEM))
                { parseParameters(name, atts); }
        }
    }
    
    public void endElement(String uri, String name, String qName)
    {
        if (name.equals(TEST_ELEM)              ||
            name.equals(SECTION_ELEM)           || 
            name.equals(PROCESSOR_ELEM)         ||
            name.equals(GROUP_ELEM)             || 
            name.equals(INSTRUCTION_ELEM)       || 
            name.equals(OPTIONS_ELEM)           ||       
            name.equals(ALL_DEPEND_ELEM)        ||
            name.equals(ALL_REG_DEPEND_ELEM)    ||
            name.equals(ONE_REG_DEPEND_ELEM)    ||
            name.equals(ALL_CUSTOM_DEPEND_ELEM) ||
            name.equals(ALL_CROSS_DEPEND_ELEM)  ||
            name.equals(ONE_CROSS_DEPEND_ELEM)  ||
            name.equals(SETTINGS_ELEM)
            )  
        {
            stack.pop();
        }
        
        else if (name.equals(PARAM_ELEM))
        {
            SimpleEntry<String,String> param = params.getLast();
            
            param.setValue(new String(lastValue));
        }
        else if (name.equals(TEMPLATE_ITER_ELEM))
        {
            TemplateIteratorConfig config = 
                (TemplateIteratorConfig)stack.pop().getValue();
            
            if (config != null) 
                { parseTemplateIteratorParameters(config); }
        }
        else if (name.equals(REG_ITERATOR_ELEM))
        {
            RegisterIteratorConfig config = 
                (RegisterIteratorConfig)stack.pop().getValue();
            if (config != null)
                parseRegisterDepIteratorParameters(config);
        }
        else if (name.equals(TEST_NAMING_STRATEGY_ELEM))
        {
            stack.pop();
            SettingsConfig config = (SettingsConfig)stack.peek().getValue();
            parseTestNamingStrategyParameters(config);
        }
    }
    
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        for (int i = start; i < start + length; i++) 
        {
            switch (ch[i]) 
            {
                case '\n': case '\r': case '\t': break;
                
                default:
                {
                    lastValue.append(ch[i]);
                    break;
                }
            }
        }
    }
    
    //*************************************************************************
    // Methods for parsing start tag of XML elements.
    // Calls in startElement(...) function.
    //*************************************************************************
    
    /**
     * Parses start tag of root XML element. 
     * 
     * @param <code>name</code> the element's name.
     * 
     * @param <code>atts</code> the attributes attached to the element. 
     * 
     * @throws <code>ConfigParserException</code> if root element has name 
     *         differs from {@link Constants#TEST_ELEM} or if 
     *         {@link Constants#TEST_ELEM_PROCESSOR_ATTR} attribute 
     *         is missed or has incorrect value.
     */
    protected void parseRootElementStartTag(String name, Attributes atts) 
            throws ConfigParserException
    {
        if (name.equals(TEST_ELEM))
        {
            String processorName = getAttrValue(atts,
                    TEST_ELEM_PROCESSOR_ATTR);
            
            if (processorName == null)
            {
                throw new ConfigParserException("attribute " + 
                        Utils.quote(TEST_ELEM_PROCESSOR_ATTR) + 
                        " for element " + Utils.quote(name) + " is missing.");
            }
            
            else if (! processorName.equals(
                    curTestConfig.getProcessor().getName())
                    )
                throw new ConfigParserException("attribute " + 
                        Utils.quote(TEST_ELEM_PROCESSOR_ATTR) + 
                        " for element " + Utils.quote(name) + 
                        " has incorrect value.");
            
            String sizeStringValue = getAttrValue(atts, TEST_ELEM_SIZE_ATTR);
            
            int testSize = getIntValue(sizeStringValue, 
                    curTestConfig.getTestSize(), 1, TestConfig.MAX_TEST_SIZE, 
                    ATTR, TEST_ELEM_SIZE_ATTR, name, "");
            
            curTestConfig.setTestSize(testSize);
            
            String selfCheckStringValue = getAttrValue(atts, 
                    TEST_ELEM_SELF_CHECK_ATTR);
            
            boolean selfCheck = getBooleanValue(selfCheckStringValue,
                    false, ATTR, TEST_ELEM_SELF_CHECK_ATTR, TEST_ELEM, "");
            
            curTestConfig.setSelfCheck(selfCheck);
            
            stack.push(new SimpleEntry<String, Config>(name, curTestConfig));
        }
        else
            throw new ConfigParserException(
                    "it is not valid XML document, unexpected tag " + name +
                    " in root element");
    }
    
    /**
     * Parses content of element with test's configuration. 
     * ({@link Constants#TEST_ELEM}).
     * 
     * @param <code>name</code> the element's name.
     * 
     * @param <code>atts</code> the attributes attached to the element.
     * 
     * @throws <code>ConfigParserException</code> if encounter element with
     *         with name differs from {@link Constants#SECTION_ELEM} or
     *         {@link Constants#SETTINGS_ELEM}.  
     * 
     */
    protected void parseTestContent(String name, Attributes atts) 
            throws ConfigParserException
    {
        TestConfig test = (TestConfig)stack.peek().getValue();
        
        if (name.equals(SECTION_ELEM))
        {
            String sectionName = getAttrValue(atts, NAME);
            
            if (sectionName == null)
                { sectionName = SectionConfig.DEFAULT_CONFIGURATION_NAME; }
            
            // TODO : check section's name for unique
            
            SectionConfig section = test.createSection(sectionName);
                
            test.registerSection(section);
            
            stack.push(new SimpleEntry<String, Config>(name, section));
        }
        else if (name.equals(OPTIONS_ELEM))
        {
            OptionsConfig options = curTestConfig.getOptions();
            stack.push(new SimpleEntry<String, Config>(name, options));
        }
        else if (name.equals(SETTINGS_ELEM))
        {
            SettingsConfig settings = curTestConfig.getSettings();
            stack.push(new SimpleEntry<String, Config>(name, settings));
        }
        else
            throw new ConfigParserException(
                    "it is not valid XML document, unexpected tag " + name + 
                    " in test element");
    }
    
    
    /**
     * Parses content of element with section's configuration. 
     * ({@link Constants#SECTION_ELEM}).
     * 
     * @param <code>name</code> the element's name.
     * 
     * @param <code>atts</code> the attributes attached to the element.
     * 
     * @throws <code>ConfigParserException</code> if encounter element with
     *         with name differs from {@link Constants#SECTION_ELEM} or
     *         {@link Constants#PROCESSOR_ELEM} or 
     *         {@link Constants#OPTIONS_ELEM}.   
     */
    protected void parseSectionContent(String name, 
            Attributes atts) throws ConfigParserException
    {
        SectionConfig section = (SectionConfig)stack.peek().getValue();
        
        if (name.equals(SECTION_ELEM))
        {
            String sectionName = getAttrValue(atts, NAME);
            
            if (sectionName == null)
                { sectionName = SectionConfig.DEFAULT_CONFIGURATION_NAME; }
            
            // TODO : check section's name for unique
            
            SectionConfig newSection = 
                curTestConfig.createSection(sectionName);
                
            section.registerSection(newSection);
            
            stack.push(new SimpleEntry<String,Config>(name, newSection));
        }
        else if (name.equals(PROCESSOR_ELEM))
        {
            String value = getAttrValue(atts, SELECTED);
            
            boolean selected = getBooleanValue(value, false, ATTR, 
                    SELECTED, name, section.getFullName()); 
            
            section.getProcessor().setSelected(selected);
            
            ProcessorConfig config = section.getProcessor();
            
            stack.push(new SimpleEntry<String,Config>(name, config));
        }
        else if (name.equals(OPTIONS_ELEM))
        {
            OptionsConfig options  = section.getOptions(); 
            stack.push(new SimpleEntry<String,Config>(name, options));
        }
        else
            throw new ConfigParserException(
                    "it is not valid XML document, unexpected tag " + name +
                    " in section element");
    }
    
    /**
     * Parses content of element with processor's configuration. 
     * ({@link Constants#PROCESSOR_ELEM}).
     * 
     * @param <code>name</code> the element's name.
     * 
     * @param <code>atts</code> the attributes attached to the element.
     * 
     * @throws <code>ConfigParserException</code> if encounter element with
     *         with name differs from {@link Constants#GROUP_ELEM} or if
     *         {@link Constants#NAME} attribute from 
     *         {@link Constants#GROUP_ELEM} element is missed or has 
     *         incorrect value.   
     */
    protected void parseProcessorContent(String name, 
            Attributes atts) throws ConfigParserException
    {
        ProcessorConfig proc = (ProcessorConfig)stack.peek().getValue();
        
        if (name.equals(GROUP_ELEM))
        {
            String groupName = getAttrValue(atts, NAME);
            if (groupName == null)
                throw new ConfigParserException(PROCESSOR_ELEM,
                        proc.getFullName());
            
            GroupConfig group = proc.getGroup(groupName);
            
            if (group == null)
                throw new ConfigParserException("group \"" + groupName + "\"" +
                        "(" + proc.getFullName() + "." +  groupName + ")" +
                        " is incorrect");
            
            String value = getAttrValue(atts, SELECTED);
            
            boolean selected = getBooleanValue(value, false, ATTR,
                    SELECTED, name, group.getFullName());
            
            group.setSelected(selected);
            
            stack.push(new SimpleEntry<String,Config>(name, group));
        }
        else
            throw new ConfigParserException(
                    "it is not valid XML document, unexpected tag " + name + 
                    " in processor element");
    }
    
    /**
     * Parses content of element with group's configuration. 
     * ({@link Constants#GROUP_ELEM}).
     * 
     * @param <code>name</code> the element's name.
     * 
     * @param <code>atts</code> the attributes attached to the element.
     * 
     * @throws <code>ConfigParserException</code> if encounter element with
     *         with name differs from {@link Constants#GROUP_ELEM} or 
     *         {@link Constants#INSTRUCTION_ELEM} or if
     *         {@link Constants#NAME} attributes from 
     *         {@link Constants#GROUP_ELEM} or 
     *         {@link Constants#INSTRUCTION_ELEM} elements are missed or 
     *         have incorrect values.
     */
    protected void parseGroupContent(String name, 
            Attributes atts) throws ConfigParserException
    {
        GroupConfig group = (GroupConfig)stack.peek().getValue();
        
        if (name.equals(GROUP_ELEM))
        {
            String groupName = getAttrValue(atts, NAME);
            
            if (groupName == null)
                throw new ConfigParserException(name, group.getFullName());
            
            GroupConfig newGroup = group.getGroup(groupName);
            
            if (newGroup == null)
                throw new ConfigParserException("group " + 
                        Utils.quote(groupName) +
                        " (" + group.getFullName() + "." +  groupName + ")" + 
                        " is incorrect");
            
            String value = getAttrValue(atts, SELECTED);
            
            boolean selected = getBooleanValue(value, false, ATTR, 
                    SELECTED, name, newGroup.getFullName());
            
            newGroup.setSelected(selected);
            
            stack.push(new SimpleEntry<String,Config>(name, newGroup));
        }
        else if (name.equals(INSTRUCTION_ELEM))
        {
            String instructionName = getAttrValue(atts, NAME);
            
            if (instructionName == null)
                throw new ConfigParserException(name, group.getFullName());
            
            InstructionConfig instruction = 
                group.getInstruction(instructionName);
            
            if (instruction == null)
                throw new ConfigParserException(" instruction " + 
                        Utils.quote(instructionName) + " ("  + 
                        group.getFullName() + "." +  instructionName + ")"
                        + " is incorrect");
            
            String value = getAttrValue(atts, SELECTED);
            
            boolean selected = getBooleanValue(value, false, ATTR, 
                    SELECTED, name, instruction.getFullName());
            
            instruction.setSelected(selected);
            
            String eqClass =  getAttrValue(atts,
                    INSTRUCTION_ELEM_EQ_CLASS_ATTR);
            
            instruction.setEquivalenceClass(eqClass);
            
            String positionsValue = getAttrValue(atts, POSITIONS);
            if(positionsValue != null)
            {
            	HashSet<Integer> positions = getPositions(positionsValue);
	            instruction.setPositions(positions);
            }
            
            stack.push(new SimpleEntry<String,Config>(name, instruction));
        }
        else 
            throw new ConfigParserException(
                    "it is not valid XML document, unexpected tag " + name +
                    " in group element");
    }
    
    /**
     *  Parses content of element with instruction's configuration. 
     * ({@link Constants#INSTRUCTION_ELEM}).
     * 
     * @param <code>name</code> the element's name.
     * 
     * @param <code>atts</code> the attributes attached to the element.
     * 
     * @throws <code>ConfigParserException</code> if encounter element with
     *         with name differs from {@link Constants#SITUATION_ELEM} or if
     *         {@link Constants#NAME} attribute from 
     *         {@link Constants#SITUATION_ELEM} is missed or has incorrect
     *         values.
     */
    protected void parseInstructionContent(String name, 
            Attributes atts) throws ConfigParserException
    {
        InstructionConfig instruction = (InstructionConfig)stack.peek().getValue();
        
        if (name.equals(SITUATION_ELEM))
        {
            String situationName = getAttrValue(atts, NAME);
            
            if (situationName == null)
                throw new ConfigParserException(name, instruction.getName());
            
            SituationConfig situation = 
                instruction.getSituation(situationName);
            
            if (situation == null)
                throw new ConfigParserException("situation " + 
                        Utils.quote(situationName) + " (" + 
                        instruction.getFullName() + "." +  situationName +
                        ") is incorrect");
            
            String selectedValue = getAttrValue(atts, SELECTED);
            
            boolean selected = getBooleanValue(selectedValue, false, ATTR, 
                    SELECTED, name, situation.getFullName());
            
            situation.setSelected(selected);
        }
        else
            throw new ConfigParserException(
                    "it is not valid XML document, unexpected tag " + name +
                    " in instruction element");
    }
    
    /**
     * Parses content of element with options' configuration. 
     * ({@link Constants#OPTIONS_ELEM}).
     * 
     * @param <code>name</code> the element's name.
     * 
     * @param <code>atts</code> the attributes attached to the element.
     * 
     * @throws ConfigParserException if encounter element with
     *         with name differing from {@link Constants#TEMPLATE_ITER_ELEM} or
     *         {@link Constants#ALL_DEPEND_ELEM}.
     */
    protected void parseOptionsContent(String name, 
            Attributes atts) throws ConfigParserException
    {
        OptionsConfig options = (OptionsConfig)stack.peek().getValue();
        
        if (name.equals(TEMPLATE_ITER_ELEM))
        {
            params.clear();
            
            String iterName = getAttrValue(atts, NAME);
            
            if (iterName != null)
            {
                if (iterName.equals(PRODUCT_ITER_ELEM))
                {
                    options.setIndex(OptionsConfig.PRODUCT_TEMPLATE_ITERATOR);
                    
                    ProductTemplateIteratorConfig config =
                        (ProductTemplateIteratorConfig)options.getTemplateIterator();
                    
                    stack.push(new SimpleEntry<String,Config>(name, config));
                }
                else if (iterName.equals(SET_ITER_ELEM))
                {
                    options.setIndex(OptionsConfig.SET_TEMPLATE_ITERATOR);
                    
                    SetTemplateIteratorConfig config = 
                        (SetTemplateIteratorConfig)options.getTemplateIterator();
                    
                    stack.push(new SimpleEntry<String,Config>(name, config));
                }
                else if (iterName.equals(MULTISET_ITER_ELEM))
                {
                    options.setIndex(OptionsConfig.MULTISET_TEMPLATE_ITERATOR);
                    
                    MultisetTemplateIteratorConfig config =
                        (MultisetTemplateIteratorConfig)options.getTemplateIterator();
                    
                    stack.push(new SimpleEntry<String,Config>(name, config));
                }
                else if (iterName.equals(SEQUENCE_ITER_ELEM))
                {
                    options.setIndex(OptionsConfig.SEQUENCE_TEMPLATE_ITERATOR);
                    
                    SequenceTemplateIteratorConfig config =
                        (SequenceTemplateIteratorConfig)options.getTemplateIterator();
                    
                    stack.push(new SimpleEntry<String,Config>(name, config));
                }
                else if (iterName.equals(SINGLE_ITER_ELEM))
                {
                    options.setIndex(OptionsConfig.SINGLE_TEMPLATE_ITERATOR);
                    
                    SingleTemplateIteratorConfig config =
                        (SingleTemplateIteratorConfig)options.getTemplateIterator();
                    
                    stack.push(new SimpleEntry<String,Config>(name, config));
                }
                else
                {
                    addIncorrectObjectWarningMessage(ATTR, NAME, 
                            TEMPLATE_ITER_ELEM, options.getFullName());
                    
                    stack.push(new SimpleEntry<String,Config>(name, null));
                }
            }
            else
            {
                addMissingObjectWarningMessage(ATTR, NAME, 
                        TEMPLATE_ITER_ELEM, options.getFullName());
                
                stack.push(new SimpleEntry<String,Config>(name, null));
            }
        }
        else if (name.equals(ALL_DEPEND_ELEM))
        {
            DependencyListConfig deps = options.getDependencies();
            stack.push(new SimpleEntry<String,Config>(name, deps));
        }
        else if (name.equals(ALL_CROSS_DEPEND_ELEM))
        {
            CrossDependencyListConfig cross = options.getCrossDependencies();
            stack.push(new SimpleEntry<String,Config>(name, cross));
        }
        else
        {
            throw new ConfigParserException(
                    "it is not valid XML document, unexpected tag " + name + 
                    " in options element");
        }
    }
    
    /**
     * Parses content of element with dependencies' configurations.
     * ({@link Constants#ALL_DEPEND_ELEM}).
     * 
     * @param <code>name</code> the element's name.
     * 
     * @param <code>atts</code> the attributes attached to the element.
     * 
     * @throws ConfigParserException if encounter element with name differs
     *         from {@link Constants#ALL_REG_DEPEND_ELEM} or 
     *         {@link Constants#ALL_CUSTOM_DEPEND_ELEM}.
     */
    protected void parseDependeciesContent(String name) 
            throws ConfigParserException
    {
        if (name.equals(ALL_REG_DEPEND_ELEM))
            { stack.push(new SimpleEntry<String,Config>(name, null)); }
        
        else if (name.equals(ALL_CUSTOM_DEPEND_ELEM))
            { stack.push(new SimpleEntry<String,Config>(name, null)); }
        
        else
        {
            throw new ConfigParserException(
                    "it is not valid XML document, unexpected tag " + name + 
                    " in dependencies element");
        }
    }
    
    /**
     * Parses content of element with register dependencies' configurations.
     * ({@link Constants#ALL_REG_DEPEND_ELEM}).
     * 
     * @param <code>name</code> the element's name.
     * 
     * @param <code>atts</code> the attributes attached to the element.
     * 
     * @throws ConfigParserException if encounter element with name differs
     *         from {@link Constants#ONE_REG_DEPEND_ELEM} or if attribute
     *         {@link Constants#NAME}} of 
     *         {@link Constants#ONE_REG_DEPEND_ELEM} element is missing or
     *         has incorrect value.  
     */
    protected void parseRegisterDependeciesContent(String name, 
            Attributes atts) throws ConfigParserException
    {
        if (name.equals(ONE_REG_DEPEND_ELEM))
        {
            SimpleEntry<String,Config> entryAllRegDeps = stack.pop();
            SimpleEntry<String,Config> entryAllDeps = stack.peek();
            stack.push(entryAllRegDeps);
            
            DependencyListConfig deps = 
                (DependencyListConfig)entryAllDeps.getValue(); 
            
            String depName = getAttrValue(atts, NAME);
            
            if (depName == null)
                throw new ConfigParserException(name, deps.getFullName());
            
            DependencyConfig dep = deps.getDependency(depName);
            if (dep == null || ! dep.isRegisterDependency())
                throw new ConfigParserException("register dependency " + 
                        Utils.quote(depName) + 
                        "(" + deps.getFullName() + "." + depName + ") " + 
                        "is incorrect");
            
            RegisterDependencyConfig regDep = (RegisterDependencyConfig)dep;
            
            stack.push(new SimpleEntry<String,Config>(name, regDep));
        }
        else
        {
            throw new ConfigParserException(
                    "it is not valid XML document, unexpected tag " + name +
                    " in register dependencies element");
        }
    }
    
    /**
     * Parses content of element with custom dependencies' configurations.
     * ({@link Constants#ALL_CUSTOM_DEPEND_ELEM}).
     * 
     * @param <code>name</code> the element's name.
     * 
     * @param <code>atts</code> the attributes attached to the element.
     * 
     * @throws ConfigParserException if encounter element with name differs
     *         from {@link Constants#ONE_CUSTOM_DEPEND_ELEM} or if attribute
     *         {@link Constants#NAME}} of 
     *         {@link Constants#ONE_CUSTOM_DEPEND_ELEM} element is missing or
     *         has incorrect value.  
     */
    protected void parseCustomDependeciesContent(String name,
            Attributes atts) throws ConfigParserException
    {
        if (name.equals(ONE_CUSTOM_DEPEND_ELEM))
        {
            SimpleEntry<String,Config> entryAllCustomDeps = stack.pop();
            SimpleEntry<String,Config> entryAllDeps = stack.pop();
            SimpleEntry<String,Config> entryOptions = stack.peek();
            stack.push(entryAllDeps);
            stack.push(entryAllCustomDeps);
            
            OptionsConfig options = (OptionsConfig)entryOptions.getValue();
            
            String depName = getAttrValue(atts, 
                    ONE_CUSTOM_DEPEND_ELEM);
            
            if (depName == null)
                throw new ConfigParserException(name, options.getFullName());  
            
            DependencyConfig dep = options.getDependencies().getDependency(depName);
            if (dep == null || dep.isRegisterDependency())
                throw new ConfigParserException("custom dependency " + 
                        Utils.quote(depName) + 
                        "(" + options.getFullName() + "." + depName + ")" + 
                        " is incorrect.");
            
            ContentDependencyConfig contentDep = (ContentDependencyConfig)dep;
            
            String value = getAttrValue(atts, SELECTED);
            
            boolean selected = getBooleanValue(value, false, ATTR, 
                    SELECTED, name, contentDep.getFullName());
            
            contentDep.setEnabled(selected);
        }
        else
        {
            throw new ConfigParserException(
                    "it is not valid XML document, unexpected tag " + name + 
                    " in custom dependencies element");
        }
    }
    
    /**
     * Parses content of element with register dependency's configuration.
     * ({@link Constants#ONE_REG_DEPEND_ELEM})
     * 
     * @param <code>name</code> the element's name.
     * 
     * @param <code>atts</code> the attributes attached to the element.
     * 
     * @throws ConfigParserException if encounter element with name differs
     *         from {@link Constants#REG_ITERATOR_ELEM}.
     */
    protected void parseRegDepContent(String name, 
            Attributes atts) throws ConfigParserException
    {
        if (name.equals(REG_ITERATOR_ELEM))
        {
            RegisterDependencyConfig regDep = 
                (RegisterDependencyConfig)stack.peek().getValue();
            
            params.clear();
            
            String iteratorName = getAttrValue(atts, NAME);
            
            if (iteratorName == null)
            {
                addMissingObjectWarningMessage(ATTR, NAME, 
                        name, regDep.getFullName());
                
                stack.push(new SimpleEntry<String,Config>(name, null));
            }
            else
            {
                if (iteratorName.equals(ExhaustiveRegisterIteratorConfig.NAME))
                {
                    regDep.setIndex(
                            RegisterDependencyConfig.
                            EXHAUSTIVE_REGISTER_ITERATOR);
                    RegisterIteratorConfig iter = regDep.getRegisterIterator();
                    stack.push(new SimpleEntry<String,Config>(name, iter));
                }
                else if (iteratorName.equals(
                        NumberRegisterIteratorConfig.NAME))
                {
                    regDep.setIndex(
                            RegisterDependencyConfig.NUMBER_REGISTER_ITERATOR);
                    RegisterIteratorConfig iter = regDep.getRegisterIterator();
                    stack.push(new SimpleEntry<String,Config>(name, iter));
                }
                else if (iteratorName.equals(
                        RandomRegisterIteratorConfig.NAME))
                {
                    regDep.setIndex(
                            RegisterDependencyConfig.RANDOM_REGISTER_ITERATOR);
                    RegisterIteratorConfig iter = regDep.getRegisterIterator();
                    stack.push(new SimpleEntry<String,Config>(name, iter));
                }
                else
                {
                    addIncorrectObjectWarningMessage(ATTR, NAME, 
                            REG_ITERATOR_ELEM, regDep.getFullName());
                    // TODO : check putting null as config
                    stack.push(new SimpleEntry<String,Config>(name, null));
                }
            }
        }
        else
        {
            throw new ConfigParserException(
                    "it is not valid XML document, unexpected tag " + name +
                    " in register dependency element");
        }
    }
    
    /**
     * Parses content of element with cross dependency list configuration.
     * ({@link Constants#ALL_CROSS_DEPEND_ELEM})
     * 
     * @param <code>name</code> the element's name.
     * 
     * @param <code>atts</code> the attributes attached to the element.
     * 
     * @throws ConfigParserException if encounter element with name differs
     *         from {@link Constants#ONE_CROSS_DEPEND_ELEM}.
     */
    protected void parseCrossDependeciesContent(String name, Attributes atts) 
        throws ConfigParserException
    {
        if (name.equals(ONE_CROSS_DEPEND_ELEM))
        {
            SimpleEntry<String,Config> crossDepsEntry = stack.peek();
            
            CrossDependencyListConfig crossDependencies = 
                (CrossDependencyListConfig)crossDepsEntry.getValue();
            
            String dependOnName = getAttrValue(atts, 
                    DEPEND_ON_SECTION_CROSS_DEPEND_PARAM);
            
            if (dependOnName == null)
            {
                // TODO : skip this cross dependency
            }
            
            SectionConfig dependOnSection = 
                curTestConfig.searchSectionByFullName(dependOnName);
            
            if (dependOnSection == null)
            {
                // TODO : skip this cross dependency
            }
            
            CrossDependencyConfig newCross = 
                crossDependencies.addCrossDependency(dependOnSection); 
            
            stack.push(new SimpleEntry<String,Config>(name, newCross));
        }
        else
        {
            throw new ConfigParserException(
                    "it is not valid XML document, unexpected tag " + name + 
                    " in cross dependencies element");
        }
    }
    
    /**
     * Parses content of element with one cross dependency configuration.
     * ({@link Constants#ONE_CROSS_DEPEND_ELEM})
     * 
     * @param <code>name</code> the element's name.
     * 
     * @throws ConfigParserException if encounter element with name differs
     *         from {@link Constants#ALL_DEPEND_ELEM}.
     */
    protected void parseCrossDependecyContent(String name) 
        throws ConfigParserException 
    {
        if (name.equals(ALL_DEPEND_ELEM))
        {
            CrossDependencyConfig cross = 
                (CrossDependencyConfig)stack.peek().getValue();
        
            DependencyListConfig deps = cross.getDependencies();
            
            stack.push(new SimpleEntry<String,Config>(name, deps));
        }
        else
        {
            throw new ConfigParserException(
                    "it is not valid XML document, unexpected tag " + name +
                    " in cross dependency element");
        }
    }
    
    /**
     * Parses start tags of elements from content of element with 
     * settings' configuration. ({@link Constants#SETTINGS_ELEM})
     * 
     * @param <code>name</code> the element's name.
     * 
     * @param <code>atts</code> the attributes attached to the element.
     * 
     * @throws <code>ConfigParserException</code> if encounter element with
     *         with name differing from #SITUATION_ELEM or if 
     *         value of attributes with situation name is missed or incorrect.
     */
    protected void parseSettingsContent(String name,
            Attributes atts) throws ConfigParserException
    {
        SettingsConfig settings = (SettingsConfig)stack.peek().getValue();

        if (name.equals(TEST_NAMING_STRATEGY_ELEM))
        {
            params.clear();
            
            String strategy = getAttrValue(atts, NAME);
            
            if (strategy != null)
            {
                if (strategy.equals(SettingsConfig.getStrategyName(
                        SettingsConfig.SPECIFIED_NAME)))
                {
                    settings.setTestNameStrategy(
                            SettingsConfig.SPECIFIED_NAME);
                }
                else if (strategy.equals(SettingsConfig.getStrategyName(
                        SettingsConfig.SITUATION_NAME)))
                {
                    settings.setTestNameStrategy(
                            SettingsConfig.SITUATION_NAME); 
                }
                else if (strategy.equals(SettingsConfig.getStrategyName(
                        SettingsConfig.INSTRUCTION_NAME)))
                {
                    settings.setTestNameStrategy(
                            SettingsConfig.INSTRUCTION_NAME);
                }
                else if (strategy.equals(SettingsConfig.getStrategyName(
                        SettingsConfig.GROUP_NAME)))
                {
                    settings.setTestNameStrategy(SettingsConfig.GROUP_NAME);
                }
                else if (strategy.equals(SettingsConfig.getStrategyName(
                        SettingsConfig.GROUP_NAME)))
                {
                    settings.setTestNameStrategy(
                            SettingsConfig.PROCESSOR_NAME);
                }
                else
                {
                    addIncorrectObjectWarningMessage(ATTR, NAME, name, "");
                }
            }
            else
            {
                addMissingObjectWarningMessage(ATTR, NAME, name, "");
            }
            
            stack.push(new SimpleEntry<String,Config>(name, null));
        }
        else
        {
            throw new ConfigParserException(
                    "it is not valid XML document, unexpected tag " + name + 
                    " in settings element");
        }
    }
    
    /**
     * Parses start tags of elements with parameters. Put parameters into
     * {@link #params}.
     * ({@link Constants#PARAM_ELEM})
     * 
     * @param <code>name</code> the element's name.
     * 
     * @param <code>atts</code> the attributes attached to the element. 
     * 
     * @throws <code>ConfigParserException</code> if encounter element with
     *         with name differing from {@link Constants#PARAM_ELEM}.
     */
    protected void parseParameters(String name, 
            Attributes atts) throws ConfigParserException
    {
        if (name.equals(PARAM_ELEM))
        {
            String value = getAttrValue(atts, NAME);
            
            if (value != null)
            {
                SimpleEntry<String,String>param = 
                    new SimpleEntry<String,String>(value, "");
                
                params.add(param);
            }
        }
        else
        {
            throw new ConfigParserException(
                    "it is not valid XML document, unexpected tag " + name +
                    " in parse parameters element");
        }
    }
    
    //*************************************************************************
    // Methods for parsing parameters
    //*************************************************************************
    
    /**
     * Parses parameters of template iterator configuration. Parameters are 
     * contained in {@link #params}.
     * 
     * @param <code>config></code> the template iterator's configuration.
     */
    protected void parseTemplateIteratorParameters(TemplateIteratorConfig config) 
    {
        String curSectionName = config.getParent().getFullName();
        
        final int MAX_SIZE = TemplateIteratorConfig.MAX_TEMPLATE_SIZE;
        
        if (config instanceof ProductTemplateIteratorConfig)
        {
            int size = getTemplateIteratorIntValue(1, 1, MAX_SIZE,
                    PRODUCT_ITER_SIZE, curSectionName);
                    
            ProductTemplateIteratorConfig productConfig = 
                (ProductTemplateIteratorConfig)config;
                
            productConfig.setTemplateSize(size);
        }
        else if (config instanceof SetTemplateIteratorConfig)
        {
            int minSize = getTemplateIteratorIntValue(1, 0, MAX_SIZE, 
                    SET_ITER_MIN_SIZE, curSectionName);
            
            int maxSize = getTemplateIteratorIntValue(1, 1, MAX_SIZE,
                    SET_ITER_MAX_SIZE, curSectionName);
            
            if (maxSize < minSize)
            {
                warningMessages.add(" Parameters " +
                        Utils.quote(SET_ITER_MIN_SIZE) + " and " +
                        Utils.quote(SET_ITER_MAX_SIZE) + " for element " +   
                        Utils.quote(TEMPLATE_ITER_ELEM) + 
                        " (" + curSectionName + ") " +
                        " are incorrect: maximum size is less than " + 
                        " minimum size. Default values are used");
                
                minSize = maxSize = 1;
            }
            
            SetTemplateIteratorConfig setConfig =
                (SetTemplateIteratorConfig)config;
            
            setConfig.setMinTemplateSize(minSize);
            setConfig.setMaxTemplateSize(maxSize);
        }
        else if (config instanceof MultisetTemplateIteratorConfig)
        {
            int minSize = getTemplateIteratorIntValue(1, 0, MAX_SIZE,
                    MULTISET_ITER_MIN_SIZE, curSectionName);
            
            int maxSize = getTemplateIteratorIntValue(1, 1, MAX_SIZE,
                    MULTISET_ITER_MAX_SIZE, curSectionName);
            
            int maxRepetition = getTemplateIteratorIntValue(1, 1, MAX_SIZE,
                    MULTISET_ITER_MAX_REPETITION_SIZE, curSectionName);
            
            if (maxSize < minSize)
            {
                warningMessages.add(" Parameters " +
                        Utils.quote(MULTISET_ITER_MIN_SIZE) + " and " +
                        Utils.quote(MULTISET_ITER_MAX_SIZE) + 
                        " for element " +  Utils.quote(TEMPLATE_ITER_ELEM) + 
                        " (" + curSectionName + ") " +
                        " are incorrect: maximum size is less than " + 
                        " minimum size. Default values are used");
                
                minSize = maxSize = 1;
            }
            
            MultisetTemplateIteratorConfig multisetConfig =
                (MultisetTemplateIteratorConfig)config;
            
            multisetConfig.setMinTemplateSize(minSize);
            multisetConfig.setMaxTemplateSize(maxSize);
            multisetConfig.setMaxRepetition(maxRepetition);
        }
        else if (config instanceof SequenceTemplateIteratorConfig)
        {
            int size = getTemplateIteratorIntValue(1, 1, MAX_SIZE,
                    SEQUENCE_ITER_SIZE, curSectionName);
            
            SequenceTemplateIteratorConfig sequenceConfig =
                (SequenceTemplateIteratorConfig)config;
            
            sequenceConfig.setTemplateSize(size);
        }
        else if (config instanceof SingleTemplateIteratorConfig)
        {
            int size = getTemplateIteratorIntValue(1, 1, MAX_SIZE,
                    SINGLE_ITER_SIZE, curSectionName);
            
            SingleTemplateIteratorConfig singleConfig =
                (SingleTemplateIteratorConfig)config;
            
            singleConfig.setTemplateSize(size);
        }
    }
    
    /**
     * Parses parameters of test naming starategy. Parameters are contained 
     * in {@link #params}.
     * 
     * @param <code>config></code> the settings's configuration.
     */
    protected void parseTestNamingStrategyParameters(SettingsConfig config)
    {
        int strategy = config.getTestNameStrategy();
        
        switch (strategy)
        {
            case SettingsConfig.SPECIFIED_NAME : 
            {
                String testName = getParamValue(
                        TEST_NAMING_STRATEGY_ELEM_TEST_NAME_PARAM);
                
                if (!SettingsConfig.isCorrectTestName(testName))
                {
                    addIncorrectObjectWarningMessage(PARAM,
                            TEST_NAMING_STRATEGY_ELEM_TEST_NAME_PARAM,
                            TEST_NAMING_STRATEGY_ELEM, "");
                }
                else
                    { config.setTestName(testName); }
                
                
                break;
            }
            case SettingsConfig.INSTRUCTION_NAME:
            case SettingsConfig.GROUP_NAME:
            case SettingsConfig.SITUATION_NAME:    
            case SettingsConfig.PROCESSOR_NAME:
            {
                String value = getParamValue(
                        TEST_NAMING_STRATEGY_ELEM_FULL_NAME_PARAM);
                
                boolean fullName = getBooleanValue(value, false, PARAM,
                        TEST_NAMING_STRATEGY_ELEM_FULL_NAME_PARAM,
                        TEST_NAMING_STRATEGY_ELEM, "");
                
                config.setFullName(fullName);
            }
        }
        
        String outputDir = getParamValue(
                TEST_NAMING_STRATEGY_ELEM_OUTPUT_DIR_PARAM);
        
        if (outputDir == null)
        {
            addMissingObjectWarningMessage(PARAM, 
                    TEST_NAMING_STRATEGY_ELEM_OUTPUT_DIR_PARAM, 
                    TEST_NAMING_STRATEGY_ELEM, "");
        }
        else if (!Utils.isCorrectFilePath(outputDir))
        {
            addIncorrectObjectWarningMessage(PARAM, 
                    TEST_NAMING_STRATEGY_ELEM_OUTPUT_DIR_PARAM, 
                    TEST_NAMING_STRATEGY_ELEM, "");
            
            outputDir = SettingsConfig.DEFAULT_OUTPUT_DIRECTORY;
        }
        
        config.setOutputDirectory(outputDir);
    }
    
    /**
     * Parses parameters of register dependency iterator configuration. 
     * Parameters are contained in {@link #params}.
     * 
     * @param <code>config></code> the register iterator configuration.
     */
    protected void parseRegisterDepIteratorParameters(RegisterIteratorConfig config)
    {
        String regDepName = config.getParent().getFullName();
        
        String value = getParamValue(REG_ITERATOR_ELEM_USE_USE_PARAM);
        boolean useUse = getBooleanValue(value, false,
                PARAM, REG_ITERATOR_ELEM_USE_USE_PARAM,
                REG_ITERATOR_ELEM, regDepName);

        value = getParamValue(REG_ITERATOR_ELEM_DEF_USE_PARAM);
        boolean defUse = getBooleanValue(value, false,
                PARAM, REG_ITERATOR_ELEM_DEF_USE_PARAM,
                REG_ITERATOR_ELEM, regDepName);
        
        value = getParamValue(REG_ITERATOR_ELEM_USE_DEF_PARAM);
        boolean useDef = getBooleanValue(value, false,
                PARAM, REG_ITERATOR_ELEM_USE_DEF_PARAM,
                REG_ITERATOR_ELEM, regDepName);
        
        value = getParamValue(REG_ITERATOR_ELEM_DEF_DEF_PARAM);
        boolean defDef = getBooleanValue(value, false,
                PARAM, REG_ITERATOR_ELEM_DEF_DEF_PARAM,
                REG_ITERATOR_ELEM, regDepName); 
        
        int minNumber = getRegDepParamIntValue(Integer.MIN_VALUE,
                REG_ITERATOR_ELEM_MIN_NUMBER_PARAM, regDepName);
        
        int maxNumber = getRegDepParamIntValue(Integer.MAX_VALUE,
                REG_ITERATOR_ELEM_MAX_NUMBER_PARAM, regDepName);
        
        config.setUseUse(useUse);
        config.setDefineUse(defUse);
        config.setUseDefine(useDef);
        config.setDefineDefine(defDef);
        
        config.setMinNumber(minNumber);
        config.setMaxNumber(maxNumber);
    }
    
    //*************************************************************************
    // Supporting methods.
    //*************************************************************************
    
    /**
     * Returns a value of specified parameter. Search in {@link #params}.
     *  
     * @param <code>paramName</code> the parameter's name.
     *  
     * @return a value of specified parameter or <code>null</code> if
     *         there isn't parameter with such name.
     */
    protected String getParamValue(String paramName)
    {
        for (SimpleEntry<String,String> entry : params)
        {
            if (entry.getKey().equals(paramName))
                { return entry.getValue(); } 
        }
        return null;
    }
    
    /**
     * Returns a value of specified attribute.
     *  
     *  @param <code>atts</code> the attributes for searching.
     *  
     * @param <code>attrName</code> the attribute's name.
     *  
     * @return a value of specified attribute or <code>null</code> if
     *         there isn't attribute with such name.
     */
    protected String getAttrValue(Attributes atts, String attrName)
    {
    	
        for(int i = 0; i < atts.getLength(); i++)
        {
        	if(atts.getLocalName(i).equals(attrName))
        		{ return atts.getValue(i); }
        }
        return null; 
    }
    
    /**
     * Returns boolean value from string representation of boolean value.
     * Call when need to parse value of {@link Constants#SELECTED} attribute
     * of some XML-element.
     * 
     * @param <code>booleanValue</code> the string representation of 
     *        boolean value.
     *        
     * @param <code>defaultValue</code> the value, wich is returned if string
     *        contains not booelan value representation.
     *        
     * @param <code>elem</code> the name of element (use if add warning 
     *        message).
     * 
     * @param <code>comment</code> the comment (use if add warning message).
     * 
     * @return boolean value, which corresponding to specified string
     *         <code>booleanValue</code>, or return <code>defValue</code>
     *         in case when specified string is incorrect.  
     */
    protected boolean getBooleanValue(String booleanValue, 
            boolean defaultValue, String objType, String objName, 
            String elem, String comment)
    {
        if (booleanValue == null)
        {
            // parameter value missing
            addMissingObjectWarningMessage(objType, objName, elem, comment);
            return defaultValue;
        }
        if (booleanValue.equalsIgnoreCase("true"))
            { return true; }
        if (booleanValue.equalsIgnoreCase("false"))
            { return false; }
        
        // incorrect value
        addIncorrectObjectWarningMessage(objType, objName, elem, comment);
        return defaultValue;
    }
    
    /**
     * Returns integer value from string representation of integer value.
     * 
     * @param <code>intValue</code> the string representation of 
     *        integerValue.
     *        
     * @param <code>defaultValue</code> the value, wich is returned if string
     *        contains incorrect value.
     * 
     * @param <code>minValue</code> the minimum acceptable value. 
     * 
     * @param <code>maxValue</code> the maximum acceptable value.
     * 
     * @param <code>objType</code> the type of object for saving value.
     *        It can possess one of the following values:
     *        {@link #ATTR} or {@link #PARAM}.
     *        
     * @param <code>objName</code> the name of object.
     *        
     * @param <code>elem</code> the name of element (use if add warning 
     *        message).
     * 
     * @param <code>comment</code> the comment (use if add warning message).
     * 
     * @return integer value, which corresponding to specified string
     *         <code>intValue</code>, or return <code>defaultValue</code>
     *         in case when specified string is incorrect.  
     */
    protected int getIntValue(String intValue, int defaultValue, 
            int minValue, int maxValue, String objType, 
            String objName, String elem, String comment)
    {
        if (intValue == null)
        {
            addMissingObjectWarningMessage(objType, objName, elem, comment);
            return defaultValue;
        }
        try 
        {
            int value = Integer.parseInt(intValue);
            if (value > maxValue || value < minValue)
                throw new NumberFormatException();
            return value;
        }
        catch (NumberFormatException e)
        {
            addIncorrectObjectWarningMessage(objType, objName, elem, comment);
            return defaultValue;
        }
    }
    
    /**
     * Returns HashSet value from string representation .
     * 
     * @param <code>positionsValue</code> string representation of positions.
     * 
     * @return HashSet of positions.  
     */
    protected HashSet<Integer> getPositions(String positionsValue)
    {
    	StringTokenizer stringTokenizer = new StringTokenizer(positionsValue, "[,]");
    	HashSet<Integer> positions = new HashSet<Integer>();
    	
    	while(stringTokenizer.hasMoreTokens())
    	{
    		String token = stringTokenizer.nextToken();
    		
    		int value = Integer.parseInt(token);
    		positions.add(value);
    	}
    	
    	return positions;
    }
    
    
    /**
     * Returns integer value of specified teplate iterator's parameter.
     * 
     * @param <code>defaultValue</code> the value, wich is returned if 
     *        parameter contains incorrect value.
     * 
     * @param <code>minValue</code> the minimum acceptable value 
     *        for specified parameter. 
     * 
     * @param <code>maxValue</code> the maximum acceptable value
     *        for specified parameter.
     *        
     * @param <code>paramName</code> the name of parameter.
     *        
     * @param <code>comment</code> the comment (use if add warning message).
     * 
     * @return integer value of specified parameter or return 
     *         <code>defaultValue</code> in case when parameter
     *         contains incorrect value.  
     */
    protected int getTemplateIteratorIntValue(int defaultValue, 
            int minValue, int maxValue, String paramName, String comment)
    {
        String intValue = getParamValue(paramName);
        
        return getIntValue(intValue, defaultValue, minValue, maxValue,
                PARAM, paramName, TEMPLATE_ITER_ELEM, comment);
    }
    
    /**
     * Returns integer value of specified register dependency iterator's
     * parameter.
     * 
     * @param <code>defaultValue</code> the value, wich is returned if 
     *        parameter contains incorrect value.
     * 
     * @param <code>paramName</code> the name of parameter.
     *        
     * @param <code>comment</code> the comment (use if add warning message).
     * 
     * @return integer value of specified parameter or return 
     *         <code>defaultValue</code> in case when parameter
     *         contains incorrect value.  
     */
    protected int getRegDepParamIntValue(int defaultValue, 
            String paramName, String comment)
    {
        String stringValue = getParamValue(paramName);  
        if (stringValue == null)
        {
            addMissingObjectWarningMessage(PARAM, paramName, 
                    REG_ITERATOR_ELEM, comment);
            return defaultValue;
        }
        try 
        {
            if (stringValue.equals(""))
                return defaultValue;
            
            return Integer.parseInt(stringValue);
        }
        catch (NumberFormatException e)
        {
            addIncorrectObjectWarningMessage(PARAM, paramName, 
                    REG_ITERATOR_ELEM, comment);
            return defaultValue;
        }
    }
    
    /**
     * Adds a warrning message about missing some object - attribute or 
     * parameter element.
     * 
     * @param <code>objectType<code> the type of object : "Attribute"
     *        or "Parameter".
     * 
     * @param <code>objName</code> the name of missing object.
     * 
     * @param <code>elem</code> the name of the parent element for
     *        missing object.
     *  
     * @param <code>comment</code> the comment.
     */
    protected void addMissingObjectWarningMessage(String objectType, 
            String objName, String elem, String comment)
    {
        comment = (Utils.isNullOrEmpty(comment)) ? 
                " " : " (" + comment + ") " ; 
        
        warningMessages.add(" " + objectType + " " + Utils.quote(objName) + 
                " for element " + Utils.quote(elem) + comment +  
                " is missing. Default value is used.");
    }
    
    /**
     * Adds a warrning message about incorrect value of some object - 
     * attribute or parameter element.
     * 
     * @param <code>objectType<code> the type of object : "Attribute"
     *        or "Parameter".
     * 
     * @param <code>objName</code> the name of incorrect object.
     * 
     * @param <code>elem</code> the name of the parent element for
     *        incorrect object.
     *  
     * @param <code>comment</code> the comment.
     */
    protected void addIncorrectObjectWarningMessage(String objectType, 
            String objName, String elem, String comment)
    {
        comment = (Utils.isNullOrEmpty(comment)) ? 
                " " : " (" + comment + ") " ; 
        
        warningMessages.add(" " + objectType + " " + Utils.quote(objName) + 
                " for element " + Utils.quote(elem) + comment +  
                " has incorrect value. Default value is used.");
    }
}
