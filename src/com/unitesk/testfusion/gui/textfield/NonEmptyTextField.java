package com.unitesk.testfusion.gui.textfield;

import java.util.ArrayList;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import com.unitesk.testfusion.gui.event.EmptyTextFieldEvent;
import com.unitesk.testfusion.gui.event.EmptyTextFieldListener;


/**
 * Class for a text field, which reports to its listeners about changing
 * state from empty to non-empty and vice versa.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class NonEmptyTextField extends JTextField
{
    private static final long serialVersionUID = 0;
    
    private ArrayList<EmptyTextFieldListener> listeners = 
        new ArrayList<EmptyTextFieldListener>();
    
    private EmptyTextFieldEvent event = new EmptyTextFieldEvent(this);  
    
    public NonEmptyTextField(Document doc, String defval, int size) 
    {
        super(doc, defval, size);
        getDocument().addDocumentListener(new NonEmptyDocumentListener());
    }
    
    public NonEmptyTextField(String defval, int size) 
    {
        super(defval, size);
        getDocument().addDocumentListener(new NonEmptyDocumentListener());
    }
    
    public NonEmptyTextField(int size) 
    {
        super(size);
        getDocument().addDocumentListener(new NonEmptyDocumentListener());
    }
    
    public void addEmptyTextFieldListener(EmptyTextFieldListener l)
    {
        listeners.add(l);
    }
    
    public void removeEmptyTextFieldListener(EmptyTextFieldListener l)
    {
        listeners.remove(l);
    }
    
    protected void fireEmptyTextField()
    {
        for (EmptyTextFieldListener listener : listeners)
            { listener.fieldEmptied(event); }
    }
    
    protected void fireNonEmptyTextField()
    {
        for (EmptyTextFieldListener listener : listeners)
            { listener.fieldFulled(event); }
    }
    
    protected class NonEmptyDocumentListener implements DocumentListener
    {
        public void removeUpdate(DocumentEvent e)
        {
            String newText = getText(e);
            if (newText != null)
            {
                if (newText.equals(""))
                    { fireEmptyTextField(); } 
            }
        }
        
        public void insertUpdate(DocumentEvent e)
        {
            String newText = getText(e);
            // get old text before insert update
            String oldText = newText.substring(0, e.getOffset()) + newText.substring(e.getOffset() + e.getLength());
            if (newText != null)
            {
                if (oldText.equals(""))
                    { fireNonEmptyTextField(); }
            }
        }
        
        public void changedUpdate(DocumentEvent e) {}
        
        protected String getText(DocumentEvent e)
        {
            try
            {
                Document doc = e.getDocument();
                return doc.getText(0, doc.getLength());
            }
            catch (BadLocationException ex) 
            {
                ex.printStackTrace();
                return null;
            }
        }
    }
}
