/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ZeroSituation.java, Apr 16, 2014 5:34:19 PM Andrei Tatarnikov
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.model.api.situation.builtin;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.DataEngine;
import ru.ispras.microtesk.model.api.situation.ISituation;
import ru.ispras.microtesk.model.api.situation.Situation;
import ru.ispras.microtesk.model.api.type.Type;

public class ZeroSituation extends Situation
{
    private static final   String    NAME = "zero";
    private static final IFactory FACTORY = new IFactory()
    {
        @Override
        public ISituation create() { return new ZeroSituation(); }
    };

    public static final IInfo INFO = new Info(NAME, FACTORY); 

    private final Set<String> outputNames;

    public ZeroSituation()
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

        for (String outputName : outputNames)
        {
            final Data data = DataEngine.valueOf(Type.CARD(32), 0);
            result.put(outputName, data);
        }

        return result;
    }
}
