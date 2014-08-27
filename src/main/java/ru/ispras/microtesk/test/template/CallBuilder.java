/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * CallBuilder.java, Aug 27, 2014 12:04:51 PM Andrei Tatarnikov
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

import java.util.HashMap;
import java.util.Map;

public final class CallBuilder
{
    private String name;
    private final Map<String, Object> attributes;
    private Primitive rootOperation;
    private String situation;

    public CallBuilder()
    {
        this.name = null;
        this.attributes = new HashMap<String, Object>();
        this.rootOperation = null;
        this.situation = null;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setTestSituation(String name)
    {
        situation = name;
    }

    public void addLabel(Label label)
    {
        
    }

    public void addOutput(Output output)
    {
        
    }

    public Call build()
    {
        return new Call(name, attributes, rootOperation, situation);
    }
}
