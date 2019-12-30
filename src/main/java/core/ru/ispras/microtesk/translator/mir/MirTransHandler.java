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
    try {
      Files.createDirectories(Paths.get(translator.getOutDir()));
      try (final ArchiveWriter archive = new ArchiveWriter(path)) {
        try (final JsonWriter writer =
            Json.createWriter(archive.newText(MirArchive.MANIFEST))) {
          writer.write(createManifest(ir));
        }
        for (final MirContext ctx : opt.values()) {
          try (final Writer writer = archive.newText(ctx.name + ".mir")) {
            writer.write(MirText.toString(ctx));
          }
        }
      }
    } catch (final IOException e) {
      Logger.error("Failed to store MIR '%s': %s", path.toString(), e.toString());
    }
    final Instantiator worker = new Instantiator(opt);
    worker.run(ir);
  }

  private static final class Instantiator {
    final Map<String, MirContext> library;
    final Map<String, MirContext> instances = new java.util.HashMap<>();

    Instantiator(final Map<String, MirContext> library) {
      this.library = library;
    }

    void run(final Ir ir) {
      final Map<Primitive, List<PrimitiveAnd>> variantMap = new java.util.HashMap<>();
      for (final Primitive p : ir.getOps().values()) {
        variantMap.put(p, variantsOf(p));
      }
      final InstanceTree tree = new InstanceTree();
      for (final Primitive p : ir.getOps().values()) {
        if (!p.isOrRule()) {
          final PrimitiveAnd op = (PrimitiveAnd) p;
          final ITNode node = newNode(op, variantMap);

          tree.nodeMap.put(op, node);
          if (node.siblings().isEmpty()) {
            tree.instances.add(node);
          }
        }
      }
      for (final ITNode node : tree.nodes()) {
        for (final PrimitiveAnd p : node.siblings()) {
          tree.nodeMap.get(p).parents.add(node);
        }
      }
    }

    static ITNode newNode(
        final PrimitiveAnd op,
        final Map<Primitive, List<PrimitiveAnd>> variantMap) {
      for (final Map.Entry<String, Primitive> param : op.getArguments().entrySet()) {
        final Primitive type = param.getValue();
        if (type.getKind().equals(Primitive.Kind.OP) && type.isOrRule()) {
          final List<PrimitiveAnd> variants = variantMap.get(param.getValue());
          return new ITNode(op, Collections.singletonMap(param.getKey(), variants));
        }
      }
      return new ITNode(op, Collections.<String, List<PrimitiveAnd>>emptyMap());
    }
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

  private static final class InstanceTree {
    final Map<PrimitiveAnd, ITNode> nodeMap = new java.util.HashMap<>();
    final List<ITNode> instances = new java.util.ArrayList<>();

    Collection<ITNode> nodes() {
      return nodeMap.values();
    }
  }

  private static final class ITNode {
    final PrimitiveAnd origin;
    final Map<String, List<PrimitiveAnd>> bindings;
    final List<ITNode> parents = new java.util.ArrayList<>();

    ITNode(final PrimitiveAnd origin, final Map<String, List<PrimitiveAnd>> bindings) {
      this.origin = origin;
      this.bindings = bindings;
    }

    List<PrimitiveAnd> siblings() {
      if (bindings.isEmpty()) {
        return Collections.emptyList();
      }
      return bindings.values().iterator().next();
    }

    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder(origin.getName());
      for (final Map.Entry<String, Primitive> entry : origin.getArguments().entrySet()) {
        if (bindings.containsKey(entry.getKey())) {
          sb.append(" (");
          final java.util.Iterator<PrimitiveAnd> it = this.siblings().iterator();
          sb.append(it.next().getName());
          while (it.hasNext()) {
            sb.append(" | ");
            sb.append(it.next().getName());
          }
          sb.append(")");
        } else {
          sb.append(" ");
          sb.append(entry.getValue().getName());
        }
      }
      return sb.toString();
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
