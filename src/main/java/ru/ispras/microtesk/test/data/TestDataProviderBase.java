/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * TestDataProviderBase.java, Oct 6, 2014 5:51:44 PM Andrei Tatarnikov
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

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestDataProvider;

abstract class TestDataProviderBase extends TestDataProvider
{
    abstract boolean isSuitable(TestBaseQuery query);
    abstract void initialize(TestBaseQuery query);

    protected static boolean checkContextAttribute(
        TestBaseQuery query, String name, String expectedValue)
    {
        return expectedValue.equals(query.getContext().get(name));
    }

    protected static String getParameter(TestBaseQuery query, String name)
    {
        return query.getParameters().get(name);
    }

    protected static int getParameterAsInt(TestBaseQuery query, String name)
    {
        return Integer.valueOf(getParameter(query, name));
    }

    protected static Map<String, Node> extractUnknownImms(TestBaseQuery query)
    {
        final Map<String, Node> result = new LinkedHashMap<String, Node>();
        for (Map.Entry<String, Node> e : query.getBindings().entrySet())
        {
            final Node value = e.getValue();
            if (value.getKind() == Node.Kind.VARIABLE &&
                !((NodeVariable) value).getVariable().hasValue() &&
                value.getDataType().getTypeId() == DataTypeId.LOGIC_INTEGER)
            {
                result.put(e.getKey(), value);
            }
        }

        return result;
    }

    protected static Map<String, Node> extractUnknown(TestBaseQuery query)
    {
        final Map<String, Node> result = new LinkedHashMap<String, Node>();
        
        for (Map.Entry<String, Node> e : query.getBindings().entrySet())
        {
            final Node value = e.getValue();
            if (value.getKind() == Node.Kind.VARIABLE &&
                !((NodeVariable) value).getVariable().hasValue())
            {
                result.put(e.getKey(), value);
            }
        }

        return result;
    }
}
