/*
 * Copyright 2014-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.memory.LocationAccessor;
import ru.ispras.microtesk.test.LabelManager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@link LabelReference} class describes a reference to a label. This means a label specified
 * as an argument of a control-transfer instruction. The important point is that a reference
 * is not linked to a specific label, it just provides information used for label lookup during
 * simulation. There may be several labels with the same name located in different blocks.
 * Which one will be chosen for control transfer will be chosen depending on the context (see
 * {@link ru.ispras.microtesk.test.LabelManager}).
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class LabelReference {
  private final LabelValue reference;
  private final LocationAccessor patcher;
  private LabelManager.Target target;

  public LabelReference(final LabelValue lazyLabel) {
    InvariantChecks.checkNotNull(lazyLabel);

    this.reference = lazyLabel;
    this.target = null;
    this.patcher = null;
  }

  public LabelReference(
      final LabelValue lazyLabel,
      final LocationAccessor patcher) {
    InvariantChecks.checkNotNull(lazyLabel);
    InvariantChecks.checkNotNull(patcher);

    this.reference = lazyLabel;
    this.target = null;
    this.patcher = patcher;
  }

  protected LabelReference(final LabelReference other) {
    InvariantChecks.checkNotNull(other);

    this.reference = other.reference.sharedCopy();
    this.target = other.target;
    this.patcher = other.patcher;
  }

  public static List<LabelReference> copyAll(final List<LabelReference> labelRefs) {
    InvariantChecks.checkNotNull(labelRefs);

    if (labelRefs.isEmpty()) {
      return Collections.emptyList();
    }

    final List<LabelReference> result = new ArrayList<>();
    for (final LabelReference labelRef : labelRefs) {
      result.add(new LabelReference(labelRef));
    }

    return result;
  }

  /**
   * Return a {@link Label} object that describes a reference to a label with a specific name made
   * from a specific block. There is no correspondence between the returned label and the actual
   * label that will be chosen for control transfer. It just provides context that helps choose the
   * most suitable label.
   *
   * @return Label object describing a reference to a label.
   */
  public Label getReference() {
    return reference.getLabel();
  }

  public void setReference(final Label label) {
    reference.setLabel(label);
  }

  public String getReferenceSuffix() {
    return reference.getSuffix();
  }

  /**
   * Returns the value assigned (instead of a real address or offset) to the primitive
   * (OP or MODE) argument the label reference is associated with.
   *
   * @return Value assigned to the associated primitive argument.
   */
  public BigInteger getArgumentValue() {
    return reference.hasAddress() ? reference.getAddress() : null;
  }

  public LabelManager.Target getTarget() {
    return target;
  }

  /**
   * Returns the unique name of the target or referenced label if the target is not assigned.
   *
   * @return Unique name of the target or referenced label if the target is not assigned.
   */
  public String getName() {
    final Label label = null != target ? target.getLabel() : reference.getLabel();
    return label.getUniqueName() + reference.getSuffix();
  }

  public void setTarget(final LabelManager.Target target) {
    InvariantChecks.checkNotNull(target);
    this.target = target;
  }

  public void resetTarget() {
    target = null;
  }

  public LocationAccessor getPatcher() {
    return patcher;
  }

  @Override
  public String toString() {
    return String.format(
        "Reference: Label %s (address %x)",
        reference.getName(),
        reference.getAddress()
        );
  }
}
