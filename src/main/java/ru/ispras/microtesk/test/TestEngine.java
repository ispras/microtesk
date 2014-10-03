/*
 * Copyright (c) 2013 ISPRAS (www.ispras.ru)
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
import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.sequence.iterator.IIterator;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.Template;

public final class TestEngine
{
    public static TestEngine getInstance(IModel model)
    {
        if (null == model)
            throw new NullPointerException();

        return new TestEngine(model);
    }

    private final IModel model;

    // Settings
    private String       fileName = null;
    private boolean  logExecution = true; 
    private boolean printToScreen = true;
    private String   commentToken = "// ";

    private TestEngine(IModel model)
    {
        this.model = model;
    }

    public Template newTemplate()
    {
        return new Template(model.getMetaData());
    }

    /*
       Processes sequence by sequence:
       1. Generate data (create concrete calls).
       2. Execute (simulate). 
       3. Print.
    */

    public void process(Template template)
        throws ConfigurationException, IOException
    {
        if (null == template)
            throw new NullPointerException();

        final IIterator<Sequence<Call>> sequenceIt =
            template.getSequences();
      
        final SequenceProcessor sequenceProcessor =
            new SequenceProcessor(model);

        final IModelStateObserver observer = model.getStateObserver();
        final Executor executor = new Executor(observer, logExecution);

        final Printer printer = new Printer(
            fileName, observer, commentToken, printToScreen);

        try
        {
            int sequenceNumber = 1;
            sequenceIt.init();
            while (sequenceIt.hasValue())
            {
                final Sequence<Call> abstractSequence =
                    sequenceIt.value();

                printStageHeader(String.format(
                    "Generating data for sequence %d", sequenceNumber));

                final Sequence<ConcreteCall> concreteSequence =
                    sequenceProcessor.process(abstractSequence);

                printStageHeader(String.format(
                    "Executing sequence %d", sequenceNumber));

                executor.executeSequence(concreteSequence);

                printStageHeader(String.format(
                    "Printing sequence %d", sequenceNumber));

                printer.printSequence(concreteSequence);

                sequenceIt.next();
                sequenceNumber++;
            }
        }
        finally
        {
            printer.close();
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

    private void printStageHeader(String text)
    {
        final int LINE_WIDTH = 80;

        final int  prefixWidth = (LINE_WIDTH - text.length()) / 2;
        final int postfixWidth = LINE_WIDTH - prefixWidth - text.length();

        final StringBuilder sb = new StringBuilder();

        sb.append("\r\n");
        for(int i = 0; i < prefixWidth - 1; ++i) sb.append('-');
        sb.append(' ');
        
        sb.append(text);

        sb.append(' ');
        for(int i = 0; i < postfixWidth - 1; ++i) sb.append('-');
        sb.append("\r\n");

        System.out.println(sb);
    }
}
