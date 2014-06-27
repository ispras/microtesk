/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IArgumentBuilderEx.java, Nov 27, 2012 5:59:08 PM Andrei Tatarnikov
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

import ru.ispras.microtesk.model.api.exception.ConfigurationException;

/**
 * The IArgumentBuilderEx interface is a Sim-nML specific extension
 * of the IArgumentBuilder interface that provides a possibility to
 * obtain the constructed addressing mode object that plays the role
 * of an instruction argument.
 * 
 * @author Andrei Tatarnikov
 */

public interface IArgumentBuilderEx extends IArgumentBuilder
{
    /**
     * Returns an addressing mode object created by the builder.
     * 
     * @return The addressing mode object.
     * @throws ConfigurationException Exception that informs of an 
     * error that occurs on attempt to build an addressing mode object
     * due to incorrect configuration.
     */
    
    public IAddressingMode getProduct() throws ConfigurationException;
}
