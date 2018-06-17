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

import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOR;
import ru.ispras.microtesk.utils.FormatMarker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.json.JsonValue;

/**
 * Class for model code coverage extraction from internal representation.
 */
public final class IrInspector {
  public static void inspect(final Ir ir) {
    final Collection<List<PrimitiveAND>> operations =
      listOperations(ir.getRoots());

    final List<Attribute> attributes = new ArrayList<>();
    attributes.add(new AttrFormat("syntax"));
    attributes.add(new AttrFormat("image"));

    for (final List<PrimitiveAND> insn : operations) {
      System.out.println(nameOf(insn));
      for (final Attribute a : attributes) {
        System.out.printf("%s: %s%n", a.name, a.get(insn).toString());
      }
      System.out.println();
    }
  }

  private static String nameOf(final List<PrimitiveAND> insns) {
    final StringBuilder builder = new StringBuilder();
    final Iterator<PrimitiveAND> it = insns.iterator();
    builder.append(it.next().getName());

    while (it.hasNext()) {
      builder.append("-").append(it.next().getName());
    }
    return builder.toString();
  }

  private static Collection<List<PrimitiveAND>> listOperations(
      final Collection<? extends Primitive> roots) {
    final Deque<PrimitiveAND> seq = new ArrayDeque<>();
    final List<List<PrimitiveAND>> insns = new ArrayList<>();
    final List<List<PrimitiveAND>> words = new ArrayList<>();

    for (final Primitive root : roots) {
      linearize(root, seq, insns, words);
    }
    return insns;
  }

  private static void linearize(
    final Primitive p,
    final Deque<PrimitiveAND> seq,
    final Collection<List<PrimitiveAND>> insns,
    final Collection<List<PrimitiveAND>> words) {

    if (p.isOrRule()) {
      for (final Primitive child : ((PrimitiveOR) p).getOrs()) {
        linearize(child, seq, insns, words);
      }
    } else {
      final PrimitiveAND op = (PrimitiveAND) p;
      seq.push(op);

      final List<PrimitiveOR> variants = new ArrayList<>();
      for (final Primitive param : op.getArguments().values()) {
        if (param.isOrRule() && param.getKind() == Primitive.Kind.OP) {
          variants.add((PrimitiveOR) param);
        }
      }
      if (variants.isEmpty()) {
        insns.add(new ArrayList<>(seq));
      } else if (variants.size() > 1) {
        // VLIW-like
        words.add(new ArrayList<>(seq));
      } else {
        linearize(variants.get(0), seq, insns, words);
      }

      seq.pop();
    }
  }

  abstract static class Attribute {
    public final String name;

    public Attribute(final String name) {
      this.name = name;
    }

    public abstract JsonValue get(final List<PrimitiveAND> p);
  }
}
