package ru.ispras.microtesk.translator.mir;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface MirTy {
  int getSize();
  String getName();
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

class Types {
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
}

class FuncTy implements MirTy {
  private final MirTy ret;
  private final List<MirTy> params;

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
}
