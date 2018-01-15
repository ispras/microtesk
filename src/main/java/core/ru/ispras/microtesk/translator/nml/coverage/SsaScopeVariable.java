/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.fortress.util.InvariantChecks;

import java.util.HashMap;
import java.util.Map;

final class SsaScopeVariable implements SsaScope {
  private final SsaScope parent;
  private final Map<String, Integer> versions;
  private final Map<String, NodeVariable> locals;
  private final Map<String, NodeVariable> outers;

  public SsaScopeVariable() {
    this(SsaScopeEmpty.get());
  }

  public SsaScopeVariable(final SsaScope parent) {
    this.parent = parent;
    this.versions = new HashMap<>();
    this.locals = new HashMap<>();
    this.outers = new HashMap<>();
  }

  @Override
  public boolean contains(final String name) {
    return locals.containsKey(name) ||
           outers.containsKey(name) ||
           parent.contains(name);
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

  private NodeVariable createUpdate(
      final Variable variable,
      final Map<String, NodeVariable> map) {
    final String name = variable.getName();
    final int version = getVersion(name) + 1;

    final NodeVariable node = new NodeVariable(variable);
    node.setUserData(version);

    map.put(name, node);
    versions.put(name, version);

    return node;
  }

  private int getVersion(final String name) {
    if (versions.containsKey(name)) {
      return versions.get(name);
    }

    if (parent instanceof SsaScopeVariable) {
      return ((SsaScopeVariable) parent).getVersion(name);
    }

    return 0;
  }
}
