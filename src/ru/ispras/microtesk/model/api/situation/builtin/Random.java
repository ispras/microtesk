/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Random.java, May 15, 2013 3:44:35 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.situation.builtin;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.metadata.IMetaSituation;
import ru.ispras.microtesk.model.api.metadata.MetaSituation;
import ru.ispras.microtesk.model.api.situation.Situation;

public class Random extends Situation
{
    private static final String NAME = "random";
    private static final IMetaSituation METADATA = new MetaSituation(NAME);

    private final Set<String> outputNames;

    public Random()
    {
        super(NAME);
        this.outputNames = new LinkedHashSet<String>();
    }

    @Override
    public IMetaSituation getMetaData()
    {
        return METADATA;
    }

    @Override
    public boolean setInput(String name, Data value)
    {
        return false;
    }

    @Override
    public boolean setOutput(String name)
    {
        return outputNames.add(name);
    }

    @Override
    public Map<String, Data> solve()
    {
        final Map<String, Data> result = new HashMap<String, Data>();

        for (String outputName : outputNames) {
            outputName.toString();
            // TODO: generate random data
        }

        return result;
    }
}
