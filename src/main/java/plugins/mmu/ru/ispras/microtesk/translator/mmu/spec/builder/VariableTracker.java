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

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import ru.ispras.microtesk.translator.mmu.ir.Field;
import ru.ispras.microtesk.translator.mmu.ir.Type;
import ru.ispras.microtesk.translator.mmu.ir.Variable;

import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerVariable;

public final class VariableTracker {

  public static enum Status {
    VARIABLE,
    GROUP,
    UNDEFINED
  }

  private final Map<String, IntegerVariable> variables;
  private final Map<String, Map<String, IntegerVariable>> variableGroups;

  public VariableTracker() {
    this.variables = new LinkedHashMap<>();
    this.variableGroups = new LinkedHashMap<>();
  }

  public Status checkDefined(final String name) {
    if (variableGroups.containsKey(name)) {
      return Status.GROUP;
    }

    if (variables.containsKey(name)) {
      return Status.VARIABLE;
    }

    return Status.UNDEFINED;
  }

  public IntegerVariable getVariable(final String name) {
    return variables.get(name);
  }

  public IntegerVariable getVariable(final String groupName, final String name) {
    final Map<String, IntegerVariable> group = getGroup(groupName);
    return (null != group) ? group.get(name) : null;
  }

  public Map<String, IntegerVariable> getGroup(final String name) {
    return variableGroups.get(name);
  }

  public void undefineVariable(final String name) {
    variables.remove(name);
  }

  public void defineVariable(final IntegerVariable variable) {
    checkNotNull(variable);
    variables.put(variable.getName(), variable);
  }

  public void defineVariableAs(final IntegerVariable variable, final String name) {
    checkNotNull(variable);
    checkNotNull(name);
    variables.put(name, variable);
  }

  public void defineVariable(final Variable variable) {
    checkNotNull(variable);

    final Type type = variable.getType();
    if (type.getFieldCount() == 0) {
      variables.put(variable.getId(), new IntegerVariable(variable.getId(), variable.getBitSize()));
      return;
    }

    final Map<String, IntegerVariable> group = new LinkedHashMap<>();
    variableGroups.put(variable.getId(), group);

    for (Field field : type.getFields()) {
      final String name = String.format("%s.%s", variable.getId(), field.getId());
      group.put(field.getId(), new IntegerVariable(name, field.getBitSize()));
    }
  }

  public void defineGroup(final String name, final List<IntegerVariable> variables) {
    checkNotNull(name);
    checkNotNull(variables);

    final Map<String, IntegerVariable> group = new LinkedHashMap<>();
    variableGroups.put(name, group);

    for (IntegerVariable variable : variables) {
      group.put(variable.getName(), variable);
    }
  }

  @Override
  public String toString() {
    return String.format("VariableTracker [variables=%s, variableGroups=%s]",
        variables, variableGroups);
  }
}
