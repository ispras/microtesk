/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: DocAction.java,v 1.7 2009/07/08 08:26:16 kamkin Exp $
 */

package com.unitesk.testfusion.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import javax.swing.JOptionPane;

import com.unitesk.testfusion.gui.GUI;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class DocAction implements ActionListener
{
    public static final String documentationURL = "http://www.google.ru/search?hl=ru&q=%22MicroTESK%22&lr=&aq=f";
    
    public DocAction(GUI frame) {}
    
    @SuppressWarnings("unchecked")
    public static void openURL(String url)
        throws Exception
    {
        String osName = System.getProperty("os.name");
        
        if(osName.startsWith("Mac OS"))
        {
          Class fileManager = Class.forName("com.apple.eio.FileManager");
      
          Method openURL = fileManager.getDeclaredMethod("openURL", new Class[] { String.class });
              openURL.invoke(null, new Object[] { url });
        }
        else if(osName.startsWith("Windows"))
        {
            Runtime.getRuntime().exec("rundll32 url.dll, FileProtocolHandler " + url);
        }
        else
        {
            String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
            String browser = null;
      
            for(int count = 0; count < browsers.length && browser == null; count++)
            {
                if(Runtime.getRuntime().exec(new String[] {"which", browsers[count]}).waitFor() == 0)
                    { browser = browsers[count]; }
            }
         
            if(browser == null)
                { throw new Exception("Could not find web browser"); }
            else
                { Runtime.getRuntime().exec(new String[] { browser, url }); }
        }
    }
    
    public void actionPerformed(ActionEvent event)
    {
        try
        {
            openURL(documentationURL);
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, "Could find documentation file: " + documentationURL, "Error", JOptionPane.WARNING_MESSAGE);            
        }
    }
}
