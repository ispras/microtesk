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

package ru.ispras.microtesk.test.template;

import java.math.BigInteger;

import ru.ispras.fortress.util.InvariantChecks;

public final class Section {
  private final String name;
  private final BigInteger pa;
  private final String args;
  private BigInteger savedPa;

  public Section(final String name, final BigInteger pa, final String args) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(pa);
    InvariantChecks.checkNotNull(args);

    this.name = name;
    this.pa = pa;
    this.args = args;
    this.savedPa = null;
  }

  public String getName() {
    return name;
  }

  public BigInteger getPa() {
    return pa;
  }

  public String getArgs() {
    return args;
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
    return String.format(".section \"%s\", %s (0x%016x)", name, args, pa);
  }
}
