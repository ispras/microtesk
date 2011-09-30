/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: RegisterDependenciesPanel.java,v 1.5 2009/05/15 16:28:54 kozlov Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.panel;

import static com.unitesk.testfusion.gui.Layout.*;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.unitesk.testfusion.core.config.DependencyListConfig;
import com.unitesk.testfusion.core.config.RegisterDependencyConfig;
import com.unitesk.testfusion.core.config.register.RegisterIteratorConfig;
import com.unitesk.testfusion.gui.textfield.IntTextField;

/**
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class RegisterDependenciesPanel extends OptionsTabPanel 
{
    public static final long serialVersionUID = 0;
    
    private static final int TEXT_FIELD_SIZE = 3;
    
    protected RegisterDependencyConfig currentRegDep;
    protected RegisterIteratorConfig currentRegDepIter;
    protected boolean isDepChanged; 
    
    protected JPanel regDepParamPanel;
    protected JComboBox regDepCombo; 
    protected JList regTypeList;
    protected JCheckBox defUseDepCheckBox;
    protected JCheckBox defDefDepCheckBox;
    protected JCheckBox useUseDepCheckBox;
    protected JCheckBox useDefDepCheckBox;
    protected IntTextField maxNumber; 
    protected IntTextField minNumber;
    
    public RegisterDependenciesPanel(final DependencyListConfig deps)
    {
        Vector<String> regDepVector = new Vector<String>();

        // initialize vectors with dependencies
        for (int i = 0; i < deps.countDependency(); i++)
        {
            String s  = deps.getDependency(i).getName();
            s = " " + s + " ";
            if (deps.getDependency(i).isRegisterDependency())
                { regDepVector.add(s); }
        }
        
        regTypeList = new JList(regDepVector);
        regTypeList.addListSelectionListener
        (
           new ListSelectionListener()
           {
               public void valueChanged(ListSelectionEvent e)
               {
                   if (!e.getValueIsAdjusting())
                   {
                       if (! defDefDepCheckBox.isEnabled())
                       {
                           setRegDepParamEnabled(true);
                       }
                       else
                       {
                           saveRegDepParam();
                       }
                       
                       String name = (String)regTypeList.getSelectedValue();
                       if (name != null)
                       {
                           name = name.substring(1, name.length() - 1);
                           
                           currentRegDep = (RegisterDependencyConfig)
                               deps.getDependency(name);
                           
                           int newIndex = currentRegDep.getIndex(); 
                           
                           currentRegDepIter = currentRegDep.getRegisterIterator(
                                   newIndex);
                           
                           isDepChanged = true;
                           
                           regDepCombo.setSelectedIndex(newIndex);
                           regDepCombo.repaint();
                           
                           updateRegDepPanel();
                           
                           isDepChanged = false;
                       }
                   }
               }
           }
        );
        
        JScrollPane listPanel = new JScrollPane(regTypeList);
        listPanel.setPreferredSize(PANEL_LIST_SIZE);
        
        useDefDepCheckBox = new JCheckBox(" Read-Write ");
        defDefDepCheckBox = new JCheckBox(" Write-Write ");
        useUseDepCheckBox = new JCheckBox(" Read-Read ");
        defUseDepCheckBox = new JCheckBox(" Write-Read ");
        
        String[] comboList = new String[]
                                        {
                " Exhaustive Register Iterator ",
                " Number Register Iterator ",
                " Random Register Iterator " 
                                        };
        
        regDepCombo = new JComboBox(comboList);
        
        regDepCombo.addItemListener
        (
            new ItemListener()
            {
                public void itemStateChanged(ItemEvent e) 
                {
                    if (e.getStateChange() == ItemEvent.SELECTED && !isDepChanged)
                    {
                        saveRegDepParam();
                        currentRegDep.setIndex(regDepCombo.getSelectedIndex());
                        
                        currentRegDepIter = currentRegDep.getRegisterIterator(
                                currentRegDep.getIndex());
                        
                        updateRegDepPanel();
                    }
                }
            }
        );
        
        // init text fields
        minNumber = new IntTextField(TEXT_FIELD_SIZE, Integer.MIN_VALUE);
        
        maxNumber = new IntTextField(TEXT_FIELD_SIZE, Integer.MAX_VALUE);
        
        regDepParamPanel = new JPanel(new GridBagLayout());
        
        GridBagConstraints constraints;
        Insets insets = new Insets(0, 0, 0, 0);
        
        insets.set(0, 0, SPACE_BETWEEN_RELATIVE_COMPONENT, 0);
        constraints = getGridBagConstraints(
                GridBagConstraints.WEST, GridBagConstraints.NONE, 
                0, 0, insets, 1.0, 1.0);
        
        regDepParamPanel.add(useDefDepCheckBox, constraints);
        
        insets.set(0, 0, SPACE_BETWEEN_RELATIVE_COMPONENT, 0);
        constraints = getGridBagConstraints(
                GridBagConstraints.WEST, GridBagConstraints.NONE, 
                1, 0, insets, 1.0, 1.0);
        regDepParamPanel.add(defUseDepCheckBox, constraints);
        
        insets.set(0, 0, SPACE_BETWEEN_RELATIVE_COMPONENT, 0);
        constraints = getGridBagConstraints(
                GridBagConstraints.WEST, GridBagConstraints.NONE, 
                0, 1, insets, 1.0, 1.0);
        regDepParamPanel.add(useUseDepCheckBox, constraints);
        
        insets.set(0, 0, SPACE_BETWEEN_DIFFERENT_COMPONENT, 0);
        constraints = getGridBagConstraints(
                GridBagConstraints.WEST, GridBagConstraints.NONE, 
                1, 1, insets, 1.0, 1.0);
        regDepParamPanel.add(defDefDepCheckBox, constraints);
        
        insets.set(0, 0, 0, 0);
        constraints = getGridBagConstraints(
                GridBagConstraints.EAST, GridBagConstraints.NONE, 
                0, 2, insets, 1.0, 1.0);
        regDepParamPanel.add(new JLabel("Min Number:"), constraints);
        
        insets.set(0, SPACE_HORIZONTAL_SEPARATION_ELEMENTS, 0, 0);
        constraints = getGridBagConstraints(
                GridBagConstraints.WEST, GridBagConstraints.NONE, 
                1, 2, insets, 1.0, 1.0);
        regDepParamPanel.add(minNumber, constraints);
        
        insets.set(SPACE_BETWEEN_RELATIVE_COMPONENT, 0, SPACE_FROM_BORDER, 0);
        constraints = getGridBagConstraints(
                GridBagConstraints.EAST, GridBagConstraints.NONE, 
                0, 3, insets, 1.0, 1.0);
        regDepParamPanel.add(new JLabel("Max Number:"), constraints);
        
        insets.set(SPACE_BETWEEN_RELATIVE_COMPONENT, 
                SPACE_HORIZONTAL_SEPARATION_ELEMENTS, SPACE_FROM_BORDER, 0);
        constraints = getGridBagConstraints(
                GridBagConstraints.WEST, GridBagConstraints.NONE, 
                1, 3, insets, 1.0, 1.0);
        regDepParamPanel.add(maxNumber, constraints);
        
        setRegDepParamEnabled(false);
        
        createTabPanel("Register Types:", listPanel,
                " Register Iterator Parameters ", regDepParamPanel);
        
        insets.set(SPACE_FROM_BORDER, 
                SPACE_FROM_BORDER + RIGHT_PANEL_DELTA, 0, 0);
        constraints = getGridBagConstraints(
                GridBagConstraints.WEST, GridBagConstraints.NONE, 
                1, 0, insets, 0.0, 0.0);
        add(new JLabel("Register Iterator Type:"), constraints);
        
        insets.set(SPACE_BETWEEN_RELATIVE_COMPONENT + DELTA,
                SPACE_FROM_BORDER + RIGHT_PANEL_DELTA, 0, 
                SPACE_FROM_RIGHT_EDGE);
        constraints = getGridBagConstraints(
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, 
                1, 1, insets, 1.0, 1.0);
        add(regDepCombo, constraints);
    }
    
    protected void setRegDepParamEnabled(boolean enable)
    {
        for (Component comp : regDepParamPanel.getComponents())
        {
            comp.setEnabled(enable);
        }
        
        regDepCombo.setEnabled(enable);
    }
    
    protected void updateRegDepPanel()
    {
        defUseDepCheckBox.setSelected(currentRegDepIter.isDefineUse());
        defDefDepCheckBox.setSelected(currentRegDepIter.isDefineDefine());
        useUseDepCheckBox.setSelected(currentRegDepIter.isUseUse());
        useDefDepCheckBox.setSelected(currentRegDepIter.isUseDefine());
        
        minNumber.setIntValue(currentRegDepIter.getMinNumber());
        maxNumber.setIntValue(currentRegDepIter.getMaxNumber());
        
        regDepParamPanel.repaint();
    }
    
    protected void saveRegDepParam()
    {
        currentRegDepIter.setDefineUse(defUseDepCheckBox.isSelected());
        currentRegDepIter.setDefineDefine(defDefDepCheckBox.isSelected());
        currentRegDepIter.setUseUse(useUseDepCheckBox.isSelected());
        currentRegDepIter.setUseDefine(useDefDepCheckBox.isSelected());
        
        currentRegDepIter.setMinNumber(minNumber.getIntValue());
        currentRegDepIter.setMaxNumber(maxNumber.getIntValue());
    }
    
    /**
     * Saves data in Register Dependencies tab.
     */
    public void saveRegisterDependenciesTab()
    {
        if (useDefDepCheckBox.isEnabled())
            { saveRegDepParam(); }
    }
}
