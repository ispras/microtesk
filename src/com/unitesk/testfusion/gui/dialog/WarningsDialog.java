/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: WarningsDialog.java,v 1.11 2008/11/12 09:15:20 kozlov Exp $
 */

package com.unitesk.testfusion.gui.dialog;

import java.util.Vector;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import com.unitesk.testfusion.gui.GUI;

import static com.unitesk.testfusion.gui.Layout.*;

/**
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class WarningsDialog extends Dialog {
	
	public static final long serialVersionUID = 0;
	
	protected static final int DEFAULT_WIDTH  = 420;
	protected static final int DEFAULT_HEIGHT = 130;
	
	protected static final int DEFAULT_EXTRA_HEIGHT = 100;
	
	/* vector with warnings messages */
	protected Vector<String> warnings;
	
	protected boolean isDetailsVisible;
	
	protected JPanel mainPanel;
	
	protected JButton moreButton;
	
	protected JScrollPane listPanel;
	
	protected JLabel separatorLabel;
	
	public WarningsDialog(GUI frame, Vector<String> warnings)
	{
		super (frame);
		
		isDetailsVisible = false;
		this.warnings = warnings;
		
		final WarningsDialog dialog = this;
		
		setTitle(GUI.APPLICATION_NAME + " - Config Parser Warnings");
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setMinimumSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        setModal(true);
        setLocation();
					
		ActionListener okListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                okPressed = true;
                dialog.dispose();
            }
        };
        
        ActionListener moreListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            	GridBagLayout gridBagLayout = (GridBagLayout)mainPanel.getLayout();
        		GridBagConstraints constraints = gridBagLayout.getConstraints(separatorLabel);
        		
            	if (isDetailsVisible)
            	{
            		isDetailsVisible = false;
            		moreButton.setText("More >>");
            		listPanel.setVisible(false);
            		
            		/* Set previous constraints for separatorLabel */
            		     
            		constraints.insets = new Insets(0, 0, 0, 0);
            		constraints.weighty = 1.0;
            		gridBagLayout.setConstraints(separatorLabel, constraints);
            		
            		/* calculate height of dialog's window after decreasing */
            		
            		// get height of row with warnings list
            		int height = gridBagLayout.getLayoutDimensions()[1][3];
            		
            		dialog.setSize(getWidth(), getHeight() - height);
            	}
            	else
            	{
            		/* 
                     * Actions for make OK and More buttons are immovable, 
                     * when dialog's window are increasing 
                     * */ 
            		
            		// get height of row with separator label
            		int height = gridBagLayout.getLayoutDimensions()[1][1];
            		
            		constraints.insets = new Insets(height, 0, 0, 0);
            		constraints.weighty = 0.0;
            		gridBagLayout.setConstraints(separatorLabel, constraints);
            		
            		isDetailsVisible = true;
            		moreButton.setText("<< More");
            		listPanel.setVisible(true);
            		dialog.setSize(getWidth(), getHeight() + DEFAULT_EXTRA_HEIGHT);
            	}
            }
        };
        
		listPanel = new JScrollPane(new JList(warnings));
		listPanel.setVisible(false);
        
		add(mainPanel = createDialogMainPanel(okListener, moreListener));
	}
	
	protected JPanel createOkMorePanel(ActionListener okButtonListener, 
            ActionListener moreButtonListener)
    {
		 JPanel panel = new JPanel();
	        panel.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
	        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
	        
	        moreButton = new JButton("More >>");
	        moreButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
	        moreButton.addActionListener(moreButtonListener);
	        
	        okButton = new JButton("OK");
	        okButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
	        okButton.setPreferredSize(new Dimension(moreButton.getPreferredSize()));
	        okButton.setMinimumSize(new Dimension(moreButton.getPreferredSize()));
	        okButton.addActionListener(okButtonListener);
	        
	        panel.add(Box.createHorizontalGlue());
	        panel.add(okButton);
	        
	        panel.add(Box.createHorizontalStrut(SPACE_BETWEEN_RELATIVE_COMPONENT));
	        panel.add(moreButton);
	        
	        return panel;
    }
	
	protected JPanel createDialogMainPanel(ActionListener okButtonListener, 
            ActionListener moreButtonListener)
    {
        JPanel panel = new JPanel();
        
        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);
        
        GridBagConstraints constraints; 
        Insets insets;

        constraints = getGridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 0, 0, 0.0, 0.0);
		panel.add(new JLabel(UIManager.getIcon("OptionPane.warningIcon")), constraints);
        
		insets = new Insets(0, SPACE_BETWEEN_DIFFERENT_COMPONENT, 0, 0);
        constraints = getGridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 1, 0, insets, 1.0, 0.0);
		panel.add(new JLabel("Some parameters or attributes are missing or have incorrect values."), constraints);
		
		insets = new Insets(0, 0, 0, 0);
        constraints = getGridBagConstraints(GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 2, 0, 1, insets, 0.0, 1.0);
		panel.add(separatorLabel = new JLabel(""), constraints);
        
        insets = new Insets(0, 0, 0, 0);
        constraints = getGridBagConstraints(GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, 1, 2, 0, 2, insets, 0.0, 0.0);
        panel.add(createOkMorePanel(okButtonListener, moreButtonListener), constraints);
        
        insets = new Insets(SPACE_FROM_OK_CANCEL, 0, 0, 0);
        constraints = getGridBagConstraints(GridBagConstraints.CENTER, GridBagConstraints.BOTH, 1, 2, 0, 3, insets, 0.0, 1.0);
        panel.add(listPanel, constraints);
        
        ActionListener actionSearch = new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) 
            {
                dispose(); 
            }
        };
        
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); 
        rootPane.registerKeyboardAction(actionSearch , stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        getRootPane().setDefaultButton(okButton);
        
        return panel;
    }
}