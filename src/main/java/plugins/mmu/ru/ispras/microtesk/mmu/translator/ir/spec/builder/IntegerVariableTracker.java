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
import static ru.ispras.microtesk.mmu.translator.ir.spec.builder.ScopeStorage.dotConc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
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
  private final Map<String, Variable> sources = new HashMap<>();
  private final Map<String, Variable> aliased = new HashMap<>();
  private final Map<String, Variable> globals = new HashMap<>();

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
    if (type.isStruct()) {
      defineGroup(new IntegerVariableGroup(variable));
    } else {
      defineVariable(new IntegerVariable(variable.getName(), variable.getBitSize()));
    }
  }

  public void declare(final Variable source) {
    checkNotNull(source);
    
    if (source.isStruct()) {
      for (final Variable field : source.getFields().values()) {
        declare(field);
      }
    } else {
      defineVariable(new IntegerVariable(source.getName(), source.getBitSize()));
    }
    sources.put(source.getName(), source);
  }

  public void declareGlobal(final Variable source) {
    declare(source);
    insertGlobal(source);
  }

  private void insertGlobal(final Variable source) {
    globals.put(source.getName(), source);
    for (final Variable field : source.getFields().values()) {
      insertGlobal(field);
    }
  }

  public boolean isGlobal(final Variable source) {
    return globals.containsKey(source.getName());
  }

  public void createAlias(final Variable source, final String alias) {
    checkNotNull(source);
    checkNotNull(alias);

    if (source.isStruct()) {
      for (final Map.Entry<String, Variable> entry : source.getFields().entrySet()) {
        createAlias(entry.getValue(), dotConc(alias, entry.getKey()));
      }
    } else {
      defineVariableAs(variables.get(source.getName()), alias);
    }
    aliased.put(alias, source);
  }

  public IntegerVariable get(final Variable variable) {
    final Variable alias = aliased.get(variable.getName());
    if (alias != null) {
      return get(alias);
    }
    return variables.get(variable.getName());
  }

  public void removeAlias(final String alias) {
    removeAlias(aliased.get(alias), alias);
  }

  private void removeAlias(final Variable source, final String alias) {
    if (source.isStruct()) {
      for (final Map.Entry<String, Variable> entry : source.getFields().entrySet()) {
        removeAlias(entry.getValue(), dotConc(alias, entry.getKey()));
      }
    } else {
      variables.remove(alias);
    }
    aliased.remove(alias);
  }

  public Variable getOrCreate(final String name, final Variable image) {
    final Variable stored = sources.get(name);
    if (stored != null) {
      return stored;
    }
    final Variable created =
        image.getName().equals(name) ? image : image.rename(name);
    sources.put(name, created);
    return created;
  }

  @Override
  public String toString() {
    return String.format("VariableTracker [variables=%s, variableGroups=%s]",
        variables, variableGroups);
  }
}
