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

package ru.ispras.microtesk.mmu.translator.coverage;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddress;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCondition;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuDevice;

/**
 * This class describes a device usage conflict, which is a simple dependency between two
 * instructions.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryHazard {

  /**
   * This enumeration contains conflict types.
   */
  public static enum Type {
    /** The conflict of the kind {@code Address1 != Address2}. */
    ADDR_NOT_EQUAL("AddrNotEqual", false),
    /** The conflict of the kind {@code Address1 == Address2}. */
    ADDR_EQUAL("AddrEqual", true),

    /** The conflict of the kind {@code Index1 != Index2}. */
    INDEX_NOT_EQUAL("IndexNotEqual", false),
    /** The conflict of the kind {@code Index1 == Index2 && Tag1 != Tag2}. */
    TAG_NOT_EQUAL("TagNotEqual", false),
    /** The conflict of the kind {@code Index1 == Index2 && Tag1 != Tag2 && Tag1 != Replaced2}. */
    TAG_NOT_REPLACED("TagNotReplaced", false),
    /** The conflict of the kind {@code Index1 == Index2 && Tag1 != Tag2 && Tag1 == Replaced2}. */
    TAG_REPLACED("TagReplaced", true),
    /** The conflict of the kind {@code Index1 == Index2 && Tag1 == Tag2}. */
    TAG_EQUAL("TagEqual", true);

    /** The conflict type name. */
    private final String name;
    /** The equality/inequality flag. */
    private final boolean equal;

    /**
     * Constructs a conflict type.
     * 
     * @param name the conflict name.
     * @param equal the equality/inequality flag.
     */
    private Type(final String name, final boolean equal) {
      this.name = name;
      this.equal = equal;
    }

    /**
     * Returns the name of the conflict.
     * 
     * @return the conflict name.
     */
    public String getName() {
      return name;
    }

    /**
     * Checks whether the conflict is expressed as an equality.
     * 
     * @return {@code true} if the conflict is expressed as an equality; {@code false} otherwise.
     */
    public boolean isEquality() {
      return equal;
    }
  }

  /** The conflict type. */
  private final Type type;
  /** The address. */
  private final MmuAddress address;
  /** The device being used. */
  private final MmuDevice device;
  /** The list of address condition. */
  private final MmuCondition condition;

  /**
   * Constructs a device conflict.
   * 
   * @param type the conflict type.
   * @param device the device being used.
   * @param condition the condition.
   * @throws IllegalArgumentException if some parameters are null.
   */
  public MemoryHazard(final Type type, final MmuDevice device, final MmuCondition condition) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(device);
    InvariantChecks.checkNotNull(condition);

    this.type = type;
    this.address = null;
    this.device = device;
    this.condition = condition;
  }

  /**
   * Constructs an address space conflict.
   * 
   * @param type the conflict type.
   * @param address the address being used.
   * @param condition the condition.
   * @throws IllegalArgumentException if some parameters are null.
   */
  public MemoryHazard(final Type type, final MmuAddress address, final MmuCondition condition) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(address);
    InvariantChecks.checkNotNull(condition);

    this.type = type;
    this.address = address;
    this.device = null;
    this.condition = condition;
  }

  /**
   * Returns the conflict type.
   * 
   * @return the conflict type.
   */
  public Type getType() {
    return type;
  }

  /**
   * Returns the conflict name.
   * 
   * @return the conflict name.
   */
  public String getName() {
    return type.getName();
  }

  /**
   * Returns the conflict name extended with the device or address space name.
   * 
   * @return the full conflict name.
   */
  public String getFullName() {
    if (device != null) {
      return device.getName() + type.getName();
    }

    if (address != null) {
      return address.getVariable().getName() + type.getName();
    }

    return getName();
  }

  /**
   * Returns the device of the conflict.
   * 
   * @return the device.
   */
  public MmuDevice getDevice() {
    return device;
  }

  /**
   * Returns the address of the conflict.
   * 
   * @return the address.
   */
  public MmuAddress getAddress() {
    return address;
  }

  /**
   * Returns the address condition of the conflict.
   * 
   * @return the list of address equalities/inequalities.
   */
  public MmuCondition getCondition() {
    return condition;
  }

  @Override
  public int hashCode() {
    return getFullName().hashCode();
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }

    if (o == null || !(o instanceof MemoryHazard)) {
      return false;
    }

    final MemoryHazard r = (MemoryHazard) o;

    return getFullName().equals(r.getFullName());
  }

  @Override
  public String toString() {
    return getFullName();
  }
}
