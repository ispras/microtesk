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

package ru.ispras.microtesk.translator.mmu.spec.builder;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.microtesk.translator.mmu.ir.Field;
import ru.ispras.microtesk.translator.mmu.ir.Type;
import ru.ispras.microtesk.translator.mmu.ir.Variable;
import ru.ispras.microtesk.translator.mmu.spec.MmuDevice;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerVariable;

final class IntegerVariableGroup {
  private final String name;
  private final Map<String, IntegerVariable> variables;
  private final MmuDevice device;

  public IntegerVariableGroup(final MmuDevice device) {
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

  public String getName() {
    return name;
  }

  public Collection<IntegerVariable> getVariables() {
    return Collections.unmodifiableCollection(variables.values());
  }

  public IntegerVariable getVariable(final String variableName) {
    return variables.get(variableName);
  }

  public MmuDevice getDevice() {
    return device;
  }

  public int size() {
    return variables.size();
  }

  @Override
  public String toString() {
    return String.format("%s[%s]", name, variables);
  }
}
