package ru.ispras.microtesk.tools.microft;

import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOR;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.json.*;

final class Attribute {
  public static List<IrPass<?>> insnPasses() {
    final List<IrPass<?>> passes = new ArrayList<>();
    passes.add(new PassFormat("Syntax"));
    passes.add(new PassFormat("Image"));
    return passes;
  }

  public static List<IrPass<JsonValue>> insnAttributes() {
    final List<IrPass<JsonValue>> attrs = new ArrayList<>();
    attrs.add(new Format("syntax", "Syntax"));
    attrs.add(new Format("image", "Image"));
    attrs.add(new Mnemonic());
    attrs.add(new IrPass<JsonValue>("name") {
      @Override
      public JsonValue run(final List<PrimitiveAND> insn, final PassContext ctx) {
        return JsonUtil.createString(insn.get(0).getName());
      }
    });

    return attrs;
  }

  public static class Mnemonic extends IrPass<JsonValue> {
    public Mnemonic() {
      super("mnemonic", "syntax");
    }

    @Override
    public JsonValue run(final List<PrimitiveAND> insn, final PassContext ctx) {
      final JsonValue syntax = ctx.getPassIr("syntax", JsonValue.class);
      if (syntax.getValueType() == JsonValue.ValueType.STRING) {
        final String s[] = ((JsonString) syntax).getString().split("\\s+");
        return JsonUtil.createString(s[0]);
      }
      return JsonValue.NULL;
    }
  }

  public static class Format extends IrPass<JsonValue> {
    public Format(final String attr, final String src) {
      super(attr, src);
    }

    @Override
    public JsonValue run(final List<PrimitiveAND> insn, final PassContext ctx) {
      final PassFormat.Info info =
        ctx.getPassIr(getDependencies().get(0), PassFormat.Info.class);
      if (info.formatLine != null) {
        return JsonUtil.createString(info.formatLine);
      } else {
        return JsonValue.NULL;
      }
    }
  }

  private Attribute() {}
}
