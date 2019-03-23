package ru.ispras.microtesk.translator.mir;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface MirTy {
  int getSize();
  String getName();
  MirTy typeOf(String s);
}

class TyRef {
  public final MirTy type;
  public final String name;
  public final Types context;

  public TyRef(final MirTy type, final String name, final Types context) {
    this.type = type;
    this.name = name;
    this.context = context;
  }

  public TyRef(final MirTy type) {
    this.type = type;
    this.name = type.getName();
    this.context = Types.singleton(this);
  }
}

final class VoidTy implements MirTy, Operand {
  public static final VoidTy VALUE = new VoidTy();

  @Override
  public MirTy getType() {
    return this;
  }

  @Override
  public int getSize() {
    return 0;
  }

  @Override
  public String getName() {
    return "void";
  }

  @Override
  public MirTy typeOf(final String s) {
    if (s.equals(this.getName())) {
      return this;
    }
    return null;
  }

  private VoidTy() {}
}

class Types {
  public static final MirTy BIT = new IntTy(1);
  public static final MirTy VOID = VoidTy.VALUE;

  private final Map<String, MirTy> types;

  Types() {
    this(new java.util.HashMap<String, MirTy>());
  }

  private Types(final Map<String, MirTy> types) {
    this.types = types;
  }

  public static Types singleton(final TyRef ref) {
    return new Types(Collections.singletonMap(ref.name, ref.type));
  }

  public static MirTy valueOf(final String s) {
    final List<MirTy> samples =
        java.util.Arrays.asList(BIT, VOID, FpTy.F32, new MirArray(1, new TyRef(BIT)));
    for (final MirTy sample : samples) {
      for (final MirTy ty = sample.typeOf(s); ty != null;) {
        return ty;
      }
    }
    throw new IllegalArgumentException();
  }

  static <T> String concat(final Iterable<T> values, final String delim) {
    final Iterator<T> it = values.iterator();
    if (it.hasNext()) {
      final T head = it.next();
      if (!it.hasNext()) {
        return head.toString();
      }
      final StringBuilder sb = new StringBuilder(head.toString());
      while (it.hasNext()) {
        sb.append(delim);
        sb.append(it.next().toString());
      }
      return sb.toString();
    }
    return "";
  }
}

class IntTy implements MirTy {
  private final int size;

  public IntTy(final int size) {
    this.size = size;
  }

  @Override
  public int getSize() {
    return size;
  }

  @Override
  public String getName() {
    return String.format("i%d", size);
  }

  @Override
  public IntTy typeOf(final String s) {
    if (s.startsWith("i")) {
      return new IntTy(Integer.valueOf(s.substring(1)));
    }
    return null;
  }
}

enum FpTy implements MirTy {
  F16(16),
  F32(32),
  F64(64),
  F128(128);

  @Override
  public int getSize() {
    return size;
  }

  @Override
  public String getName() {
    return String.format("f%d", size);
  }

  @Override
  public MirTy typeOf(final String s) {
    for (final FpTy ty : FpTy.values()) {
      if (ty.getName().equals(s)) {
        return ty;
      }
    }
    return null;
  }

  private FpTy(final int size) {
    this.size = size;
  }

  private final int size;
}

class MirArray implements MirTy {
  public final int size;
  public final TyRef ref;

  public MirArray(final int size, final TyRef ref) {
    this.size = size;
    this.ref = ref;
  }

  @Override
  public int getSize() {
    return ref.type.getSize() * size;
  }

  @Override
  public String getName() {
    return String.format("[%d, %s]", size, ref.name);
  }

  @Override
  public MirTy typeOf(final String s) {
    final Pattern p = Pattern.compile("\\[(\\d+)x(\\w+)\\]");
    final Matcher m = p.matcher(s);

    while (m.find()) {
      final int size = Integer.valueOf(m.group(1));
      final MirTy ty = Types.valueOf(m.group(2));

      return new MirArray(size, new TyRef(ty));
    }
    return null;
  }
}

class MirStruct implements MirTy {
  public final Map<String, TyRef> fields;

  public MirStruct(final Map<String, TyRef> fields) {
    this.fields = fields;
  }

  @Override
  public int getSize() {
    int size = 0;
    for (final TyRef ref : fields.values()) {
      size += ref.type.getSize();
    }
    return size;
  }

  @Override
  public String getName() {
    final List<String> names = new java.util.ArrayList<>(fields.size());
    for (final TyRef ref : fields.values()) {
      names.add(ref.name);
    }
    return String.format("{%s}", Types.concat(names, ", "));
  }

  @Override
  public MirTy typeOf(final String s) {
    return null;
  }
}

class MirPointer implements MirTy {
  private final int size;
  private final TyRef ref;

  public MirPointer(final int size, final TyRef ref) {
    this.size = size;
    this.ref = ref;
  }

  @Override
  public int getSize() {
    return size;
  }

  @Override
  public String getName() {
    return ref.name + " *";
  }

  @Override
  public MirTy typeOf(final String s) {
    return null;
  }
}

class FuncTy implements MirTy {
  public final MirTy ret;
  public final List<MirTy> params;

  public FuncTy(final MirTy ret, final List<MirTy> params) {
    this.ret = ret;
    this.params = params;
  }

  @Override
  public int getSize() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName() {
    final List<String> names = new java.util.ArrayList<>(params.size());
    for (final MirTy type : params) {
      names.add(type.getName());
    }
    return String.format("(%s) -> %s", Types.concat(names, ", "), ret.getName());
  }

  @Override
  public MirTy typeOf(final String s) {
    return null;
  }
}
