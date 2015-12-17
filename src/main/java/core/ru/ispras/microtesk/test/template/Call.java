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
  private final String text;
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
  private final BigInteger alignment;
  private final BigInteger alignmentInBytes;

  // These fields are used when a call describes an invocation of a preparator with a lazy
  // value. Such a call is created when one preparator refers to another. It will be replaced
  // with a sequence of calls once the value is known and a specific preparator is chosen.
  private final Primitive preparatorTarget;
  private final LazyValue preparatorValue;

  public Call(
      final String text,
      final Primitive rootOperation,
      final List<Label> labels,
      final List<LabelReference> labelRefs,
      final List<Output> outputs,
      final BigInteger origin,
      final BigInteger alignment,
      final BigInteger alignmentInBytes,
      final Primitive preparatorTarget,
      final LazyValue preparatorValue) {
    InvariantChecks.checkNotNull(labels);
    InvariantChecks.checkNotNull(labelRefs);
    InvariantChecks.checkNotNull(outputs);

    // Both either null or not null
    InvariantChecks.checkTrue((null == alignment) == (null == alignmentInBytes));
    InvariantChecks.checkTrue((null == preparatorTarget) == (null == preparatorValue));

    // Both cannot be not null. A call cannot be both an instruction and a preparator invocation.
    InvariantChecks.checkTrue((null == rootOperation) || (null == preparatorTarget));

    this.text = text;
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
    this.alignment = alignment;
    this.alignmentInBytes = alignmentInBytes;

    this.preparatorTarget = preparatorTarget;
    this.preparatorValue = preparatorValue;
  }

  public Call(final Call other) {
    InvariantChecks.checkNotNull(other);

    this.text = other.text;
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
    this.alignment = other.alignment;
    this.alignmentInBytes = other.alignmentInBytes;

    this.preparatorTarget =
        null != other.preparatorTarget ? other.preparatorTarget.newCopy() : null;

    this.preparatorValue =
        null != other.preparatorValue ? new LazyValue(other.preparatorValue) : null;
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
    return null == text      &&
           !isExecutable()   &&
           labels.isEmpty()  &&
           outputs.isEmpty() &&
           null == origin    &&
           null == alignment;
  }

  public String getText() {
    return text;
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

  public BigInteger getAlignment() {
    return alignment;
  }

  public BigInteger getAlignmentInBytes() {
    return alignmentInBytes;
  }

  public Primitive getPreparatorTarget() {
    return preparatorTarget;
  }

  public LazyValue getPreparatorValue() {
    return preparatorValue;
  }

  @Override
  public String toString() {
    return String.format(
        "instruction call %s" + 
        "(root: %s, branch: %b, cond: %b, exception: %b, load: %b, store: %b, blockSize: %d)",
        null != text ? text : "", 
        isExecutable() ? rootOperation.getName() : "null",
        isBranch(),
        isConditionalBranch(),
        canThrowException(),
        isLoad(),
        isStore(),
        getBlockSize()
        );
  }
}
