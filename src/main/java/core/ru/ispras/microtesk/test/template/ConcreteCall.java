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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.instruction.InstructionCall;

public final class ConcreteCall {
  private final List<Label> labels;
  private final List<LabelReference> labelRefs;
  private final List<Output> outputs;
  private final InstructionCall executable;

  private long address = 0;
  private String text = null;
  private int executionCount = 0;

  public ConcreteCall(
      final Call abstractCall,
      final InstructionCall executable) {
    InvariantChecks.checkNotNull(abstractCall);
    InvariantChecks.checkNotNull(executable);

    this.labels = abstractCall.getLabels();
    this.labelRefs = abstractCall.getLabelReferences();
    this.outputs = abstractCall.getOutputs();
    this.executable = executable;
  }

  public ConcreteCall(final Call abstractCall) {
    InvariantChecks.checkNotNull(abstractCall);

    this.labels = abstractCall.getLabels();
    this.labelRefs = abstractCall.getLabelReferences();
    this.outputs = abstractCall.getOutputs();
    this.executable = null;
  }

  public ConcreteCall(final InstructionCall executable) {
    InvariantChecks.checkNotNull(executable);

    this.labels = Collections.<Label>emptyList();
    this.labelRefs = Collections.<LabelReference>emptyList();
    this.outputs = Collections.<Output>emptyList();
    this.executable = executable;
  }

  public boolean isExecutable() {
    return null != executable;
  }

  public void execute() {
    if (isExecutable()) {
      executable.execute();
      ++executionCount;
    }
  }

  public int getExecutionCount() {
    return executionCount;
  }

  public String getText() {
    if (null == text) {
      text = isExecutable() ? executable.getText() : null;
    }

    return text;
  }

  public void setText(final String text) {
    InvariantChecks.checkNotNull(text);
    this.text = text;
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

  public void setAddress(final long address) {
    this.address = address;
  }
}
