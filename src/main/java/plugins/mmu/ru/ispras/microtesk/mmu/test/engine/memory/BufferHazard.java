/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.test.engine.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.Nodes;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;

/**
 * {@link BufferHazard} describes a buffer access hazard.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BufferHazard {
  public static enum Type {
    /** The conflict of the kind {@code Index1 != Index2}. */
    INDEX_NOT_EQUAL("IndexNotEqual", false) {
      @Override
      public Node getCondition(
          final MmuBufferAccess bufferAccess1,
          final MmuBufferAccess bufferAccess2) {

        // Index1 != Index2.
        return Nodes.and(
            Collections.<Node>singletonList(
                Nodes.noteq(
                    bufferAccess1.getIndexExpression(),
                    bufferAccess2.getIndexExpression())));
      }
    },

    /** The conflict of the kind {@code Index1 == Index2 && Tag1 != Tag2}. */
    TAG_NOT_EQUAL("TagNotEqual", false) {
      @Override
      public Node getCondition(
          final MmuBufferAccess bufferAccess1,
          final MmuBufferAccess bufferAccess2) {
        final MmuBuffer buffer = bufferAccess1.getBuffer();
        final List<Node> atoms = new ArrayList<>();

        if (buffer.getSets() > 1 && buffer.getIndexExpression() != null) {
          atoms.add(Nodes.eq(
              bufferAccess1.getIndexExpression(),
              bufferAccess2.getIndexExpression()));
        }

        atoms.add(Nodes.noteq(
            bufferAccess1.getTagExpression(),
            bufferAccess2.getTagExpression()));

        // Index1 == Index2 && Tag1 != Tag2.
        return Nodes.and(atoms);
      }
    },

    /** The conflict of the kind {@code Index1 == Index2 && Tag1 == Tag2}. */
    TAG_EQUAL("TagEqual", true) {
      @Override
      public Node getCondition(
          final MmuBufferAccess bufferAccess1,
          final MmuBufferAccess bufferAccess2) {
        final MmuBuffer buffer = bufferAccess1.getBuffer();
        final List<Node> atoms = new ArrayList<>();

        if (buffer.getSets() > 1 && buffer.getIndexExpression() != null) {
          atoms.add(Nodes.eq(
              bufferAccess1.getIndexExpression(),
              bufferAccess2.getIndexExpression()));
        }

        atoms.add(Nodes.eq(
            bufferAccess1.getTagExpression(),
            bufferAccess2.getTagExpression()));

        // Index1 == Index2 && Tag1 == Tag2.
        return Nodes.and(atoms);
      }
    };

    /** The conflict type name. */
    private final String name;
    /** The equality/inequality flag. */
    private final boolean equal;

    private Type(final String name, final boolean equal) {
      this.name = name;
      this.equal = equal;
    }

    public String getName() {
      return name;
    }

    public boolean isEquality() {
      return equal;
    }

    /**
     * Returns the condition that characterizes the conflict between two buffer accesses.
     * 
     * @param bufferAccess1 the first buffer access.
     * @param bufferAccess2 the second buffer access (depends some how on the first one).
     * 
     * @return the hazard condition.
     */
    public abstract Node getCondition(
        final MmuBufferAccess bufferAccess1,
        final MmuBufferAccess bufferAccess2);
  }

  public static Collection<BufferHazard> getHazards(final MmuBuffer buffer) {
    InvariantChecks.checkNotNull(buffer);

    final Collection<BufferHazard> hazards = new ArrayList<>();

    if (buffer.getSets() > 1 && buffer.getIndexExpression() != null) {
      // Index1 != Index2.
      hazards.add(new BufferHazard(Type.INDEX_NOT_EQUAL, buffer));
    }

    if (buffer.getTagExpression() != null) {
      // Index1 == Index2 && Tag1 != Tag2.
      hazards.add(new BufferHazard(Type.TAG_NOT_EQUAL, buffer));
      // Index1 == Index2 && Tag1 == Tag2.
      hazards.add(new BufferHazard(Type.TAG_EQUAL, buffer));
    }

    return hazards;
  }

  public static class Instance {
    private final BufferHazard hazardType;

    private final MmuBufferAccess bufferAccess1;
    private final MmuBufferAccess bufferAccess2;

    private final Node condition;

    private Instance(
        final BufferHazard hazardType,
        final MmuBufferAccess bufferAccess1,
        final MmuBufferAccess bufferAccess2,
        final Node condition) {
      InvariantChecks.checkNotNull(hazardType);
      InvariantChecks.checkNotNull(bufferAccess1);
      InvariantChecks.checkNotNull(bufferAccess2);
      InvariantChecks.checkNotNull(condition);

      this.hazardType = hazardType;

      this.bufferAccess1 = bufferAccess1;
      this.bufferAccess2 = bufferAccess2;

      this.condition = condition;
    }

    public BufferHazard getHazardType() {
      return hazardType;
    }

    public MmuBufferAccess getPrimaryAccess() {
      return bufferAccess1;
    }

    public MmuBufferAccess getSecondaryAccess() {
      return bufferAccess2;
    }

    public Node getCondition() {
      return condition;
    }

    @Override
    public int hashCode() {
      int hashCode = hazardType.hashCode();

      hashCode = 31 * hashCode + bufferAccess1.hashCode();
      hashCode = 31 * hashCode + bufferAccess2.hashCode();

      return hashCode;
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }

      if (!(o instanceof Instance)) {
        return false;
      }

      final Instance r = (Instance) o;

      return hazardType.equals(r.hazardType)
          && bufferAccess1.equals(r.bufferAccess1)
          && bufferAccess2.equals(r.bufferAccess2);
    }

    @Override
    public String toString() {
      return String.format("%s[%s,%s]", hazardType, bufferAccess1, bufferAccess2);
    }
  }

  private final Type type;
  private final MmuBuffer buffer;

  public BufferHazard(
      final Type type,
      final MmuBuffer buffer) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(buffer);

    this.type = type;
    this.buffer = buffer;
  }

  public Type getType() {
    return type;
  }

  public MmuBuffer getBuffer() {
    return buffer;
  }

  public String getName() {
    return type.getName();
  }

  /**
   * Returns the hazard name extended with the buffer name.
   * 
   * @return the full hazard name.
   */
  public String getFullName() {
    return String.format("%s.%s", buffer.getName(), type.getName());
  }

  public Instance makeInstance(
      final MmuBufferAccess bufferAccess1,
      final MmuBufferAccess bufferAccess2) {
    InvariantChecks.checkNotNull(bufferAccess1);
    InvariantChecks.checkNotNull(bufferAccess2);
    InvariantChecks.checkTrue(bufferAccess1.getBuffer().equals(bufferAccess2.getBuffer()));

    return new Instance(
        this,
        bufferAccess1,
        bufferAccess2,
        type.getCondition(bufferAccess1, bufferAccess2));
  }

  @Override
  public int hashCode() {
    return getFullName().hashCode();
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }

    if (o == null || !(o instanceof BufferHazard)) {
      return false;
    }

    final BufferHazard r = (BufferHazard) o;
    return getFullName().equals(r.getFullName());
  }

  @Override
  public String toString() {
    return getFullName();
  }
}
