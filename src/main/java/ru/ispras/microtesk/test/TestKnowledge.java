/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * TestKnowledge.java, Sep 29, 2014 4:15:20 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.test;

import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.situation.ISituation;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;

final class TestKnowledge
{
    private final IModel model;

    TestKnowledge(IModel model)
    {
        if (null == model)
            throw new NullPointerException();

        this.model = model;
    }

    public ISituation getSituation(
        Situation situation, Primitive primitive)
    {
        // TODO Auto-generated method stub
        model.getInitializers();
        return null;
    }
}
