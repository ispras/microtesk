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

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.NodeVariable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class SsaScopeFactory {
  private static final SsaScope EMPTY_SCOPE = new SsaScope() {
    @Override
    public boolean contains(String name) {
      return false;
    }

    @Override
    public NodeVariable create(String name, Data data) {
      throw new UnsupportedOperationException();
    }

    @Override
    public NodeVariable fetch(String name) {
      throw new IllegalArgumentException();
    }

    @Override
    public NodeVariable update(String name) {
      throw new IllegalArgumentException();
    }
  };

  private SsaScopeFactory() {}

  public static SsaScope createScope() {
    return new SsaVariableScope(EMPTY_SCOPE);
  }

  public static SsaScope createInnerScope(SsaScope scope) {
    if (scope == null) {
      throw new NullPointerException();
    }
    return new SsaVariableScope(scope);
  }

  public static SsaScope collapse(SsaScope input) {
    if (input == null) {
      throw new NullPointerException();
    }
    if (input.getClass() != SsaVariableScope.class) {
      return input;
    }
    final SsaVariableScope scope = (SsaVariableScope) input;
    if (scope.getParentScope() == EMPTY_SCOPE) {
      return scope;
    }
    final SsaVariableScope parent = (SsaVariableScope) scope.getParentScope();
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

  SsaVariableScope(SsaScope parent) {
    this.parent = parent;
    this.versions = new HashMap<>();
    this.locals = new HashMap<>();
    this.outers = new HashMap<>();
  }

  Map<String, Integer> getVersions() {
    return versions;
  }

  Map<String, NodeVariable> getLocals() {
    return locals;
  }

  Map<String, NodeVariable> getOuters() {
    return outers;
  }

  @Override
  public boolean contains(String name) {
    return locals.containsKey(name) ||
           outers.containsKey(name) ||
           parent.contains(name);
  }

  @Override
  public NodeVariable create(String name, Data data) {
    if (data == null) {
      throw new NullPointerException();
    }
    if (locals.containsKey(name)) {
      throw new IllegalArgumentException("Overriding variable " + name);
    }
    return createUpdate(new Variable(name, data), locals);
  }

  protected NodeVariable createUpdate(Variable var,
                                      Map<String, NodeVariable> map) {
    final String name = var.getName();
    final int version = getVersion(name) + 1;
    final NodeVariable node = new NodeVariable(var);
    node.setUserData(version);

    map.put(name, node);
    versions.put(name, version);

    return node;
  }

  @Override
  public NodeVariable fetch(String name) {
    if (locals.containsKey(name)) {
      return locals.get(name);
    }
    if (outers.containsKey(name)) {
      return outers.get(name);
    }
    return parent.fetch(name);
  }

  @Override
  public NodeVariable update(String name) {
    if (!this.contains(name)) {
      throw new IllegalArgumentException("Updating non-existing variable " + name);
    }
    if (locals.containsKey(name)) {
      return createUpdate(locals.get(name).getVariable(), locals);
    }
    return createUpdate(fetch(name).getVariable(), outers);
  }

  public int getVersion(String name) {
    if (versions.containsKey(name)) {
      return versions.get(name);
    }
    if (parent instanceof SsaVariableScope) {
      return ((SsaVariableScope) parent).getVersion(name);
    }
    return 0;
  }

  public SsaScope getParentScope() {
    return parent;
  }

  public static Pair<String, Integer> splitName(String name) {
    final Pair<String, String> splitted = Utility.splitOnLast(name, '!');
    if (splitted.second.isEmpty()) {
      return new Pair<>(splitted.first, 1);
    }
    return new Pair<>(splitted.first, Integer.valueOf(splitted.second));
  }
}
