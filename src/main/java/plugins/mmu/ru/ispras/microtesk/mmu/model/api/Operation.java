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

package ru.ispras.microtesk.mmu.model.api;

import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.GenerationAbortedException;

/**
 * The {@link Operation} class describes objects responsible for initializing
 * fields of an address passed to the MMU simulator when simulation of a memory
 * access is started. Each {@code Operation} object is associated with a specific
 * operation defined in the ISA model and is called when a memory access has been
 * initiated by that ISA operation.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 *
 * @param <A> the address type.
 */

public abstract class Operation<A extends Address & Data> {
  public abstract void init(final A address);

  private static final Map<String, Operation<? extends Address>> INSTANCES = new HashMap<>();

  protected static <A extends Address & Data> void register(final Operation<A> operation) {
    InvariantChecks.checkNotNull(operation);
    final String operationName = operation.getClass().getSimpleName();
    INSTANCES.put(operationName, operation);
  }

  private static String getCurrentOperation() {
    return ru.ispras.microtesk.model.api.instruction.Operation.getCurrentOperation();
  }

  @SuppressWarnings("unchecked")
  public static <A extends Address & Data> void initAddress(final A address) {
    InvariantChecks.checkNotNull(address);

    final String operationId = getCurrentOperation();
    InvariantChecks.checkNotNull(operationId, "No operations on call stack.");

    final Operation<? extends Address> operation = INSTANCES.get(operationId);
    if (null != operation) {
      ((Operation<A>) operation).init(address);
      return;
    }

    // An address initializer is needed when there are uninitialized fields.
    // This means fields other than "value", which is the only field initialized by default.
    final boolean isInitialized = address.asBitVector().equals(address.getValue());
    if (!isInitialized) {
      throw new GenerationAbortedException(String.format(
          "No address initializer is defined for the %s operation.", operationId));
    }
  }
}
