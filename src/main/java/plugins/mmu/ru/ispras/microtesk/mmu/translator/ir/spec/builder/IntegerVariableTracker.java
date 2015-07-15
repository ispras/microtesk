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

import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.microtesk.basis.solver.IntegerVariable;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.Variable;

final class IntegerVariableTracker {
  public static enum Status {
    VARIABLE,
    GROUP,
    UNDEFINED
  }

  private final Map<String, IntegerVariable> variables;
  private final Map<String, IntegerVariableGroup> variableGroups;

  public IntegerVariableTracker() {
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

  public void undefine(final String name) {
    variables.remove(name);
    variableGroups.remove(name);
  }

  public IntegerVariable getVariable(final String name) {
    return variables.get(name);
  }

  public IntegerVariable getVariable(final String groupName, final String name) {
    final IntegerVariableGroup group = getGroup(groupName);
    return (null != group) ? group.getVariable(name) : null;
  }

  public IntegerVariableGroup getGroup(final String name) {
    return variableGroups.get(name);
  }

  public void defineVariable(final IntegerVariable variable) {
    checkNotNull(variable);
    variables.put(variable.getName(), variable);
  }

  public void defineGroup(final IntegerVariableGroup group) {
    checkNotNull(group);
    variableGroups.put(group.getName(), group);
  }

  public void defineGroupAs(final IntegerVariableGroup group, final String name) {
    checkNotNull(group);
    checkNotNull(name);
    variableGroups.put(name, group);
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
      defineVariable(new IntegerVariable(variable.getId(), variable.getBitSize()));
    } else {
      defineGroup(new IntegerVariableGroup(variable));
    }
  }

  @Override
  public String toString() {
    return String.format("VariableTracker [variables=%s, variableGroups=%s]",
        variables, variableGroups);
  }
}
