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
  public static List<IrPass<?>> modePasses() {
    return insnPasses();
  }

  public static List<IrPass<?>> insnPasses() {
    final List<IrPass<?>> passes = new ArrayList<>();
    passes.add(new PassFormat("Syntax"));
    passes.add(new PassFormat("Image"));

    return passes;
  }

  public static List<IrPass<JsonValue>> modeAttributes() {
    final List<IrPass<JsonValue>> attrs = new ArrayList<>();
    attrs.add(new Format("syntax", "Syntax"));
    attrs.add(new Format("image", "Image"));
    attrs.add(new Parameters());
    attrs.add(new IrPass<JsonValue>("name") {
      @Override
      public JsonValue run(final List<PrimitiveAND> insn, final PassContext ctx) {
        return JsonUtil.createString(insn.get(0).getName());
      }
    });
    return attrs;
  }

  public static List<IrPass<JsonValue>> insnAttributes() {
    final List<IrPass<JsonValue>> attrs = new ArrayList<>();
    attrs.add(new Format("syntax", "Syntax"));
    attrs.add(new Format("image", "Image"));
    attrs.add(new Mnemonic());
    attrs.add(new Parameters());
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

  public static class Parameters extends IrPass<JsonValue> {
    public Parameters() {
      super("parameters", "Image", "Syntax");
    }

    @Override
    public JsonValue run(final List<PrimitiveAND> insn, final PassContext ctx) {
      final List<String> deps = getDependencies();
      final PassFormat.Info p0 = ctx.getPassIr(deps.get(0), PassFormat.Info.class);
      final PassFormat.Info p1 = ctx.getPassIr(deps.get(1), PassFormat.Info.class);

      final Map<String, Primitive> source = new LinkedHashMap<>();
      source.putAll(p0.parameters);
      source.putAll(p1.parameters);

      final JsonArrayBuilder params = Json.createArrayBuilder();
      for (final Map.Entry<String, Primitive> entry : source.entrySet()) {
        final JsonObjectBuilder param = Json.createObjectBuilder();

        final JsonArrayBuilder types = Json.createArrayBuilder();
        for (final Primitive type : variantsOf(entry.getValue())) {
          types.add(type.getName());
        }

        param.add("name", entry.getKey());
        param.add("type", types);

        params.add(param);
      }
      return params.build();
    }

    private static List<Primitive> variantsOf(final Primitive src) {
      if (!src.isOrRule()) {
        return Collections.singletonList(src);
      }

      final List<Primitive> variants = new ArrayList<>();
      final Deque<Primitive> queue = new ArrayDeque<>();

      queue.add(src);
      while (!queue.isEmpty()) {
        final Primitive p = queue.remove();
        if (p.isOrRule()) {
          queue.addAll(PrimitiveOR.class.cast(p).getOrs());
        } else {
          variants.add(p);
        }
      }
      return variants;
    }
  }

  private Attribute() {}
}
