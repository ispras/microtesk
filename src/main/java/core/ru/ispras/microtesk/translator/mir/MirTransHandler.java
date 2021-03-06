/*
    Copyright 2019 ISP RAS (http://www.ispras.ru)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package ru.ispras.microtesk.translator.mir;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ru.ispras.castle.util.Logger;
import ru.ispras.microtesk.model.memory.Memory;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.nml.analysis.IrInquirer;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.expr.Location;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAnd;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOr;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryResource;
import ru.ispras.microtesk.utils.NamePath;

import javax.json.*;
import javax.json.stream.JsonGenerator;

public class MirTransHandler implements TranslatorHandler<Ir> {
  private final Translator<Ir> translator;
  private final Map<Ir, Map<NamePath, MirContext>> cache =
      new java.util.IdentityHashMap<>();

  public MirTransHandler(final Translator<Ir> t) {
    this.translator = t;
  }

  @Override
  public void processIr(final Ir ir) {
    processIr(ir, new Options());
  }

  @Override
  public void processIr(final Ir ir, final Options opts) {
    Logger.message("Translating to MIR...");
    final var mir = translateIr(ir, loadMir(ir));
    Logger.message("done.");

    Logger.message("Optimizing MIR...");
    final Map<String, MirContext> opt = MirPassDriver.newDefault().run(mir);
    Logger.message("done.");

    final List<MirContext> isa;
    if (opts.getValueAsBoolean(Option.ENABLE_ISA_MIR)) {
      Logger.message("Instantiating MIR...");
      isa = buildIsa(ir, mir);
      Logger.message("done.");
    } else {
      isa = Collections.emptyList();
    }

    final Path outDir = Paths.get(translator.getOutDir());
    final String libName = ir.getModelName() + ".zip";
    final String isaName = ir.getModelName() + "-isa.zip";
    final JsonObject manifest = createManifest(ir);
    try {
      writeArchive(outDir.resolve(libName), manifest, opt.values());
      writeArchive(outDir.resolve(isaName), manifest, isa);
    } catch (final IOException e) {
      Logger.error("Failed to store MIR archive: %s", e.toString());
    }
  }

  private static Map<String, MirContext> translateIr(
      final Ir ir, final Map<NamePath, MirContext> mirs) {
    for (final Primitive p : ir.getOps().values()) {
      if (!p.isOrRule()) {
        final PrimitiveAnd item = (PrimitiveAnd) p;
        for (final Attribute attr : item.getAttributes().values()) {
          if (attr.getKind().equals(Attribute.Kind.ACTION)) {
            Logger.debug("MIRTRANS: %s.%s", item.getName(), attr.getName());
            final MirContext mir =
                NmlIrTrans.translate(item, attr.getName(), attr.getStatements());
            final NamePath name = NamePath.get(item.getName(), attr.getName());
            mirs.put(name, mir);
          }
        }
      }
    }
    for (final Primitive p : ir.getModes().values()) {
      if (!p.isOrRule() && p.getReturnType() != null) {
        final PrimitiveAnd item = (PrimitiveAnd) p;
        final NmlIrTrans.ModeAccess access = NmlIrTrans.translateMode(item);
        final NamePath name = NamePath.get(item.getName());
        mirs.put(name.resolve("read"), access.read);
        mirs.put(name.resolve("write"), access.write);
      }
    }
    final Map<String, MirContext> source = new java.util.TreeMap<>();
    for (final MirContext ctx : mirs.values()) {
      source.put(ctx.name, ctx);
    }
    return source;
  }

  private static List<MirContext> buildIsa(final Ir ir, final Map<String, MirContext> source) {
    final MirPassDriver driver = MirPassDriver.newDefault();
    driver.getPasses().set(0, new InlineNoAccess().setComment("inline (no access)"));
    driver.getPasses().add(1, new InlinePreserve().setComment("inline/dup access"));
    driver.setStorage(source);

    final List<PrimitiveAnd> opList = ir.getOps().containsKey("instruction")
        ? variantsOf(ir.getOps().get("instruction"))
        : Collections.emptyList();

    final ProcessReport report = new ProcessReport();
    final List<MirContext> isa = opList.stream()
        .flatMap(x -> instancesOf(x, report))
        .map(x -> report.notifyDone(driver.apply(x)))
        .collect(Collectors.toList());
    return isa;
  }

  static class ProcessReport {
    private int total = 0;
    private int ndone = 0;
    private int cycle = 0;

    <T> Collection<T> addAll(Collection<T> c) {
      total += c.size();
      return c;
    }

    <T> T notifyDone(T item) {
      final int threshold = Math.min(500, Math.max(total / 5 - 1, 1));
      ndone += 1;
      cycle += 1;
      if (cycle > threshold) {
        Logger.message("  %d/%d...", ndone, total);
        cycle %= threshold;
      }
      return item;
    }
  }

  static void writeArchive(
      final Path path, final JsonObject manifest, final Collection<MirContext> values)
      throws IOException {
    Files.createDirectories(path.getParent());
    try (final ArchiveWriter archive = new ArchiveWriter(path)) {
      try (final JsonWriter writer =
          Json.createWriter(archive.newText(MirArchive.MANIFEST))) {
        writer.write(manifest);
      }
      for (final MirContext ctx : values) {
        try (final Writer writer = archive.newText(ctx.name + ".mir")) {
          writer.write(MirText.toString(ctx));
        }
      }
    }
  }

  static Stream<MirContext> instancesOf(final PrimitiveAnd p, final ProcessReport report) {
    final var name = p.getName() + ".action";
    final var instances = instantiateRec(p, new MirBuilder(p.getName()));
    report.addAll(instances);

    return instances.stream()
      .peek(b -> b.makeCall(name, 0))
      .map(MirBuilder::build);
  }

  static List<MirBuilder> instantiateRec(
      final PrimitiveAnd op, final MirBuilder builder) {
    final List<MirBuilder> queue = Lists.newList(builder);
    final List<MirBuilder> swap = Lists.newList();
    for (final var entry : op.getArguments().entrySet()) {
      final Primitive p = entry.getValue();
      if (p.isOrRule() && p.getKind().equals(Primitive.Kind.OP)) {
        for (final PrimitiveAnd v : variantsOf(p)) {
          for (final var b : queue) {
            final String name = b.getName() + "_" + v.getName();
            swap.addAll(instantiateRec(v, b.copyAs(name)));
          }
        }
        Lists.moveAll(queue, swap);
      } else if (p.getKind().equals(Primitive.Kind.OP)) {
        for (final var b : queue) {
          swap.addAll(instantiateRec((PrimitiveAnd) p, b));
        }
        Lists.moveAll(queue, swap);
      } else {
        for (final var b : queue) {
          b.refParameter(b.addParameter(NmlIrTrans.typeOf(p)));
        }
      }
    }
    for (final var b : queue) {
      b.makeClosure(op.getName(), op.getArguments().size());
    }
    return queue;
  }

  static List<PrimitiveAnd> variantsOf(final Primitive p) {
    if (p.isOrRule()) {
      final List<PrimitiveAnd> variants = new java.util.ArrayList<>();
      collectVariants((PrimitiveOr) p, variants);
      return variants;
    }
    return Collections.singletonList((PrimitiveAnd) p);
  }

  static void collectVariants(final PrimitiveOr input, final List<PrimitiveAnd> variants) {
    for (final Primitive p : input.getOrs()) {
      if (p.isOrRule()) {
        collectVariants((PrimitiveOr) p, variants);
      } else {
        variants.add((PrimitiveAnd) p);
      }
    }
  }

  private static JsonObject createManifest(final Ir ir) {
    final IrInquirer inquirer = new IrInquirer(ir);
    final JsonObjectBuilder manifest = Json.createObjectBuilder();
    final JsonArrayBuilder hwstate = Json.createArrayBuilder();

    for (final MemoryResource mem : ir.getMemory().values()) {
      final Location l = Location.createMemoryBased(mem.getName(), mem, null);
      if (inquirer.isPC(l)) {
        manifest.add("program_counter", Json.createObjectBuilder()
          .add("name", mem.getName())
          .add("size", mem.getType().getBitSize()));
      }
      if (!mem.getKind().equals(Memory.Kind.VAR)) {
        hwstate.add(Json.createObjectBuilder()
          .add("name", mem.getName())
          .add("kind", mem.getKind().toString().toLowerCase())
          .add("type", NmlIrTrans.typeOf(mem).getName()));
      }
    }
    manifest.add("hwstate", hwstate);

    return manifest.build();
  }

  private Map<NamePath, MirContext> loadMir(final Ir ir) {
    if (!cache.containsKey(ir)) {
      cache.put(ir, new java.util.HashMap<NamePath, MirContext>());
    }
    return cache.get(ir);
  }
}
