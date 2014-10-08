/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * TdpZero.java, Oct 7, 2014 12:35:50 PM Andrei Tatarnikov
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

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.testbase.TestBaseContext;
import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestData;

final class TdpRandom extends TestDataProviderBase 
{
    public static String NAME = "random";
    public static int COUNT = 1;

    private int size = 0;
    private int minImm = 0;
    private int maxImm = 0;

    private int iteration = 0;
    private TestData testData = null;

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
        size = getParameterAsInt(query, "size");
        minImm = getParameterAsInt(query, "min_imm");
        maxImm = getParameterAsInt(query, "max_imm");

        final Map<String, Node> unknowns = extractUnknown(query);
        final Map<String, Node> outputData = new LinkedHashMap<String, Node>();

        for (Map.Entry<String, Node> e : unknowns.entrySet())
        {
            final String name = e.getKey();
            final DataType type = e.getValue().getDataType();

            final Node value;
            if (DataTypeId.LOGIC_INTEGER == type.getTypeId())
            {
                value = NodeValue.newInteger(
                    Randomizer.get().nextIntRange(minImm, maxImm));
            }
            else if(DataTypeId.UNKNOWN == type.getTypeId())
            {
                final BitVector bv = BitVector.newEmpty(size);
                Randomizer.get().fill(bv);
                value = NodeValue.newBitVector(bv);
            }
            else
            {
                throw new IllegalArgumentException(String.format(
                    "The %s variable has unupported type: %s", name, type));
            }

            outputData.put(name, value);
        }

        testData = new TestData(outputData);
    }

    @Override
    public boolean hasNext()
    {
        return (testData != null) && (iteration < COUNT);
    }

    @Override
    public TestData next()
    {
        if (!hasNext())
            throw new NoSuchElementException();

        iteration++;
        return testData;
    }
}
