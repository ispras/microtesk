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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerVariable;

public final class VariableTracker {

  public static enum Status {
    VARIABLE,
    GROUP,
    UNDEFINED
  }

  private Map<String, IntegerVariable> variables;
  private Map<String, Map<String, IntegerVariable>> variableGroups;

  public VariableTracker() {
    this.variables = new HashMap<>();
    this.variableGroups = new HashMap<>();
  }

  public Status checkDefined(String name) {
    if (variableGroups.containsKey(name)) {
      return Status.GROUP;
    }

    if (variables.containsKey(name)) {
      return Status.VARIABLE;
    }

    return Status.UNDEFINED;
  }

  public IntegerVariable getVariable(String name) {
    return variables.get(name);
  }

  public Map<String, IntegerVariable> getGroup(String name) {
    return variableGroups.get(name);
  }

  public void defineVariable(IntegerVariable variable) {
    checkNotNull(variable);
    variables.put(variable.getName(), variable);
  }

  public void defineVariableAs(IntegerVariable variable, String name) {
    checkNotNull(variable);
    checkNotNull(name);
    variables.put(name, variable);
  }

  public void undefineVariable(String name) {
    variables.remove(name);
  }
}
