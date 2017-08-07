/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.engine;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;

public final class SequenceSelector {
  private final String engineId;
  private final boolean significantOnly;

  public SequenceSelector(final String engineId) {
    this(engineId, true);
  }

  public SequenceSelector(final String engineId, final boolean significantOnly) {
    InvariantChecks.checkNotNull(engineId);
    this.engineId = engineId;
    this.significantOnly = significantOnly;
  }

  public AbstractSequence select(final AbstractSequence sequence) {
    InvariantChecks.checkNotNull(sequence);

    boolean hasSelected = false;
    final AbstractSequence.Builder builder = new AbstractSequence.Builder(sequence.getSection());

    for (int position = 0; position < sequence.getSequence().size(); ++position) {
      final AbstractCall call = sequence.getSequence().get(position);
      InvariantChecks.checkNotNull(call);

      if (isSelected(call.getRootOperation())) {
        builder.addCall(call, position, true);
        hasSelected = true;
      } else if (!significantOnly) {
        builder.addCall(call, position, false);
      }
    }

    return hasSelected ? builder.build() : null;
  }

  private boolean isSelected(final Primitive primitive) {
    if (null == primitive) {
      return false;
    }

    if (isSelected(primitive.getSituation())) {
      return true;
    }

    for (final Argument argument : primitive.getArguments().values()) {
      if (argument.getValue() instanceof Primitive &&
          isSelected((Primitive) argument.getValue())) {
        return true;
      }
    }

    return false;
  }

  private boolean isSelected(final Situation situation) {
    if (null == situation) {
      return false;
    }

    return engineId.equals(
        situation.getAttributes().get("engine"));
  }
}
