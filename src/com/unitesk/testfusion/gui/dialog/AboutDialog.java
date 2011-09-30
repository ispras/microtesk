/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: AboutDialog.java,v 1.24 2009/07/08 08:26:19 kamkin Exp $
 */

package com.unitesk.testfusion.gui.dialog;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import com.unitesk.testfusion.gui.GUI;

import static com.unitesk.testfusion.gui.Layout.*; 

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class AboutDialog extends Dialog
{
    public static final long serialVersionUID = 0;

    protected JLabel logo;
    protected JTextArea title;
    protected JTextArea text;
    
    public AboutDialog(GUI frame)
    {
        super(frame);
        
        final AboutDialog dialog = this;
        
        ClassLoader loader = getClass().getClassLoader(); 
        
        setTitle(GUI.APPLICATION_NAME + " - About");
        setModal(true);
        setSize(WIDTH, HEIGHT);
        
        JPanel panel = new JPanel();

        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);

        logo = new JLabel();
        logo.setIcon(new ImageIcon(loader.getResource("img/ispras.gif")));
        
        GridBagConstraints constraints = getGridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 0, 0, new Insets(0, 0, 0, 0), 0.0, 0.0);
        gridBagLayout.setConstraints(logo, constraints);
        panel.add(logo);
        
        title = new JTextArea();
        title.append("\n" + GUI.APPLICATION_NAME + "\n");
        title.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 16));
        title.setEditable(false);
        title.setBackground(getBackground());
        
        constraints = getGridBagConstraints(GridBagConstraints.NORTH, GridBagConstraints.NONE, 0, 0, new Insets(0, SPACE_BETWEEN_RELATIVE_COMPONENT, 0, 0), 0.0, 0.0);
        gridBagLayout.setConstraints(title, constraints);
        panel.add(title);

        JLabel separator = new JLabel();
        separator.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        constraints = getGridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, 0, 1, new Insets(0, 0, 0, 0), 0.0, 1.0);
        gridBagLayout.setConstraints(separator, constraints);
        panel.add(separator);
        
        text = new JTextArea();
        text.append("Combinatorial coverage-directed test program generator for microprocessors.\n\n");
        text.append("Copyright (c) 2007-2009 Institute for System Programming of RAS\n");
        text.append("25, A. Solzhenitsyn Street, Moscow, 109004, Russia\n");
        text.append("All rights reserved.\n\n");
        text.append("This product includes SoftFloat IEC/IEEE floating-point arithmetic package.\n");
        text.append("Visit http://www.cs.berkeley.edu/~jhauser/arithmetic/SoftFloat.html.\n\n");
        text.append("It also integrates TeSLa library for test situation description and construction.\n");
        text.append("Visit http://tesla-project.googlecode.com.\n");
        text.setFont(new Font("Arial", 0, 12));
        text.setEditable(false);
        text.setBorder(new TitledBorder(""));

        constraints = getGridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, 0, 2, new Insets(0, 0, 0, 0), 0.0, 1.0);
        gridBagLayout.setConstraints(text, constraints);
        panel.add(text);
        
        separator = new JLabel();
        separator.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        constraints = getGridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, 0, 3, new Insets(0, 0, 0, 0), 0.0, 1.0);
        gridBagLayout.setConstraints(separator, constraints);
        panel.add(separator);
        
        ActionListener okListener = new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    okPressed = true;
                    dialog.dispose();
                }
                    
            };
        
        setLocation();
        
        add(createDialogMainPanel(panel, okListener));
    }
}
