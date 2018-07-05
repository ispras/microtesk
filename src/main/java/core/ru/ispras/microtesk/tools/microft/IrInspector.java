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
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOR;
import ru.ispras.microtesk.utils.FormatMarker;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.json.*;

/**
 * Class for model code coverage extraction from internal representation.
 */
public final class IrInspector implements TranslatorHandler<Ir> {
  private final JsonStorage db = new JsonStorage();
  private final JsonStorage.RefList arch = db.createList("arch");
  private final Translator<?> translator;

  public IrInspector(final Translator<?> translator) {
    this.translator = translator;
  }

  public static void inspect(final Ir ir) {
    final IrInspector inspector = new IrInspector(null);
    inspector.processIr(ir);
  }

  private OutputStream newOutputStream(final String name) throws IOException {
    final Path path;
    if (translator != null) {
      path = Paths.get(translator.getOutDir(), name + ".json");
    } else {
      path = Paths.get(name + ".json");
    }
    return Files.newOutputStream(path);
  }

  private void store(final String modelName) {
    try {
      this.db.write(newOutputStream(modelName));
    } catch (final IOException e) {
      // TODO
    }
  }

  @Override
  public void processIr(final Ir ir) {
    final JsonObjectBuilder builder = Json.createObjectBuilder();
    builder.add("name", ir.getModelName());

    arch.add(builder.build());

    final JsonStorage.RefItem ref = arch.getLast().set(builder);
    inspectInsns(ir, ref);
    store(ir.getModelName());
  }

  private void inspectInsns(final Ir ir, final JsonStorage.RefItem entry) {
    final Collection<List<PrimitiveAND>> operations =
      listOperations(ir.getRoots());

    final List<IrPass<?>> attributes = new ArrayList<>();
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

    final List<IrPass<?>> orderedAttrs = topologicalOrder(attributes);
    final JsonStorage.RefList insns = entry.createList("insn");
    int index = 0;
    for (final List<PrimitiveAND> insn : operations) {
      final Map<String, JsonValue> attrs = inspectInsn(insn, orderedAttrs);
      attrs.put("id", JsonUtil.createNumber(index++));

      final JsonObjectBuilder builder = Json.createObjectBuilder();
      JsonUtil.addAll(builder, select(attrs, "id", "name", "mnemonic"));
      insns.add(builder.build());

      JsonUtil.addAll(builder, attrs);
      insns.getLast().set(builder.build());
    }
  }

  public static List<IrPass<?>> topologicalOrder(
      final Collection<? extends IrPass<?>> source) {

    final java.util.Set<String> resolved = new java.util.HashSet<>(source.size());
    final List<IrPass<?>> ordered = new ArrayList<>(source.size());
    final List<IrPass<?>> queue = new ArrayList<>(source);

    while (!queue.isEmpty()) {
      final Iterator<IrPass<?>> it = queue.iterator();
      while (it.hasNext()) {
        final IrPass<?> pass = it.next();
        if (resolved.containsAll(pass.getDependencies())) {
          resolved.add(pass.getName());
          ordered.add(pass);
          it.remove();
        }
      }
    }
    return ordered;
  }

  private static <K, V> Map<K, V> select(final Map<? super K, ? extends V> source, final K... keys) {
    final Map<K, V> target = new LinkedHashMap<>(keys.length);
    for (final K key : keys) {
      if (source.containsKey(key)) {
        target.put(key, source.get(key));
      }
    }
    return target;
  }

  private static Map<String, JsonValue> inspectInsn(
    final List<PrimitiveAND> insn,
    final List<IrPass<?>> attrs) {
    final PassContext ctx = new PassContext();
    ctx.fill(insn, attrs);

    return ctx.select(JsonValue.class);
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

  abstract static class Attribute extends IrPass<JsonValue> {
    public Attribute(final String name, final String... deps) {
      super(name, Arrays.asList(deps));
    }

    @Override
    public JsonValue run(final List<PrimitiveAND> insn, final PassContext ctx) {
      return get(insn, ctx.select(JsonValue.class));
    }

    public abstract JsonValue get(final List<PrimitiveAND> p, final Map<String, JsonValue> env);
  }
}
