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

package ru.ispras.microtesk.mmu.translator.ir;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.microtesk.mmu.translator.ir.spec.builder.ScopeStorage.dotConc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.expression.NodeVariable;

public final class Variable extends Nested<Variable> {
  private final String name;
  private final Type type;
  private final Variable parent;
  private final Object typeSource;
  private final NodeVariable node;
  private final Map<String, Variable> fields;

  public Variable(final String name, final Type type) {
    this(name, type, null, null);
  }

  public Variable(final String name, final Type type, final Object typeSource) {
    this(name, type, null, typeSource);
  }

  private Variable(
      final String name,
      final Type type,
      final Variable parent,
      final Object typeSource) {
    checkNotNull(name);
    checkNotNull(type);

    this.name = name;
    this.type = type;
    this.parent = parent;
    this.typeSource = typeSource;

    if (type.isStruct()) {
      final Map<String, Variable> fields = new HashMap<>(type.getFields().size());
      for (final Map.Entry<String, Type> fieldType : type.getFields().entrySet()) {
        final String varName = dotConc(name, fieldType.getKey());
        fields.put(fieldType.getKey(), new Variable(varName, fieldType.getValue(), this, null));
      }
      this.fields = Collections.unmodifiableMap(fields);
    } else {
      this.fields = Collections.emptyMap();
    }

    this.node = new NodeVariable(name, type.getDataType());
    this.node.setUserData(this);
  }


  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public Object getTypeSource() {
    return typeSource;
  }

  public int getBitSize() {
    return type.getBitSize();
  }

  public DataType getDataType() {
    return type.getDataType();
  }

  public boolean isStruct() {
    return type.isStruct();
  }

  public boolean isField() {
    return null != parent;
  }

  public NodeVariable getNode() {
    return node;
  }

  public Map<String, Variable> getFields() {
    return fields;
  }

  public Variable rename(final String name) {
    return new Variable(name, this.type);
  }

  @Override
  protected Variable getNested(final String name) {
    return getFields().get(name);
  }

  @Override
  public String toString() {
    return String.format("%s: %s", name, type);
  }
}
