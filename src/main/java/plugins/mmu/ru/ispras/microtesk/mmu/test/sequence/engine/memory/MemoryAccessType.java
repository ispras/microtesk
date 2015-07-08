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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.spec.basis.DataType;
import ru.ispras.microtesk.mmu.translator.ir.spec.basis.MemoryOperation;

/**
 * {@link MemoryAccessType} describes a memory access type, which is an operation (load or store) in
 * couple with a block size (byte, word, etc.).
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAccessType {
  private final MemoryOperation operation;
  private final DataType dataType;

  public MemoryAccessType(final MemoryOperation operation, final DataType dataType) {
    InvariantChecks.checkNotNull(operation);
    InvariantChecks.checkNotNull(dataType);

    this.operation = operation;
    this.dataType = dataType;
  }

  public MemoryOperation getOperation() {
    return operation;
  }

  public DataType getDataType() {
    return dataType;
  }

  @Override
  public String toString() {
    return String.format("%s[%s]", operation, dataType);
  }
}
