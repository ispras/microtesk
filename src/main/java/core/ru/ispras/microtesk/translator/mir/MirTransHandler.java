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
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAnd;
import ru.ispras.microtesk.utils.NamePath;

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
    final PassDriver driver = new PassDriver(
      new ForwardPass(),
      new InlinePass(),
      new ForwardPass(),
      new ConcFlowPass(),
      new GlobalNumbering()
      );
    driver.run(source);

    final Path path = Paths.get(translator.getOutDir(), ir.getModelName() + ".zip");
    try (final ArchiveWriter archive = new ArchiveWriter(path)) {
      for (final MirContext ctx : mirs.values()) {
        try (final Writer writer = archive.newText(ctx.name + ".mir")) {
          final MirText text = new MirText(ctx);
          writer.write(text.toString());

          for (final Pass pass : driver.passList) {
            final MirText passText = new MirText(pass.result.get(ctx.name));
            writer.write(passText.toString());
          }
        }
      }
    } catch (final IOException e) {
      Logger.error("Failed to store MIR '%s': %s", path.toString(), e.toString());
    }
  }

  final static class PassDriver {
    public final List<Pass> passList;
    private final Map<String, MirContext> storage = new java.util.HashMap<>();

    PassDriver(final Pass... passes) {
      this.passList = Arrays.asList(passes);
      for (final Pass pass : passList) {
        pass.storage = this.storage;
      }
    }

    public void run(Map<String, MirContext> source) {
      final List<String> ordered = dependencyOrder(source);
      for (final String name : ordered) {
        MirContext ctx = source.get(name);
        for (final Pass pass : passList) {
          pass.source.put(name, ctx);
          ctx = pass.apply(ctx);
          pass.result.put(name, ctx);
        }
        storage.put(name, ctx);
      }
    }

    public static List<String> dependencyOrder(final Map<String, MirContext> source) {
      final Map<String, List<String>> deps = new java.util.HashMap<>();
      for (final MirContext ctx : source.values()) {
        deps.put(ctx.name, listDeps(ctx, source));
      }
      final List<String> ordered = new java.util.ArrayList<>();
      while (!deps.isEmpty()) {
        orderDeps(deps.keySet().iterator().next(), ordered, deps);
      }
      return ordered;
    }

    private static void orderDeps(
        final String name,
        final List<String> ordered,
        final Map<String, List<String>> deps) {
      if (deps.containsKey(name)) {
        for (final String dep : deps.get(name)) {
          orderDeps(dep, ordered, deps);
        }
        ordered.add(name);
        deps.remove(name);
      }
    }

    private static List<String> listDeps(final MirContext ctx, final Map<String, MirContext> source) {
      final Set<String> deps = new java.util.HashSet<>();
      for (final BasicBlock bb : ctx.blocks) {
        for (final Instruction insn : bb.insns) {
          if (insn instanceof Call) {
            final Call call = (Call) insn;
            final String dep = InlinePass.resolveCalleeName(call);
            if (source.containsKey(dep)) {
              deps.add(dep);
            }
          }
        }
      }
      if (deps.isEmpty()) {
        return Collections.emptyList();
      }
      return new java.util.ArrayList<>(deps);
    }
  }

  private Map<NamePath, MirContext> loadMir(final Ir ir) {
    if (!cache.containsKey(ir)) {
      cache.put(ir, new java.util.HashMap<NamePath, MirContext>());
    }
    return cache.get(ir);
  }
}
