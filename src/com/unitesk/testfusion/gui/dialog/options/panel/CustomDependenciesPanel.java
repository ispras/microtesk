/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: CustomDependenciesPanel.java,v 1.3 2008/12/09 12:37:42 kozlov Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.panel;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.unitesk.testfusion.core.config.ContentDependencyConfig;
import com.unitesk.testfusion.core.config.DependencyListConfig;

/**
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class CustomDependenciesPanel extends OptionsTabPanel 
{
    public static final long serialVersionUID = 0;
    
    protected ContentDependencyConfig currentContentDep;
    protected JList customDepTypeList;
    protected JCheckBox depTypeCheckBox;
    
    public CustomDependenciesPanel(final DependencyListConfig deps)
    {

        Vector<String> customDepVector = new Vector<String>();
        
        // initialize vectors with dependencies
        for (int i = 0; i < deps.countDependency(); i++)
        {
            String s  = deps.getDependency(i).getName();
            s = " " + s + " ";
            if (!deps.getDependency(i).isRegisterDependency())
                { customDepVector.add(s); }
        }
        
        customDepTypeList = new JList(customDepVector);
        
        customDepTypeList.addListSelectionListener
        (
           new ListSelectionListener()
           {
               public void valueChanged(ListSelectionEvent e)
               {
                   if (!depTypeCheckBox.isEnabled())
                   {
                       depTypeCheckBox.setEnabled(true);
                   }
                   
                   String name = (String)customDepTypeList.getSelectedValue();
                   if (name != null)
                   {
                       name = name.substring(1, name.length() - 1);
                       
                       currentContentDep = (ContentDependencyConfig)
                           deps.getDependency(name);
                       
                       depTypeCheckBox.setSelected(
                               currentContentDep.isEnabled());
                   }
               }
           }
        );
        
        JScrollPane listPanel = new JScrollPane(customDepTypeList);
        
        listPanel.setPreferredSize(PANEL_LIST_SIZE);

        depTypeCheckBox = new JCheckBox(" Set Dependency ");
        depTypeCheckBox.addItemListener
        (
                new ItemListener()
                {
                    public void itemStateChanged(ItemEvent e)
                    {
                        currentContentDep.setEnabled(
                                depTypeCheckBox.isSelected());
                    }
                }
        );
        
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(
                new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        
        checkBoxPanel.add(Box.createVerticalGlue());
        checkBoxPanel.add(depTypeCheckBox);
        checkBoxPanel.add(Box.createVerticalGlue());
        
        depTypeCheckBox.setEnabled(false);
        
        createTabPanel("Dependency Types:", listPanel,
                "Custom Dependency Parameters", checkBoxPanel);
    }
    
    /**
     * Saves data in Custom Dependencies tab.
     */
    public void saveCustomDependenciesTab()
    {
        
    }
}
