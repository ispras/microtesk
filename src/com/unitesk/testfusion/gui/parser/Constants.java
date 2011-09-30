/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: Constants.java,v 1.14 2009/06/08 12:59:29 kamkin Exp $
 */

package com.unitesk.testfusion.gui.parser;

import com.unitesk.testfusion.core.config.template.*;
import com.unitesk.testfusion.core.util.Utils;
import com.unitesk.testfusion.gui.GUI;

/**
 * Class contains string constants for XML elements' names and their
 * attributes' names.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public final class Constants 
{
    /** XML comment header. */
    public static final String HEADER = 
        "<!-- " + GUI.APPLICATION_NAME + " Test File -->";
    
    //*************************************************************************
    // Common constants
    //*************************************************************************
    
    /** Name for attribute which shows value of some parameter. */
    public static final  String SIZE = "size";
    
    /** Name for attribute which shows some minimum value. */
    public static final String MIN_SIZE = "minSize";
    
    /** Name for attribute which shows some maximum value. */
    public static final String MAX_SIZE = "maxSize";

    /** 
     * Name for attribute which shows is selection config selected 
     * for testing or not. 
     */
    public static final String SELECTED = "selected";
    
    /** Name for attribute which shows instruction positions. */
    public static final String POSITIONS = "positions";
    
    /** Name for attribute which shows some name. */
    public static final String NAME = "name";
    
    /** Name for element which contains some parameter. */ 
    public static final String PARAM_ELEM = "param";
    
    //*************************************************************************
    // Constants for test's configuration
    //*************************************************************************
    
    /** Element's name for test's configuration. */
    public static final String TEST_ELEM = "test";
    
    /** Name for attribute which contains name of processor. */
    public static final String TEST_ELEM_PROCESSOR_ATTR = "processorName";
    
    /** Name for attribute which contains test program size. */
    public static final String TEST_ELEM_SIZE_ATTR = "testSize";
    
    /** Name for attribute which contains test program size. */
    public static final String TEST_ELEM_SELF_CHECK_ATTR = "selfCheck";
    
    //*************************************************************************
    // Constants for section's configuration
    //*************************************************************************
    
    /** Element's name for section's configuration. */
    public static final String SECTION_ELEM = "section";
    
    //*************************************************************************
    // Constants for processor's configuration
    //*************************************************************************
    
    /** Element's name for processor's configuration. */
    public static final String PROCESSOR_ELEM = "processor";
    
    //*************************************************************************
    // Constants for group's configuration
    //*************************************************************************
    
    /** Element's name for group's configuration. */
    public static final String GROUP_ELEM = "group";
    
    //*************************************************************************
    // Constants for instruction's configuration
    //*************************************************************************
    
    /** Element's name for instruction's configuration. */
    public static final String INSTRUCTION_ELEM = "instruction";
    
    /** 
     * Name for attribute which contains name of instruction's 
     * equivalence class. 
     */
    public static final String INSTRUCTION_ELEM_EQ_CLASS_ATTR = 
        "equivalenceClass";
    
    //*************************************************************************
    // Constants for situation's configuration
    //*************************************************************************
    
    /** Element's name for situation's configuration. */
    public static final String SITUATION_ELEM = "situation";
    
    //*************************************************************************
    // Constants for options' configuration
    //*************************************************************************
    
    /** Element's name for options' configuration. */
    public static final String OPTIONS_ELEM = "options";
    
    /** Element's name for all dependencies' configurations. */
    public static final String ALL_DEPEND_ELEM = "dependencies";
    
    /** Element's name for all register dependencies' configurations. */
    public static final String ALL_REG_DEPEND_ELEM = "registerDependencies";
    
    /** Element's name for all custom dependencies' configurations. */
    public static final String ALL_CUSTOM_DEPEND_ELEM = "customDependencies";
    
    /** Element's name for one register dependency's configuration. */
    public static final String ONE_REG_DEPEND_ELEM = "registerDependency";
    
    /** Element's name for one register iterator's configuration. */
    public static final String REG_ITERATOR_ELEM= "registerIterator";

    /** 
     * Name for attribute, which shows is register iterator's
     * use-use parameter is selected or not. 
     */ 
    public static final String REG_ITERATOR_ELEM_USE_USE_PARAM = "use-use";
    
    /** 
     * Name for attribute, which shows is register iterator's
     * define-use parameter is selected or not. 
     */ 
    public static final String REG_ITERATOR_ELEM_DEF_USE_PARAM = "def-use";

    /** 
     * Name for attribute, which shows is register iterator's
     * use-define parameter is selected or not. 
     */ 
    public static final String REG_ITERATOR_ELEM_USE_DEF_PARAM = "use-def";
    
    /** 
     * Name for attribute, which shows is register iterator's
     * define-define parameter is selected or not. 
     */ 
    public static final String REG_ITERATOR_ELEM_DEF_DEF_PARAM = "def-def";
    
    /** 
     * Name for attribute, which shows is register iterator's
     * minimum number value. 
     */ 
    public static final String REG_ITERATOR_ELEM_MIN_NUMBER_PARAM =
        "minNumber";
    
    /** 
     * Name for attribute, which shows is register iterator's
     * maximum number value. 
     */ 
    public static final String REG_ITERATOR_ELEM_MAX_NUMBER_PARAM = 
        "maxNumber";
    
    /** Name of element with one custom dependency configuration. */
    public static final String ONE_CUSTOM_DEPEND_ELEM = "customDependency";
    
    /** Name of element with all cross-dependencies configurations. */
    public static final String ALL_CROSS_DEPEND_ELEM = "crossDependencies";
    
    /** Name of element with one cross-dependency configuration. */
    public static final String ONE_CROSS_DEPEND_ELEM = "crossDependency";
    
    /** Name of parameter with first section of cross dependency. */
    public static final String DEPEND_ON_SECTION_CROSS_DEPEND_PARAM = 
        "dependOn";
    
    //*************************************************************************
    // Constants for template iterators' configuration
    //*************************************************************************
    
    /** Element's name for template iterator's configuration. */
    public static final String TEMPLATE_ITER_ELEM = "templateIterator";
    
    /** Element's name for product template iterator's configuration. */
    public static final String PRODUCT_ITER_ELEM = 
        Utils.removeBlanks(ProductTemplateIteratorConfig.NAME);
    
    /** Element's name for set template iterator's configuration. */
    public static final String SET_ITER_ELEM = 
        Utils.removeBlanks(SetTemplateIteratorConfig.NAME);
    
    /** Element's name for multiset template iterator's configuration. */
    public static final String MULTISET_ITER_ELEM = 
        Utils.removeBlanks(MultisetTemplateIteratorConfig.NAME);
    
    /** Element's name for sequence template iterator's configuration. */
    public static final String SEQUENCE_ITER_ELEM = 
        Utils.removeBlanks(SequenceTemplateIteratorConfig.NAME);
    
    /** Element's name for single template iterator's configuration. */
    public static final String SINGLE_ITER_ELEM = 
        Utils.removeBlanks(SingleTemplateIteratorConfig.NAME);
    
    //*************************************************************************
    // Constants for template iterators' parameters
    //*************************************************************************
    
    /** Name for attribute, which shows product template iterator's size. */
    public static final String PRODUCT_ITER_SIZE = SIZE;
    
    /** 
     * Name for attribute, which shows set template iterator's minimum
     * template size.
     */
    public static final String SET_ITER_MIN_SIZE = MIN_SIZE;
    
    /**
     * Name for attribute, which shows set template iterator's maximum
     * template size.
     */
    public static final String SET_ITER_MAX_SIZE = MAX_SIZE;
    
    /**
     * Name for attribute, which shows multiset template iterator's 
     * minimum template size.
     */
    public static final String MULTISET_ITER_MIN_SIZE = MIN_SIZE;
    
    /**
     * Name for attribute, which shows multiset template iterator's 
     * maximum template size.
     */
    public static final String MULTISET_ITER_MAX_SIZE = MAX_SIZE;
    
    /**
     * Name for attribute, which shows multiset template iterator's 
     * maximum repetition size.
     */
    public static final String MULTISET_ITER_MAX_REPETITION_SIZE = 
        "maxRepetition";
    
    /** Name for attribute, which shows sequence template iterator's size. */
    public static final String SEQUENCE_ITER_SIZE = SIZE;
    
    /** Name for attribute, which shows single template iterator's size. */
    public static final String SINGLE_ITER_SIZE = SIZE;
    
    //*************************************************************************
    // Constants for settings' configuration
    //*************************************************************************
    
    /** Element's name for settings' configuration. */
    public static final String SETTINGS_ELEM = "settings";
    
    /** 
     * Element's name for settings related with name of generated
     * test programs.
     */
    public static final String TEST_NAMING_STRATEGY_ELEM = 
        "testNamingStrategy";
    
    /** 
     * Name for attribute, which contains the name of generated test programs.
     */
    public static final String TEST_NAMING_STRATEGY_ELEM_TEST_NAME_PARAM =
        "testName";
    
    /** 
     * Name for attribute, which contains output directory, where 
     * generated test programs are saved.
     */
    public static final String TEST_NAMING_STRATEGY_ELEM_OUTPUT_DIR_PARAM = 
        "outputDir";
    
    /**
     * Name for attribute, which contains boolean value, which shows is need
     * to add full name of parent configuration to a name of generated test
     * programs for following name generation strategies - SituationName, 
     * InstructionName, GroupName. 
     */
    public static final String TEST_NAMING_STRATEGY_ELEM_FULL_NAME_PARAM = 
        "fullName";
}   