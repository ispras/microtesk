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

package ru.ispras.microtesk.mmu.translator.ir.spec.builder;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.microtesk.basis.solver.IntegerVariable;
import ru.ispras.microtesk.mmu.translator.ir.Field;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.Variable;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;

final class IntegerVariableGroup {
  private final String name;
  private final Map<String, IntegerVariable> variables;
  private final MmuBuffer device;

  public IntegerVariableGroup(final MmuBuffer device) {
    checkNotNull(device);

    this.name = device.getName();
    this.variables =  new LinkedHashMap<>();
    this.device = device;

    for (final IntegerVariable variable : device.getFields()) {
      this.variables.put(variable.getName(), variable);
    }
  }

  public IntegerVariableGroup(final Variable variable) {
    checkNotNull(variable);

    final Type type = variable.getType();
    if (type.getFieldCount() == 0) {
      throw new IllegalArgumentException(variable.getId() + " does not have fields.");
    }

    this.name = variable.getId();
    this.variables =  new LinkedHashMap<>();
    this.device = null;

    for (final Field field : type.getFields()) {
      final String variableName = String.format("%s.%s", variable.getId(), field.getId());
      variables.put(field.getId(), new IntegerVariable(variableName, field.getBitSize()));
    }
  }

  private IntegerVariableGroup(final String name,
                               final Map<String, IntegerVariable> variables,
                               final MmuBuffer device) {
    this.name = name;
    this.variables = variables;
    this.device = device;
  }

  public String getName() {
    return name;
  }

  public IntegerVariableGroup rename(final String newName) {
    return new IntegerVariableGroup(newName, this.variables, this.device);
  }

  public Collection<IntegerVariable> getVariables() {
    return Collections.unmodifiableCollection(variables.values());
  }

  public IntegerVariable getVariable(final String variableName) {
    return variables.get(variableName);
  }

  public MmuBuffer getDevice() {
    return device;
  }

  public int size() {
    return variables.size();
  }

  public int getWidth() {
    int width = 0;

    for (final IntegerVariable variable : variables.values()) {
      width += variable.getWidth();
    }

    return width;
  }

  @Override
  public String toString() {
    return String.format("%s[%s]", name, variables);
  }
}
