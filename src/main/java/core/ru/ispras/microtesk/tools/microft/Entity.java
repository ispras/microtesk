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

import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;

import java.util.ArrayList;
import java.util.Collections;
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

  public static Entity create(final List<PrimitiveAND> list) {
    final List<PrimitiveAND> ordered = new ArrayList<>(list);
    Collections.reverse(ordered);

    final Map<Entity, Map<String, Entity>> layout = new IdentityHashMap<>();
    final Map<Entity, Map<String, Entity>> shared =
      Collections.unmodifiableMap(layout);
    final Map<Entity, Map<String, Expr>> values = Collections.emptyMap();

    for (int i = 0; i < ordered.size(); ++i) {
      final PrimitiveAND p = ordered.get(i);
      final Map<String, Entity> args = new HashMap<>();

      for (final Map.Entry<String, Primitive> param : p.getArguments().entrySet()) {
        final Primitive child = param.getValue();

        PrimitiveAND arg = null;
        if (child.isOrRule() && child.getKind() == Primitive.Kind.OP) {
          arg = ordered.get(i + 1);
        } else if (!child.isOrRule()) {
          arg = (PrimitiveAND) child;
        }
        if (arg != null) {
          args.put(param.getKey(), new Entity(arg, shared, values));
        }
      }
      // FIXME reuse existing entities
      final Entity e = new Entity(p, shared, values);
      layout.put(e, args);
    }
    return null;
  }
}
