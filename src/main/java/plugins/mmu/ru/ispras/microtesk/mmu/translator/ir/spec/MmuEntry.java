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
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;

/**
 * {@link MmuEntry} represents an entry of a {@link MmuBuffer}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuEntry {
  private final Map<IntegerVariable, BigInteger> fields = new LinkedHashMap<>();

  private boolean valid = false;

  public MmuEntry(final Collection<IntegerVariable> variables) {
    InvariantChecks.checkNotNull(variables);

    for (final IntegerVariable variable : variables) {
      fields.put(variable, BigInteger.ZERO);
    }
  }

  public BigInteger getValue(final IntegerVariable variable) {
    InvariantChecks.checkNotNull(variable);
    return fields.get(variable);
  }

  public void setValue(final IntegerVariable variable, final BigInteger value) {
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(value);

    fields.put(variable, value);
  }

  public void setValue(final IntegerVariable variable, final long value) {
    setValue(variable, BigInteger.valueOf(value));
  }

  public boolean isValid() {
    return valid;
  }

  public void setValid(final boolean valid) {
    this.valid = valid;
  }

  @Override
  public String toString() {
    return fields.toString();
  }
}
