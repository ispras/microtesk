/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * RandomSituation.java, May 15, 2013 3:44:35 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.situation.builtin;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.DataEngine;
import ru.ispras.microtesk.model.api.situation.ISituation;
import ru.ispras.microtesk.model.api.situation.Situation;
import ru.ispras.microtesk.model.api.type.ETypeID;
import ru.ispras.microtesk.model.api.type.Type;

public final class RandomSituation extends Situation
{
    private static final   String    NAME = "random";
    private static final IFactory FACTORY = new IFactory()
    {
        @Override
        public ISituation create() { return new RandomSituation(); }
    };

    public static final IInfo INFO = new Info(NAME, FACTORY); 

    private final Set<String> outputNames;

    public RandomSituation()
    {
        super(INFO);
        this.outputNames = new LinkedHashSet<String>();
    }

    @Override
    public boolean setInput(String name, Data value)
    {
        return false;
    }

    @Override
    public boolean setOutput(String name)
    {
        final String REX = "^src[\\d]+$";

        final Matcher matcher = Pattern.compile(REX).matcher(name);
        if (!matcher.matches())
            return false;
        
        return outputNames.add(name);
    }

    @Override
    public Map<String, Data> solve()
    {
        final Map<String, Data> result = new HashMap<String, Data>();

        final Random random = new Random();
        for (String outputName : outputNames)
        {
            final int randomInt = random.nextInt();

            final Data randomData =
                DataEngine.valueOf(new Type(ETypeID.CARD, 32), randomInt);

            result.put(outputName, randomData);
        }

        return result;
    }
}
