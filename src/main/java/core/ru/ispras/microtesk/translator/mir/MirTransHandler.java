package ru.ispras.microtesk.translator.mir;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    final Map<String, MirContext> source = new java.util.HashMap<>();
    for (final MirContext ctx : mirs.values()) {
      source.put(ctx.name, ctx);
    }
    final PassDriver driver = new PassDriver(
      new InlinePass());
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

  final class PassDriver {
    public final List<Pass> passList;

    PassDriver(final Pass... passes) {
      this.passList = Arrays.asList(passes);
    }

    public void run(Map<String, MirContext> source) {
      for (final Pass pass : passList) {
        source = pass.run(source);
      }
    }
  }

  private Map<NamePath, MirContext> loadMir(final Ir ir) {
    if (!cache.containsKey(ir)) {
      cache.put(ir, new java.util.HashMap<NamePath, MirContext>());
    }
    return cache.get(ir);
  }
}
