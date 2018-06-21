/*
 * Copyright 2017-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.util.InvariantChecks;

import java.math.BigInteger;

public final class Section {
  private final String name;
  private final boolean standard;

  private final BigInteger basePa;
  private final BigInteger baseVa;
  private final boolean translate;
  private final String args;

  private BigInteger pa;
  private BigInteger savedPa;

  public Section(
      final String name,
      final boolean standard,
      final BigInteger basePa,
      final BigInteger baseVa) {
    this(name, standard, basePa, baseVa, "");
  }

  public Section(
      final String name,
      final boolean standard,
      final BigInteger basePa,
      final BigInteger baseVa,
      final String args) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(basePa);
    InvariantChecks.checkNotNull(baseVa);
    InvariantChecks.checkNotNull(args);

    this.name = name;
    this.standard = standard;

    this.basePa = basePa;
    this.baseVa = baseVa;
    this.translate = !basePa.equals(baseVa);
    this.args = args;

    this.pa = basePa;
    this.savedPa = null;
  }

  public String getName() {
    return name;
  }

  public boolean isStandard() {
    return standard;
  }

  public String getAsmText() {
    if (".data".equals(name) || ".text".equals(name)) {
      return name;
    }

    final StringBuilder sb = new StringBuilder(".section ");
    sb.append(name);

    if (args != null && !args.isEmpty()) {
      sb.append(',');
      sb.append(args);
    }

    return sb.toString();
  }

  public BigInteger getBasePa() {
    return basePa;
  }

  public BigInteger getBaseVa() {
    return baseVa;
  }

  public String getArgs() {
    return args;
  }

  public BigInteger getPa() {
    return pa;
  }

  public void setPa(final BigInteger pa) {
    InvariantChecks.checkNotNull(pa);
    this.pa = pa;
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

  void resetState() {
    pa = basePa;
  }

  void setUseTempState(final boolean value) {
    final boolean isTempStateUsed = savedPa != null;
    if (value) {
      InvariantChecks.checkFalse(isTempStateUsed, "Already in a temp state!");
      savedPa = pa;
    } else {
      InvariantChecks.checkTrue(isTempStateUsed, "Not in a temp state!");
      pa = savedPa;
      savedPa = null;
    }
  }

  @Override
  public String toString() {
    return String.format("%s [pa=0x%016x, va=0x%016x]", getAsmText(), basePa, baseVa);
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
