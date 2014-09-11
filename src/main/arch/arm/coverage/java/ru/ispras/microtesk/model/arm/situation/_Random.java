/*
 * Copyright (c) 2014 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * _Random.java, Mar 21, 2014 4:12:11 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.arm.situation;

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
import ru.ispras.microtesk.model.api.type.TypeId;
import ru.ispras.microtesk.model.api.type.Type;

public final class _Random extends Situation
{
    private static final   String    NAME = "random";
    private static final IFactory FACTORY = new IFactory()
    {
        @Override
        public ISituation create() { return new _Random(); }
    };

    public static final IInfo INFO = new Info(NAME, FACTORY); 

    private final Set<String> outputNames;

    public _Random()
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
                DataEngine.valueOf(new Type(TypeId.CARD, 32), randomInt);

            result.put(outputName, randomData);
        }

        return result;
    }
}
