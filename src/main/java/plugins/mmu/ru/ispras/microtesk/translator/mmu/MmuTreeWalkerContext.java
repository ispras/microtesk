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

package ru.ispras.microtesk.translator.mmu;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.microtesk.translator.mmu.ir.AbstractStorage;
import ru.ispras.microtesk.translator.mmu.ir.Variable;

final class MmuTreeWalkerContext {

  public static enum Kind {
    GLOBAL,
    BUFFER,
    MEMORY
  }

  public static final MmuTreeWalkerContext GLOBAL = 
      new MmuTreeWalkerContext(Kind.GLOBAL, "");

  private final Kind kind;
  private final String id;

  private final Map<String, NodeVariable> variables;
  private final Map<String, AbstractStorage> globalObjects;

  private final Map<Node, Node> assignments;

  MmuTreeWalkerContext(Kind kind, String id) {
    checkNotNull(kind);
    checkNotNull(id);

    this.kind = kind;
    this.id = id;
    this.variables = new HashMap<>();
    this.globalObjects = new HashMap<>();
    this.assignments = new HashMap<>(); 
  }

  public Kind getKind() {
    return kind;
  }

  public String getId() {
    return id;
  }

  public void defineVariable(Variable variable) {
    checkNotNull(variable);
    variables.put(variable.getId(), variable.getVariable());
  }

  public void defineVariable(NodeVariable variable) {
    checkNotNull(variable);
    variables.put(variable.getName(), variable);
  }

  public void defineVariableAs(String variableId, Variable variable) {
    checkNotNull(variableId);
    checkNotNull(variable);
    variables.put(variableId, variable.getVariable());
  }

  public void defineVariableAs(String variableId, NodeVariable variable) {
    checkNotNull(variableId);
    checkNotNull(variable);
    variables.put(variableId, variable);
  }

  public NodeVariable getVariable(String variableId) {
    return variables.get(variableId);
  }

  public void defineGlobalObjects(Collection<? extends AbstractStorage> objects) {
    checkNotNull(objects);
    for (AbstractStorage object : objects) {
      defineGlobalObject(object);
    }
  }

  public void defineGlobalObject(AbstractStorage object) {
    checkNotNull(object);
    globalObjects.put(object.getId(), object);
  }

  public AbstractStorage getGlobalObject(String objectId) {
    return globalObjects.get(objectId);
  }

  /**
   * Remembers an assignment (caches the assigned value expression). This helps
   * to remember constant values assigned to variables. We need these values rather
   * than expressions in operations like indexing and bit field extraction.
   * 
   * @param lhs Left hand side expression.
   * @param rhs Right hand side expression.
   */

  public void setAssignedValue(Node lhs, Node rhs) {
    assignments.put(lhs, rhs);
  }

  /**
   * Checks whether the specified expression can be substituted with a constant
   * value (it is has been earlier assigned) and returns the corresponding value.
   * If it cannot or if it is already a constant value, the expression returned as is.
   * Such substitutions are needed for operations like indexing and bit field
   * extraction that require constant values rather than expressions.
   * 
   * @param lhs Expression to be substituted with a constant value.
   * @return A constant value or the initial expression if there is no constant
   * value associated with (or assigned to) this expression or if it is already
   * a constant value.
   */

  public Node getAssignedValue(Node lhs) {
    if (lhs.getKind() == Node.Kind.VALUE) {
      return lhs;
    }

    final Node rhs = assignments.get(lhs);
    if (null == rhs) {
      return lhs;
    }

    if (rhs.getKind() == Node.Kind.VALUE) {
      return rhs;
    }

    return lhs;
  }
}
