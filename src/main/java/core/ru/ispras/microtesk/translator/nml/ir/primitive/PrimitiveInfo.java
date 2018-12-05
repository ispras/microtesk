/*
 * Copyright 2016-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.ArgumentMode;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public final class PrimitiveInfo {
  private boolean exception;

  private boolean branch;
  private boolean conditionalBranch;
  // The condition for the branch operation.
  private Node conditionForBranch;

  private boolean memoryReference;
  private Boolean load;
  private Boolean store;
  private Integer blockSize;

  private Map<String, ArgumentMode> argsUsage;
  private Map<Class<?>, Object> attributes;

  public PrimitiveInfo() {
    this.exception = false;
    this.branch = false;
    this.conditionalBranch = false;
    this.conditionForBranch = null;
    this.memoryReference = false;
    this.load = null;
    this.store = null;
    this.blockSize = null;
    this.argsUsage = new HashMap<>();
    this.attributes = new IdentityHashMap<>();
  }

  public PrimitiveInfo(final PrimitiveInfo other) {
    InvariantChecks.checkNotNull(other);

    this.exception = other.exception;
    this.branch = other.branch;
    this.conditionalBranch = other.conditionalBranch;
    this.conditionForBranch = other.conditionForBranch;
    this.memoryReference = other.memoryReference;
    this.load = other.load;
    this.store = other.store;
    this.blockSize = other.blockSize;
    this.argsUsage = new HashMap<>(other.argsUsage);
    this.attributes = new IdentityHashMap<>(other.attributes);
  }

  public boolean canThrowException() {
    return exception;
  }

  public void setCanThrowException(final boolean value) {
    this.exception = value;
  }

  public boolean isBranch() {
    return branch;
  }

  public void setBranch(final boolean value) {
    this.branch = value;

    if (!value) {
      conditionalBranch = false;
    }
  }

  public boolean isConditionalBranch() {
    return conditionalBranch;
  }

  public void setConditionalBranch(final boolean value) {
    this.conditionalBranch = value;

    if (value) {
      branch = true;
    }
  }

  /**
   * Sets the branch condition for the branch instruction.
   *
   * @param value {@code Node} where the branch condition and the operands are located.
   */
  public void setConditionForBranch(final Node value) {
    this.conditionForBranch = value;
  }

  /**
   * Returns the branch condition for the branch instruction.
   *
   * @return {@code Node} where the branch condition and the operands are located.
   */
  public Node getConditionForBranch() {
    return this.conditionForBranch;
  }

  public boolean isMemoryReference() {
    return memoryReference;
  }

  public void setMemoryReference(final boolean value) {
    this.memoryReference = value;
  }

  public boolean isLoad() {
    checkInitialized(load, "load");
    return load;
  }

  public void setLoad(final boolean value) {
    checkReinitialized(load, "load");
    this.load = value;
  }

  public boolean isStore() {
    checkInitialized(store, "store");
    return store;
  }

  public void setStore(final boolean value) {
    checkReinitialized(store, "store");
    this.store = value;
  }

  public int getBlockSize() {
    checkInitialized(blockSize, "blockSize");
    return blockSize;
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
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(usage);

    if (usage == ArgumentMode.NA) {
      return;
    }

    final ArgumentMode prevUsage = argsUsage.get(name);
    InvariantChecks.checkTrue(usage.isIn() || usage.isOut(), usage.toString());

    if (prevUsage == null) { // Argument has not been used yet
      argsUsage.put(name, usage);
      return;
    }

    if (prevUsage == usage || // Same access type
        prevUsage == ArgumentMode.INOUT || // Already marked as both IN and OUT
        prevUsage == ArgumentMode.OUT && (usage == ArgumentMode.IN ||     // IN or INOUT
                                          usage == ArgumentMode.INOUT)) { // after OUT means OUT
      return;
    }

    if (prevUsage == ArgumentMode.IN && usage == ArgumentMode.OUT) { // OUT after IN means INOUT
      argsUsage.put(name, ArgumentMode.INOUT);
      return;
    }

    throw new IllegalStateException(
        String.format("Argument %s: usage=%s, prevUsage=%s", name, usage, prevUsage));
  }

  public void setAttribute(final Object attribute) {
    InvariantChecks.checkNotNull(attribute);
    attributes.put(attribute.getClass(), attribute);
  }

  public Object getAttribute(final Class<?> attributeClass) {
    return attributes.get(attributeClass);
  }

  @Override
  public String toString() {
    return String.format(
        "PrimitiveInfo [exception=%s, branch=%s, conditionalBranch=%s, conditionForBranch=%s, "
            + "memoryReference=%s, load=%s, store=%s, blockSize=%s, argsUsage=%s]",
        exception,
        branch,
        conditionalBranch,
        conditionForBranch,
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
