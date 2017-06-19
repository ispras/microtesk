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

package ru.ispras.microtesk.model.memory;

import java.math.BigInteger;
import ru.ispras.fortress.util.InvariantChecks;

public final class Section {
  private final String text;

  private final BigInteger basePa;
  private final BigInteger baseVa;
  private final boolean translate;

  private BigInteger savedPa;

  public Section(
      final String text,
      final BigInteger basePa,
      final BigInteger baseVa) {
    InvariantChecks.checkNotNull(text);
    InvariantChecks.checkNotNull(basePa);
    InvariantChecks.checkNotNull(baseVa);

    this.text = text;
    this.basePa = basePa;
    this.baseVa = baseVa;
    this.translate = !basePa.equals(baseVa);

    this.savedPa = null;
  }

  public String getText() {
    return text;
  }

  public BigInteger getBasePa() {
    return basePa;
  }

  public BigInteger getBaseVa() {
    return baseVa;
  }

  public BigInteger getSavedPa() {
    InvariantChecks.checkNotNull(savedPa, "Not assigned");
    return savedPa;
  }

  public void setSavedPa(final BigInteger value) {
    InvariantChecks.checkNotNull(value);
    InvariantChecks.checkTrue(null == savedPa, "Already assigned.");
    this.savedPa = value;
  }

  @Override
  public String toString() {
    return String.format("%s [pa=0x%016x, va=0x%016x]", text, basePa, baseVa);
  }

  public BigInteger virtualToPhysical(final BigInteger va) {
    InvariantChecks.checkNotNull(va);
    return translate ? physicalFromOrigin(virtualToOrigin(va)) : va;
  }

  public BigInteger virtualFromOrigin(final BigInteger origin) {
    InvariantChecks.checkNotNull(origin);
    return baseVa.add(origin);
  }

  public BigInteger virtualToOrigin(final BigInteger va) {
    InvariantChecks.checkNotNull(va);
    return va.subtract(baseVa);
  }

  public BigInteger physicalToVirtual(final BigInteger pa) {
    InvariantChecks.checkNotNull(pa);
    return translate ? virtualFromOrigin(physicalToOrigin(pa)) : pa;
  }

  public BigInteger physicalToOrigin(final BigInteger pa) {
    InvariantChecks.checkNotNull(pa);
    return pa.subtract(basePa);
  }

  public BigInteger physicalFromOrigin(final BigInteger origin) {
    InvariantChecks.checkNotNull(origin);
    return basePa.add(origin);
  }
}
