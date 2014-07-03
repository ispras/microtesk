/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * OperationBuilder.java, Jun 30, 2014 8:58:34 PM Andrei Tatarnikov
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

import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.exception.config.UninitializedException;

public final class OperationBuilder implements IOperationBuilder
{
    private final String                      opName;
    private final IOperation.IFactory        factory;
    private final Map<String, Operation.Param> decls;
    private final Map<String, Object>           args;
    
    public OperationBuilder(
        String opName,
        IOperation.IFactory factory,
        Operation.ParamDecls decls
        )
    {
        this.opName  = opName;
        this.factory = factory;
        this.decls   = decls.getDecls();
        this.args    = new HashMap<String, Object>();
    }

    @Override
    public IOperationBuilder setArgument(String name, String value) throws ConfigurationException
    {
        // TODO Auto-generated method stub
        // args.put(name, value);
        return this;
    }

    @Override
    public IOperationBuilder setArgument(String name, int value) throws ConfigurationException
    {
        // TODO Auto-generated method stub
        // args.put(name, value);
        return this;
    }

    @Override
    public IOperationBuilder setArgument(String name, IAddressingMode value) throws ConfigurationException
    {
        // TODO Auto-generated method stub
        args.put(name, value);
        return this;
    }

    @Override
    public IOperationBuilder setArgument(String name, IOperation value) throws ConfigurationException
    {
        // TODO Auto-generated method stub
        args.put(name, value);
        return this;
    }

    @Override
    public IOperation build() throws ConfigurationException
    {
        checkInitialized();
        return factory.create(args);
    }

    private void checkInitialized() throws UninitializedException
    {
        final String ERROR_FORMAT =
            "The % argument of the %s operation is not initialized.";

        for (String name : decls.keySet())
        {
            if (!args.containsKey(name))
                throw new UninitializedException(String.format(ERROR_FORMAT, name, opName)); 
        }
    }
}
