/*
 * Copyright 2014-2016 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.microtesk.test.sequence.engine.allocator.Allocator;
import ru.ispras.microtesk.utils.SharedObject;

/**
 * The {@link UnknownValue} class describes an unknown immediate value to be specified as an
 * argument of an addressing mode or operation. A corresponding concrete value must be produced
 * as a result of test data generation for some test situation linked to the primitive (MODE or OP)
 * this unknown value is passed to an argument. The generated concrete value is assigned to the
 * object via the {@code setValue} method. 
 * 
 * <p>N.B. The value can be assigned only once, otherwise an exception will be raised. This is
 * done to avoid misuse of the class. For example, when several MODE or OP object hold a reference
 * to the same unknown value object the concrete value must be generated and assigned only once.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class UnknownImmediateValue extends SharedObject<UnknownImmediateValue>
                                         implements Value {
  private final Allocator allocator;
  private final List<Value> retain;
  private final List<Value> exclude;
  private BigInteger value;

  protected UnknownImmediateValue() {
    this.allocator = null;
    this.retain = null;
    this.exclude = null;
    this.value = null;
  }

  protected UnknownImmediateValue(
      final Allocator allocator,
      final List<Value> retain,
      final List<Value> exclude) {
    this.allocator = allocator;
    this.retain = retain;
    this.exclude = exclude;
    this.value = null;
  }

  protected UnknownImmediateValue(final UnknownImmediateValue other) {
    super(other);

    this.allocator = other.allocator;
    this.retain = copyValues(other.retain);
    this.exclude = copyValues(other.exclude);
    this.value = other.value;
  }

  private static List<Value> copyValues(final List<Value> values) {
    if (null == values) {
      return null;
    }

    if (values.isEmpty()) {
      return Collections.emptyList();
    }

    final List<Value> result = new ArrayList<>(values.size());
    for (final Value value : values) {
      if (value instanceof SharedObject) {
        result.add((Value)((SharedObject<?>) value).getCopy());
      } else {
        result.add(value);
      }
    }
    return result;
  }

  public Allocator getAllocator() {
    return allocator;
  }

  public List<Value> getRetain() {
    return retain;
  }

  public List<Value> getExclude() {
    return exclude;
  }

  @Override
  public UnknownImmediateValue newCopy() {
    return new UnknownImmediateValue(this);
  }

  @Override
  public Value copy() {
    return newCopy();
  }

  public boolean isValueSet() {
    return value != null;
  }

  @Override
  public BigInteger getValue() {
    if (!isValueSet()) {
      throw new IllegalStateException("Value is not set.");
    }
    return value;
  }

  public void setValue(final BigInteger value) {
    if (isValueSet()) {
      throw new IllegalStateException("Value is already set.");
    }
    this.value = value;
  }

  @Override
  public String toString() {
    return null != value ? value.toString() : "UnknownImmediateValue";
  }
}
