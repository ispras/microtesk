/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.api.ArgumentKind;
import ru.ispras.microtesk.model.api.ArgumentMode;
import ru.ispras.microtesk.model.api.data.Type;
import ru.ispras.microtesk.model.api.metadata.MetaArgument;
import ru.ispras.microtesk.model.api.metadata.MetaData;

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
   * @return
   */

  public ArgumentDecls add(
      final String name,
      final Type type) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(type);

    final Argument argument = new Argument(
        name,
        ArgumentKind.IMM,
        ArgumentMode.IN,
        Collections.singleton(AddressingModeImm.NAME),
        type) {

      @Override
      public boolean isSupported(final IPrimitive o) {
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
   * @param mode
   * @param info
   * @return
   */

  public ArgumentDecls add(
      final String name,
      final ArgumentMode mode, // IN/OUT/INOUT/NA (if no return type)
      final AddressingMode.IInfo info) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(mode);
    InvariantChecks.checkNotNull(info);

    final Argument argument = new Argument(
        name,
        ArgumentKind.MODE,
        mode,
        getNames(info.getMetaData()),
        info.getType()) {

      @Override
      public boolean isSupported(final IPrimitive o) {
        return (o instanceof IAddressingMode) && info.isSupported((IAddressingMode) o);
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
   * @return
   */

  public ArgumentDecls add(
      final String name,
      final IOperation.IInfo info) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(info);

    final Argument argument = new Argument(
        name,
        ArgumentKind.OP,
        ArgumentMode.NA,
        getNames(info.getMetaData()),
        null) {

      @Override
      public boolean isSupported(final IPrimitive o) {
        return (o instanceof IOperation) && info.isSupported((IOperation) o);
      }
    };

    decls.put(name, argument);
    return this;
  }

  public Map<String, MetaArgument> getMetaData() {
    final Map<String, MetaArgument> metaData =
        new LinkedHashMap<>(decls.size());

    for (final Argument p : decls.values()) {
      metaData.put(p.getName(), p.getMetaData());
    }

    return metaData;
  }

  public Map<String, Argument> getDecls() {
    return Collections.unmodifiableMap(decls);
  }

  private static Set<String> getNames(final Collection<? extends MetaData> items) {
    final Set<String> names = new LinkedHashSet<>(items.size());
    for (final MetaData item : items) {
      names.add(item.getName());
    }
    return names;
  }

  static abstract class Argument {
    private final String name;
    private final ArgumentKind kind;
    private final ArgumentMode mode;
    private final Set<String> typeNames;
    private final Type dataType;
    private final MetaArgument metaData;

    private Argument(
        final String name,
        final ArgumentKind kind,
        final ArgumentMode mode,
        final Set<String> typeNames,
        final Type dataType) {
      this.name = name;
      this.kind = kind;
      this.mode = mode;

      this.typeNames = typeNames;
      this.dataType = dataType;

      this.metaData = new MetaArgument(
          kind, mode, name, typeNames, dataType);
    }

    public final String getName() {
      return name;
    }

    public final ArgumentKind getKind() {
      return kind;
    }

    public final ArgumentMode getMode() {
      return mode;
    }

    public final Type getType() {
      return dataType;
    }

    public final MetaArgument getMetaData() {
      return metaData;
    }

    public abstract boolean isSupported(final IPrimitive o);

    @Override
    public String toString() {
      return String.format(
          "Argument [name=%s, kind=%s, mode=%s, typeNames=%s, dataType=%s]",
          name,
          kind,
          mode,
          typeNames,
          dataType
          );
    }
  }
}
