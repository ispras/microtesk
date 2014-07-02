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

public final class OperationBuilder implements IOperationBuilder
{
    private final String               opName;
    private final IOperation.IFactory factory;
    private final Operation.ParamDecls  decls;

    public OperationBuilder(
        String opName,
        IOperation.IFactory factory,
        Operation.ParamDecls decls
        )
    {
        this.opName  = opName;
        this.factory = factory;
        this.decls   = decls;
    }

    @Override
    public IOperationBuilder setArgument(String name, String value)
    {
        // TODO Auto-generated method stub
        return this;
    }

    @Override
    public IOperationBuilder setArgument(String name, int value)
    {
        // TODO Auto-generated method stub
        return this;
    }

    @Override
    public IOperationBuilder setArgument(String name, IAddressingMode value)
    {
        // TODO Auto-generated method stub
        return this;
    }

    @Override
    public IOperationBuilder setArgument(String name, IOperation value)
    {
        // TODO Auto-generated method stub
        return this;
    }

    @Override
    public IOperation build()
    {
        // TODO Auto-generated method stub
        return factory.create(null);
    }
}
