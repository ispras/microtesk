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

import java.math.BigInteger;

import ru.ispras.microtesk.model.api.memory.Memory;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.symbols.Where;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.nml.ir.expression.Expr;

public final class MemoryExprFactory extends WalkerFactoryBase {

  public MemoryExprFactory(final WalkerContext context) {
    super(context);
  }

  public MemoryExpr createMemory(
      final Where where,
      final Memory.Kind kind,
      final Type type,
      final Expr sizeExpr,
      final Alias alias) throws SemanticException {

    final BigInteger size = sizeExpr != null ?
        sizeExpr.bigIntegerValue() : BigInteger.ONE;

    if (null == alias) {
      return new MemoryExpr(kind, type, size, null);
    }

    final BigInteger bitSize = size.multiply(BigInteger.valueOf(type.getBitSize()));
    final int aliasBitSize;

    if (Alias.Kind.LOCATION == alias.getKind()) {
      aliasBitSize = alias.getLocation().getType().getBitSize();
    } else { // Alias.Kind.MEMORY == alias.getKind()
      aliasBitSize = (alias.getMax() - alias.getMin() + 1) * 
          alias.getMemory().getType().getBitSize();
    }

    if (!bitSize.equals(BigInteger.valueOf(aliasBitSize))) {
      raiseError(where, String.format(
          "Size of the alias (%d) must be equal to the size of the defined memory (%d).",
          aliasBitSize,
          bitSize
          ));
    }

    return new MemoryExpr(kind, type, size, alias);
  }

  public Alias createAlias(
      final Where where,
      final String memoryId, 
      final Expr min,
      final Expr max) throws SemanticException {

    final MemoryExpr memory = getIR().getMemory().get(memoryId);
    if (null == memory) {
      raiseError(where, memoryId + " is not defined or is not a memory storage.");
    }

    final int minIndex = min.integerValue();
    final int maxIndex = max.integerValue();

    if (!(0 <= minIndex) && (minIndex < memory.getSize().intValue())) {
      raiseError(where, String.format("min (%d) is out of bounds: [0, %d)",
          minIndex, memory.getSize())); 
    }

    if (!(0 <= maxIndex) && (maxIndex < memory.getSize().intValue())) {
      raiseError(where, String.format("max (%d) is out of bounds: [0, %d)",
          maxIndex, memory.getSize()));
    }

    return Alias.forMemory(memoryId, memory, minIndex, maxIndex);
  }
}
