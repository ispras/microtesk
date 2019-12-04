/*
 * Copyright 2014-2019 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.template.directive.Directive;

public final class AbstractCallBuilder {
  private final BlockId blockId;

  private Where where;
  private String text;
  private Primitive rootOperation;

  private final List<Label> labels;
  private final List<LabelReference> labelRefs;
  private final List<Output> outputs;
  private final List<Directive> directives;

  private PreparatorReference preparatorReference;

  public AbstractCallBuilder(final BlockId blockId) {
    InvariantChecks.checkNotNull(blockId);

    this.blockId = blockId;
    this.where = null;
    this.text = null;
    this.rootOperation = null;
    this.labels = new ArrayList<>();
    this.labelRefs = new ArrayList<>();
    this.outputs = new ArrayList<>();
    this.directives = new ArrayList<>();
    this.preparatorReference = null;
  }

  public BlockId getBlockId() {
    return blockId;
  }

  public void setWhere(final Where where) {
    InvariantChecks.checkNotNull(where);
    this.where = where;
  }

  public void setText(final String text) {
    InvariantChecks.checkNotNull(text);
    this.text = text;
  }

  public void setRootOperation(final Primitive rootOperation) {
    InvariantChecks.checkNotNull(rootOperation);

    if (rootOperation.getKind() != Primitive.Kind.OP) {
      throw new IllegalArgumentException("Illegal kind: " + rootOperation.getKind());
    }

    this.rootOperation = rootOperation;
  }

  public void addLabel(final Label label) {
    InvariantChecks.checkNotNull(label);
    labels.add(label);
  }

  public void addLabelReference(final LabelValue label) {
    InvariantChecks.checkNotNull(label);

    final LabelReference labelRef = new LabelReference(label);
    labelRefs.add(labelRef);
  }

  public void addOutput(final Output output) {
    InvariantChecks.checkNotNull(output);
    outputs.add(output);
  }

  public void addDirective(final Directive directive) {
    InvariantChecks.checkNotNull(directive);
    this.directives.add(directive);
  }

  public void setPreparatorReference(final PreparatorReference preparatorReference) {
    InvariantChecks.checkNotNull(preparatorReference);
    this.preparatorReference = preparatorReference;
  }

  public AbstractCall build() {
    return new AbstractCall(
        where,
        text,
        rootOperation,
        labels,
        labelRefs,
        outputs,
        directives,
        preparatorReference,
        null,
        null,
        null
        );
  }
}
