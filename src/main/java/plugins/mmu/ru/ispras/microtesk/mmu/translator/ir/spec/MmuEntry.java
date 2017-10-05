/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link MmuEntry} represents an entry of a {@link MmuBuffer}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuEntry {
  private BitVector address;

  private final Set<Variable> validFields = new LinkedHashSet<>();
  private final Map<Variable, BitVector> fields = new LinkedHashMap<>();
  private final int sizeInBits;

  private boolean valid = false;

  public MmuEntry(final Collection<Variable> variables) {
    InvariantChecks.checkNotNull(variables);

    int sizeInBits = 0;
    for (final Variable variable : variables) {
      fields.put(variable, BitVector.newEmpty(variable.getType().getSize()));
      sizeInBits += variable.getType().getSize();
    }

    this.sizeInBits = sizeInBits;
  }

  public BitVector getAddress() {
    return address;
  }

  public void setAddress(final BitVector address) {
    this.address = address;
  }

  public Collection<Variable> getVariables() {
    return fields.keySet();
  }

  public int getSizeInBits() {
    return sizeInBits;
  }

  public boolean isValid() {
    return valid;
  }

  public boolean isValid(final Variable variable) {
    return validFields.contains(variable);
  }

  public BitVector getValue(final Variable variable) {
    InvariantChecks.checkNotNull(variable);
    return fields.get(variable);
  }

  public void setValue(final Variable variable, final BitVector value, final boolean valid) {
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(value);

    fields.put(variable, value);

    if (valid) {
      validFields.add(variable);
    } else {
      validFields.remove(variable);
    }
  }

  public void setValue(final Variable variable, final BitVector value) {
    setValue(variable, value, true);
  }

  public void setValid(final boolean valid) {
    this.valid = valid;

    if (!valid) {
      validFields.clear();
    }
  }

  private static String getShortName(final Variable variable) {
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
    for (final Map.Entry<Variable, BitVector> entry : fields.entrySet()) {
      final String name = getShortName(entry.getKey());
      final String value = entry.getValue().toHexString();

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
