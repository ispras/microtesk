/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * TestEngine.java, May 8, 2013 11:00:02 AM Andrei Tatarnikov
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

package ru.ispras.microtesk.test;

import java.io.IOException;

import ru.ispras.microtesk.model.api.IModel;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.state.IModelStateObserver;
import ru.ispras.microtesk.test.block.AbstractCall;
import ru.ispras.microtesk.test.block.BlockBuilderFactory;
import ru.ispras.microtesk.test.data.ConcreteCall;
import ru.ispras.microtesk.test.data.DataGenerator;
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.sequence.iterator.IIterator;

public final class TestEngine
{
    public static TestEngine getInstance(IModel model)
    {
        return new TestEngine(model);
    }

    private final IModel model;

    private final BlockBuilderFactory blockBuilderFactory;
    private final DataGenerator dataGenerator;
    
    // Settings
    private String       fileName = null;
    private boolean  logExecution = true; 
    private boolean printToScreen = true;
    private String   commentToken = "\\ ";

    private TestEngine(IModel model)
    {
        this.model = model;
        this.blockBuilderFactory = new BlockBuilderFactory();
        this.dataGenerator = new DataGenerator(model);
    }

    public BlockBuilderFactory getBlockBuilders()
    {
        return blockBuilderFactory;
    }

    public DataGenerator getDataGenerator()
    {
        return dataGenerator;
    }

    public void process(IIterator<Sequence<AbstractCall>> sequenceIt)
        throws ConfigurationException, IOException
    {
        if (null == sequenceIt)
            throw new NullPointerException();

        final IModelStateObserver observer = model.getStateObserver();

        final Executor executor = new Executor(observer, logExecution);
        final Printer printer = new Printer(fileName, observer, commentToken, printToScreen);

        // Processing sequence by sequence:
        // 1. Generate data (create concrete calls).
        // 2. Execute (simulate). 
        // 3. Print.

        sequenceIt.init();
        while (sequenceIt.hasValue())
        {
            final Sequence<AbstractCall> abstractSequence =
                sequenceIt.value();

            final Sequence<ConcreteCall> concreteSequence =
                dataGenerator.generate(abstractSequence);

            executor.executeSequence(concreteSequence);
            printer.printSequence(concreteSequence);
        }
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public void setLogExecution(boolean logExecution)
    {
        this.logExecution = logExecution;
    }

    public void setPrintToScreen(boolean printToScreen)
    {
        this.printToScreen = printToScreen;
    }

    public void setCommentToken(String commentToken)
    {
        this.commentToken = commentToken;
    }
}
