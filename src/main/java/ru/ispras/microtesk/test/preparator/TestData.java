/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * TestData.java, Sep 30, 2014 5:28:51 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.test.preparator;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.test.template.Primitive;

public final class TestData
{
    public static TestData newTestData(
        String argumentName, Data value)
    {
        if (null == value)
            throw new NullPointerException();

        if (null == argumentName)
            throw new NullPointerException();

        return new TestData(
            value, TargetKind.IMM, null, argumentName);
    }

    public static TestData newTestData(
        String argumentName, Primitive mode, Data value)
    {
        if (null == value)
            throw new NullPointerException();

        if (null == argumentName)
            throw new NullPointerException();

        if (Primitive.Kind.MODE != mode.getKind())
            throw new IllegalArgumentException();

        return new TestData(
            value, TargetKind.MODE, mode, argumentName);
    }

    public static TestData newTestData(
        Location location, Data value)
    {
        if (null == location)
            throw new NullPointerException();

        if (null == value)
            throw new NullPointerException();

        return new TestData(
            value, TargetKind.LOCATION, location, null);
    }

    public static enum TargetKind
    {
        IMM      (null),            // Immediate value
        MODE     (Primitive.class), // Addressing mode
        LOCATION (Location.class);  // Location (MEM, REG, VAR)

        private final Class<?> c;
        private TargetKind(Class<?> c) { this.c = c; }

        private void checkClass(Object target)
        {
            if ((null == c) && (null == target)) return;
            if (c == target.getClass()) return;
            throw new IllegalArgumentException();
        }
    }

    private final Data value;
    private final TargetKind targetKind;
    private final Object target;
    private final String argumentName;

    public TestData(
        Data value, TargetKind kind, Object target, String argumentName)
    {
        if (null == value)
            throw new NullPointerException();

        if (null == kind)
            throw new NullPointerException();

        kind.checkClass(target);

        this.value = value;
        this.targetKind = kind;
        this.target = target;
        this.argumentName = argumentName;
    }

    public Data getValue()
    {
        return value;
    }

    public TargetKind getTargetKind()
    {
        return targetKind;
    }

    public Object getTarget()
    {
        return target;
    }

    public String getArgumentName()
    {
        return argumentName;
    }
}
