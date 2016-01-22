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
import ru.ispras.microtesk.model.api.ExecutionException;
import ru.ispras.microtesk.model.api.instruction.InstructionCall;
import ru.ispras.microtesk.test.TestSettings;

public final class ConcreteCall {
  private final List<Label> labels;
  private final List<LabelReference> labelRefs;
  private final List<Output> outputs;
  private final InstructionCall executable;
  private final BigInteger origin;
  private final BigInteger alignment;
  private final BigInteger alignmentInBytes;

  private long address = 0;
  private String text = null;
  private int executionCount = 0;

  // TODO:
  public static ConcreteCall newText(final String text) {
    InvariantChecks.checkNotNull(text);
    return new ConcreteCall(Call.newText(text));
  }

  public static ConcreteCall newLine() {
    return new ConcreteCall(Call.newLine());
  }

  public static ConcreteCall newComment(final String comment) {
    InvariantChecks.checkNotNull(comment);
    return new ConcreteCall(Call.newComment(comment));
  }

  public ConcreteCall(
      final Call abstractCall,
      final InstructionCall executable) {
    InvariantChecks.checkNotNull(abstractCall);
    InvariantChecks.checkNotNull(executable);

    this.text = abstractCall.getText();
    this.labels = copyLabels(abstractCall.getLabels());
    this.labelRefs = abstractCall.getLabelReferences();
    this.outputs = abstractCall.getOutputs();
    this.executable = executable;
    this.origin = abstractCall.getOrigin();
    this.alignment = abstractCall.getAlignment();
    this.alignmentInBytes = abstractCall.getAlignmentInBytes();
  }

  public ConcreteCall(
      final Call abstractCall,
      final InstructionCall executable,
      final List<LabelReference> labelRefs) {
    InvariantChecks.checkNotNull(abstractCall);
    InvariantChecks.checkNotNull(executable);
    InvariantChecks.checkNotNull(labelRefs);

    this.text = abstractCall.getText();
    this.labels = copyLabels(abstractCall.getLabels());
    this.labelRefs = labelRefs;
    this.outputs = abstractCall.getOutputs();
    this.executable = executable;
    this.origin = abstractCall.getOrigin();
    this.alignment = abstractCall.getAlignment();
    this.alignmentInBytes = abstractCall.getAlignmentInBytes();
  }

  public ConcreteCall(final Call abstractCall) {
    InvariantChecks.checkNotNull(abstractCall);

    this.text = abstractCall.getText();
    this.labels = copyLabels(abstractCall.getLabels());
    this.labelRefs = abstractCall.getLabelReferences();
    this.outputs = abstractCall.getOutputs();
    this.executable = null;
    this.origin = abstractCall.getOrigin();
    this.alignment = abstractCall.getAlignment();
    this.alignmentInBytes = abstractCall.getAlignmentInBytes();
  }

  public ConcreteCall(final InstructionCall executable) {
    InvariantChecks.checkNotNull(executable);

    this.text = null;
    this.labels = Collections.<Label>emptyList();
    this.labelRefs = Collections.<LabelReference>emptyList();
    this.outputs = Collections.<Output>emptyList();
    this.executable = executable;
    this.origin = null;
    this.alignment = null;
    this.alignmentInBytes = null;
  }

  private static List<Label> copyLabels(final List<Label> labels) {
    if (labels.isEmpty()) {
      return Collections.emptyList();
    }

    final List<Label> result = new ArrayList<>(labels.size());
    for (final Label label : labels) {
      result.add(new Label(label));
    }

    return result;
  }

  public boolean isExecutable() {
    return null != executable;
  }

  /**
   * Returns exception name if was interrupted.
   * @return exception name if was interrupted.
   */

  public String execute() {
    if (!isExecutable()) {
      return null;
    }

    try {
      executable.execute();
    } catch (final ExecutionException e) {
      return e.getMessage();
    }

    ++executionCount;
    return null;
  }

  public int getExecutionCount() {
    return executionCount;
  }

  public String getText() {
    if (null == text) {
      return isExecutable() ? executable.getText() : null;
    }

    return text;
  }

  public void setText(final String text) {
    InvariantChecks.checkNotNull(text);
    this.text = text;
  }

  public String getImage() {
    return null != executable ? executable.getImage() : "";
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

  public int getByteSize() {
    if (!isExecutable()) {
      return 0;
    }

    return executable.getByteSize();
  }

  public long getAddress() {
    return address;
  }

  public long setAddress(final long value) {
    long thisAddress = value;

    if (origin != null) {
      thisAddress = TestSettings.getBaseVirtualAddress().longValue() + origin.longValue();
    }

    if (alignmentInBytes != null) {
      final long alignmentLength = alignmentInBytes.longValue();
      final long unalignedLength = thisAddress % alignmentLength;
      if (0 != unalignedLength) {
        thisAddress = thisAddress + (alignmentLength - unalignedLength);
      }
    }

    this.address = thisAddress;
    return thisAddress + getByteSize();
  }

  public BigInteger getOrigin() {
    return origin;
  }

  public BigInteger getAlignment() {
    return alignment;
  }
}
