package ru.ispras.microtesk.translator.mir;

import java.util.Map;

import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.primitive.Attribute;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
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
        final PrimitiveAND item = (PrimitiveAND) p;
        for (final Attribute attr : item.getAttributes().values()) {
          if (attr.getKind().equals(Attribute.Kind.ACTION)) {
            mirs.put(
              NamePath.get(item.getName(), attr.getName()),
              NmlIrTrans.translate(item, attr.getStatements()));
          }
        }
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
