/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.shared;

import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.nml.ir.expression.Expr;
import ru.ispras.microtesk.translator.nml.ir.location.Location;

public final class MemoryExprFactory extends WalkerFactoryBase {
  private static final String ERROR_INVALID_SIZE = 
      "Size of the alias (%d) must be equal to the size of the defined memory (%d).";

  private static final Expr DEFAULT_SIZE = Expr.newConstant(1);

  public MemoryExprFactory(WalkerContext context) {
    super(context);
  }

  public MemoryExpr createMemory(
      Where where,
      Memory.Kind kind,
      Type type,
      Expr size,
      Location alias) throws SemanticException {

    if (null == size) {
      size = DEFAULT_SIZE;
    }

    if (null != alias) {
      final int memoryBitSize = type.getBitSize() * size.integerValue();
      final int aliasBitSize = alias.getType().getBitSize();

      if (memoryBitSize != aliasBitSize) {
        raiseError(where, String.format(ERROR_INVALID_SIZE, aliasBitSize, memoryBitSize));
      }
    }

    return new MemoryExpr(
        kind, type, size, null != alias ? Alias.forLocation(alias) : null);
  }
}
