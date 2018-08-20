/*
 * Copyright 2013-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.primitive;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PrimitiveOr extends Primitive {
  private final List<Primitive> ors;
  private final Set<String> names;

  public PrimitiveOr(final String name, final Kind kind, final List<Primitive> ors) {
    super(
        name,
        kind,
        getCommonModifier(ors),
        true,
        getReturnType(ors),
        (null == ors) || ors.isEmpty() ? null : ors.get(0).getAttrNames()
        );

    this.ors = ors;
    this.names = makeNames(ors);
    this.names.add(name);
  }

  private static Modifier getCommonModifier(final List<Primitive> primitives) {
    InvariantChecks.checkNotEmpty(primitives);

    final int totalCount = primitives.size();
    final Map<Modifier, Integer> statistics = new EnumMap<>(Modifier.class);

    for (final Primitive primitive : primitives) {
      final Modifier modifier = primitive.getModifier();

      final Integer oldCount = statistics.get(modifier);
      final Integer newCount = oldCount == null ? 1 : oldCount + 1;

      statistics.put(modifier, newCount);
      if (newCount == totalCount) {
        return modifier;
      }
    }

    return Modifier.NORMAL;
  }

  private static Set<String> makeNames(final List<Primitive> ors) {
    final Set<String> result = new HashSet<String>();
    for (final Primitive primitive : ors) {
      if (primitive.isOrRule()) {
        result.addAll(((PrimitiveOr) primitive).getNames());
      } else {
        result.add(primitive.getName());
      }
    }
    return result;
  }

  public void addParentReference(final PrimitiveAnd parent, final String referenceName) {
    super.addParentReference(parent, referenceName);

    for (final Primitive target : ors) {
      target.addParentReference(parent, referenceName);
    }
  }

  public List<Primitive> getOrs() {
    return ors;
  }

  public Set<String> getNames() {
    return names;
  }

  private static Type getReturnType(final List<Primitive> ors) {
    InvariantChecks.checkNotNull(ors);
    return ors.get(0).getReturnType();
  }
}
