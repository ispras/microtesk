/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: InputOutputUtils.java,v 1.6 2008/11/10 12:58:16 kozlov Exp $
 */

package com.unitesk.testfusion.gui.panel.table.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.unitesk.testfusion.core.config.TestSuiteConfig;
import com.unitesk.testfusion.gui.GUI;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class InputOutputUtils 
{
    public static String[] getWorkspaceTests(GUI frame)
    {
    	TestSuiteConfig testSuiteConfig = frame.getTestSuite();
    	String testSuitePath = testSuiteConfig.toString();
    	
    	// Add test configs into configuration
        File dir = new File(testSuitePath);

        // Define filename filter
        FilenameFilter filter = new FilenameFilter()
        {
            public boolean accept(File dir, String name) 
            {
                return name.endsWith("." + GUI.EXTENSION);
            }
        };
        
        String[] childrens = dir.list(filter);
 
        if(childrens != null)
        {
	        // Cut off extension
	        for(int j = 0; j < childrens.length; j++)
            {
	        	childrens[j] = childrens[j].substring(0, childrens[j].length() - 
                        (GUI.EXTENSION.length() + 1));
            }
        }
        
        return childrens;
    }
   
    public static class TestFeatures extends DefaultHandler
    {
    	public final static String extractionTag = new String("description");
    	public String tag;
    	public boolean isIntoTag = false;
    	public String buffer = new String("");
    	
        public String getTestDescription(String filename) throws SAXException, IOException
        {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(this);
            xr.setErrorHandler(this);
        
            FileReader r = new FileReader(new File(filename));
            
            xr.parse(new InputSource(r));

            return getBuffer();
        }
    	
	    // SAX calls this method when it encounters an element
	    public void startElement(String namespaceURI,
	                             String localName,
	                             String qualifiedName,
	                             Attributes att) throws SAXException 
	    {
	    	tag = qualifiedName;
	        isIntoTag = true;
	    }
	    
	    // SAX calls this method to pass in character data
	    public void characters(char ch[], int start, int length) throws SAXException 
	    {
	        if(tag.equals(extractionTag) && isIntoTag)
	        	{ buffer = buffer.concat(new String(ch, start, length)); }
	        
	    }
	    
	    // SAX call this method when it encounters an end tag
	    public void endElement(String namespaceURI,
	                           String localName,
	                           String qualifiedName) throws SAXException 
	    {
	        isIntoTag = false;
	    }
	    
	    public String getBuffer()
	    {
	    	return buffer;
	    }
    } 
}