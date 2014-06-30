/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ICallFactory.java, Jun 30, 2014 5:58:32 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.api;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.instruction.IAddressingModeBuilder;
import ru.ispras.microtesk.model.api.instruction.IOperation;
import ru.ispras.microtesk.model.api.instruction.InstructionCall;

public interface ICallFactory
{
    public IAddressingModeBuilder newModeInstance(String name) throws ConfigurationException;

    //public IOperationBuilder newOpInstance(String name, String rootName);

    public InstructionCall newCall(IOperation op);
}

/*
interface IOperationBuilder
{
    public void setArgument(String name, String value);
    public void setArgument(String name, int value);
    public void setArgument(String name, IAddressingMode value);
    public void setArgument(String name, IOperation value);
    public IOperation build();
}
*/
