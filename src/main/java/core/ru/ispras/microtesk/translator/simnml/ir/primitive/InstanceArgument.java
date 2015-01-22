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

package ru.ispras.microtesk.translator.simnml.ir.primitive;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import ru.ispras.microtesk.translator.simnml.ir.expression.Expr;

public final class InstanceArgument {

  public static enum Kind {
    INSTANCE (Instance.class),
    EXPR (Expr.class),
    PRIMITIVE (Primitive.class);

    private final Class<?> valueClass;
    private Kind(Class<?> valueClass) {
      this.valueClass = valueClass;
    }
  }

  public static InstanceArgument newInstance(Instance instance) {
    return new InstanceArgument(Kind.INSTANCE, instance, null);
  }

  public static InstanceArgument newExpr(Expr expr) {
    return new InstanceArgument(Kind.EXPR, expr, null);
  }

  public static InstanceArgument newPrimitive(String name, Primitive type) {
    return new InstanceArgument(Kind.PRIMITIVE, type, name);
  }

  private final Kind kind;
  private final Object value;
  private final String name;

  private InstanceArgument(Kind kind, Object value, String name) {
    checkNotNull(kind);
    checkNotNull(value);

    if (!kind.valueClass.isAssignableFrom(value.getClass())) {
      throw new IllegalArgumentException();
    }

    this.kind = kind;
    this.value = value;
    this.name = name;
  }

  public Kind getKind() {
    return kind;
  }

  public Expr getExpr() {
    return (Expr) getValueIfAssignable(Expr.class); 
  }

  public Instance getInstance() {
    return (Instance) getValueIfAssignable(Instance.class); 
  }

  public Primitive getPrimitive() {
    return (Primitive) getValueIfAssignable(Primitive.class);
  }

  public String getName() {
    return name;
  }

  private Object getValueIfAssignable(Class<?> targetClass) {
    if (!targetClass.isAssignableFrom(value.getClass())) {
      throw new IllegalStateException();
    }

    return value;
  }
}
