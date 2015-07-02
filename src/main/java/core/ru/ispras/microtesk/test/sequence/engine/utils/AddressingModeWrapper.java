/*
 * Copyright 2013-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.sequence.engine.utils;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Primitive;

/**
 * Wrapper class for addressing mode primitives that allows checking equality and calculating
 * hash code. This is needed to avoid initializations of the same resources that would overwrite
 * each other. 
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class AddressingModeWrapper {
  private final Primitive mode;

  public AddressingModeWrapper(Primitive mode) {
    checkNotNull(mode);
    if (mode.getKind() != Primitive.Kind.MODE) {
      throw new IllegalArgumentException(mode.getSignature() + " is not an addresing mode.");
    }
    this.mode = mode;
  }

  public Primitive getModePrimitive() {
    return mode;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for (Argument arg : mode.getArguments().values()) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(arg.getName() + ": " + arg.getTypeName());
      sb.append(" = " + arg.getImmediateValue());
    }

    return String.format("%s %s(%s)", mode.getKind().getText(), mode.getName(), sb);
  }

  @Override
  public int hashCode() {
    final int prime = 31;

    int result = prime + mode.getName().hashCode();
    for (final Argument arg : mode.getArguments().values()) {
      result = prime * result + arg.getName().hashCode();
      result = prime * result + arg.getImmediateValue().hashCode();
    }

    return result;
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

    final AddressingModeWrapper other = (AddressingModeWrapper) obj;
    final Primitive otherMode = other.mode;

    if (!mode.getName().equals(otherMode.getName())) {
      return false;
    }

    if (mode.getArguments().size() != otherMode.getArguments().size()) {
      return false;
    }

    final java.util.Iterator<Argument> thisIt = mode.getArguments().values().iterator();
    final java.util.Iterator<Argument> otherIt = otherMode.getArguments().values().iterator();

    while (thisIt.hasNext() && otherIt.hasNext()) {
      final Argument thisArg = thisIt.next();
      final Argument otherArg = otherIt.next();

      if (!thisArg.getName().equals(otherArg.getName())) {
        return false;
      }

      if (thisArg.getImmediateValue() != otherArg.getImmediateValue()) {
        return false;
      }
    }

    return true;
  }
}
