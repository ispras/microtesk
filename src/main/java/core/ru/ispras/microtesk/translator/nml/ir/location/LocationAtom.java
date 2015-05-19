/*
 * Copyright 2013-2014 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.translator.nml.ir.location;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import ru.ispras.microtesk.translator.nml.ESymbolKind;
import ru.ispras.microtesk.translator.nml.ir.expression.Expr;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryExpr;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

public final class LocationAtom implements Location {
  public static interface Source {
    public ESymbolKind getSymbolKind();
    public Type getType();
  }

  public static final class MemorySource implements Source {
    private final MemoryExpr memory;

    private MemorySource(MemoryExpr memory) {
      checkNotNull(memory);
      this.memory = memory;
    }

    @Override
    public ESymbolKind getSymbolKind() {
      return ESymbolKind.MEMORY;
    }

    @Override
    public Type getType() {
      return memory.getType();
    }

    public MemoryExpr getMemory() {
      return memory;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }

      if (obj == null) {
        return false;
      }
      
      if (getClass() != obj.getClass()) {
        return false;
      }

      final MemorySource other = (MemorySource) obj;
      return memory == other.memory;
    }
  }

  public static final class PrimitiveSource implements Source {
    private final Primitive primitive;

    private PrimitiveSource(Primitive primitive) {
      checkNotNull(primitive);
      this.primitive = primitive;
    }

    @Override
    public ESymbolKind getSymbolKind() {
      return ESymbolKind.ARGUMENT;
    }

    @Override
    public Type getType() {
      return primitive.getReturnType();
    }

    public Primitive getPrimitive() {
      return primitive;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }

      if (obj == null) {
        return false;
      }

      if (getClass() != obj.getClass()) {
        return false;
      }

      final PrimitiveSource other = (PrimitiveSource) obj;
      return primitive == other.primitive;
    }
  }

  public static final class Bitfield {
    private final Expr from;
    private final Expr to;
    private final Type type;

    private Bitfield(Expr from, Expr to, Type type) {
      checkNotNull(from);
      checkNotNull(to);
      checkNotNull(type);

      this.from = from;
      this.to = to;
      this.type = type;
    }

    public Expr getFrom() {
      return from;
    }

    public Expr getTo() {
      return to;
    }

    public Type getType() {
      return type;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }

      if (obj == null) {
        return false;
      }

      if (getClass() != obj.getClass()) {
        return false;
      }

      final Bitfield other = (Bitfield) obj;
      if (!type.equals(other.getType())) {
        return false;
      }

      return from.equals(other.from) && to.equals(other.to);
    }
  }

  private final String name;
  private final Source source;
  private final Type type;
  private final Expr index;
  private final Bitfield bitfield;
  private final int repeatCount;

  private LocationAtom(
      final String name,
      final Source source,
      final Expr index,
      final Bitfield bitfield) {
    this(name, source, index, bitfield, 1);
  }

  private LocationAtom(
      final String name,
      final Source source,
      final Expr index,
      final Bitfield bitfield,
      final int repeatCount) {
    checkNotNull(name);
    checkNotNull(source);

    this.name = name;
    this.source = source;
    this.index = index;
    this.bitfield = bitfield;
    this.repeatCount = repeatCount;

    final Type sourceType = null != bitfield ? bitfield.getType() : source.getType();
    this.type = sourceType.resize(sourceType.getBitSize() * repeatCount);
  }

  static LocationAtom createMemoryBased(String name, MemoryExpr memory, Expr index) {
    return new LocationAtom(
      name,
      new MemorySource(memory),
      index,
      null
    );
  }

  static LocationAtom createPrimitiveBased(String name, Primitive primitive) {
    return new LocationAtom(
      name,
      new PrimitiveSource(primitive),
      null,
      null
    );
  }

  static LocationAtom createBitfield(LocationAtom location, Expr from, Expr to, Type type) {
    return new LocationAtom(
      location.getName(),
      location.getSource(),
      location.getIndex(),
      new Bitfield(from, to, type)
    );
  }

  LocationAtom repeat(final int count) {
    return new LocationAtom(
        name,
        source,
        index,
        bitfield,
        repeatCount * count
        );
  }

  public String getName() {
    return name;
  }

  public Source getSource() {
    return source;
  }

  @Override
  public Type getType() {
    return type;
  }

  public Expr getIndex() {
    return index;
  }

  public Bitfield getBitfield() {
    return bitfield;
  }

  public int getRepeatCount() {
    return repeatCount;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final LocationAtom other = (LocationAtom) obj;
    if (!name.equals(other.getName())) {
      return false;
    }

    if (!source.equals(other.source)) {
      return false;
    }

    if (null != index && !index.equals(other.index)) {
      return false;
    }

    if (null != bitfield && !bitfield.equals(other.bitfield)) {
      return false;
    }

    if (repeatCount != other.repeatCount) {
      return false;
    }

    return true;
  }
}
