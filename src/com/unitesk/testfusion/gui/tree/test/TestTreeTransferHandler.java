/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TestTreeTransferHandler.java,v 1.4 2008/08/22 11:15:05 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree.test;

import com.unitesk.testfusion.core.config.SectionListConfig;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.action.MoveSectionAction;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.TransferHandler;

import javax.swing.JTree;
import javax.swing.JComponent;
import javax.swing.tree.TreePath;

/**
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class TestTreeTransferHandler extends TransferHandler
{
    public static final long serialVersionUID = 0;
    
    protected GUI frame;
    
    /** tree path of dragged node */
    protected TreePath oldPath;
    
    /** new index of node after transfer */
    protected int newIndex;
    
    /** new parent node of the transfered node */
    protected TestTreeNode newParentNode;
    
    public TestTreeTransferHandler(GUI frame)
    {
        this.frame = frame;
    }
    
    public int getSourceActions(JComponent c) 
    {
        return MOVE;
    }
    
    protected Transferable createTransferable(JComponent c) 
    {
        TestTree tree = (TestTree)c;
        oldPath = tree.getSelectionPath();
        
        TestTreeNode node = (TestTreeNode)oldPath.getLastPathComponent();
        
        if (node.isRoot())
        {
            return new StringSelection(TestNode.NODE_ID);
        }
        else
        {
            return new StringSelection(SectionNode.NODE_ID);
        }
    }
    
    public boolean canImport(TransferSupport support)
    {
        String data;
        try 
        {
            data = (String)support.getTransferable().getTransferData(
                    DataFlavor.stringFlavor);
        } 
        catch (UnsupportedFlavorException e) 
        {
            return false;
        }
        catch (IOException e) 
        {
            return false;
        }
        
        if (data.equals(TestNode.NODE_ID))
        {
            // can't move root node
            return false;
        }
        else
        {
            JTree.DropLocation dl = 
                (JTree.DropLocation)support.getDropLocation();
            
            TreePath path = dl.getPath();
            
            TestTreeNode oldNode = 
                (TestTreeNode)oldPath.getLastPathComponent();
            
            String name = oldNode.getConfig().getName();
            
            TestTreeNode parentNode = 
                (TestTreeNode)path.getLastPathComponent();
            
            SectionListConfig parentConfig = parentNode.getConfig();
            
            if (oldPath.isDescendant(path))
            {
                // can't move node to it's child
                return false;
            }
            else if (oldPath.getParentPath().equals(path))
            {
                // only change position in the same section
                return true;
            }
            else if (parentConfig.getSection(name) != null) 
            {
                // move to section, where there is section with the same name
                return false; 
            }
            else
                { return true; }
        }
    }
    
    protected void exportDone(JComponent c, Transferable t, int action) 
    {
        if (action == MOVE)
        {
            TestTreeNode oldNode = 
                (TestTreeNode)oldPath.getLastPathComponent();
            
            // insert node into new place
            if (newIndex == -1)
            {
                // insert as the last child
                newIndex = newParentNode.getChildCount();
                
                MoveSectionAction moveAction = new MoveSectionAction(frame, 
                        oldNode, newParentNode, newIndex);
                
                moveAction.execute();
            }
            else
            {
                // insert as a sibling
                MoveSectionAction moveAction = new MoveSectionAction(frame, 
                        oldNode, newParentNode, newIndex);
                
                moveAction.execute();
            }
        }
    }
    
    public boolean importData(TransferSupport support)
    {
        if (!support.isDrop()) 
        {
            return false;
        }
        
        JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
        TreePath newPath = dl.getPath();
        
        newParentNode = (TestTreeNode)newPath.getLastPathComponent();
        
        newIndex = dl.getChildIndex();
        
        return true;
    }
}
