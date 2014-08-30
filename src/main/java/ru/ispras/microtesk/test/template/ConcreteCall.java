/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ConcreteCall.java, Aug 30, 2014 7:48:20 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.test.template;

import java.util.Collections;
import java.util.List;

import ru.ispras.microtesk.model.api.instruction.InstructionCall;

public final class ConcreteCall
{
    private final List<Label> labels;
    private final List<LabelReference> labelRefs;
    private final List<Output> outputs;
    private final InstructionCall executable;

    public ConcreteCall(Call abstractCall, InstructionCall executable)
    {
        if (null == abstractCall)
            throw new NullPointerException();

        if (null == executable)
            throw new NullPointerException();

        this.labels = abstractCall.getLabels();
        this.labelRefs = abstractCall.getLabelReferences();
        this.outputs = abstractCall.getOutputs();
        this.executable = executable;
    }

    public ConcreteCall(Call abstractCall)
    {
        if (null == abstractCall)
            throw new NullPointerException();

        this.labels = abstractCall.getLabels();
        this.labelRefs = abstractCall.getLabelReferences();
        this.outputs = abstractCall.getOutputs();
        this.executable = null;
    }

    public ConcreteCall(InstructionCall executable)
    {
        if (null == executable)
            throw new NullPointerException();

        this.labels = Collections.<Label>emptyList();
        this.labelRefs = Collections.<LabelReference>emptyList();
        this.outputs = Collections.<Output>emptyList();
        this.executable = executable;
    }

    public void execute()
    {
        if (null != executable)
            executable.execute();
    }

    public String getText()
    {
        return (null != executable) ? executable.getText() : "";
    }

    public List<Label> getLabels()
    {
        return labels;
    }

    public List<LabelReference> getLabelReferences()
    {
        return labelRefs;
    }

    public List<Output> getOutputs()
    {
        return outputs;
    }
}
