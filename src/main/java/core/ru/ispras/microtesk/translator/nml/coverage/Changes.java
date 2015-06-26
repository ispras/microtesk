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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.util.InvariantChecks;

final class Batch {
  public final String key;
  public final List<NodeVariable> batch;

  public NodeVariable load;
  public NodeVariable store;

  public Batch(String key) {
    InvariantChecks.checkNotNull(key);

    this.key = key;
    this.batch = new ArrayList<>();
    this.load = null;
    this.store = null;
  }

  public void clear() {
    this.batch.clear();
    this.load = null;
    this.store = null;
  }

  public boolean isSet() {
    return this.load != null;
  }
}

final class Changes {
  private final Map<String, NodeVariable> base;
  private final Map<String, NodeVariable> store;
  private final Map<String, Batch> diff;
  private final Map<String, Node> summary;

  public Changes(Map<String, NodeVariable> base, Map<String, NodeVariable> store) {
    this.base = base;
    this.store = store;
    this.diff = new HashMap<>();
    this.summary = new HashMap<>();
  }

  public Map<String, Node> getSummary() {
    return this.summary;
  }

  public NodeVariable getBase(String name) {
    return this.base.get(name);
  }

  public NodeVariable getLatest(String name) {
    final Batch diff = this.diff.get(name);
    if (diff != null && diff.isSet()) {
      return (diff.batch.isEmpty()) ? diff.store : findLatest(diff.batch);
    }
    return store.get(name);
  }

  public NodeVariable newLatest(String name) {
    final NodeVariable prev = getLatest(name);
    final NodeVariable latest = new NodeVariable(prev.getVariable());
    latest.setUserData(getVersion(prev) + 1);

    fetchInsertBatch(name).batch.add(latest);

    return latest;
  }

  public NodeVariable rebase(NodeVariable node) {
    return rebase(node.getVariable(), getVersion(node));
  }

  public NodeVariable rebase(String name, Data data, int relVer) {
    return rebase(new Variable(name, data), relVer);
  }

  private NodeVariable rebase(Variable var, int relative) {
    final Batch batch = getBatch(var);
    if (relative == 1) {
      return batch.load;
    }

    final int version = getVersion(batch.store) + relative - 1;
    final NodeVariable stored = find(batch.batch, version);
    if (stored != null) {
      return stored;
    }

    final NodeVariable node = new NodeVariable(batch.store.getVariable());
    node.setUserData(version);
    batch.batch.add(node);

    return node;
  }

  private Batch getBatch(Variable var) {
    final Batch batch = fetchInsertBatch(var.getName());
    if (batch.isSet()) {
      return batch;
    }
    final NodeVariable store = this.store.get(var.getName());
    if (store != null && store.getDataType().equals(var.getType())) {
      batch.store = store;
    } else {
      final NodeVariable alter = new NodeVariable(var);
      final int version = (store != null) ? getVersion(store) + 1 : 1;
      alter.setUserData(version);

      batch.store = alter;
      batch.batch.add(alter);
    }

    final NodeVariable load = this.base.get(var.getName());
    if (load != null && load.getDataType().equals(var.getType())) {
      batch.load = load;
    } else if (getVersion(batch.store) == 1) {
      batch.load = batch.store;
    } else {
      batch.load = new NodeVariable(var);
      batch.load.setUserData(1);
    }
    return batch;
  }

  private Batch fetchInsertBatch(String name) {
    Batch batch = diff.get(name);
    if (batch == null) {
      batch = new Batch(name);
      diff.put(name, batch);
    }
    return batch;
  }

  public Collection<Changes> fork(int n) {
    final Map<String, NodeVariable> slice =
        Collections.unmodifiableMap(new HashMap<>(this.store));
    final List<Changes> branches = new ArrayList<>(n);
    for (int i = 0; i < n; ++i) {
      branches.add(new Changes(slice, this.store));
    }
    return branches;
  }

  public void commit() {
    for (Batch batch : diff.values()) {
      if (!batch.batch.isEmpty()) {
        final NodeVariable latest = findLatest(batch.batch);
        store.put(latest.getName(), latest);
        if (getVersion(latest) != getVersion(batch.store)) {
          summary.put(latest.getName(), latest);
        }
      }
      batch.clear();
    }
  }

  private static NodeVariable find(Collection<NodeVariable> vars, int version) {
    for (NodeVariable node : vars) {
      if (getVersion(node) == version) {
        return node;
      }
    }
    return null;
  }

  private static NodeVariable findLatest(Collection<NodeVariable> vars) {
    int version = -1;
    NodeVariable variable = null;
    for (NodeVariable node : vars) {
      if (getVersion(node) > version) {
        version = getVersion(node);
        variable = node;
      }
    }
    return variable;
  }

  private static int getVersion(Node node) {
    return (node != null) ? (Integer) node.getUserData() : 0;
  }
}
