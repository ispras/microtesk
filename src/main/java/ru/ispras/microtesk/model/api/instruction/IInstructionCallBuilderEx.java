/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IInstructionCallBuilderEx.java, Nov 28, 2012 1:12:49 PM Andrei Tatarnikov
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
 * The IInstructionCallBuilderEx interface is an extension of the
 * IInstructionCallBuilder interface that provides a possibility to
 * get the constructed instruction call object. This interface is
 * user internally while the IInstructionCallBuilder interface is 
 * accessible externally.  
 * 
 * @author Andrei Tatarnikov
 */

public interface IInstructionCallBuilderEx extends IInstructionCallBuilder
{
    /**
     * Returns created and initialized instruction call object.  
     * 
     * @return Instruction call object.
     */

    public InstructionCall getCall() throws ConfigurationException;
}
