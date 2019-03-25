package ru.ispras.microtesk.translator.mir;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    final Path path = Paths.get(translator.getOutDir(), ir.getModelName() + ".zip");
    try (final ArchiveWriter archive = new ArchiveWriter(path)) {
      for (final Primitive p : ir.getOps().values()) {
        if (!p.isOrRule()) {
          final PrimitiveAnd item = (PrimitiveAnd) p;
          for (final Attribute attr : item.getAttributes().values()) {
            if (attr.getKind().equals(Attribute.Kind.ACTION)) {
              final MirContext mir = NmlIrTrans.translate(item, attr.getStatements());
              final NamePath name = NamePath.get(item.getName(), attr.getName());
              mirs.put(name, mir);

              try (final Writer writer = archive.newText(name.resolve("mir").toString())) {
                final MirText text = new MirText(mir);
                writer.write(text.toString());
              }
            }
          }
        }
      }
    } catch (final IOException e) {
      Logger.error("Failed to store MIR '%s': %s", path.toString(), e.toString());
    }
  }

  private Map<NamePath, MirContext> loadMir(final Ir ir) {
    if (!cache.containsKey(ir)) {
      cache.put(ir, new java.util.HashMap<NamePath, MirContext>());
    }
    return cache.get(ir);
  }
}
