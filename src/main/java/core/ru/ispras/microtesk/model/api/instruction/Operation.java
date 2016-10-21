/*
 * Copyright 2012-2016 ISP RAS (http://www.ispras.ru)
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

/**
 * The Operation abstract class is the base class for all classes that simulate behavior specified
 * by "op" nML statements. The class provides definitions of classes to be used by its
 * descendants (generated classes that are to implement the IOperation interface).
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public abstract class Operation extends Primitive {
  /**
   * The IInfo interface provides information on an operation object or a group of operation object
   * united by an OR rule. This information is needed for runtime checks to make sure that
   * instructions are configured with proper operation objects.
   * 
   * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
   */
  public interface IInfo {
    /**
     * Returns the name of the operation or the name of the OR rule used for grouping operations.
     * 
     * @return The mode name.
     */
    String getName();

    PrimitiveBuilder<Operation> createBuilder();

    PrimitiveBuilder<Operation> createBuilderForShortcut(String contextName);

    /**
     * Checks if the current operation (or group of operations) implements (or contains) the
     * specified operation. This method is used in runtime checks to make sure that the object
     * composition in the model is valid.
     * 
     * @param op An operation object.
     * @return true if the operation is supported or false otherwise.
     */
    boolean isSupported(Operation op);
  }

  protected static final class Shortcuts {
    private final Map<String, InfoAndRule> shortcuts;

    public Shortcuts() {
      this.shortcuts = new LinkedHashMap<>();
    }

    public Shortcuts addShortcut(final InfoAndRule operation, final String... contexts) {
      for (final String context : contexts) {
        assert !shortcuts.containsKey(context);
        shortcuts.put(context, operation);
      }

      return this;
    }

    public IInfo getShortcut(final String contextName) {
      return shortcuts.get(contextName);
    }
  }

  /**
   * The Info class is an implementation of the IInfo interface. It is designed to store information
   * about a single operation. The class is to be used by generated classes that implement behavior
   * of particular operations.
   * 
   * @author Andrei Tatarnikov
   */
  public static abstract class InfoAndRule implements IInfo, PrimitiveFactory<Operation> {
    private final Class<?> opClass;
    private final String name;
    private final ArgumentDecls decls;
    private final Shortcuts shortcuts;

    public InfoAndRule(
        final Class<?> opClass,
        final String name,
        final ArgumentDecls decls,
        final Shortcuts shortcuts) {
      this.opClass = opClass;
      this.name = name;
      this.decls = decls;
      this.shortcuts = shortcuts;
    }

    public InfoAndRule(
        final Class<?> opClass,
        final String name,
        final ArgumentDecls decls) {
      this(
          opClass,
          name,
          decls,
          new Shortcuts()
          );
    }

    @Override
    public final String getName() {
      return name;
    }

    @Override
    public final boolean isSupported(final Operation op) {
      return opClass.equals(op.getClass());
    }

    @Override
    public final PrimitiveBuilder<Operation> createBuilder() {
      return new PrimitiveBuilder<>(name, this, decls);
    }

    @Override
    public final PrimitiveBuilder<Operation> createBuilderForShortcut(final String contextName) {
      final IInfo shortcut = shortcuts.getShortcut(contextName);
      if (null == shortcut) {
        return null;
      }

      return shortcut.createBuilder();
    }
  }

  /**
   * The InfoOrRule class is an implementation of the IInfo interface that provides logic for
   * storing information about a group of operations united by an OR-rule. The class is to be used
   * by generated classes that specify a set of operations united by an OR rule.
   * 
   * @author Andrei Tatarnikov
   */
  public static final class InfoOrRule implements IInfo {
    private final String name;
    private final IInfo[] childs;

    public InfoOrRule(final String name, final IInfo... childs) {
      this.name = name;
      this.childs = childs;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public boolean isSupported(final Operation op) {
      for (final IInfo i : childs) {
        if (i.isSupported(op)) {
          return true;
        }
      }

      return false;
    }

    @Override
    public PrimitiveBuilder<Operation> createBuilder() {
      return null;
    }

    @Override
    public PrimitiveBuilder<Operation> createBuilderForShortcut(final String contextName) {
      return null;
    }
  }
}
