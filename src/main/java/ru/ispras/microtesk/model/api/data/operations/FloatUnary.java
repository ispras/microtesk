/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * FloatUnary.java, Sep 24, 2014 4:37:26 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.api.data.operations;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.IUnaryOperator;
import ru.ispras.microtesk.model.api.data.fp.FloatX;
import ru.ispras.microtesk.model.api.type.TypeId;
import ru.ispras.microtesk.model.api.type.Type;

public abstract class FloatUnary implements IUnaryOperator
{
    public static FloatX dataToFloatX(Data data)
    {
        if (null == data)
            throw new NullPointerException();

        final Type type = data.getType();
        if (type.getTypeId() != TypeId.FLOAT)
            throw new IllegalArgumentException();

        return new FloatX(data.getRawData(),
            type.getFieldSize(0), type.getFieldSize(1));
    }
    
    public static Data floatXToData(FloatX floatX)
    {
        return new Data(floatX.getData(),
            Type.FLOAT(floatX.getFractionSize(), floatX.getExponentSize()));
    }

    @Override
    public final Data execute(Data data)
    {
        final FloatX arg = dataToFloatX(data);
        return calculate(arg);
    }

    protected abstract Data calculate(FloatX arg);

    @Override
    public final boolean supports(Type argType)
    {
        return argType.getTypeId() == TypeId.FLOAT;
    }
}
