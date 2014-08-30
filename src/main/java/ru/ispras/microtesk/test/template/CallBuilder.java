/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * CallBuilder.java, Aug 27, 2014 12:04:51 PM Andrei Tatarnikov
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

import java.util.ArrayList;
import java.util.List;

public final class CallBuilder
{
    private Primitive rootOperation;
    private String situation;

    private final List<Label> labels;
    private final List<LabelReference> labelRefs;
    private final List<Output> outputs;

    public CallBuilder()
    {
        this.rootOperation = null;
        this.situation = null;

        this.labels = new ArrayList<Label>();
        this.labelRefs = new ArrayList<LabelReference>();
        this.outputs = new ArrayList<Output>();
    }

    public void setRootOperation(Primitive rootOperation)
    {
        if (null == rootOperation)
            throw new NullPointerException();

        if (rootOperation.getKind() != Primitive.Kind.OP && 
            rootOperation.getKind() != Primitive.Kind.INSTR)
            throw new IllegalArgumentException(
                "Illegal kind: " + rootOperation.getKind());

        this.rootOperation = rootOperation;
    }

    public void setSituation(String name)
    {
        if (null == name)
            throw new NullPointerException();

        situation = name;
    }

    public void addLabel(Label label)
    {
        if (null == label)
            throw new NullPointerException();

        labels.add(label);
    }

    public void addLabelReference(LabelReference labelRef)
    {
        if (null == labelRef)
            throw new NullPointerException();

        labelRefs.add(labelRef);
    }

    public void addOutput(Output output)
    {
        if (null == output)
            throw new NullPointerException();

        outputs.add(output);
    }

    public Call build()
    {
        return new Call(
            rootOperation,
            situation,
            labels,
            labelRefs,
            outputs
            );
    }
}
