/*
 * Copyright 2014-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.tools.microft;

import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.primitive.Instance;
import ru.ispras.microtesk.translator.nml.ir.primitive.InstanceArgument;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

class Entity {
  private final Map<Entity, Map<String, Entity>> layout;
  private final Map<Entity, Map<String, Expr>> values;

  private final PrimitiveAND type;

  private Entity(
      final PrimitiveAND type,
      final Map<Entity, Map<String, Entity>> layout,
      final Map<Entity, Map<String, Expr>> values) {
    this.type = type;
    this.layout = layout;
    this.values = values;
  }

  public PrimitiveAND getType() {
    return type;
  }

  public Map<String, Primitive> getTypeParameters() {
    return getType().getArguments();
  }

  public Map<String, Entity> getTypeArguments() {
    if (layout.containsKey(this)) {
      return layout.get(this);
    }
    return Collections.emptyMap();
  }

  public Map<String, Expr> getBindings() {
    if (values.containsKey(this)) {
      return values.get(this);
    }
    return Collections.emptyMap();
  }

  public boolean isTerminal() {
    for (final Primitive p : getTypeParameters().values()) {
      if (p.getKind() != Primitive.Kind.IMM) {
        return false;
      }
    }
    return true;
  }

  public boolean hasUnbound() {
    final Map<String, Expr> bindings = getBindings();
    for (final Map.Entry<String, Primitive> param : getTypeParameters().entrySet()) {
      if (param.getValue().getKind() == Primitive.Kind.IMM) {
        final Expr value = bindings.get(param.getKey());
        if (value == null || !value.isConstant()) {
          return true;
        }
      }
    }
    return false;
  }

  public static Entity create(final List<PrimitiveAND> src) {
    final Context ctx = new Context(src);
    return ctx.transformPrimitive(src);
  }

  public static Entity create(final Instance src, final Entity env) {
    final Context ctx = new Context(src);
    return ctx.transformInstance(src, env);
  }

  private static class Context {
    public final Map<Entity, Map<String, Entity>> layout =
      new IdentityHashMap<>();
    public final Map<Entity, Map<String, Entity>> shared =
      Collections.unmodifiableMap(layout);

    public final Map<Entity, Map<String, Expr>> values;

    public Context(final List<PrimitiveAND> src) {
      this.values = Collections.emptyMap();
    }

    public Context(final Instance src) {
      this.values = new IdentityHashMap<>();
    }

    public Entity transformPrimitive(final List<PrimitiveAND> src) {
      final Deque<Entity> created = new ArrayDeque<>();
      for (final PrimitiveAND p : src) {
        final Map<String, Entity> types = new HashMap<>();

        for (final Map.Entry<String, Primitive> param : p.getArguments().entrySet()) {
          final Primitive child = param.getValue();

          if (child.isOrRule() && child.getKind() == Primitive.Kind.OP) {
            types.put(param.getKey(), created.peek());
          } else if (!child.isOrRule() && child.getKind() != Primitive.Kind.IMM) {
            types.put(param.getKey(), newEntity((PrimitiveAND) child));
          }
        }
        created.push(newEntity(p, types));
      }
      return created.peek();
    }

    public Entity transformInstance(final Instance src, final Entity env) {
      final Arguments args = new Arguments();
      final IterablePair<String, InstanceArgument> pairs =
        IterablePair.create(src.getPrimitive().getArguments().keySet(), src.getArguments());
      for (final Pair<String, InstanceArgument> pair : pairs) {
        args.dispatch(pair.first, pair.second, env);
      }
      return newEntity(src.getPrimitive(), args);
    }

    private Entity newEntity(final PrimitiveAND p) {
      return new Entity(p, this.shared, this.values);
    }

    private Entity newEntity(final PrimitiveAND p, final Map<String, Entity> args) {
      final Entity e = newEntity(p);
      layout.put(e, normalizedMap(args));
      return e;
    }

    private Entity newEntity(final PrimitiveAND p, final Arguments args) {
      final Entity e = newEntity(p);
      layout.put(e, normalizedMap(args.types));
      values.put(e, normalizedMap(args.values));
      return e;
    }

    private static <K, V> Map<K, V> normalizedMap(final Map<K, V> src) {
      if (src.isEmpty()) {
        return Collections.emptyMap();
      }
      return Collections.unmodifiableMap(src);
    }
  }

  private static class Arguments {
    public final Map<String, Entity> types = new HashMap<>();
    public final Map<String, Expr> values = new HashMap<>();

    public void dispatch(final String name, final InstanceArgument arg, final Entity env) {
      switch (arg.getKind()) {
      case INSTANCE:
        types.put(name, Entity.create(arg.getInstance(), env));
        break;

      case PRIMITIVE:
        types.put(name, env.getTypeArguments().get(arg.getName()));
        break;

      case EXPR:
        values.put(name, arg.getExpr());
        break;
      }
    }
  }
}
