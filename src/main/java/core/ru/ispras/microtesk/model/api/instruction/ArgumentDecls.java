/*
 * Copyright 2015-2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.instruction;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.api.ArgumentKind;
import ru.ispras.microtesk.model.api.data.Type;

/**
 * The {@link ArgumentDecls} class is aimed to specify declarations of
 * addressing mode and operation arguments.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class ArgumentDecls {
  private final Map<String, Argument> decls;

  public ArgumentDecls() {
    this.decls = new LinkedHashMap<>();
  }

  /**
   * Adds an immediate argument.
   * 
   * @param name
   * @param type
   * @return This object.
   */
  public ArgumentDecls add(final String name, final Type type) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(type);

    final Argument argument = new Argument(name, ArgumentKind.IMM, type) {
      @Override
      public boolean isSupported(final IsaPrimitive o) {
        return false;
      }
    };

    decls.put(name, argument);
    return this;
  }

  /**
   * Adds an addressing-mode-based argument.
   * 
   * @param name
   * @param info
   * @return This object.
   */
  public ArgumentDecls add(final String name, final AddressingMode.IInfo info) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(info);

    final Argument argument = new Argument(name, ArgumentKind.MODE, info.getType()) {
      @Override
      public boolean isSupported(final IsaPrimitive o) {
        return (o instanceof AddressingMode) && info.isSupported((AddressingMode) o);
      }
    };

    decls.put(name, argument);
    return this;
  }

  /**
   * Adds an operation-based argument.
   * 
   * @param name
   * @param info
   * @return This object.
   */
  public ArgumentDecls add(final String name, final Operation.IInfo info) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(info);

    final Argument argument = new Argument(name, ArgumentKind.OP, null) {
      @Override
      public boolean isSupported(final IsaPrimitive o) {
        return (o instanceof Operation) && info.isSupported((Operation) o);
      }
    };

    decls.put(name, argument);
    return this;
  }

  public Map<String, Argument> getDecls() {
    return Collections.unmodifiableMap(decls);
  }

  static abstract class Argument {
    private final String name;
    private final ArgumentKind kind;
    private final Type dataType;

    private Argument(final String name, final ArgumentKind kind, final Type dataType) {
      this.name = name;
      this.kind = kind;
      this.dataType = dataType;
    }

    public final String getName() {
      return name;
    }

    public final ArgumentKind getKind() {
      return kind;
    }

    public final Type getType() {
      return dataType;
    }

    public abstract boolean isSupported(final IsaPrimitive o);

    @Override
    public String toString() {
      return String.format(
          "Argument [name=%s, kind=%s, dataType=%s]",
          name,
          kind,
          dataType
          );
    }
  }
}
