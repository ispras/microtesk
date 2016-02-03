/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

/**
 * The {@link PreparatorReference} class describes an invocation of a preparator
 * with a lazy value. Such an object is associated with a call which is created
 * when one preparator refers to another. The call will be replaced with a
 * sequence of calls when the value is known and a specific preparator is chosen.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class PreparatorReference {
  private final Primitive target;
  private final LazyValue value;
  private final int valueOffset;
  private final String variantName;

  protected PreparatorReference(
      final Primitive target,
      final LazyValue value,
      final int valueOffset,
      final String variantName) {
    InvariantChecks.checkNotNull(target);
    InvariantChecks.checkNotNull(value);

    if (target.getKind() != Primitive.Kind.MODE) {
      throw new IllegalArgumentException(String.format(
          "Illegal preparator target kind: %s. An addressing mode is expected.", target.getKind()));
    }

    this.target = target;
    this.value = value;
    this.valueOffset = valueOffset;
    this.variantName = variantName;
  }

  protected PreparatorReference(final PreparatorReference other) {
    InvariantChecks.checkNotNull(other);

    this.target = other.target.newCopy();
    this.value = new LazyValue(other.value);
    this.valueOffset = other.valueOffset;
    this.variantName = other.variantName;
  }

  public Primitive getTarget() {
    return target;
  }

  public LazyValue getValue() {
    return value;
  }

  public int getValueOffset() {
    return valueOffset;
  }

  public String getVariantName() {
    return variantName;
  }

  @Override
  public String toString() {
    return String.format(
        "%s[valueOffset=%s, variantName=%s]",
        target.getName(),
        valueOffset,
        variantName
        );
  }
}
