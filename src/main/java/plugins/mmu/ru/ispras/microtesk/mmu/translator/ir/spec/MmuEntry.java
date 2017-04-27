/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.ir.spec;

import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;

/**
 * {@link MmuEntry} represents an entry of a {@link MmuBuffer}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuEntry {
  private BigInteger address = BigInteger.ZERO;

  private final Set<IntegerVariable> validFields = new LinkedHashSet<>();
  private final Map<IntegerVariable, BigInteger> fields = new LinkedHashMap<>();
  private final int sizeInBits;

  private boolean valid = false;

  public MmuEntry(final Collection<IntegerVariable> variables) {
    InvariantChecks.checkNotNull(variables);

    int sizeInBits = 0;
    for (final IntegerVariable variable : variables) {
      fields.put(variable, BigInteger.ZERO);
      sizeInBits += variable.getWidth();
    }

    this.sizeInBits = sizeInBits;
  }

  public BigInteger getAddress() {
    return address;
  }

  public void setAddress(final BigInteger address) {
    this.address = address;
  }

  public Collection<IntegerVariable> getVariables() {
    return fields.keySet();
  }

  public int getSizeInBits() {
    return sizeInBits;
  }

  public boolean isValid() {
    return valid;
  }

  public boolean isValid(final IntegerVariable variable) {
    return validFields.contains(variable);
  }

  public BigInteger getValue(final IntegerVariable variable) {
    InvariantChecks.checkNotNull(variable);
    return fields.get(variable);
  }

  public void setValue(final IntegerVariable variable, final BigInteger value, final boolean valid) {
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(value);

    fields.put(variable, value);

    if (valid) {
      validFields.add(variable);
    } else {
      validFields.remove(variable);
    }
  }

  public void setValue(final IntegerVariable variable, final BigInteger value) {
    setValue(variable, value, true);
  }

  public void setValid(final boolean valid) {
    this.valid = valid;

    if (!valid) {
      validFields.clear();
    }
  }

  private static String getShortName(final IntegerVariable variable) {
    final String fullName = variable.getName();
    final int lastIndex = fullName.lastIndexOf('.');

    return lastIndex == -1 ? fullName : fullName.substring(lastIndex + 1);
  }

  @Override
  public String toString() {
    final String separator = ", ";
    final StringBuilder builder = new StringBuilder();

    builder.append("[");

    boolean comma = false;
    for (final Map.Entry<IntegerVariable, BigInteger> entry : fields.entrySet()) {
      final String name = getShortName(entry.getKey());
      final String value = entry.getValue().toString(16);

      builder.append(comma ? separator : "");
      builder.append(name);
      builder.append("=");
      builder.append(value.length() != 1 ? "0x" : "");
      builder.append(value);
      comma = true;
    }

    builder.append("]");

    return builder.toString();
  }
}
