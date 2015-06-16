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

import java.util.Collections;
import java.util.List;

public final class Call {
  private final Primitive rootOperation;
  private final List<Label> labels;
  private final List<LabelReference> labelRefs;
  private final List<Output> outputs;

  Call(
      final Primitive rootOperation,
      final List<Label> labels,
      final List<LabelReference> labelRefs,
      final List<Output> outputs) {
    this.rootOperation = rootOperation;
    this.labels = Collections.unmodifiableList(labels);
    this.labelRefs = Collections.unmodifiableList(labelRefs);
    this.outputs = Collections.unmodifiableList(outputs);
  }

  public boolean isExecutable() {
    return null != rootOperation;
  }

  public boolean isEmpty() {
    return !isExecutable() && labels.isEmpty() && outputs.isEmpty();
  }

  public Primitive getRootOperation() {
    return rootOperation;
  }

  public List<Label> getLabels() {
    return labels;
  }

  public List<LabelReference> getLabelReferences() {
    return labelRefs;
  }

  public List<Output> getOutputs() {
    return outputs;
  }

  public String getText() {
    return String.format("instruction call (root: %s)",
      isExecutable() ? rootOperation.getName() : "null");
  }

  public boolean isBranch() {
    if (!isExecutable()) {
      return false;
    }

    // TODO
    throw new UnsupportedOperationException();
  }

  public boolean isConditionalBranch() {
    if (!isExecutable()) {
      return false;
    }

    // TODO
    throw new UnsupportedOperationException();
  }

  public boolean canThrowException() {
    if (!isExecutable()) {
      return false;
    }

    // TODO
    throw new UnsupportedOperationException();
  }
}
