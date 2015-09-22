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
  private final Set<IntegerVariable> validFields = new LinkedHashSet<>();
  private final Map<IntegerVariable, BigInteger> fields = new LinkedHashMap<>();

  private boolean valid = false;

  public MmuEntry(final Collection<IntegerVariable> variables) {
    InvariantChecks.checkNotNull(variables);

    for (final IntegerVariable variable : variables) {
      fields.put(variable, BigInteger.ZERO);
    }
  }

  public Collection<IntegerVariable> getVariables() {
    return fields.keySet();
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

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();

    builder.append("[");

    boolean comma = false; 
    for (final Map.Entry<IntegerVariable, BigInteger> entry : fields.entrySet()) {
      builder.append(comma ? ", " : "");
      builder.append(entry.getKey());
      builder.append("=");
      builder.append(String.format("%s [%s]",
          entry.getValue().toString(16), isValid(entry.getKey()) ? "Valid" : "Invalid"));
      comma = true;
    }

    builder.append("]");

    return builder.toString();
  }
}
