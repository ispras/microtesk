/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.test.template;

import java.util.ArrayList;
import java.util.List;

public final class CallBuilder {
  private final BlockId blockId;

  private Primitive rootOperation;
  private final List<Label> labels;
  private final List<LabelReference> labelRefs;
  private final List<Output> outputs;

  CallBuilder(BlockId blockId) {
    if (null == blockId) {
      throw new NullPointerException();
    }

    this.blockId = blockId;
    this.rootOperation = null;
    this.labels = new ArrayList<Label>();
    this.labelRefs = new ArrayList<LabelReference>();
    this.outputs = new ArrayList<Output>();
  }

  public void setRootOperation(Primitive rootOperation) {
    if (null == rootOperation) {
      throw new NullPointerException();
    }

    if (rootOperation.getKind() != Primitive.Kind.OP) {
      throw new IllegalArgumentException("Illegal kind: " + rootOperation.getKind());
    }

    this.rootOperation = rootOperation;
  }

  public void addLabel(Label label) {
    if (null == label) {
      throw new NullPointerException();
    }

    labels.add(label);
  }

  public void addLabelReference(String labelName, String primitiveName, String argumentName,
      int argumentValue) {
    if (null == labelName) {
      throw new NullPointerException();
    }

    if (null == primitiveName) {
      throw new NullPointerException();
    }

    if (null == argumentName) {
      throw new NullPointerException();
    }

    final LabelReference labelRef =
      new LabelReference(labelName, blockId, primitiveName, argumentName, argumentValue);

    System.out.println(labelRef.toString());
    labelRefs.add(labelRef);
  }

  public void addOutput(Output output) {
    if (null == output) {
      throw new NullPointerException();
    }

    outputs.add(output);
  }

  public Call build() {
    return new Call(rootOperation, labels, labelRefs, outputs);
  }
}
