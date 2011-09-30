/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SelectionListener.java,v 1.2 2009/07/09 14:48:17 kamkin Exp $
 */

package com.unitesk.testfusion.gui.tree;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.tree.TreePath;

import com.unitesk.testfusion.core.config.Config;
import com.unitesk.testfusion.gui.GUI;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class SelectionListener implements KeyListener
{
    protected GUI frame;
    protected Tree tree;
    
    public SelectionListener(GUI frame, Tree tree)
    {
        this.frame = frame;
        this.tree = tree;
    }
    
    public void keyPressed(KeyEvent arg0) 
    {
        // None action
    }

    public void keyReleased(KeyEvent e) 
    {
        int keyCode = e.getKeyCode();
        
        boolean keyAccepted = keyCode == KeyEvent.VK_UP      || keyCode == KeyEvent.VK_DOWN      ||
                              keyCode == KeyEvent.VK_LEFT    || keyCode == KeyEvent.VK_RIGHT     ||
                              keyCode == KeyEvent.VK_PAGE_UP || keyCode == KeyEvent.VK_PAGE_DOWN ||
                              keyCode == KeyEvent.VK_HOME    || keyCode == KeyEvent.VK_END;

        
        if(keyAccepted)
        {
            TreePath path  = tree.getSelectionModel().getSelectionPath();
            TreeNode node = (TreeNode)path.getPathComponent(path.getPathCount() - 1);
            Config config = node.getConfig();
            
            // Show configuration
            frame.getPanel().showPanel(config);
        }
    }

    public void keyTyped(KeyEvent arg0) 
    {
        // None action
    }
}