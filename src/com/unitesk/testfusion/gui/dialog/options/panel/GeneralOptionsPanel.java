/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: GeneralOptionsPanel.java,v 1.7 2009/08/06 11:09:35 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.panel;

import static com.unitesk.testfusion.gui.Layout.*;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.unitesk.testfusion.core.config.TestConfig;
import com.unitesk.testfusion.core.context.GeneratorContext;
import com.unitesk.testfusion.core.engine.GeneratorEngine;
import com.unitesk.testfusion.core.engine.Test;
import com.unitesk.testfusion.core.engine.TestProgramTemplate;
import com.unitesk.testfusion.core.model.Program;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.dialog.OptionsDialog;
import com.unitesk.testfusion.gui.dialog.PrefixSuffixDialog;
import com.unitesk.testfusion.gui.textfield.NonEmptyIntTextField;

/**
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class GeneralOptionsPanel extends OptionsTabPanel
{
    public static final long serialVersionUID = 0;
    
    protected GUI frame;
    
    protected NonEmptyIntTextField testSizeField;
    protected JCheckBox selfCheck;
    
    public static final int DEFAULT_TEXT_FIELD_SIZE = 5;
    
    public GeneralOptionsPanel(GUI frame, OptionsDialog dialog)
    {
        final int SPACE_BETWEEN_GROUP = 20;
        
        this.frame = frame;
        
        GridBagConstraints constraints;
        Insets insets = new Insets(0, 0, 0, 0);
        
        int testSize = frame.getConfig().getTestSize();
        testSizeField = new NonEmptyIntTextField(testSize, DEFAULT_TEXT_FIELD_SIZE,
                1, TestConfig.MAX_TEST_SIZE);
        testSizeField.addEmptyTextFieldListener(dialog);
        
        
        selfCheck = new JCheckBox(" Self-Checking Test ");
        selfCheck.setSelected(frame.getConfig().isSelfCheck());
        
        GeneratorEngine generator = frame.getConfig().getGenerator();
        
        Test targetTest = generator.getTargetTest();
        Test controlTest = generator.getControlTest();
        
        GeneratorContext targetContext = targetTest.getContext();
        GeneratorContext controlContext = (controlTest == null) ? 
                null : controlTest.getContext();
        
        TestProgramTemplate targetTemplate = targetTest.getTemplate();
        TestProgramTemplate controlTemplate = (controlTest == null) ? 
                null : controlTest.getTemplate();
        
        /* get prefixes and suffixes */ 
        Program targetPrefix  = targetTemplate.getTestPrefix(targetContext);
        Program controlPrefix = (controlTemplate == null) ? new Program() : 
            controlTemplate.getTestPrefix(controlContext);
        
        Program targetSuffix  = targetTemplate.getTestSuffix(targetContext);
        Program controlSuffix = (controlTemplate == null) ? new Program() : 
                controlTemplate.getTestSuffix(controlContext);
        
        Program targetActionPrefix  = targetTemplate.getTestSituationPrefix(
                targetContext, false, false);
        Program controlActionPrefix = (controlTemplate == null) ? new Program() : 
            controlTemplate.getTestSituationPrefix(controlContext, false, false);
        
        Program targetActionSuffix  = targetTemplate.getTestActionSuffix(
                targetContext, false, false);
        Program controlActionSuffix = (controlTemplate == null) ? new Program() : 
            controlTemplate.getTestActionSuffix(controlContext, false, false);
        
        /* create and init buttons */
        JButton testProgramPrefixButton = new JButton("View");
        testProgramPrefixButton.addActionListener(
                new ShowPrefixSuffixAction(
                        targetPrefix, controlPrefix, "Test Program Prefix"));
        
        JButton testProgramSuffixButton = new JButton("View");
        testProgramSuffixButton.addActionListener(
                new ShowPrefixSuffixAction(
                        targetSuffix, controlSuffix,
                        "Test Program Suffix"));
        
        JButton testActionPrefixButton = new JButton("View");
        testActionPrefixButton.addActionListener(
                new ShowPrefixSuffixAction(
                        targetActionPrefix, controlActionPrefix,
                        "Test Action Prefix"));
        
        JButton testActionSuffixButton = new JButton("View");
        testActionSuffixButton.addActionListener(
                new ShowPrefixSuffixAction(
                        targetActionSuffix, controlActionSuffix, 
                        "Test Action Suffix"));
        
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBorder(new TitledBorder("Test Parameters"));
        
        insets.set(SPACE_BETWEEN_RELATIVE_COMPONENT, 
                SPACE_BETWEEN_RELATIVE_COMPONENT, 0, 0);
        constraints = getGridBagConstraints(GridBagConstraints.WEST, 
                GridBagConstraints.NONE, 0, 0, insets, 0.0, 0.0);
        leftPanel.add(new JLabel("Test Program Size:"), constraints);
        
        insets.set(SPACE_BETWEEN_RELATIVE_COMPONENT, 
                SPACE_HORIZONTAL_SEPARATION_ELEMENTS, 0, 0);
        constraints = getGridBagConstraints(GridBagConstraints.WEST, 
                GridBagConstraints.NONE, 1, 0, insets, 1.0, 1.0);
        leftPanel.add(testSizeField, constraints);
        
        insets.set(SPACE_BETWEEN_DIFFERENT_COMPONENT, 
                SPACE_BETWEEN_RELATIVE_COMPONENT, 0, 0);
        constraints = getGridBagConstraints(GridBagConstraints.WEST, 
                GridBagConstraints.NONE, 1, 2, 0, 1, insets, 0.0, 0.0);
        leftPanel.add(selfCheck, constraints);
        
        JPanel rightPanel = new JPanel(new GridBagLayout());
        
        addBlockToGrigBag(rightPanel, 0, "Test Program Prefix:", 
                testProgramPrefixButton, SPACE_FROM_BORDER, 0);
        addBlockToGrigBag(rightPanel, 1, "Test Program Suffix:",
                testProgramSuffixButton, SPACE_BETWEEN_RELATIVE_COMPONENT, 0);
        addBlockToGrigBag(rightPanel, 2, "Test Action Prefix:", 
                testActionPrefixButton, SPACE_BETWEEN_GROUP, 0);
        addBlockToGrigBag(rightPanel, 3, "Test Action Suffix:", 
                testActionSuffixButton, SPACE_BETWEEN_RELATIVE_COMPONENT,
                SPACE_FROM_BORDER);
        
        createTabPanel(" ", leftPanel, 
                "Test Prefix and Suffix", rightPanel);
    }
    
    protected class ShowPrefixSuffixAction implements ActionListener
    {
        protected Program targetProcProgram;
        protected Program controlProcProgram;
        
        protected String dialogName;
        
        public ShowPrefixSuffixAction(Program targetProcProgram,
                Program controlProcProgram, String dialogName)
        {
            this.targetProcProgram  = targetProcProgram;
            this.controlProcProgram = controlProcProgram;
            
            this.dialogName = dialogName;
        }
        
        public void actionPerformed(ActionEvent event)
        {
            PrefixSuffixDialog dialog = new PrefixSuffixDialog(frame, 
                    targetProcProgram, controlProcProgram, dialogName);
            
            dialog.setVisible(true);
        }
    }
    
    /**
     * Saves data in General Options tab.
     */
    public void saveGeneralOptionsTab()
    {
        frame.getConfig().setTestSize(testSizeField.getIntValue());
        frame.getConfig().setSelfCheck(selfCheck.isSelected());
    }
}
