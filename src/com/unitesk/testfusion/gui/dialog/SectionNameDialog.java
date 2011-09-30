/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SectionNameDialog.java,v 1.7 2008/12/13 12:04:21 kozlov Exp $
 */

package com.unitesk.testfusion.gui.dialog;

import com.unitesk.testfusion.core.config.SectionConfig;
import com.unitesk.testfusion.core.config.SectionListConfig;
import com.unitesk.testfusion.gui.GUI;

/**
 * Dialog for set section's name.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class SectionNameDialog extends EnterNameDialog 
{
    private static final long serialVersionUID = 0L;
    
    /** Section for set name. */ 
    protected SectionConfig section;
    
    /** Parent config for section. */
    protected SectionListConfig parent;
    
    /** Parent frame for this dialog. */
    protected GUI frame;
    
    /**
     * Base constructor.
     * 
     * @param <code>frame</code> the parent frame.
     * 
     * @param <code>section</code> the section for set name.
     * 
     * @param <code>parent</code> the parent config for section.
     */
    public SectionNameDialog(GUI frame, SectionConfig section,
            SectionListConfig parent)
    {
        super(frame, "Section Name", "Section Name");
        
        this.frame = frame;
        this.section = section;
        this.parent = parent;
        
        setDefaultText(section.getName());
    }
    
    /**
     * Actions, which will be implemented in OK button listener.
     */
    public void actionsInOkButtonListener()
    {
        String text = textField.getText();
        
        if (text.contains("."))
        {
            frame.showWarningMessage("Incorrect symbol in the name: '.' ", 
                    "Error");
            
            setDefaultText(section.getName());
        }
        else
        {
            SectionConfig otherSection = parent.getSection(text);
            
            // check new section name for unique into parent config 
            if (otherSection == null || otherSection.equals(section))
            {
                // name is unique
                section.setName(text);
                
                okPressed = true;
                
                dispose();
            }
            else
            {
                // name is not unique
                frame.showWarningMessage("Entered section name is not unique", 
                        "Error");
                
                setDefaultText(section.getName());
            }
        }
    }
}
