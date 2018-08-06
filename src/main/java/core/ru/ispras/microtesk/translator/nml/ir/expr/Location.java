/*
 * Copyright 2013-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.expr;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.shared.MemoryResource;
import ru.ispras.microtesk.translator.nml.ir.shared.Type;

public final class Location {

  public static final class Bitfield {
    private final Expr from;
    private final Expr to;
    private final Type type;

    private Bitfield(final Expr from, final Expr to, final Type type) {
      InvariantChecks.checkNotNull(from);
      InvariantChecks.checkNotNull(to);
      InvariantChecks.checkNotNull(type);

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
    public boolean equals(final Object obj) {
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
  private final LocationSource source;
  private final Type type;
  private final Expr index;
  private final Bitfield bitfield;

  private Location(
      final String name,
      final LocationSource source,
      final Expr index,
      final Bitfield bitfield) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(source);

    this.name = name;
    this.source = source;
    this.index = index;
    this.bitfield = bitfield;

    final Type sourceType = null != bitfield ? bitfield.getType() : source.getType();
    this.type = sourceType;
  }

  public static Location createMemoryBased(
      final String name,
      final MemoryResource memory,
      final Expr index) {
    return new Location(
        name,
        new LocationSourceMemory(memory),
        index,
        null
        );
  }

  public static Location createPrimitiveBased(
      final String name,
      final Primitive primitive) {
    return new Location(
      name,
      new LocationSourcePrimitive(primitive),
      null,
      null
    );
  }

  public static Location createBitfield(
      final Location location,
      final Expr from,
      final Expr to,
      final Type type) {
    return new Location(
        location.getName(),
        location.getSource(),
        location.getIndex(),
        new Bitfield(from, to, type)
        );
  }

  public String getName() {
    return name;
  }

  public LocationSource getSource() {
    return source;
  }

  public Type getType() {
    return type;
  }

  public Expr getIndex() {
    return index;
  }

  public Bitfield getBitfield() {
    return bitfield;
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

    final Location other = (Location) obj;
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

    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(name);

    if (index != null) {
      sb.append(String.format("[%s]", index.getNode()));
    }

    if (bitfield != null) {
      sb.append(String.format("<%s..%s>",
          bitfield.getFrom().getNode(), bitfield.getTo().getNode()));
    }

    return sb.toString();
  }
}
