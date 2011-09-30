/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: HistoryManager.java,v 1.10 2008/08/28 10:40:21 kamkin Exp $
 */

package com.unitesk.testfusion.gui;

import java.util.ArrayList;

import com.unitesk.testfusion.core.config.Config;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class HistoryManager
{
    protected int maxSize = -1;
    
    protected ArrayList<Config> history = new ArrayList<Config>();
    protected int index = 0;
    
    public HistoryManager() {}
    
    public HistoryManager(int maxSize)
    {
        this.maxSize = maxSize;
    }

    public int getMaxSize()
    {
        return maxSize;
    }
    
    public void setMaxSize(int maxSize)
    {
        this.maxSize = maxSize;
        
        if(maxSize == -1)
            { return; }
        
        if(maxSize == 0)
            { history.clear(); index = 0; return; }
        
        while(history.size() > maxSize + 1 /* history + current item */)
        {
            if(index > 0)
                { history.remove(0); index--; }
            
            if(history.size() == maxSize)
                { break; }
            
            if(index < history.size() - 1)
                { history.remove(history.size() - 1);}
        }
    }
    
    public boolean isEmpty()
    {
        return history.isEmpty();
    }
    
    public int size()
    {
        return history.size();
    }

    public Config get()
    {
        if(history.isEmpty())
            { return null; }
        
        return history.get(index);
    }
    
    public boolean isFirst()
    {
        return index == 0;
    }

    public boolean isLast()
    {
        return index + 1 >= size();
    }
    
    public void back()
    {
        if(!isFirst())
            { index--; }
    }
    
    public void forward()
    {
        if(!isLast())
            { index++; }
    }
    
    public void add(Config config)
    {
        if(maxSize == 0)
            { return; }
        
        if(isEmpty())
            { history.add(config); return; }

        if(history.get(index) == config)
            { return; }
        
        index++;
        
        while(index < history.size())
            { history.remove(index); }

        if(maxSize >= 0 && history.size() == maxSize + 1 /* history + current item */)
            { history.remove(0); index--; }
            
        history.add(config);
    }
    
    public void remove(Config config)
    {
        for(int i = 0; i < history.size(); i++)
        {
            Config current = history.get(i);
            
            if(current == config)
            {
                if(index >= i && index > 0)
                    { index--; }
                
                history.remove(i--);
            }
        }
    }
    
    public void clear()
    {
        history.clear();
        index = 0;
    }
}
