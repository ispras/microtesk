/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ISituation.java, May 14, 2013 11:13:45 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.situation;

import java.util.Map;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.metadata.IMetaSituation;

public interface ISituation
{
    public boolean setInput(String name, Data value);
    public boolean setOutput(String name);
    public Map<String, Data> solve();

    public interface IInfo
    {
        public IMetaSituation getMetaData();
        public String getName();
        public ISituation createSituation();
    }

    public interface IFactory
    {
        public ISituation create();
    }         
}
