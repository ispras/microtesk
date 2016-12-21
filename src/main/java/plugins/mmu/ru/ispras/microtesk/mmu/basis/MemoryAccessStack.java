/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.basis;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;

/**
 * {@link MemoryAccessStack} represents a memory access stack.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAccessStack {
  /**
   * {@link Frame} represents a memory access stack's frame.
   */
  public final static class Frame {
    private final String id;
    private final Map<IntegerVariable, IntegerVariable> frame = new HashMap<>();

    private Frame(final String id) {
      InvariantChecks.checkNotNull(id);
      this.id = id;
    }

    public IntegerVariable getInstance(final IntegerVariable variable) {
      InvariantChecks.checkNotNull(variable);

      // Constants are not duplicated in stack frames.
      if (variable.isDefined()) {
        return variable;
      }

      IntegerVariable frameVariable = frame.get(variable);

      if (frameVariable == null) {
        final String name = String.format("%s$%s", id, variable.getName());
        final int width = variable.getWidth();

        frame.put(variable, frameVariable = new IntegerVariable(name, width));
      }

      return frameVariable;
    }

    public IntegerField getInstance(final IntegerField field) {
      InvariantChecks.checkNotNull(field);

      final IntegerVariable frameVariable = getInstance(field.getVariable());

      return new IntegerField(frameVariable, field.getLoIndex(), field.getHiIndex());
    }

    @Override
    public String toString() {
      return String.format("%s: %s", id, frame);
    }
  }

  private final String id;
  private final Stack<Frame> stack = new Stack<>();

  public MemoryAccessStack(final String id) {
    InvariantChecks.checkNotNull(id);
    this.id = id;
  }

  public MemoryAccessStack() {
    this("");
  }

  public MemoryAccessStack(final MemoryAccessStack r) {
    InvariantChecks.checkNotNull(r);
    this.id = r.id;
    this.stack.addAll(r.stack);
  }

  public boolean isEmpty() {
    return stack.isEmpty();
  }

  public int size() {
    return stack.size();
  }

  public Frame call(final String id) {
    InvariantChecks.checkNotNull(id);

    final Frame frame = new Frame(getFullId(id));
    return call(frame);
  }
  
  public Frame call(final Frame frame) {
    InvariantChecks.checkNotNull(frame);
    InvariantChecks.checkFalse(stack.contains(frame));

    stack.push(frame);
    return frame;
  }

  public Frame ret() {
    InvariantChecks.checkNotEmpty(stack);
    return stack.pop();
  }

  public Frame getFrame() {
    InvariantChecks.checkNotNull(stack);
    return stack.peek();
  }

  public IntegerVariable getInstance(final IntegerVariable variable) {
    InvariantChecks.checkNotNull(variable);

    if (stack.isEmpty()) {
      return variable;
    }

    final Frame frame = stack.peek();
    return frame.getInstance(variable);
  }

  public IntegerField getInstance(final IntegerField field) {
    InvariantChecks.checkNotNull(field);

    if (stack.isEmpty()) {
      return field;
    }

    final Frame frame = stack.peek();
    return frame.getInstance(field);
  }

  private String getFullId(final String localId) {
    final StringBuffer buffer = new StringBuffer(id);

    boolean delimiter = !id.isEmpty();

    for (final Frame frame : stack) {
      if (delimiter) {
        buffer.append(".");
      }

      buffer.append(frame.id);
      delimiter = true;
    }

    if (delimiter) {
      buffer.append(".");
    }

    buffer.append(localId);

    return buffer.toString();
  }

  @Override
  public String toString() {
    return stack.toString();
  }
}
