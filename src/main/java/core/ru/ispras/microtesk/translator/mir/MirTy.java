package ru.ispras.microtesk.translator.mir;

import java.util.Iterator;
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
}

class Types {
  private final Map<String, MirTy> types = new java.util.HashMap<>();
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
  private final int size;
  private final TyRef ref;

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
  private final Map<String, TyRef> fields;

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
    final Iterator<TyRef> it = fields.values().iterator();
    final StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append(it.next().name);
    while (it.hasNext()) {
      sb.append(", ");
      sb.append(it.next().name);
    }
    sb.append("}");

    return sb.toString();
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
