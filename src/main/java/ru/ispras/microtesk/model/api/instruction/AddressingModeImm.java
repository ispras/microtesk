/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * AddressingModeImm.java, Nov 19, 2012 12:09:23 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.api.instruction;

import java.util.Map;

import ru.ispras.microtesk.model.api.memory.Location;
import ru.ispras.microtesk.model.api.type.Type;
import ru.ispras.microtesk.model.api.data.Data;

/**
 * The AddressingModeImm class is a stub class that implements the
 * immediate addressing mode. It allows specifying immediate parameters
 * of an instruction in the same way as with mode parameters. Basically,
 * a constant value parameter is represented by a built-in immediate
 * addressing mode that provides access to the read-only location that
 * sores the data.     
 * 
 * @author Andrei Tatarnikov
 */

public final class AddressingModeImm extends AddressingMode
{
    public static final String NAME = "#IMM";
    public static final String PARAM_NAME = "value";

    public static final IFactory FACTORY = new IFactory()
    {
        @Override
        public IAddressingMode create(Map<String, Data> args)
        {
            return new AddressingModeImm(args);
        }
    };

    public static IInfo INFO(Type type)
    {
        return new Info(
            AddressingModeImm.class,
            NAME,
            FACTORY,
            new ParamDecls()
                .declareParam(PARAM_NAME, type)
            );
    }

    private final Location value;

    public AddressingModeImm(Map<String, Data> args)
    {
        assert args.containsKey(PARAM_NAME) :
            String.format("The %s parameter does not exist.", PARAM_NAME);

        final Data data = args.get(PARAM_NAME);
        this.value = new Location(data);
    }

    @Override
    public String syntax()
    {
        assert false : "Must not be called!";
        return null;
    }

    @Override
    public String image()
    {
        assert false : "Must not be called!";
        return null;
    }

    @Override
    public void action()
    {
        // NOTHING
        assert false : "Must not be called!";
    }

    @Override
    public void onBeforeLoad()
    {
        // NOTHING
        assert false : "Must not be called!";
    }

    @Override
    public void onBeforeStore()
    {
        // NOTHING
        assert false : "Must not be called!";
    }

    @Override
    public Location access()
    {
        return value;
    }
}
