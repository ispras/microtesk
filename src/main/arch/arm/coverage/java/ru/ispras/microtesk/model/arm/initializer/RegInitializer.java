/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * RegInitializer.java, Apr 16, 2014 1:17:16 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.arm.initializer;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.data.IInitializerGenerator;

public final class RegInitializer implements IInitializerGenerator 
{
    private final CallFactory callFactory;

    public RegInitializer(IModel model)
    {
        this.callFactory = new CallFactory(model);
    }

    @Override 
    public boolean isCompatible(Argument dest)
    {
        if (null == dest)
            throw new NullPointerException();

        if (dest.getKind() != Argument.Kind.MODE)
            return false;

        final String modeName =
            ((Primitive) dest.getValue()).getName();

        return modeName.equals("REG");
    }

    @Override
    public List<ConcreteCall> createInitializingCode(Argument dest, Data data) throws ConfigurationException
    {
        final List<ConcreteCall> result = new ArrayList<ConcreteCall>();
        result.add(callFactory.createEOR(dest));

        final byte dataBytes[] = data.getRawData().toByteArray(); 
        for (int byteIndex = 0; byteIndex < 4; ++byteIndex)
        {
            result.add(callFactory.createMOV(dest));
            result.add(callFactory.createADD_IMMEDIATE(dest, dataBytes[byteIndex]));
        }

        return result;
    }
}
