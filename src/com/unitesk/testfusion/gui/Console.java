/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: Console.java,v 1.13 2009/08/13 15:54:25 kamkin Exp $
 */

package com.unitesk.testfusion.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Console extends JPanel
{
    public static final long serialVersionUID = 0;

    protected JLabel header;
    protected JTextArea console;
    
    protected class ConsoleOutputStream extends OutputStream 
    {
        protected int maxSize  = -1;
        protected int position = 0;
        
        public ConsoleOutputStream() {}

        public ConsoleOutputStream(int maxSize)
        {
            this.maxSize = maxSize;
        }

        public int getMaxSize()
        {
            return maxSize;
        }

        protected void deleteLine()
        {
            try
            {
                int start = console.getLineStartOffset(0);
                int end   = console.getLineEndOffset(0);
                
                console.replaceRange("", start, end);

                position -= (end - start);
            }
            catch(Exception exception) {}
        }
        
        protected void deleteAllLines()
        {
            try
            {
                int start = console.getLineStartOffset(0);
                int end   = console.getLineEndOffset(console.getLineCount() - 1);
                
                console.replaceRange("", start, end);
                
                position = 0;
            }
            catch(Exception exception) {}
        }
        
        public void setMaxSize(int maxSize)
        {
            this.maxSize = maxSize;
            
            if(maxSize == -1)
                { return; }
            
            if(maxSize == 0)
                { deleteAllLines(); return; }
            
            while(console.getLineCount() > maxSize)
                { deleteLine(); }
        }
        
        public void write(int b) throws IOException 
        {
            if(maxSize == 0)
                { return; }
            
            console.append(String.valueOf((char)b));

            if(console.getLineCount() > maxSize)
                { deleteLine(); }
            
            console.setCaretPosition(++position);
        }   
    }    
    
    protected ConsoleOutputStream stream;
    
    public Console(final GUI frame)
    {
        super(new BorderLayout());

        GUISettings settings = frame.getSettings();
        
        header = new JLabel(" Generator Console");
        header.setBorder(BorderFactory.createEtchedBorder());
        add(header, BorderLayout.PAGE_START);
        
        console = new JTextArea();
        console.setFont(new Font("Courier New", 0, 12));
        console.setBackground(Color.WHITE);
        console.setForeground(Color.DARK_GRAY);
        console.setEditable(false);
        console.setBorder(BorderFactory.createEmptyBorder());
        add(new JScrollPane(console));

        setBorder(BorderFactory.createEmptyBorder());

        stream = new ConsoleOutputStream(settings.getConsoleSize());
        
        PrintStream out = new PrintStream(stream);

        System.setOut(out);
        System.setErr(out);
    }
    
    public int getMaxSize()
    {
        return stream.getMaxSize();
    }
    
    public void setMaxSize(int maxSize)
    {
        stream.setMaxSize(maxSize);
    }
    
    public void clearConsole()
    {
    	stream.deleteAllLines();
    }
}
