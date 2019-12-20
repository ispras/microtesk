package ru.ispras.microtesk.translator.mir;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.castle.util.Logger;
import ru.ispras.microtesk.model.memory.Memory;
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
    final Map<NamePath, MirContext> mirs = loadMir(ir);
    for (final Primitive p : ir.getOps().values()) {
      if (!p.isOrRule()) {
        final PrimitiveAnd item = (PrimitiveAnd) p;
        for (final Attribute attr : item.getAttributes().values()) {
          if (attr.getKind().equals(Attribute.Kind.ACTION)) {
            Logger.debug("TRANSLATE: nML -> MIR: %s.%s", item.getName(), attr.getName());
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
        Logger.debug("TRANSLATE: nML -> MIR: %s", item.getName());
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
    final MirPassDriver driver = MirPassDriver.newDefault();
    final Map<String, MirContext> opt = driver.run(source);

    final Path path = Paths.get(translator.getOutDir(), ir.getModelName() + ".zip");
    try (final ArchiveWriter archive = new ArchiveWriter(path)) {
      try (final JsonWriter writer =
          Json.createWriter(archive.newText(MirArchive.MANIFEST))) {
        writer.write(createManifest(ir));
      }
      for (final MirContext ctx : opt.values()) {
        try (final Writer writer = archive.newText(ctx.name + ".mir")) {
          final MirText text = new MirText(ctx);
          writer.write(text.toString());
        }
      }
    } catch (final IOException e) {
      Logger.error("Failed to store MIR '%s': %s", path.toString(), e.toString());
    }
    final Instantiator worker = new Instantiator(opt);
    worker.run(ir.getOps().get("instruction"));
  }

  private static final class Instantiator {
    final Map<String, MirContext> library;
    final Map<String, MirContext> instances = new java.util.HashMap<>();

    Instantiator(final Map<String, MirContext> library) {
      this.library = library;
    }

    void run(final Primitive p) {
      final List<PrimitiveAnd> entries =
          listVariants(new java.util.ArrayList<PrimitiveAnd>(), p);
      for (final PrimitiveAnd entry : entries) {
        final List<PrimitiveAnd> variants = new java.util.ArrayList<>();

        int count = 0;
        for (final Primitive param : entry.getArguments().values()) {
          if (param.getKind().equals(Primitive.Kind.OP) && param.isOrRule()) {
            listVariants(variants, param);
            ++count;
          }
        }
        final String name = String.format("%s.action", entry.getName());
        if (count == 0) {
          instances.put(name, library.get(name));
        } else if (count == 1) {
        }
      }
    }

    private static List<PrimitiveAnd> listVariants(
        final List<PrimitiveAnd> variants, final Primitive root) {
      final List<Primitive> queue = new java.util.ArrayList<>();
      queue.add(root);

      while (!queue.isEmpty()) {
        final Primitive p = removeLast(queue);
        if (p.isOrRule()) {
          queue.addAll(((PrimitiveOr) p).getOrs());
        } else {
          variants.add((PrimitiveAnd) p);
        }
      }
      return variants;
    }

    private static <T> T removeLast(final List<T> list) {
      return list.remove(list.size() - 1);
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
