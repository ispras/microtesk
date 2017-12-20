/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.coverage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.util.InvariantChecks;

public final class SsaScopeFactory {
  private static final SsaScope EMPTY_SCOPE = SsaScopeEmpty.get();
  private SsaScopeFactory() {}

  public static SsaScope createScope() {
    return new SsaVariableScope(EMPTY_SCOPE);
  }

  public static SsaScope collapse(SsaScope input) {
    InvariantChecks.checkNotNull(input);

    if (input.getClass() != SsaVariableScope.class) {
      return input;
    }

    final SsaVariableScope scope = (SsaVariableScope) input;
    if (scope.getParent() == EMPTY_SCOPE) {
      return scope;
    }

    final SsaVariableScope parent = (SsaVariableScope) scope.getParent();
    parent.getVersions().putAll(scope.getVersions());

    final Set<String> locals = new HashSet<>(scope.getOuters().keySet());
    locals.retainAll(parent.getLocals().keySet());
    for (String name : locals) {
      parent.getLocals().put(name, scope.getOuters().get(name));
    }

    final Set<String> outers = new HashSet<>(scope.getOuters().keySet());
    outers.removeAll(locals);
    for (String name : outers) {
      parent.getOuters().put(name, scope.getOuters().get(name));
    }

    return parent;
  }

  public static SsaScope collapseAll(SsaScope scope) {
    SsaScope current = null;

    do {
      current = scope;
      scope = collapse(scope);
    } while (current != scope);

    return scope;
  }
}

class SsaVariableScope implements SsaScope {
  protected static final String NAME_FORMAT = "%s!%d";

  protected final SsaScope parent;
  protected final Map<String, Integer> versions;
  protected final Map<String, NodeVariable> locals;
  protected final Map<String, NodeVariable> outers;

  SsaVariableScope(final SsaScope parent) {
    this.parent = parent;
    this.versions = new HashMap<>();
    this.locals = new HashMap<>();
    this.outers = new HashMap<>();
  }

  public SsaScope getParent() {
    return parent;
  }

  public Map<String, Integer> getVersions() {
    return versions;
  }

  public Map<String, NodeVariable> getLocals() {
    return locals;
  }

  public Map<String, NodeVariable> getOuters() {
    return outers;
  }

  @Override
  public boolean contains(final String name) {
    return locals.containsKey(name) || outers.containsKey(name) || parent.contains(name);
  }

  @Override
  public NodeVariable create(final String name, final Data data) {
    InvariantChecks.checkNotNull(data);

    if (locals.containsKey(name)) {
      throw new IllegalArgumentException("Overriding variable " + name);
    }

    return createUpdate(new Variable(name, data), locals);
  }

  @Override
  public NodeVariable fetch(final String name) {
    if (locals.containsKey(name)) {
      return locals.get(name);
    }

    if (outers.containsKey(name)) {
      return outers.get(name);
    }

    return parent.fetch(name);
  }

  @Override
  public NodeVariable update(final String name) {
    if (!this.contains(name)) {
      throw new IllegalArgumentException("Updating non-existing variable " + name);
    }

    if (locals.containsKey(name)) {
      return createUpdate(locals.get(name).getVariable(), locals);
    }

    return createUpdate(fetch(name).getVariable(), outers);
  }

  private NodeVariable createUpdate(final Variable var, final Map<String, NodeVariable> map) {
    final String name = var.getName();
    final int version = getVersion(name) + 1;

    final NodeVariable node = new NodeVariable(var);
    node.setUserData(version);

    map.put(name, node);
    versions.put(name, version);

    return node;
  }

  private int getVersion(final String name) {
    if (versions.containsKey(name)) {
      return versions.get(name);
    }

    if (parent instanceof SsaVariableScope) {
      return ((SsaVariableScope) parent).getVersion(name);
    }

    return 0;
  }
}
