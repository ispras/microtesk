/*
 * Copyright 2017-2020 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.microtesk.test.template.Where;

import java.math.BigInteger;

public final class Section {
  private final Where where;
  private final String name;
  private final String prefix;
  private final boolean standard;

  private final BigInteger basePa;
  private final BigInteger baseVa;
  private final boolean translate;
  private final String args;

  private final boolean file;

  private BigInteger pa;
  private BigInteger savedPa;

  public Section(
      final Where where,
      final String name,
      final String prefix,
      final boolean standard,
      final BigInteger basePa,
      final BigInteger baseVa) {
    this(where, name, prefix, standard, basePa, baseVa, "", false);
  }

  public Section(
      final Where where,
      final String name,
      final String prefix,
      final boolean standard,
      final BigInteger basePa,
      final BigInteger baseVa,
      final String args,
      final boolean file) {
    InvariantChecks.checkNotNull(where);
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(prefix);
    InvariantChecks.checkTrue(basePa != null && basePa.compareTo(BigInteger.ZERO) >= 0);
    InvariantChecks.checkTrue(baseVa != null && baseVa.compareTo(BigInteger.ZERO) >= 0);
    InvariantChecks.checkNotNull(args);

    this.where = where;
    this.name = name;
    this.prefix = prefix;
    this.standard = standard;

    this.basePa = basePa;
    this.baseVa = baseVa;
    this.translate = !basePa.equals(baseVa);
    this.args = args;

    this.file = file;

    this.pa = basePa;
    this.savedPa = null;
  }

  public Where getWhere() {
    return where;
  }

  public String getName() {
    return name;
  }

  public String getPrefix() {
    return prefix;
  }

  public boolean isStandard() {
    return standard;
  }

  public String getAsmText() {
    final StringBuilder sb = new StringBuilder();

    if (!prefix.isEmpty()) {
      sb.append(prefix);
      sb.append(' ');
    }

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

  public boolean isSeparateFile() {
    return file;
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
