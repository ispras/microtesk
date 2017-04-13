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
import ru.ispras.microtesk.utils.SharedObject;

public final class Call {
  private final Where where;
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

  private final boolean relativeOrigin;
  private final BigInteger origin;
  private final BigInteger alignment;
  private final BigInteger alignmentInBytes;

  private final PreparatorReference preparatorReference;
  private final DataSection data;
  private final List<Call> atomicSequence;
  private final Primitive modeToFree;
  private final boolean freeAllModes;

  public static Call newData(final DataSection data) {
    InvariantChecks.checkNotNull(data);

    return new Call(
        null,
        null,
        null,
        Collections.<Label>emptyList(),
        Collections.<LabelReference>emptyList(),
        Collections.<Output>emptyList(),
        false,
        null,
        null,
        null,
        null,
        data,
        null,
        null,
        false
        );
  }

  // TODO:
  public static Call newText(final String text) {
    InvariantChecks.checkNotNull(text);

    return new Call(
        null,
        text,
        null,
        Collections.<Label>emptyList(),
        Collections.<LabelReference>emptyList(),
        Collections.<Output>emptyList(),
        false,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        false
        );
  }

  public static Call newLine() {
    return newText("");
  }

  public static Call newComment(final String comment) {
    InvariantChecks.checkNotNull(comment);

    return new Call(
        null,
        null,
        null,
        Collections.<Label>emptyList(),
        Collections.<LabelReference>emptyList(),
        Collections.singletonList(new Output(Output.Kind.COMMENT, comment)),
        false,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        false
        );
  }

  public static Call newOrigin(final BigInteger origin, final boolean isRelative) {
    InvariantChecks.checkNotNull(origin);

    return new Call(
        null,
        null,
        null,
        Collections.<Label>emptyList(),
        Collections.<LabelReference>emptyList(),
        Collections.<Output>emptyList(),
        isRelative,
        origin,
        null,
        null,
        null,
        null,
        null,
        null,
        false
        );
  }

  public static Call newAtomicSequence(final List<Call> sequence) {
    InvariantChecks.checkNotNull(sequence);

    return new Call(
        null,
        null,
        null,
        Collections.<Label>emptyList(),
        Collections.<LabelReference>emptyList(),
        Collections.<Output>emptyList(),
        false,
        null,
        null,
        null,
        null,
        null,
        expandAtomic(sequence),
        null,
        false
        );
  }

  public static List<Call> expandAtomic(final List<Call> sequence) {
    InvariantChecks.checkNotNull(sequence);

    final List<Call> result = new ArrayList<>();
    for (final Call call : sequence) {
      if (call.isAtomicSequence()) {
        result.addAll(call.getAtomicSequence());
      } else {
        result.add(call);
      }
    }

    return result;
  }

  public static Call newFreeAllocatedMode(final Primitive mode, final boolean freeAll) {
    InvariantChecks.checkNotNull(mode);
    InvariantChecks.checkTrue(mode.getKind() == Primitive.Kind.MODE);

    return new Call(
        null,
        null,
        null,
        Collections.<Label>emptyList(),
        Collections.<LabelReference>emptyList(),
        Collections.<Output>emptyList(),
        false,
        null,
        null,
        null,
        null,
        null,
        null,
        mode,
        freeAll
        );
  }

  public Call(
      final Where where,
      final String text,
      final Primitive rootOperation,
      final List<Label> labels,
      final List<LabelReference> labelRefs,
      final List<Output> outputs,
      final boolean relativeOrigin,
      final BigInteger origin,
      final BigInteger alignment,
      final BigInteger alignmentInBytes,
      final PreparatorReference preparatorReference,
      final DataSection data,
      final List<Call> atomicSequence,
      final Primitive modeToFree,
      final boolean freeAllModes) {
    InvariantChecks.checkNotNull(labels);
    InvariantChecks.checkNotNull(labelRefs);
    InvariantChecks.checkNotNull(outputs);

    // Both either null or not null
    InvariantChecks.checkTrue((null == alignment) == (null == alignmentInBytes));

    // Both cannot be not null. A call cannot be both an instruction and a preparator invocation.
    InvariantChecks.checkTrue((null == rootOperation) || (null == preparatorReference));

    this.where = where;
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

    this.relativeOrigin = relativeOrigin;
    this.origin = origin;
    this.alignment = alignment;
    this.alignmentInBytes = alignmentInBytes;

    this.preparatorReference = preparatorReference;
    this.data = data;
    this.atomicSequence = atomicSequence;
    this.modeToFree = modeToFree;
    this.freeAllModes = freeAllModes;
  }

  public Call(final Call other) {
    InvariantChecks.checkNotNull(other);

    this.where = other.where;
    this.text = other.text;
    this.rootOperation = null != other.rootOperation ? 
        other.rootOperation.newCopy() : null;

    this.labels = Label.copyAll(other.labels);
    this.labelRefs = LabelReference.copyAll(other.labelRefs);
    this.outputs = Output.copyAll(other.outputs);

    this.exception = other.exception;
    this.branch = other.branch;
    this.conditionalBranch = other.conditionalBranch;

    this.load = other.load;
    this.store = other.store;
    this.blockSize = other.blockSize;

    this.relativeOrigin = other.relativeOrigin;
    this.origin = other.origin;
    this.alignment = other.alignment;
    this.alignmentInBytes = other.alignmentInBytes;

    this.preparatorReference = null != other.preparatorReference ?
        new PreparatorReference(other.preparatorReference) : null;

    this.data = null != other.data ?
        new DataSection(other.data) : null;

    this.atomicSequence = null != other.atomicSequence ?
        copyAll(other.atomicSequence) : null;

    this.modeToFree = null != other.modeToFree ?
       (Primitive)((SharedObject<?>) other.modeToFree).getCopy() : null;

    this.freeAllModes = other.freeAllModes;
  }

  public static List<Call> copyAll(final List<Call> calls) {
    InvariantChecks.checkNotNull(calls);

    if (calls.isEmpty()) {
      return Collections.emptyList();
    }

    final List<Call> result = new ArrayList<>(calls.size());
    for (final Call call : calls) {
      result.add(new Call(call));
    }

    SharedObject.freeSharedCopies();
    return result;
  }

  public boolean isExecutable() {
    return null != rootOperation;
  }

  public boolean isPreparatorCall() {
    return null != preparatorReference;
  }

  public boolean isEmpty() {
    return null == text        &&
           !isExecutable()     &&
           !isPreparatorCall() &&
           !hasData()          &&
           !isAtomicSequence() &&
           !isModeToFree()     &&
           labels.isEmpty()    &&
           outputs.isEmpty()   &&
           null == origin      &&
           null == alignment;
  }

  public Where getWhere() {
    return where;
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

  public boolean isRelativeOrigin() {
    return relativeOrigin;
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

  public PreparatorReference getPreparatorReference() {
    return preparatorReference;
  }

  public boolean hasData() {
    return null != data;
  }

  public DataSection getData() {
    return data;
  }

  public boolean isAtomicSequence() {
    return null != atomicSequence;
  }

  public List<Call> getAtomicSequence() {
    return atomicSequence;
  }

  public boolean isModeToFree() {
    return null != modeToFree;
  }

  public boolean isFreeAllModes() {
    return freeAllModes;
  }

  public Primitive getModeToFree() {
    return modeToFree;
  }

  @Override
  public String toString() {
    return String.format(
        "instruction call %s" + 
        "(root: %s, branch: %b, cond: %b, exception: %b, load: %b, store: %b, blockSize: %d, " +
        "preparator: %s, data: %b, atomic: %b, modeToFree: %s)",
        null != text ? text : "", 
        isExecutable() ? rootOperation.getName() : "null",
        isBranch(),
        isConditionalBranch(),
        canThrowException(),
        isLoad(),
        isStore(),
        getBlockSize(),
        isPreparatorCall() ? preparatorReference : "null",
        hasData(),
        isAtomicSequence(),
        isModeToFree() ? modeToFree.getName() : "null"
        );
  }
}
