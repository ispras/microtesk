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

package ru.ispras.microtesk.model.api.instruction;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.api.data.Type;

public abstract class IsaPrimitiveInfoAnd<T extends IsaPrimitive> extends IsaPrimitiveInfo
                                                            implements IsaPrimitiveFactory<T> {
  private final Class<?> objectClass;
  private final Map<String, IsaPrimitiveInfo> arguments;
  private final Map<String, IsaPrimitiveInfoAnd<T>> shortcuts;

  protected IsaPrimitiveInfoAnd(
      final IsaPrimitiveKind kind,
      final String name,
      final Class<?> objectClass,
      final Type type) {
    super(kind, name, type);
    InvariantChecks.checkNotNull(objectClass);

    this.objectClass = objectClass;
    this.arguments = new LinkedHashMap<>();
    this.shortcuts = new LinkedHashMap<>();
  }

  protected final void addArgument(final String name, final Type type) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(type);
    arguments.put(name, new Immediate.Info(type));
  }

  protected final void addArgument(final String name, final IsaPrimitiveInfo info) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(info);
    arguments.put(name, info);
  }

  protected final void addShortcut(final IsaPrimitiveInfoAnd<T> info, final String... contexts) {
    for (final String context : contexts) {
      InvariantChecks.checkFalse(shortcuts.containsKey(context));
      shortcuts.put(context, info);
    }
  }

  @Override
  public final boolean isSupported(final IsaPrimitive primitive) {
    return objectClass.equals(primitive.getClass());
  }

  public Set<String> getArgumentNames() {
    return arguments.keySet();
  }

  public IsaPrimitiveInfo getArgument(final String name) {
    return arguments.get(name);
  }

  public final IsaPrimitiveBuilder<T> createBuilder() {
    /*
    return new PrimitiveBuilder<>(name, this, decls);
    */
    return null;
  }

  public final IsaPrimitiveBuilder<T> createBuilderForShortcut(final String contextName) {
    final IsaPrimitiveInfoAnd<T> shortcut = shortcuts.get(contextName);
    if (null == shortcut) {
      return null;
    }

    return shortcut.createBuilder();
  }
}
