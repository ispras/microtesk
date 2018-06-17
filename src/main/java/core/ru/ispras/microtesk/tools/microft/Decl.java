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

import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

class Decl {
  public final PrimitiveAND root;
  public final Map<PrimitiveAND, Map<String, PrimitiveAND>> tree;

  public Decl create(final PrimitiveAND p) {
    return new Decl(p, this.tree);
  }

  public PrimitiveAND getArgument(final String name) {
    return getArguments().get(name);
  }

  public Map<String, PrimitiveAND> getArguments() {
    if (tree.containsKey(root)) {
      return tree.get(root);
    }
    return Collections.emptyMap();
  }

  public Primitive getParameter(final String name) {
    return getParameters().get(name);
  }

  public Map<String, Primitive> getParameters() {
    return root.getArguments();
  }

  public static Decl create(final List<PrimitiveAND> list) {
    final List<PrimitiveAND> ordered = new ArrayList<>(list);
    Collections.reverse(ordered);


    final Map<PrimitiveAND, Map<String, PrimitiveAND>> tree =
      new IdentityHashMap<>();
    for (int i = 0; i < ordered.size(); ++i) {
      final PrimitiveAND p = ordered.get(i);
      for (final Map.Entry<String, Primitive> param : p.getArguments().entrySet()) {
        final Primitive child = param.getValue();
        if (child.isOrRule() && child.getKind() == Primitive.Kind.OP) {
          tree.put(p, Collections.singletonMap(param.getKey(), ordered.get(i + 1)));
        }
      }
    }
    return new Decl(ordered.get(0), Collections.unmodifiableMap(tree));
  }

  private Decl(
    final PrimitiveAND root,
    final Map<PrimitiveAND, Map<String, PrimitiveAND>> tree) {
    this.root = root;
    this.tree = tree;
  }
}
