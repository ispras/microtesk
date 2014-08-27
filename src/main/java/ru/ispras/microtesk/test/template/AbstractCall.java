/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * AbstractCall.java, Aug 27, 2014 11:39:59 AM Andrei Tatarnikov
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

package ru.ispras.microtesk.test.template;

import java.util.Map;

public final class AbstractCall
{
    private final String name;
    private final Map<String, Object> attributes;

    private final Primitive rootOperation;
    private final String situation;

    AbstractCall(
        String name,
        Map<String, Object> attributes,
        Primitive rootOperation,
        String situation
        )
    {
        if (null == name)
            throw new NullPointerException();

        if (null == attributes)
            throw new NullPointerException();

        if (null == rootOperation)
            throw new NullPointerException();

        this.name = name;
        this.attributes = attributes;
        this.rootOperation = rootOperation;
        this.situation = situation;
    }

    public String getName()
    {
        return name;
    }

    public Object getAttribute(String name)
    {
        return attributes.get(name);
    }

    public Map<String, Object> getAttributes()
    {
        return attributes;
    }

    public Primitive getRootOperation()
    {
        return rootOperation;
    }

    public String getSituation()
    {
        return situation;
    }
}
