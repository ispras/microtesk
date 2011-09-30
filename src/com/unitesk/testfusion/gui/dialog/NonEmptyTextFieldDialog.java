package com.unitesk.testfusion.gui.dialog;

import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.event.EmptyTextFieldEvent;
import com.unitesk.testfusion.gui.event.EmptyTextFieldListener;

/**
 * Abstract class for dialog, which consist only non empty text fields.
 * If one of text fields has become empty, than OK button of the dialog
 * window become disable.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public abstract class NonEmptyTextFieldDialog extends Dialog 
        implements EmptyTextFieldListener
{
    /** Number of empty text fields on the dialog. */
    protected int emptyFields;
    
    /**
     * Constructor.
     * 
     * @param <code>frame</code> the GUI frame.
     */
    public NonEmptyTextFieldDialog(GUI frame)
    {
        super(frame);
    }
    
    public void fieldEmptied(EmptyTextFieldEvent e)
    {
        if (okButton != null)
        {
            emptyFields++;
            // first empty text field
            if (emptyFields == 1)
            {
                okButton.setEnabled(false);
            }
        }
    }
    
    public void fieldFulled(EmptyTextFieldEvent e)
    {
        if (okButton != null)
        {
            emptyFields--;
            // last emty text field
            if (emptyFields == 0)
            {
                okButton.setEnabled(true);
            }
        }
    }
}
