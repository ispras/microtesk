/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SettingsDialog.java,v 1.59 2009/01/30 09:29:04 kozlov Exp $
 */

package com.unitesk.testfusion.gui.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.zip.Deflater;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import com.unitesk.testfusion.core.config.SettingsConfig;
import com.unitesk.testfusion.gui.Console;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.GUISettings;
import com.unitesk.testfusion.gui.HistoryManager;
import com.unitesk.testfusion.gui.textfield.NonEmptyIntTextField;
import com.unitesk.testfusion.gui.textfield.NonEmptyTextField;

import com.unitesk.testfusion.core.util.Utils;

import static com.unitesk.testfusion.gui.Layout.*;

/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class SettingsDialog extends NonEmptyTextFieldDialog 
{
    public static final long serialVersionUID = 0;
 
    public static final int FAST_METOD_COMPRESS    = 0;
    public static final int NORMAL_METOD_COMPRESS  = 1;
    public static final int MAXIMUM_METOD_COMPRESS = 2;
    
    protected JPanel generationSettings;
    protected JPanel guiSettingsPanel;
    protected JPanel archiveSettingsPanel;
    
    protected NonEmptyTextField testProgramName;
    protected NonEmptyTextField outputDir;
    protected JCheckBox defaultCheckBox; 
    protected JCheckBox fullNameCheckBox; 
    protected JComboBox testNameGranularity; 

    protected JCheckBox compressCheckBox; 
    protected JComboBox formatCompress; 
    protected JComboBox methodCompress; 

    protected NonEmptyIntTextField consoleBuffer;
    protected NonEmptyIntTextField historySize;
    
    protected GUISettings guiSettings;
    
    protected SettingsConfig settingConfig;
    
    protected JButton outDirButton;
    
    protected JFileChooser fileChooser;
    
    protected static final int COLUMN_WIDHT = 4;
    
    public SettingsDialog(final GUI frame)
    {
        super(frame);
        
        final SettingsDialog dialog = this;

        setTitle(GUI.APPLICATION_NAME + " - Settings");
        setModal(true);
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        
        setLocation();
        
        guiSettings = frame.getSettings();
        
        settingConfig = frame.getConfig().getSettings();

        // tabPanel
        JTabbedPane tabPanel = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        
        generationSettings = createGenerationSettingPanel();
        
        guiSettingsPanel = createGUISettingPanel();
        
        archiveSettingsPanel = createArchiveSettingPanel();

        tabPanel.addTab("Generator Setting", generationSettings);
        tabPanel.addTab("GUI Setting", guiSettingsPanel);
        tabPanel.addTab("Archiving Settings", archiveSettingsPanel);
        // end tabPanel
        
        ActionListener okListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            	if (!SettingsConfig.isCorrectTestName(testProgramName.getText()))
            	{
            		frame.showWarningMessage("Incorrect test name: test name contains illegal characters", "Error");
            	}
            	else if (!Utils.isCorrectFilePath(outputDir.getText()))
            	{
            		frame.showWarningMessage("Incorrect dirictory name: directory name contains illegal characters", "Error");
            	}
            	// all inputed data are correct
            	else
            	{
            		guiSettings.setConsoleSize(consoleBuffer.getIntValue());
                    
                    // Change size of the console's buffer
                    Console console = frame.getConsole();
                    console.setMaxSize(consoleBuffer.getIntValue() != 0 ? consoleBuffer.getIntValue() : guiSettings.getConsoleSize());
                    
                    guiSettings.setHistorySize(historySize.getIntValue());

                    // Change size of the history
                    HistoryManager history = frame.getHistory();
                    history.setMaxSize(historySize.getIntValue() != 0 ? historySize.getIntValue() : guiSettings.getHistorySize());

                    // Update state of the forward and back buttons
                    frame.enableBackAction(!history.isFirst());
                    frame.enableForwardAction(!history.isLast());
                    
                    if(defaultCheckBox.isSelected())
                    {
                        settingConfig.setTestNameStrategy(testNameGranularity.getSelectedIndex() + 1);
                    }
                    else
                    {
                        settingConfig.setTestNameStrategy(0);
                    }
                    
                    settingConfig.setCompressionMethod(getDeflater(methodCompress.getSelectedIndex()));
                    settingConfig.setCompress(compressCheckBox.isSelected());
                    
                    settingConfig.setTestName(testProgramName.getText());
                    settingConfig.setFullName(fullNameCheckBox.isSelected());
                    settingConfig.setOutputDirectory(outputDir.getText());
                    
                    okPressed = true;
                    
                    dialog.dispose();
            	}
            }
        };
        
        ActionListener cancelListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                dialog.dispose();
            }
        };
        
        add(createDialogMainPanel(tabPanel, okListener, cancelListener));
        
        this.okButton.setPreferredSize(outDirButton.getPreferredSize());
        this.cancelButton.setPreferredSize(outDirButton.getPreferredSize());
    }
    
    protected int getIndexMetodCompress(int metod)
    {
        switch (metod) 
        {
            case Deflater.BEST_SPEED:          return FAST_METOD_COMPRESS;
            case Deflater.DEFAULT_COMPRESSION: return NORMAL_METOD_COMPRESS;
            case Deflater.BEST_COMPRESSION:    return MAXIMUM_METOD_COMPRESS;
            default: return NORMAL_METOD_COMPRESS;
        }
    }
    
    protected int getDeflater(int metod)
    {
        switch (metod) 
        {
            case FAST_METOD_COMPRESS:    return Deflater.BEST_SPEED;
            case NORMAL_METOD_COMPRESS:  return Deflater.DEFAULT_COMPRESSION;
            case MAXIMUM_METOD_COMPRESS: return Deflater.BEST_COMPRESSION;
            default: return Deflater.DEFAULT_COMPRESSION;
        }
    }
    
    protected JPanel createArchiveSettingPanel()
    {
        JPanel archiveSettingPanel = new JPanel();

        GridBagLayout gridBagLayout = new GridBagLayout();
        archiveSettingPanel.setLayout(gridBagLayout);
        
        // compress Checkbox
        compressCheckBox = new JCheckBox("Compress Test Programs");

        GridBagConstraints constraints =  getGridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 0, 0, new Insets(SPACE_FROM_BORDER, SPACE_FROM_BORDER, SPACE_BETWEEN_RELATIVE_COMPONENT, 0), 0.0, 0.0);
        gridBagLayout.setConstraints(compressCheckBox, constraints);
        archiveSettingPanel.add(compressCheckBox);
        // end compress Checkbox
        
        // compressPanel
        JPanel compressPanel = createCompressPanel();
        
        constraints =  getGridBagConstraints(GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, GridBagConstraints.REMAINDER, 0, 1, new Insets(0, SPACE_FROM_BORDER, SPACE_BETWEEN_DIFFERENT_COMPONENT, SPACE_FROM_BORDER), 0.0, 0.0);
        gridBagLayout.setConstraints(compressPanel, constraints);
        archiveSettingPanel.add(compressPanel);
        // end compressPanel

        //do
        ItemListener compressOnListner = new ItemListener()
        {
            public void itemStateChanged(ItemEvent arg0)
            {
                if(compressCheckBox.isSelected())
                {
                    setEnabledArchiving(true);
                }
                else
                {
                    setEnabledArchiving(false);
                }
            };
        };       

        compressCheckBox.addItemListener(compressOnListner);
        //end do

        // empty
        JLabel emptyLabel = new JLabel("");
        
        constraints =  getGridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 0, 2, new Insets(0, SPACE_HORIZONTAL_SEPARATION_ELEMENTS, SPACE_BETWEEN_DIFFERENT_COMPONENT, SPACE_FROM_BORDER), 1.0, 1.0);
        gridBagLayout.setConstraints(emptyLabel, constraints);
        archiveSettingPanel.add(emptyLabel);
        // end empty

        if(settingConfig.isCompress())
        {
            compressCheckBox.setSelected(true);
            setEnabledArchiving(true);
        }
        else
        {
            compressCheckBox.setSelected(false);
            setEnabledArchiving(false);
        }

        return archiveSettingPanel;
    }
    
    protected void setEnabledArchiving(Boolean value)
    {
        formatCompress.setEnabled(value);
        methodCompress.setEnabled(value);
    }
    
    protected JPanel createCompressPanel()
    {
        JPanel compressPanel = new JPanel();
        GridBagLayout gridBagLayout = new GridBagLayout();
        compressPanel.setLayout(gridBagLayout);

        compressPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Archiving Format and Method"), 
                BorderFactory.createEmptyBorder(0,0,0,0)));        
        
        // formatCompressLabel
        JLabel formatCompressLabel = new JLabel("Archiving Format:");
        
        GridBagConstraints constraints =  getGridBagConstraints(GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 0, new Insets(0, SPACE_FROM_BORDER, SPACE_BETWEEN_DIFFERENT_COMPONENT, SPACE_HORIZONTAL_SEPARATION_ELEMENTS), 0.0, 0.0);
        gridBagLayout.setConstraints(formatCompressLabel, constraints);
        compressPanel.add(formatCompressLabel);
        // end formatCompressLabel
        
        // formatCompress
        formatCompress = new JComboBox();
        
        formatCompress.addItem("ZIP");
       // formatCompress.addItem("TAR");
        
        formatCompress.setSelectedItem("ZIP");
        
        constraints =  getGridBagConstraints(GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, GridBagConstraints.RELATIVE, 0, new Insets(0, 0, SPACE_BETWEEN_DIFFERENT_COMPONENT, 0), 0.0, 0.0);
        gridBagLayout.setConstraints(formatCompress, constraints);
        compressPanel.add(formatCompress);
        // end formatCompress
        
        // methodCompressLabel
        JLabel methodCompressLabel = new JLabel("Archiving Method:");
        
        constraints =  getGridBagConstraints(GridBagConstraints.WEST, GridBagConstraints.NONE, 0, 1, new Insets(0, SPACE_FROM_BORDER, SPACE_BETWEEN_DIFFERENT_COMPONENT, SPACE_HORIZONTAL_SEPARATION_ELEMENTS), 0.0, 0.0);
        gridBagLayout.setConstraints(methodCompressLabel, constraints);
        compressPanel.add(methodCompressLabel);
        // end methodCompressLabel

        // methodCompress
        methodCompress = new JComboBox();
        
        methodCompress.addItem("Fast");
        methodCompress.addItem("Normal");
        methodCompress.addItem("Maximum");
        
        methodCompress.setSelectedIndex(getIndexMetodCompress(settingConfig.getCompressionMethod()));
        
        constraints =  getGridBagConstraints(GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, GridBagConstraints.RELATIVE, 1, new Insets(0, 0, SPACE_BETWEEN_DIFFERENT_COMPONENT, 0), 0.0, 0.0);
        gridBagLayout.setConstraints(methodCompress, constraints);
        compressPanel.add(methodCompress);
        // end methodCompress
        
        // empty
        JLabel emptyLabel = new JLabel("");
        
        constraints =  getGridBagConstraints(GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, GridBagConstraints.RELATIVE, 1, new Insets(0, SPACE_FROM_BORDER, SPACE_BETWEEN_DIFFERENT_COMPONENT, SPACE_HORIZONTAL_SEPARATION_ELEMENTS), 1.0, 0.0);
        gridBagLayout.setConstraints(emptyLabel, constraints);
        compressPanel.add(emptyLabel);
        // end methodCompressLabel
       
        return compressPanel;
    }

    protected JPanel createGUISettingPanel()
    {
        JPanel panel = new JPanel();

        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);

        // consoleBufferLabel
        JLabel consoleBufferLabel = new JLabel("Console Buffer:");
        
        GridBagConstraints constraints =  getGridBagConstraints(GridBagConstraints.NORTHEAST, GridBagConstraints.VERTICAL, 0, 0, new Insets(SPACE_FROM_BORDER, SPACE_FROM_BORDER, SPACE_BETWEEN_DIFFERENT_COMPONENT, 0), 0.0, 0.0);
        gridBagLayout.setConstraints(consoleBufferLabel, constraints);
        panel.add(consoleBufferLabel);
        // end consoleBufferLabel

        // consoleBuffer
        constraints =  getGridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.VERTICAL, 1, 0, new Insets(SPACE_FROM_BORDER, SPACE_HORIZONTAL_SEPARATION_ELEMENTS, SPACE_BETWEEN_DIFFERENT_COMPONENT, SPACE_FROM_BORDER), 1.0, 0.0);

        consoleBuffer = new NonEmptyIntTextField(guiSettings.getConsoleSize(), 
                DEFAULT_TEXT_FIELD_SIZE, 0, GUISettings.MAX_CONSOLE_SIZE);
        consoleBuffer.addEmptyTextFieldListener(this);
        
        gridBagLayout.setConstraints(consoleBuffer, constraints);
        panel.add(consoleBuffer);
        // end consoleBuffer
        
        // historySizeLabel
        JLabel historySizeLabel = new JLabel("History Size:");
        
        constraints =  getGridBagConstraints(GridBagConstraints.NORTHEAST, GridBagConstraints.VERTICAL, 0, 1, new Insets(0, SPACE_FROM_BORDER, SPACE_BETWEEN_DIFFERENT_COMPONENT, 0), 0.0, 0.0);
        gridBagLayout.setConstraints(historySizeLabel, constraints);
        panel.add(historySizeLabel);
        // end historySizeLabel

        // historySize
        historySize = new NonEmptyIntTextField(guiSettings.getHistorySize(), 
                DEFAULT_TEXT_FIELD_SIZE, 0, GUISettings.MAX_HISTORY_SIZE);
        historySize.addEmptyTextFieldListener(this);
        
        constraints =  getGridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.VERTICAL, 1, 1, new Insets(0, SPACE_HORIZONTAL_SEPARATION_ELEMENTS, SPACE_BETWEEN_DIFFERENT_COMPONENT, SPACE_FROM_BORDER), 1.0, 0.0);
        gridBagLayout.setConstraints(historySize, constraints);
        panel.add(historySize);
        // end historySize

        // empty
        JLabel emptyLabel = new JLabel("");
        
        constraints =  getGridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, GridBagConstraints.REMAINDER, GridBagConstraints.REMAINDER, 0, 2, new Insets(0, SPACE_HORIZONTAL_SEPARATION_ELEMENTS, SPACE_BETWEEN_DIFFERENT_COMPONENT, SPACE_FROM_BORDER), 1.0, 1.0);
        gridBagLayout.setConstraints(emptyLabel, constraints);
        panel.add(emptyLabel);
        // end empty

        return panel;
    }
    
    protected JPanel createGenerationSettingPanel()
    {
        final JPanel generationSetting = new JPanel();
        GridBagLayout gridBagLayout = new GridBagLayout();
        generationSetting.setLayout(gridBagLayout);
        
        // test name insetsabel
        JLabel testNameLabel = new JLabel("Test Program Name:");
        
        GridBagConstraints constraints =  getGridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.VERTICAL, 0, 0, new Insets(SPACE_FROM_BORDER, SPACE_FROM_BORDER, SPACE_BETWEEN_DIFFERENT_COMPONENT, 0), 0.0, 0.0);
        gridBagLayout.setConstraints(testNameLabel, constraints);
        generationSetting.add(testNameLabel);
        // end test name label

        // test name label
        testProgramName = new NonEmptyTextField(settingConfig.getTestName(), COLUMN_WIDHT);
        testProgramName.addEmptyTextFieldListener(this);
        testProgramName.setColumns(COLUMN_WIDHT);
        
        constraints =  getGridBagConstraints(GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1, 0, new Insets(SPACE_FROM_BORDER, SPACE_HORIZONTAL_SEPARATION_ELEMENTS, SPACE_BETWEEN_DIFFERENT_COMPONENT, SPACE_FROM_BORDER), 0.0, 0.0);
        gridBagLayout.setConstraints(testProgramName, constraints);
        generationSetting.add(testProgramName);
        // end test name label

        // default Checkbox
        defaultCheckBox = new JCheckBox("Generate Test Program Name Automatically");
        
        constraints =  getGridBagConstraints(GridBagConstraints.WEST, GridBagConstraints.NONE, 1, 2, 0, 1, new Insets(0, SPACE_FROM_BORDER, SPACE_BETWEEN_RELATIVE_COMPONENT, SPACE_FROM_BORDER), 0.0, 0.0);
        gridBagLayout.setConstraints(defaultCheckBox, constraints);
        generationSetting.add(defaultCheckBox);
        // end default Checkbox

        // default panel
        final JPanel defaultPanel = createDefaultPanel();
        
        constraints =  getGridBagConstraints(GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1, GridBagConstraints.REMAINDER, 0, 2, new Insets(0, SPACE_FROM_BORDER, SPACE_BETWEEN_DIFFERENT_COMPONENT, SPACE_FROM_BORDER), 0.0, 0.0);
        gridBagLayout.setConstraints(defaultPanel, constraints);
        generationSetting.add(defaultPanel);
        // end default panel

        if(settingConfig.getTestNameStrategy() == 0)
        {
            defaultCheckBox.setSelected(false);
            setEnabledDefault(false);
        }
        else
        {
            defaultCheckBox.setSelected(true);
            setEnabledDefault(true);
            testNameGranularity.setSelectedIndex(settingConfig.getTestNameStrategy() - 1);
        }
        
        // output directory
        JLabel directoryLabel = new JLabel("Output Directory:");
        
        constraints =  getGridBagConstraints(GridBagConstraints.NORTHEAST, GridBagConstraints.VERTICAL, 0, 3, new Insets(0, SPACE_FROM_BORDER, SPACE_BETWEEN_DIFFERENT_COMPONENT, 0), 0.0, 0.0);
        gridBagLayout.setConstraints(directoryLabel, constraints);
        generationSetting.add(directoryLabel);
        
        outputDir = new NonEmptyTextField(settingConfig.getOutputDirectory(), COLUMN_WIDHT);
        outputDir.addEmptyTextFieldListener(this);
        outputDir.setColumns(COLUMN_WIDHT);

        constraints =  getGridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, 1, 3, new Insets(0, SPACE_HORIZONTAL_SEPARATION_ELEMENTS, SPACE_BETWEEN_DIFFERENT_COMPONENT, 0), 1.0, 0.0);
        gridBagLayout.setConstraints(outputDir, constraints);
        generationSetting.add(outputDir);
        
        outDirButton = new JButton("Browse");
        
        outDirButton.addActionListener
        (
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                { 
                    fileChooser = new JFileChooser(outputDir.getText());
                    
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                    fileChooser.setFileHidingEnabled(true); // enabled hiding
                    
                    fileChooser.setDialogTitle(GUI.APPLICATION_NAME + " – Browse");
                    
                    int returnValue = fileChooser.showDialog(generationSetting, "OK");
                    
                    if (returnValue == JFileChooser.APPROVE_OPTION) 
                    {
                        File file = fileChooser.getSelectedFile();

                        outputDir.setText(file.getPath());
                    } 
                    else 
                    {
                        // nothing
                    }
                }
            }
        );
        
        constraints =  getGridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 2, 3, new Insets(0, SPACE_HORIZONTAL_SEPARATION_ELEMENTS, SPACE_BETWEEN_DIFFERENT_COMPONENT, SPACE_FROM_BORDER), 0.0, 0.0);
        gridBagLayout.setConstraints(outDirButton, constraints);
        generationSetting.add(outDirButton);
        // end output directory
        
        // empty
        JLabel emptyLabel = new JLabel("");
        
        constraints =  getGridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 1, GridBagConstraints.REMAINDER, 0, 4, new Insets(0, SPACE_HORIZONTAL_SEPARATION_ELEMENTS, SPACE_BETWEEN_DIFFERENT_COMPONENT, SPACE_FROM_BORDER), 1.0, 1.0);
        gridBagLayout.setConstraints(emptyLabel, constraints);
        generationSetting.add(emptyLabel);
        // end empty

        //do
        ItemListener defaultOnListner = new ItemListener()
        {
            public void itemStateChanged(ItemEvent arg0)
            {
                if(defaultCheckBox.isSelected())
                {
                    setEnabledDefault(true);
                }
                else
                {
                    setEnabledDefault(false);
                }
            };
        };       

        defaultCheckBox.addItemListener(defaultOnListner);
        //end do
        
        return generationSetting;
    }
    
    protected void setEnabledDefault(Boolean value)
    {
        testProgramName.setEnabled(!value);
        testNameGranularity.setEnabled(value);
        fullNameCheckBox.setEnabled(value);
    }
    
    protected JPanel createDefaultPanel()
    {
        JPanel defaultPanel = new JPanel();
        GridBagLayout gridBagLayout = new GridBagLayout();
        defaultPanel.setLayout(gridBagLayout);

        defaultPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Generation Strategy"), 
                BorderFactory.createEmptyBorder(0,0,0,0)));        
        
        // fullNameCheckBox
        fullNameCheckBox = new JCheckBox("Full Name");
        
        fullNameCheckBox.setSelected(settingConfig.isFullName());
        
        Insets insets = new Insets(0, SPACE_FROM_BORDER, 0, SPACE_FROM_BORDER);
        GridBagConstraints constraints = getGridBagConstraints(GridBagConstraints.WEST, GridBagConstraints.BOTH, 0, 0, insets, 1.0, 0.0);
        gridBagLayout.setConstraints(fullNameCheckBox, constraints);
        defaultPanel.add(fullNameCheckBox);
        // end fullNamehCheckBox
        
        // testNameGranularity
        testNameGranularity = new JComboBox();
        
        testNameGranularity.addItem("Situation");
        testNameGranularity.addItem("Instruction");
        testNameGranularity.addItem("Group");
        testNameGranularity.addItem("Processor");
        
        testNameGranularity.setSelectedItem("Situation");
        testNameGranularity.setEnabled(false);
        
        insets = new Insets(SPACE_BETWEEN_DIFFERENT_COMPONENT, SPACE_FROM_BORDER, SPACE_FROM_BORDER, SPACE_FROM_BORDER); 
        constraints =  getGridBagConstraints(GridBagConstraints.WEST, GridBagConstraints.BOTH, 0, 1, insets, 1.0, 0.0);
        gridBagLayout.setConstraints(testNameGranularity, constraints);
        defaultPanel.add(testNameGranularity);
        // end testNameGranularity
        
        return defaultPanel;
    }
}
