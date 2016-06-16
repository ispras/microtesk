/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.primitive;

import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.ArgumentMode;

public final class PrimitiveInfo {
  private boolean exception;

  private boolean branch;
  private boolean conditionalBranch;

  private Boolean memoryReference;
  private Boolean load;
  private Boolean store;
  private Integer blockSize;

  private Map<String, ArgumentMode> argsUsage;

  public PrimitiveInfo() {
    this.exception = false;
    this.branch = false;
    this.conditionalBranch = false;
    this.memoryReference = null;
    this.load = null;
    this.store = null;
    this.blockSize = null;
    this.argsUsage = new HashMap<>();
  }

  public PrimitiveInfo(final PrimitiveInfo other) {
    InvariantChecks.checkNotNull(other);

    this.exception = other.exception;
    this.branch = other.branch;
    this.conditionalBranch = other.conditionalBranch;
    this.memoryReference = other.memoryReference;
    this.load = other.load;
    this.store = other.store;
    this.blockSize = other.blockSize;
    this.argsUsage = new HashMap<>(other.argsUsage);
  }

  public boolean canThrowException() {
    return exception;
  }

  public boolean isBranch() {
    return branch;
  }

  public boolean isConditionalBranch() {
    return conditionalBranch;
  }

  public boolean isMemoryReference() {
    checkInitialized(memoryReference, "memoryReference");
    return memoryReference;
  }

  public boolean isLoad() {
    checkInitialized(load, "load");
    return load;
  }

  public boolean isStore() {
    checkInitialized(store, "store");
    return store;
  }

  public int getBlockSize() {
    checkInitialized(blockSize, "blockSize");
    return blockSize;
  }

  public void setCanThrowException(final boolean value) {
    this.exception = value;
  }

  public void setBranch(final boolean value) {
    this.branch = value;

    if (!value) {
      conditionalBranch = false;
    }
  }

  public void setConditionalBranch(final boolean value) {
    this.conditionalBranch = value;

    if (value) {
      branch = true;
    }
  }

  public void setMemoryReference(final boolean value) {
    checkReinitialized(memoryReference, "memoryReference");
    this.memoryReference = value;
  }

  public void setLoad(final boolean value) {
    checkReinitialized(load, "load");
    this.load = value;
  }

  public void setStore(final boolean value) {
    checkReinitialized(store, "store");
    this.store = value;
  }

  public void setBlockSize(final int value) {
    InvariantChecks.checkGreaterOrEqZero(value);
    checkReinitialized(blockSize, "blockSize");
    blockSize = value;
  }

  public ArgumentMode getArgUsage(final String name) {
    final ArgumentMode result = argsUsage.get(name);
    return result != null ? result : ArgumentMode.NA;
  }

  public void setArgUsage(final String name, final ArgumentMode usage) {
    InvariantChecks.checkTrue(usage == ArgumentMode.IN || usage == ArgumentMode.OUT);
    final ArgumentMode prevUsage = argsUsage.get(name);

    if (prevUsage == null) { // Argument has not been used yet
      argsUsage.put(name, usage);
      return;
    }

    if (prevUsage == usage || // Same access type
        prevUsage == ArgumentMode.INOUT || // Already marked as both IN and OUT
        prevUsage == ArgumentMode.OUT && usage == ArgumentMode.IN) { // IN after OUT means OUT
      return;
    }

    if (prevUsage == ArgumentMode.IN && usage == ArgumentMode.OUT) { // OUT after IN means INOUT
      argsUsage.put(name, ArgumentMode.INOUT);
      return;
    }

    throw new IllegalStateException(
        String.format("Argument %s: usage=%s, prevUsage=%s", name, usage, prevUsage));
  }

  @Override
  public String toString() {
    return String.format(
        "PrimitiveInfo [exception=%s, branch=%s, conditionalBranch=%s, " +
        "memoryReference=%s, load=%s, store=%s, blockSize=%s, argsUsage=%s]",
        exception,
        branch,
        conditionalBranch,
        memoryReference,
        load,
        store,
        blockSize,
        argsUsage
        );
  }

  private static void checkInitialized(final Object field, final String fieldName) {
    if (!isInitialized(field)) {
      throw new IllegalStateException(fieldName + " is not initialized!");
    }
  }

  private static void checkReinitialized(final Object field, final String fieldName) {
    if (isInitialized(field)) {
      throw new IllegalStateException(fieldName + " is not reinitialized!");
    }
  }

  private static boolean isInitialized(final Object field) {
    return null != field;
  }
}
