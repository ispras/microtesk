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

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.ArrayList;
import java.util.List;

public final class CallBuilder {
  private final BlockId blockId;

  private Primitive rootOperation;
  private final List<Label> labels;
  private final List<LabelReference> labelRefs;
  private final List<Output> outputs;

  CallBuilder(final BlockId blockId) {
    checkNotNull(blockId);

    this.blockId = blockId;
    this.rootOperation = null;
    this.labels = new ArrayList<Label>();
    this.labelRefs = new ArrayList<LabelReference>();
    this.outputs = new ArrayList<Output>();
  }

  public void setRootOperation(final Primitive rootOperation) {
    checkNotNull(rootOperation);

    if (rootOperation.getKind() != Primitive.Kind.OP) {
      throw new IllegalArgumentException("Illegal kind: " + rootOperation.getKind());
    }

    this.rootOperation = rootOperation;
  }

  public void addLabel(final Label label) {
    checkNotNull(label);
    labels.add(label);
  }

  public void addLabelReference(
      final String labelName,
      final Primitive primitive,
      final String argumentName,
      final int argumentValue) {

    checkNotNull(labelName);
    checkNotNull(primitive);
    checkNotNull(argumentName);

    final LabelReference labelRef = new LabelReference(
        labelName, blockId, primitive, argumentName, argumentValue);

    labelRefs.add(labelRef);
  }
  
  public void addLabelReference(
      final LazyLabel label,
      final Primitive primitive,
      final String argumentName
      ) {
    checkNotNull(label);
    checkNotNull(primitive);
    checkNotNull(argumentName);
    // TODO
  }

  public void addOutput(final Output output) {
    checkNotNull(output);
    outputs.add(output);
  }

  public Call build() {
    return new Call(rootOperation, labels, labelRefs, outputs);
  }
}
