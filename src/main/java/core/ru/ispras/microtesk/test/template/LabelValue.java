/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.microtesk.utils.SharedObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LabelValue extends SharedObject<LabelValue> implements Value {

  public static LabelValue newLazy() {
    return new LabelValue(null, null);
  }

  public static LabelValue newUnknown(final Label label) {
    InvariantChecks.checkNotNull(label);
    return new LabelValue(label, null);
  }

  public static LabelValue newKnown(final Label label, final BigInteger address) {
    InvariantChecks.checkNotNull(label);
    InvariantChecks.checkNotNull(address);
    return new LabelValue(label, address);
  }

  private Label label;
  private BigInteger address;
  private String suffix;

  public LabelValue(final LabelValue other) {
    super(other);

    this.label = other.label != null ? new Label(other.label) : null;
    this.address = other.address;
    this.suffix = other.suffix;
  }

  private LabelValue(final Label label, final BigInteger address) {
    this.label = label;
    this.address = address;
    this.suffix = "";
  }

  @Override
  public LabelValue newCopy() {
    return new LabelValue(this);
  }

  @Override
  public Value copy() {
    return newCopy();
  }

  public String getName() {
    if (null == label) {
      return null;
    }

    return label.getName();
  }

  public Label getLabel() {
    return label;
  }

  public void setLabel(final Label value) {
    InvariantChecks.checkNotNull(value);
    this.label = value;
  }

  public BigInteger getAddress() {
    if (null == address) {
      return BigInteger.ZERO;
    }

    return address;
  }

  public void setAddress(final BigInteger value) {
    InvariantChecks.checkNotNull(value);
    this.address = value;
  }

  public boolean hasAddress() {
    return address != null;
  }

  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(final String suffix) {
    InvariantChecks.checkNotNull(suffix);
    InvariantChecks.checkTrue(this.suffix.isEmpty());
    this.suffix = suffix;
  }

  @Override
  public BigInteger getValue() {
    return getAddress();
  }

  @Override
  public String toString() {
    return String.format("LabelValue %d[label=%s, address=%s]",
        System.identityHashCode(this), getName(), getAddress());
  }
}
