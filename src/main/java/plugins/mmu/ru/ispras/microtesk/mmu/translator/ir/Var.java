/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.utils.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Var extends Nested<Var> {
  private final String name;
  private final Type type;
  private final Var parent;
  private final Object typeSource;
  private final NodeVariable node;
  private final Map<String, Var> fields;

  public Var(final String name, final Type type) {
    this(name, type, null, null);
  }

  public Var(final String name, final Type type, final Object typeSource) {
    this(name, type, null, typeSource);
  }

  private Var(
      final String name,
      final Type type,
      final Var parent,
      final Object typeSource) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(type);

    this.name = name;
    this.type = type;
    this.parent = parent;
    this.typeSource = typeSource;

    if (type.isStruct()) {
      final Map<String, Var> fields = new HashMap<>(type.getFields().size());
      for (final Map.Entry<String, Type> fieldType : type.getFields().entrySet()) {
        final String varName = StringUtils.dotConc(name, fieldType.getKey());
        fields.put(fieldType.getKey(), new Var(varName, fieldType.getValue(), this, null));
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

  public boolean isParent(final Var variable) {
    InvariantChecks.checkNotNull(variable);
    return this == variable || (null != parent && parent.isParent(variable));
  }

  public NodeVariable getNode() {
    return node;
  }

  public Map<String, Var> getFields() {
    return fields;
  }

  public Var rename(final String name) {
    return new Var(name, this.type);
  }

  @Override
  protected Var getNested(final String name) {
    return getFields().get(name);
  }

  @Override
  public String toString() {
    return String.format("%s: %s", name, type);
  }
}
