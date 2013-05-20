/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * AddNormalSituation.java, May 20, 2013 11:50:19 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.situation.builtin;

import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.situation.ISituation;
import ru.ispras.microtesk.model.api.situation.Situation;

public final class AddNormalSituation extends Situation
{
    private static final   String    NAME = "normal";
    private static final IFactory FACTORY = new IFactory()
    {
        @Override
        public ISituation create() { return new AddNormalSituation(); }
    };

    public static final IInfo INFO = new Info(NAME, FACTORY); 

    public AddNormalSituation()
    {
        super();
    }

    @Override
    public boolean setInput(String name, Data value)
    {
        return false;
    }

    @Override
    public boolean setOutput(String name)
    {
        return false;
    }

    @Override
    public Map<String, Data> solve()
    {
        final Map<String, Data> result = new HashMap<String, Data>();

        return result;
    }
}
