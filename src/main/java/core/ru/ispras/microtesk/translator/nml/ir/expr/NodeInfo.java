/*
 * Copyright 2015-2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.expr;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NodeInfo {

  public enum Kind {
    CONST,
    LOCATION,
    OPERATOR;
  }

  public enum Coercion {
    IMPLICIT("valueOf");

    private final String methodName;

    Coercion(final String methodName) {
      this.methodName = methodName;
    }

    public String getMethodName() {
      return methodName;
    }
  }

  public static NodeInfo newConst(final Type type) {
    return new NodeInfo(NodeInfo.Kind.CONST, null, type, false);
  }

  public static NodeInfo newLocation(final Location location) {
    InvariantChecks.checkNotNull(location);
    return new NodeInfo(NodeInfo.Kind.LOCATION, location, location.getType(), true);
  }

  public static NodeInfo newOperator(
      final Operator operator,
      final Type type) {
    InvariantChecks.checkNotNull(operator);
    return new NodeInfo(NodeInfo.Kind.OPERATOR, operator, type, false);
  }

  public static NodeInfo newOperator(
      final Operator operator,
      final Type type,
      final boolean isLocation) {
    InvariantChecks.checkNotNull(operator);
    return new NodeInfo(NodeInfo.Kind.OPERATOR, operator, type, isLocation);
  }

  private final Kind kind;
  private final Object source;
  private final Type type;
  private final boolean isLocation;
  private final List<Type> coercedTypes;
  private final List<Coercion> coercions;

  private NodeInfo(
      final Kind kind,
      final Object source,
      final Type type,
      final boolean isLocation,
      final List<Type> coercedTypes,
      final List<Coercion> coercions) {
    this.kind = kind;
    this.source = source;
    this.type = type;
    this.isLocation = isLocation;
    this.coercedTypes = Collections.unmodifiableList(coercedTypes);
    this.coercions = Collections.unmodifiableList(coercions);
  }

  private NodeInfo(
      final Kind kind,
      final Object source,
      final Type type,
      final boolean isLocation) {
    this(
        kind,
        source,
        type,
        isLocation,
        Collections.<Type>emptyList(),
        Collections.<Coercion>emptyList()
        );
  }

  public NodeInfo coerceTo(final Type newType, final Coercion coercion) {
    InvariantChecks.checkNotNull(newType);
    InvariantChecks.checkNotNull(coercion);

    if (type != null && type.equals(newType)) {
      return this;
    }

    final List<Type> newCoercedTypes = new ArrayList<>(this.coercedTypes.size() + 1);
    newCoercedTypes.add(newType);
    newCoercedTypes.add(this.type);
    newCoercedTypes.addAll(this.coercedTypes);

    final List<Coercion> newCoercions = new ArrayList<>(this.coercions.size() + 1);
    newCoercions.add(coercion);
    newCoercions.addAll(this.coercions);

    return new NodeInfo(kind, source, newType, isLocation, newCoercedTypes, newCoercions);
  }

  public Kind getKind() {
    return kind;
  }

  public Object getSource() {
    return source;
  }

  public Type getType() {
    return type;
  }

  public boolean isCoersionApplied() {
    return !coercions.isEmpty();
  }

  public List<Type> getCoercionChain() {
    return coercedTypes;
  }

  public List<Coercion> getCoercions() {
    return coercions;
  }

  public boolean isLocation() {
    return isLocation;
  }
}
