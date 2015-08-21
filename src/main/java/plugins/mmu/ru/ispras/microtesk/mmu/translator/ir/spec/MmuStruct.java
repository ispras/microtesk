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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;

/**
 * The {@link MmuStruct} class describes a variable represented
 * by a structure (a list of {@link IntegerVariable} objects).
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public class MmuStruct {
  private final String name;
  private final MmuBuffer device;

  private final List<IntegerVariable> fields = new ArrayList<>();
  private int bitSize = 0;

  /**
   * Constructs an MmuStruct object.
   * 
   * @param name structure name.
   * @param variables field variables.
   * 
   * @throws IllegalArgumentException if {@code name} equals {@code null} or
   *         if any of the field variables equals {@code null}.
   */
  public MmuStruct(
      final String name,
      final IntegerVariable... variables) {
    this(name, null, variables);
  }

  /**
   * Constructs an MmuStruct object.
   * 
   * @param name structure name.
   * @param device device associated with the structure.
   * @param variables field variables.
   * 
   * @throws IllegalArgumentException if {@code name} equals {@code null} or
   *         if any of the field variables equals {@code null}.
   */
  public MmuStruct(
      final String name,
      final MmuBuffer device,
      final IntegerVariable... variables) {
    InvariantChecks.checkNotNull(name);

    this.name = name;
    this.device = device;

    for (final IntegerVariable variable : variables) {
      addField(variable);
    }
  }

  /**
   * Returns the structure name.
   * 
   * @return structure name.
   */
  public final String getName() {
    return name;
  }

  /**
   * Returns the device associated with the structure (for buffer entries)
   * or {@code null} if not applicable.
   * 
   * @return the device associated with the structure or {@code null} if not applicable.
   */
  public MmuBuffer getDevice() {
    return device;
  }

  /**
   * Returns structure fields.
   * 
   * @return the list of structure fields.
   */
  public final List<IntegerVariable> getFields() {
    return Collections.unmodifiableList(fields);
  }

  /**
   * Registers an new field.
   * 
   * @param field a field to be registered.
   * 
   * @throws IllegalArgumentException if {@code field == null}.
   */
  public final void addField(final IntegerVariable field) {
    InvariantChecks.checkNotNull(field);
    fields.add(field);
    bitSize += field.getWidth();
  }

  /**
   * Returns the number of fields in the structure.
   * 
   * @return number of fields in the structure.
   */
  public final int getFieldCount() {
    return fields.size();
  }

  /**
   * Returns the total size of all fields in the structure.
   * 
   * @return structure size in bits.
   */
  public final int getBitSize() {
    return bitSize;
  }

  @Override
  public String toString() {
    return name;
  }
}
