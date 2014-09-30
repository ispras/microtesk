/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * TestResult.java, Sep 30, 2014 6:02:45 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.api.situation2;

import java.util.Collections;
import java.util.List;

public final class TestResult
{
    public static enum Status
    {
        OK,
        FAILED,
        NO_SOLUTION
    }

    private final Status status;
    private final List<TestData> testData;

    TestResult(Status status)
    {
        this(status, Collections.<TestData>emptyList());
    }

    TestResult(Status status, List<TestData> testData)
    {
        if (null == status)
            throw new NullPointerException();

        if (null == testData)
            throw new NullPointerException();

        this.status = status;
        this.testData = testData;
    }

    public Status getStatus()
    {
        return status;
    }

    public List<TestData> getTestData()
    {
        return testData;
    }
}
