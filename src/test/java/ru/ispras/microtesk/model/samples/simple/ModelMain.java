/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ModelMain.java, Nov 22, 2012 11:46:54 AM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.samples.simple;

import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.debug.MetaModelPrinter;
import ru.ispras.microtesk.model.api.debug.ModelStatePrinter;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;

public class ModelMain
{
    private static final class TestCode extends ModelISA
    {
        private TestCode(IModel model) throws ConfigurationException
        {
            super(model);

            mov(reg(0), imm(0x0F));
            mov(reg(1), imm(0x01));

            mov(reg(2), reg(0));
            mov(reg(3), reg(1));

            add(reg(3), reg(1));
            add(reg(3), reg(1));

            mov(reg(4), imm(0x01));
            sub(reg(5), reg(4));
        }
    }

    public static void main(String[] args)
    {
        final IModel model = new Model();

        final MetaModelPrinter metaModelPrinter = new MetaModelPrinter(model.getMetaData());
        metaModelPrinter.printAll();

        final ModelStatePrinter modelStatePrinter = new ModelStatePrinter(model);
        modelStatePrinter.printRegisters();
       
        try
        {
            final TestCode testCode = new TestCode(model);
            
            testCode.execute();
            testCode.print();
        }
        catch (ConfigurationException e)
        {
            System.out.println(e.getMessage());
        }

        modelStatePrinter.printRegisters();
    }
}
