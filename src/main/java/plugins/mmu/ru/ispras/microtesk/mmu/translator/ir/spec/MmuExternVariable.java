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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.model.api.state.Reader;

public final class MmuExternVariable {
  private final IntegerVariable variable;

  public MmuExternVariable(
      final String name,
      final int width,
      final Reader reader) {
    this.variable = new RedefinableIntegerVariable(name, width, reader);
  }

  public IntegerVariable get() {
    return variable;
  }

  @Override
  public String toString() {
    return String.format("MmuExternVariable [variable=%s]", variable);
  }

  private static class RedefinableIntegerVariable extends IntegerVariable {
    private final Reader reader;

    public RedefinableIntegerVariable(
        final String name,
        final int width,
        final Reader reader) {
      super(name, width);

      InvariantChecks.checkNotNull(reader);
      this.reader = reader;
    }

    @Override
    public BigInteger getValue() {
      return reader.read().bigIntegerValue(false);
    }
  }
}
