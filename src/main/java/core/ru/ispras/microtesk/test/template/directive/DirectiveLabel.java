/*
 * Copyright 2016-2019 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template.directive;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.test.template.Label;
import ru.ispras.microtesk.test.template.LabelValue;

public class DirectiveLabel extends Directive {
  protected final LabelValue label;

  DirectiveLabel(final Options options, final LabelValue label) {
    super(options);

    InvariantChecks.checkNotNull(label);
    InvariantChecks.checkNotNull(label.getLabel());

    this.label = label;
  }

  public final Label getLabel() {
    return label.getLabel();
  }

  public boolean isRealLabel() {
    return true;
  }

  @Override
  public Kind getKind() {
    return Kind.LABEL;
  }

  @Override
  public String getText() {
    return label.getLabel().getUniqueName() + ":";
  }

  @Override
  public boolean needsIndent() {
    return false;
  }

  @Override
  public Directive copy() {
    return new DirectiveLabel(options, label.newCopy());
  }

  @Override
  public String toString() {
    return String.format("%s (%s)", getText(), label);
  }
}
