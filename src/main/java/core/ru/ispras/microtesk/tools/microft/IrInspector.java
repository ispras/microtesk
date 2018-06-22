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
import ru.ispras.microft.service.json.JsonStorage;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOR;
import ru.ispras.microtesk.utils.FormatMarker;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.json.*;

/**
 * Class for model code coverage extraction from internal representation.
 */
public final class IrInspector {
  private final JsonStorage db = new JsonStorage();
  private final JsonStorage.RefList arch = db.createList("arch");

  public static void inspect(final Ir ir) {
    try {
      final IrInspector inspector = new IrInspector();
      inspector.inspectIr(ir);
      inspector.store(Files.newOutputStream(Paths.get("microft-db.json")));
    } catch (final IOException e) {
      // TODO
    }
  }

  private void store(final OutputStream os) throws IOException {
    this.db.write(os);
  }

  private void inspectIr(final Ir ir) {
    final JsonObjectBuilder builder = Json.createObjectBuilder();
    builder.add("name", ir.getModelName());

    arch.add(builder.build());

    final JsonStorage.RefItem ref = arch.getLast().set(builder);
    inspectInsns(ir, ref);
  }

  private void inspectInsns(final Ir ir, final JsonStorage.RefItem entry) {
    final Collection<List<PrimitiveAND>> operations =
      listOperations(ir.getRoots());

    final List<Attribute> attributes = new ArrayList<>();
    attributes.add(new AttrFormat("syntax"));
    attributes.add(new AttrFormat("image"));
    attributes.add(new Attribute("mnemonic", "syntax") {
      @Override
      public JsonValue get(final List<PrimitiveAND> p, final Map<String, JsonValue> env) {
        final JsonValue syntax = env.get("syntax");
        if (syntax.getValueType() == JsonValue.ValueType.STRING) {
          final String s[] = ((JsonString) syntax).getString().split("\\s+");
          return JsonUtil.createString(s[0]);
        }
        return JsonValue.NULL;
      }
    });
    attributes.add(new Attribute("name") {
      @Override
      public JsonValue get(final List<PrimitiveAND> p, final Map<String, JsonValue> env) {
        return JsonUtil.createString(p.get(0).getName());
      }
    });

    final TreeMap<String, Map<String, JsonValue>> payload = new TreeMap<>();
    for (final List<PrimitiveAND> insn : operations) {
      final Map<String, JsonValue> attrs = inspectInsn(insn, attributes);
      payload.put(nameOf(insn), attrs);
    }

    final JsonStorage.RefList insns = entry.createList("insn");
    final List<JsonObject> index = new ArrayList<>();
    for (final Map<String, JsonValue> attrs : payload.values()) {
      final JsonObjectBuilder builder = Json.createObjectBuilder();
      for (final Map.Entry<String, JsonValue> attr : attrs.entrySet()) {
        builder.add(attr.getKey(), attr.getValue());
      }
      final JsonObject jsonInsn = builder.build();
      insns.add(jsonInsn);
      index.add(jsonInsn);
    }

    final IterablePair<JsonStorage.Ref, JsonObject> pairs =
      IterablePair.create(insns, index);
    for (final Pair<JsonStorage.Ref, JsonObject> pair : pairs) {
      pair.first.set(pair.second);
    }
  }

  private static Map<String, JsonValue> inspectInsn(
    final List<PrimitiveAND> insn,
    final List<Attribute> attrs) {
    final Map<String, JsonValue> values = new TreeMap<>();
    final List<Attribute> queue = new ArrayList<>(attrs);

    while (!queue.isEmpty()) {
      final Iterator<Attribute> it = queue.iterator();
      while (it.hasNext()) {
        final Attribute attr = it.next();
        if (values.keySet().containsAll(attr.getDependencies())) {
          values.put(attr.getName(), attr.get(insn, values));
          it.remove();
        }
      }
    }
    return values;
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
    private final String name;
    private final List<String> deps;

    public Attribute(final String name) {
      this.name = name;
      this.deps = Collections.emptyList();
    }

    public Attribute(final String name, final String... deps) {
      this.name = name;
      this.deps = Arrays.asList(deps);
    }

    public String getName() {
      return name;
    }

    public List<String> getDependencies() {
      return deps;
    }

    public abstract JsonValue get(final List<PrimitiveAND> p, final Map<String, JsonValue> env);
  }
}
