/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

public final class Call {
  private final Primitive rootOperation;

  private final List<Label> labels;
  private final List<LabelReference> labelRefs;
  private final List<Output> outputs;

  private final boolean exception;
  private final boolean branch;
  private final boolean conditionalBranch;

  private final boolean load;
  private final boolean store;
  private final int blockSize;

  private final BigInteger origin;

  public Call(
      final Primitive rootOperation,
      final List<Label> labels,
      final List<LabelReference> labelRefs,
      final List<Output> outputs,
      final BigInteger origin) {
    InvariantChecks.checkNotNull(labels);
    InvariantChecks.checkNotNull(labelRefs);
    InvariantChecks.checkNotNull(outputs);

    this.rootOperation = rootOperation;
    this.labels = Collections.unmodifiableList(labels);
    this.labelRefs = Collections.unmodifiableList(labelRefs);
    this.outputs = Collections.unmodifiableList(outputs);

    if (null != rootOperation) {
      this.exception = rootOperation.canThrowException();
      this.branch = rootOperation.isBranch();
      this.conditionalBranch = rootOperation.isConditionalBranch();
      this.load = rootOperation.isLoad();
      this.store = rootOperation.isStore();
      this.blockSize = rootOperation.getBlockSize();
    } else {
      this.exception = false;
      this.branch = false;
      this.conditionalBranch = false;
      this.load = false;
      this.store = false;
      this.blockSize = 0;
    }

    this.origin = origin;
  }

  public Call(final Call other) {
    InvariantChecks.checkNotNull(other);

    this.rootOperation = null != other.rootOperation ? 
        other.rootOperation.newCopy() : null;

    this.labels = other.labels;
    this.labelRefs = copyLabelReferences(other.labelRefs);
    this.outputs = other.outputs;

    this.exception = other.exception;
    this.branch = other.branch;
    this.conditionalBranch = other.conditionalBranch;

    this.load = other.load;
    this.store = other.store;
    this.blockSize = other.blockSize;

    this.origin = other.origin;
  }

  public static List<Call> newCopy(final List<Call> calls) {
    InvariantChecks.checkNotNull(calls);
    if (calls.isEmpty()) {
      return Collections.emptyList();
    }

    final List<Call> result = new ArrayList<>(calls.size());
    for (final Call call : calls) {
      result.add(new Call(call));
    }

    return result;
  }

  private static List<LabelReference> copyLabelReferences(
      final List<LabelReference> labelRefs) {
    if (labelRefs.isEmpty()) {
      return Collections.emptyList();
    }

    final List<LabelReference> result = new ArrayList<>();
    for (final LabelReference labelRef : labelRefs) {
      result.add(new LabelReference(labelRef));
    }

    return result;
  }

  public boolean isExecutable() {
    return null != rootOperation;
  }

  public boolean isEmpty() {
    return !isExecutable() && labels.isEmpty() && outputs.isEmpty() && null == origin;
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
    return String.format(
        "instruction call " + 
        "(root: %s, branch: %b, cond: %b, exception: %b, load: %b, store: %b, blockSize: %d)",
        isExecutable() ? rootOperation.getName() : "null",
        isBranch(),
        isConditionalBranch(),
        canThrowException(),
        isLoad(),
        isStore(),
        getBlockSize()
        );
  }

  public boolean isBranch() {
    return branch;
  }

  public boolean isConditionalBranch() {
    return conditionalBranch;
  }

  public boolean canThrowException() {
    return exception;
  }

  public boolean isLoad() {
    return load;
  }

  public boolean isStore() {
    return store;
  }

  public int getBlockSize() {
    return blockSize;
  }

  public Label getTargetLabel() {
    final LabelReference reference = labelRefs.get(0);
    if (null == reference) {
      return null;
    }

    return reference.getReference();
  }

  public BigInteger getOrigin() {
    return origin;
  }
}
