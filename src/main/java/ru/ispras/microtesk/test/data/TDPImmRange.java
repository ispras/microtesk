/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * TDPRandomImm.java, Oct 6, 2014 3:52:12 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.test.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.testbase.TestBaseContext;
import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestData;

public final class TDPImmRange extends TestDataProviderBase 
{
    public static String NAME = "imm_range";
    public static int COUNT = 1;

    private int iteration = 0;

    private int from = 0;
    private int to = 0;
    private int step = 0;

    private Map<String, Node> unknownImms = null;

    @Override
    boolean isSuitable(TestBaseQuery query)
    {
        return checkContextAttribute(
            query, TestBaseContext.TESTCASE, NAME);
    }

    @Override
    void initialize(TestBaseQuery query)
    {
        iteration = 0;
        from = getParameterAsInt(query, "from");
        to   = getParameterAsInt(query, "to");
        step = getParameterAsInt(query, "step");
        unknownImms = extractUnknownImms(query);
    }

    @Override
    public boolean hasNext()
    {
        return (unknownImms != null) && (iteration < COUNT);
    }

    @Override
    public TestData next()
    {
        if (!hasNext())
            throw new NoSuchElementException();

        final Map<String, Node> outputData = 
            new LinkedHashMap<String, Node>();

        final int delta = Math.abs(to - from);

        int index = 0;
        for (Map.Entry<String, Node> e : unknownImms.entrySet())
        {
            final int value = from + ((delta == 0) ? 0 : (index % delta)); 
            outputData.put(e.getKey(), NodeValue.newInteger(value));
            index += step;
        }

        iteration++;
        return new TestData(outputData);
    }
}
