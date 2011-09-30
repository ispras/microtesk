/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ConfigDialogs.java,v 1.29 2009/05/21 17:29:09 kamkin Exp $
 */

package com.unitesk.testfusion.gui.dialog;

import java.io.File;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.action.SaveTestAction;
import com.unitesk.testfusion.gui.parser.ConfigParser;
import com.unitesk.testfusion.gui.parser.ConfigWriter;

import com.unitesk.testfusion.core.config.TestConfig;
import com.unitesk.testfusion.core.config.TestSuiteConfig;

/**
 * @author <a href="mailto:chupilko@ispras.ru">Mikhail Chupilko</a>
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class ConfigDialogs
{
	// returned values by function openWorkspace(GUI frame)
	public static final int WORKSPACE_SELECTED     = 0;
	public static final int WORKSPACE_UNSELECTED   = 1;
	
	// returned values by function queryToReadFile(GUI frame)
	public static final int FILE_HAS_READ          = 2;
	public static final int FILE_READING_FAILURE   = 3;
	public static final int FILE_READING_CANCELED  = 4;
	
	// returned values by function queryToWriteFile(GUI frame)
	public static final int FILE_HAS_WRITTEN       = 5;
	public static final int FILE_WRITING_FAILURE   = 6;
	public static final int FILE_WRITING_CANCELED  = 7;
	
	// returned values by function queryToNewTest(GUI frame)
	public static final int TEST_HAS_CREATED       = 8;
	public static final int TEST_CREATING_CANCELED = 9;
	
    public static final String DESCRIPTION = GUI.APPLICATION_NAME + 
            " Project (" + "*." + GUI.EXTENSION + ")";
    
    public static class ExtFileFilter extends FileFilter
    {
        protected String extension;
        protected String description;
    
        ExtFileFilter(String extension, String description)
        {
            this.extension = extension;
            this.description = description;
        }
    
        public boolean accept(File file)
        {
            if(file != null)
            {
                if(file.isDirectory())
                    { return false; }
                
                String ext = getExtension(file);
                
                if(ext == null)
                    { return extension == null || extension.length() == 0; }
                
                return ext.equals(extension);
            }
                
            return false;
        }
    
        public String getExtension()
        {
            return extension;
        }
        
        public String getExtension(File file)
        {
            if(file == null)
                { return null; }
            
            String filename = file.getName();
            int i = filename.lastIndexOf('.');

            if(i > 0 && i < filename.length() - 1)
                { return filename.substring(i + 1).toLowerCase(); }
            
            return null;
        }
    
        public String getDescription()
        {
            return description;
        }
    }
    
    protected static class FileChooserDirectoryChange implements PropertyChangeListener
    {
    	protected GUI frame; 
    	
    	public FileChooserDirectoryChange(GUI frame)
    	{
    		this.frame = frame;
    	}
    	
		public void propertyChange(PropertyChangeEvent e)
		{
			((JFileChooser)e.getSource()).setCurrentDirectory(new File(frame.getTestSuite().getName()));
		}
    }
    
    /*
     * function returns FILE_HAS_READ, 
     * FILE_READING_FAILURE or FILE_READING_CANCELED
     */
    public static int queryToNewTest(GUI frame)
    {
    	if (frame.isTestHasChanges())
    	{
    		int choice = frame.showConfirmYesNoWarningDialog(
                    "Current test has been changed. " + 
                    "Do you want to save changes?", "Warning");
            
    		switch (choice)
    		{
    		    case (GUI.YES_OPTION)    : 
                {
                    int writeChoise = queryToWriteFile(frame, false);
                    
                    if (writeChoise == FILE_HAS_WRITTEN)
                    {
                        SaveTestAction action = 
                            new SaveTestAction(frame, false);
                        action.updateConfig();
                    }
                    
                    createNewTest(frame);
                    return TEST_HAS_CREATED;      
                }
                                           
    		    case (GUI.NO_OPTION)     : createNewTest(frame); 
                                           return TEST_HAS_CREATED;
                                           
    		    case (GUI.CLOSED_OPTION) : return TEST_CREATING_CANCELED;
    		}
    		// unachievable code
    		return TEST_CREATING_CANCELED;
    	}
    	else
    	{
    		createNewTest(frame);
    		return TEST_HAS_CREATED;
    	}
    }
    
    // function returns FILE_HAS_READ, FILE_READING_FAILURE or FILE_READING_CANCELED
    public static int queryToReadFile(GUI frame)
    {
    	if (frame.isTestHasChanges())
    	{
            int choice = frame.showConfirmYesNoWarningDialog(
                    "Current test has been changed. " + 
                    "Do you want to save changes?", "Warning");
            
    		switch (choice)
    		{
    		    case (GUI.YES_OPTION)    : 
                {
                    int writeChoise = queryToWriteFile(frame, false);
                    
                    if (writeChoise == FILE_HAS_WRITTEN)
                    {
                        SaveTestAction action = 
                            new SaveTestAction(frame, false);
                        action.updateConfig();
                    }
                    
                    return readFile(frame);
                }
                
    		    case (GUI.NO_OPTION)     : return readFile(frame);
                
    		    case (GUI.CLOSED_OPTION) : return FILE_READING_CANCELED;
    		}
    		// unachievable code
    		return FILE_READING_CANCELED;
    	}
    	else
    		return readFile(frame);
    }
    
    // function returns FILE_HAS_WRITTEN, FILE_WRITING_FAILURE, FILE_WRITING_CANCELED
    public static int queryToWriteFile(GUI frame, boolean isSaveAs)
    {
		if (isSaveAs || frame.getConfig().isUndefined())
			return writeFile(frame, frame.getTestSuite().isUndefined());
		else
		{
			try
            {
                String fileName = frame.getConfig().getFileName();
                String dirName = frame.getTestSuite().getName();
				File file = new File(dirName+ "\\" + fileName);
				
                if(!file.canWrite()) 
                {
                	frame.showWarningMessage("File " + file.getName() + " is not writeable!", "Error");
                    return FILE_WRITING_FAILURE;
                }

                ConfigWriter writer = new ConfigWriter(frame);                
                int returnValue = writer.writeConfig(file);
                if (returnValue == ConfigWriter.FILE_HAS_BEEN_WRITTEN)
                {
                	return FILE_HAS_WRITTEN;
                }
                else 
                {
                	return FILE_WRITING_FAILURE;
                }
            }
            catch (Exception e) 
            {
            	e.printStackTrace();
            	return FILE_WRITING_FAILURE;
            }
		}
    }
    
    public static void createNewTest(GUI frame)
    {
        TestConfig config  = frame.getDefaultConfig().clone();
        config.registerSection(config.createSection("section"));
    	frame.setConfig(config);
        
    	frame.setSettings(frame.getDefaultSettings().clone());
    }
    
    protected static void initFileChooser(JFileChooser fileChooser, GUI frame, boolean isWokspaceUndefined)
    {
    	fileChooser.setCurrentDirectory(new File(frame.getTestSuite().getName()));
    	
    	if (isWokspaceUndefined)
    		// allow to show dirs 
    		fileChooser.setFileFilter(new FileNameExtensionFilter(DESCRIPTION, GUI.EXTENSION));
    	else
    	{
    		fileChooser.addPropertyChangeListener(JFileChooser.DIRECTORY_CHANGED_PROPERTY, new FileChooserDirectoryChange(frame));
    		// don't allow to show dirs 
    		fileChooser.setFileFilter(new ExtFileFilter(GUI.EXTENSION, DESCRIPTION));
    	}
    	
    	// TODO : make allow work only with *.mt files
    	//fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }
    
    protected static File tryToAddExtention(File file)
    {
        String fileName = file.getName();
        
        int extentionSize = GUI.EXTENSION.length() + 1;
        int length = fileName.length();
        
        
        if (length <= extentionSize || !
                fileName.substring(length - extentionSize, length).equals("." + GUI.EXTENSION))
        {
            file = new File(file.getPath() + "." + GUI.EXTENSION);
        }
        
        return file;
    }
    
    // function returns FILE_HAS_READ, FILE_READING_FAILURE or FILE_READING_CANCELED
    protected static int readFile(GUI frame)
    {
    	JFileChooser fileChooser = new JFileChooser();
    	
    	fileChooser.setDialogTitle(GUI.APPLICATION_NAME + " – Open Test...");
    	
    	initFileChooser(fileChooser, frame, false);
        
        if(fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) 
        {
             File file = fileChooser.getSelectedFile();
             
             if (! file.exists())
                 { file = tryToAddExtention(file); }
             
             return readFile(frame, file);
        }
        else
        	return FILE_READING_CANCELED;
    }
    
    public static int readFile(GUI frame, File file)
    {
        try 
        {
            if(!file.isFile()) 
            {
             frame.showWarningMessage("File " + file.getName() + " is not a file!", "Error");
                return FILE_READING_FAILURE; 
            }
            
            if(!file.canRead())
            {
             frame.showWarningMessage("File " + file.getName() + " is not readable!", "Error");
                return FILE_READING_FAILURE;
            }

            ConfigParser read = new ConfigParser(frame);
            int returnValue = read.parseFile(file);
            if (returnValue == ConfigParser.FILE_HAS_NOT_BEEN_READ)
                return FILE_READING_FAILURE;
            else 
                return FILE_HAS_READ;
        }
        catch(Exception e)
        {
         e.printStackTrace();
         return FILE_READING_FAILURE;
        }
    }
    
    // function returns FILE_HAS_WRITTEN, FILE_WRITING_FAILURE, FILE_WRITING_CANCELED
    protected static int writeFile(GUI frame, boolean isWokspaceUndefined)
    {
        final JFileChooser fileChooser = new JFileChooser();
        
        fileChooser.setDialogTitle(GUI.APPLICATION_NAME + " – Save Test As...");
        
        initFileChooser(fileChooser, frame, isWokspaceUndefined);
        
        if(fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
        {
            File file = fileChooser.getSelectedFile();
            
            if (! file.exists())
                { file = tryToAddExtention(file); }
            
            return writeFile(frame, file, isWokspaceUndefined);
        }
        else
        	return FILE_WRITING_CANCELED; 
    }
    
    public static int writeFile(GUI frame, File file, boolean isWokspaceUndefined)
    {
        try
        {
            file.createNewFile();
            
            if(!file.canWrite()) 
            {
                frame.showWarningMessage("File " + file.getName() + " is not writeable!", "Error");
                return FILE_WRITING_FAILURE;
            }

            ConfigWriter writer = new ConfigWriter(frame);                
            int returnValue = writer.writeConfig(file);
            if (returnValue == ConfigWriter.FILE_HAS_BEEN_WRITTEN)
            {
                if (isWokspaceUndefined)
                    { frame.getTestSuite().setName(file.getParentFile().getPath()); }
                
                // changes have been saved
                frame.setTestHasChanges(false);
                frame.updateTitle();
                
                return FILE_HAS_WRITTEN;
            }
            else 
                return FILE_WRITING_FAILURE;
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            return FILE_WRITING_FAILURE;
        }
        
    }
    
    // function returns WORKSPACE_SELECTED if user has selected some workspace
    // and returns WORKSPACE_UNSELECTED in other cases
    public static int switchWorkspace(GUI frame)
    {
    	TestSuiteConfig testSuite = frame.getTestSuite();
    	JFileChooser fileChooser = new JFileChooser();
    	
    	if (! testSuite.isUndefined())
    		fileChooser.setCurrentDirectory(new File(testSuite.getName()));
    	
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        fileChooser.setFileHidingEnabled(true); // enabled hiding
        fileChooser.setDialogTitle(GUI.APPLICATION_NAME + " – Switch Workspace...");
        
        int returnValue = fileChooser.showDialog(frame, "OK");
        
        if (returnValue == JFileChooser.APPROVE_OPTION) 
        {
            File file = fileChooser.getSelectedFile();

            testSuite.setName(file.getPath());
            return WORKSPACE_SELECTED;  
        } 
        else 
            return WORKSPACE_UNSELECTED;
    }
}