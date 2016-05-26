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

import ru.ispras.fortress.util.InvariantChecks;

public final class PrimitiveInfo {
  private Boolean exception;

  private Boolean branch;
  private Boolean conditionalBranch;

  private Boolean memoryReference;
  private Boolean load;
  private Boolean store;
  private Integer blockSize;

  public boolean canThrowException() {
    checkInitialized(exception, "exception");
    return exception;
  }

  public boolean isBranch() {
    checkInitialized(branch, "branch");
    return branch;
  }

  public boolean isConditionalBranch() {
    checkInitialized(conditionalBranch, "conditionalBranch");
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
    checkReinitialized(exception, "exception");
    this.exception = value;
  }

  public void setBranch(final boolean value) {
    checkReinitialized(branch, "branch");
    this.branch = value;

    if (!value) {
      conditionalBranch = false;
    }
  }

  public void setConditionalBranch(final boolean value) {
    checkReinitialized(conditionalBranch, "conditionalBranch");
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

  @Override
  public String toString() {
    return String.format(
        "PrimitiveInfo [exception=%s, branch=%s, conditionalBranch=%s, " +
        "memoryReference=%s, load=%s, store=%s, blockSize=%s]",
        exception,
        branch,
        conditionalBranch,
        memoryReference,
        load,
        store,
        blockSize
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
