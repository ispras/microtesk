/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TemplateIteratorPanel.java,v 1.6 2009/07/09 14:48:14 kamkin Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.panel;

import static com.unitesk.testfusion.core.config.OptionsConfig.BRANCH_TEMPLATE_ITERATOR;
import static com.unitesk.testfusion.core.config.OptionsConfig.MULTISET_TEMPLATE_ITERATOR;
import static com.unitesk.testfusion.core.config.OptionsConfig.PRODUCT_TEMPLATE_ITERATOR;
import static com.unitesk.testfusion.core.config.OptionsConfig.SEQUENCE_TEMPLATE_ITERATOR;
import static com.unitesk.testfusion.core.config.OptionsConfig.SET_TEMPLATE_ITERATOR;
import static com.unitesk.testfusion.core.config.OptionsConfig.SINGLE_TEMPLATE_ITERATOR;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.unitesk.testfusion.core.config.OptionsConfig;
import com.unitesk.testfusion.core.config.template.*;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.dialog.OptionsDialog;
import com.unitesk.testfusion.gui.dialog.options.panel.iterator.BranchTemplateIteratorPanel;
import com.unitesk.testfusion.gui.dialog.options.panel.iterator.MultisetTemplateIteratorPanel;
import com.unitesk.testfusion.gui.dialog.options.panel.iterator.ProductTemplateIteratorPanel;
import com.unitesk.testfusion.gui.dialog.options.panel.iterator.SequenceTemplateIteratorPanel;
import com.unitesk.testfusion.gui.dialog.options.panel.iterator.SetTemplateIteratorPanel;
import com.unitesk.testfusion.gui.dialog.options.panel.iterator.SingleTemplateIteratorPanel;

/**
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class TemplateIteratorPanel extends OptionsTabPanel
{
    public static final long serialVersionUID = 0;
    
    private static final String SIZE_IS_ZERO = "template size is nought";
    
    private static final String MAX_SIZE_IS_ZERO =
        "maximum template size is nought";
    
    private static final String MAX_REPETITION_IS_ZERO =
        "maximum repetition size is nought";
    
    private static final String MAX_SIZE_LESS_MAX_POSITION =
        "maximum template size should be equal or more " +
        "then maximum position number";
    
    private static final String MAX_SIZE_LESS_MIN_SIZE =
        "maximum template size should be equal or more " +
        "then minimum template size";
        
    
    protected JComboBox templateIterCombo;
    
    protected ProductTemplateIteratorPanel  productIterPanel;
    protected BranchTemplateIteratorPanel   branchIterPanel;
    protected SetTemplateIteratorPanel      setIterPanel;
    protected MultisetTemplateIteratorPanel multisetIterPanel;
    protected SequenceTemplateIteratorPanel sequenceIterPanel;
    protected SingleTemplateIteratorPanel   singleIterPanel;
    
    protected OptionsConfig currentOptions;
    
    public TemplateIteratorPanel(GUI frame, OptionsDialog dialog)
    {
        this.currentOptions = dialog.getCurrentOptionsConfig();
        
        String[] comboList = new String[]
        {
            " " + ProductTemplateIteratorConfig.NAME,
            " " + BranchTemplateIteratorConfig.NAME,
            " " + SetTemplateIteratorConfig.NAME,
            " " + MultisetTemplateIteratorConfig.NAME,
            " " + SequenceTemplateIteratorConfig.NAME,
            " " + SingleTemplateIteratorConfig.NAME
        };
        templateIterCombo = new JComboBox(comboList);
        
        templateIterCombo.setPreferredSize(
                new Dimension(LEFT_PANEL_WIDTH,
                        templateIterCombo.getPreferredSize().height));
        
        templateIterCombo.addItemListener
        (
            new ItemListener()
            {
                public void itemStateChanged(ItemEvent e) 
                {
                    productIterPanel.setVisible(false);
                    branchIterPanel.setVisible(false);
                    setIterPanel.setVisible(false);
                    multisetIterPanel.setVisible(false);
                    sequenceIterPanel.setVisible(false);
                    singleIterPanel.setVisible(false);
                    
                    currentOptions.setIndex(
                            templateIterCombo.getSelectedIndex());
                    
                    switch (templateIterCombo.getSelectedIndex()) 
                    {
                        case PRODUCT_TEMPLATE_ITERATOR: 
                            productIterPanel.setVisible(true);
                            break;
                        case BRANCH_TEMPLATE_ITERATOR: 
                            branchIterPanel.setVisible(true);
                            break;
                        case SET_TEMPLATE_ITERATOR:
                            setIterPanel.setVisible(true);
                            break;
                        case MULTISET_TEMPLATE_ITERATOR:
                            multisetIterPanel.setVisible(true);
                            break;    
                        case SEQUENCE_TEMPLATE_ITERATOR: 
                            sequenceIterPanel.setVisible(true);
                            break;
                        case SINGLE_TEMPLATE_ITERATOR:
                            singleIterPanel.setVisible(true);
                            break;
                    }
                }
            }
        );
        
        productIterPanel  = new ProductTemplateIteratorPanel(dialog);
        branchIterPanel   = new BranchTemplateIteratorPanel(dialog);
        setIterPanel      = new SetTemplateIteratorPanel(dialog);
        multisetIterPanel = new MultisetTemplateIteratorPanel(dialog);
        sequenceIterPanel = new SequenceTemplateIteratorPanel(frame, dialog);
        singleIterPanel   = new SingleTemplateIteratorPanel(frame, dialog);
                
        JPanel paramPanel = new JPanel();
        paramPanel.setLayout(new BoxLayout(paramPanel, BoxLayout.Y_AXIS));
        
        paramPanel.add(productIterPanel);
        paramPanel.add(branchIterPanel);
        paramPanel.add(setIterPanel);
        paramPanel.add(multisetIterPanel);
        paramPanel.add(sequenceIterPanel);
        paramPanel.add(singleIterPanel);
        paramPanel.add(Box.createVerticalGlue());
        
        templateIterCombo.setSelectedIndex(currentOptions.getIndex());
        ItemEvent e = new ItemEvent(templateIterCombo, 
                ItemEvent.ITEM_STATE_CHANGED, 
                templateIterCombo.getItemAt(currentOptions.getIndex()), 
                ItemEvent.SELECTED);
        
        templateIterCombo.getItemListeners()[0].itemStateChanged(e);
        
        createTabPanel("Template Iterator Types:", templateIterCombo,
                "Template Iterator Parameters", paramPanel);
    }
    
    /**
     * Saves data in Template Iterator tab. 
     */
    public void saveTemplateIteratorTab()
    {
        ////////////////////////////////////////////////////////////////////////
        // Product template iterator
        ////////////////////////////////////////////////////////////////////////
        ProductTemplateIteratorConfig productConfig = 
            (ProductTemplateIteratorConfig)
            currentOptions.getTemplateIterator(PRODUCT_TEMPLATE_ITERATOR);   
        
        productConfig.setTemplateSize(productIterPanel.getSizeValue());
        
        ////////////////////////////////////////////////////////////////////////
        // Branch template iterator
        ////////////////////////////////////////////////////////////////////////
        BranchTemplateIteratorConfig branchConfig =
            (BranchTemplateIteratorConfig)
            currentOptions.getTemplateIterator(BRANCH_TEMPLATE_ITERATOR);
        
        branchConfig.setMinTemplateSize(branchIterPanel.getMinValue());
        branchConfig.setMaxTemplateSize(branchIterPanel.getMaxValue());
        branchConfig.setMinBranchNumber(branchIterPanel.getMinBranchNumberValue());
        branchConfig.setMaxBranchNumber(branchIterPanel.getMaxBranchNumberValue());
        branchConfig.setMaxBranchExecution(branchIterPanel.getMaxBranchExecutionValue());

        ////////////////////////////////////////////////////////////////////////
        // Set template iterator
        ////////////////////////////////////////////////////////////////////////
        SetTemplateIteratorConfig setConfig = 
            (SetTemplateIteratorConfig)
            currentOptions.getTemplateIterator(SET_TEMPLATE_ITERATOR);
        
        setConfig.setMinTemplateSize(setIterPanel.getMinValue());
        setConfig.setMaxTemplateSize(setIterPanel.getMaxValue());
        
        ////////////////////////////////////////////////////////////////////////
        // Multiset template iterator
        ////////////////////////////////////////////////////////////////////////
        MultisetTemplateIteratorConfig multisetConfig = 
            (MultisetTemplateIteratorConfig)
            currentOptions.getTemplateIterator(MULTISET_TEMPLATE_ITERATOR);
        
        multisetConfig.setMinTemplateSize(multisetIterPanel.getMinValue());
        multisetConfig.setMaxTemplateSize(multisetIterPanel.getMaxValue());
        multisetConfig.setMaxRepetition(
                multisetIterPanel.getMaxRepetitionSizeValue());

        ////////////////////////////////////////////////////////////////////////
        // Sequence template iterator
        ////////////////////////////////////////////////////////////////////////
        int sequenceTemplateSize = sequenceIterPanel.getSizeValue();
        sequenceIterPanel.setSizeValue(sequenceTemplateSize);
        SequenceTemplateIteratorConfig sequenceConfig = 
            (SequenceTemplateIteratorConfig) 
            currentOptions.getTemplateIterator(SEQUENCE_TEMPLATE_ITERATOR);
        sequenceConfig.setTemplateSize(sequenceTemplateSize);
        
        ////////////////////////////////////////////////////////////////////////
        // Single template iterator
        ////////////////////////////////////////////////////////////////////////
        int singleTemplateSize = singleIterPanel.getSizeValue();
        singleIterPanel.setSizeValue(singleIterPanel.getSizeValue());
        SingleTemplateIteratorConfig singleConfig = 
            (SingleTemplateIteratorConfig)
            currentOptions.getTemplateIterator(SINGLE_TEMPLATE_ITERATOR);
        singleConfig.setTemplateSize(singleTemplateSize);
    }
    
    public SingleTemplateIteratorPanel getSingleTemplateIteratorPanel()
    {
        return singleIterPanel;
    }
    
    public SequenceTemplateIteratorPanel getSequenceTemplateIteratorPanel()
    {
        return sequenceIterPanel;
    }
    
    public String checkParametersCorrectness()
    {
        ////////////////////////////////////////////////////////////////////////
        // Product template iterator
        ////////////////////////////////////////////////////////////////////////
        if (productIterPanel.getSizeValue() == 0)
        {
            return ProductTemplateIteratorConfig.NAME + ": " + SIZE_IS_ZERO;
        }
        
        ////////////////////////////////////////////////////////////////////////
        // Set template iterator
        ////////////////////////////////////////////////////////////////////////
        if (setIterPanel.getMaxValue() == 0)
        {
            return SetTemplateIteratorConfig.NAME + ": " + MAX_SIZE_IS_ZERO;
        }
        
        if (setIterPanel.getMaxValue() < setIterPanel.getMinValue())
        {
            return SetTemplateIteratorConfig.NAME + ": " + 
                MAX_SIZE_LESS_MIN_SIZE;
        }
        
        ////////////////////////////////////////////////////////////////////////
        // Multiset template iterator
        ////////////////////////////////////////////////////////////////////////
        if (multisetIterPanel.getMaxValue() == 0)
        {
            return MultisetTemplateIteratorConfig.NAME + ":" + 
                MAX_SIZE_IS_ZERO; 
        }
        
        if (multisetIterPanel.getMaxRepetitionSizeValue() == 0)
        {
            return MultisetTemplateIteratorConfig.NAME + ": " +
                MAX_REPETITION_IS_ZERO;
        }
        
        
        if (multisetIterPanel.getMaxValue() < 
                multisetIterPanel.getMinValue())
        {
            return MultisetTemplateIteratorConfig.NAME + ": " +
                MAX_SIZE_LESS_MIN_SIZE;
        }
        
        ////////////////////////////////////////////////////////////////////////
        // Sequence template iterator
        ////////////////////////////////////////////////////////////////////////
        if(sequenceIterPanel.getSizeValue() < 
                sequenceIterPanel.getMaxPosition())
        {
            return SequenceTemplateIteratorConfig.NAME + ": " + 
                MAX_SIZE_LESS_MAX_POSITION;
        }
        
        ////////////////////////////////////////////////////////////////////////
        // Single template iterator
        ////////////////////////////////////////////////////////////////////////
        if (singleIterPanel.getSizeValue() < 
                singleIterPanel.getMaxPosition())
        {
            return SingleTemplateIteratorConfig.NAME + ": " +
                MAX_SIZE_LESS_MAX_POSITION;
        }
        
        return "";
    }
}
