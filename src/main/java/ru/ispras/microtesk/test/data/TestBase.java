/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * TestBase.java, Oct 7, 2014 6:06:24 PM Andrei Tatarnikov
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestBaseQueryResult;

public final class TestBase
{
    private final List<TestDataProviderBase> providers;

    public TestBase()
    {
        this.providers = Arrays.asList(
            new TDPImmRandom(),
            new TDPImmRange(),
            new TDPZero(),
            new TDPRandom()
            );
    }

    public TestBaseQueryResult executeQuery(TestBaseQuery query)
    {
        if (null == query)
            throw new NullPointerException();

        for(TestDataProviderBase provider : providers)
        {
            if (provider.isSuitable(query))
            {
                provider.initialize(query);
                return TestBaseQueryResult.success(provider);
            }
        }

        return TestBaseQueryResult.reportErrors(
            Collections.<String>singletonList(
                "No suitable test data generator is found."));
    }
}
