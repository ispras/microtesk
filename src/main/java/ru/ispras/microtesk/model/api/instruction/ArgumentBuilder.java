/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ArgumentBuilder.java, Nov 19, 2012 4:53:01 PM Andrei Tatarnikov
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

import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.exception.config.ReassignmentException;
import ru.ispras.microtesk.model.api.exception.config.UnsupportedTypeException;

/**
 * The ArgumentBuilder class implements logic for constructing instruction
 * arguments represented by addressing modes.
 * 
 * @author Andrei Tatarnikov
 */

public class ArgumentBuilder implements IArgumentBuilderEx
{
    private final Map<String, IAddressingModeBuilderEx> modeBuilders;
    private IAddressingModeBuilderEx chosenModeBuilder = null;

    /**
     * Creates an argument builder based on a table of addressing mode builders.
     * 
     * @param modeBuilders Table of addressing mode builders.
     */

    public ArgumentBuilder(Map<String, IAddressingModeBuilderEx> modeBuilders)
    {
        assert null != modeBuilders;
        this.modeBuilders = modeBuilders;
    }

    @Override
    public IAddressingModeBuilder getModeBuilder(String name) throws ConfigurationException
    {
        checkModeSupported(name);
        checkModeReassignment();

        chosenModeBuilder = modeBuilders.get(name);
        return chosenModeBuilder;
    }

    @Override
    public IAddressingMode getProduct() throws ConfigurationException
    {
        if (null != chosenModeBuilder)
            return chosenModeBuilder.getProduct();

        return null;
    }

    private void checkModeSupported(String name) throws UnsupportedTypeException
    {
        final String ERROR_FORMAT =
             "The current instruction argument cannot be specified using the %s addressing mode.";

        if (!modeBuilders.containsKey(name))
            throw new UnsupportedTypeException(String.format(ERROR_FORMAT, name));
    }
    
    private void checkModeReassignment() throws ReassignmentException
    {
        final String ERROR_MESSAGE =
            "An addressing mode has already been selected for the current instruction argument.";

        if (null != chosenModeBuilder)
            throw new ReassignmentException(ERROR_MESSAGE);
    }
}
