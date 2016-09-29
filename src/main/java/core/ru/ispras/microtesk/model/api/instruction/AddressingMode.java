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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.microtesk.model.api.data.Type;

/**
 * The AddressingMode abstract class is the base class for all classes that simulate behavior
 * specified by "mode" nML statements. The class provides definitions of classes and static
 * methods to be used by its descendants (ones that are to implement the IAddressingMode interface).
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public abstract class AddressingMode extends Primitive {
  /**
   * The IInfo interface provides information on an addressing mode object or a group of addressing
   * mode object united by an OR rule. This information is needed to instantiate a concrete
   * addressing mode object at runtime depending on the selected builder.
   * 
   * @author Andrei Tatarnikov
   */
  public interface IInfo {
    /**
     * Returns the name of the mode or the name of the OR rule used for grouping addressing modes.
     * 
     * @return The mode name.
     */
    String getName();

    /**
     * Returns the type of data accessed via the addressing mode.
     * 
     * @return Data type.
     */
    Type getType();

    /**
     * Returns a table of builder for the addressing mode (or the group of addressing modes)
     * described by the current info object.
     * 
     * @return A table of addressing mode builders (key is the mode name, value is the builder).
     */
    Map<String, AddressingModeBuilder> createBuilders();

    /**
     * Checks if the current addressing mode (or group of addressing modes) implements (or contains)
     * the specified addressing mode. This method is used in runtime checks to make sure that the
     * object composition in the model is valid.
     * 
     * @param mode An addressing mode object.
     * @return true if the mode is supported or false otherwise.
     */
    boolean isSupported(AddressingMode mode);
  }

  /**
   * The AddressingMode.Info class is an implementation of the IInfo interface that provides logic
   * for storing information about a single addressing mode. The class is to be used by generated
   * classes that implement behavior of particular addressing modes.
   * 
   * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
   */
  protected static abstract class InfoAndRule implements IInfo, Factory<AddressingMode> {
    private final Class<?> modeClass;
    private final String name;
    private final Type type;
    private final ArgumentDecls decls;

    public InfoAndRule(
        final Class<?> modeClass,
        final String name,
        final Type type,
        final ArgumentDecls decls) {
      this.modeClass = modeClass;
      this.name = name;
      this.type = type; 
      this.decls = decls;
    }

    @Override
    public final String getName() {
      return name;
    }

    @Override
    public final Type getType() {
      return type;
    }

    @Override
    public final Map<String, AddressingModeBuilder> createBuilders() {
      final AddressingModeBuilder builder = new AddressingModeBuilder(name, this, decls);
      return Collections.singletonMap(name, builder);
    }

    @Override
    public final boolean isSupported(final AddressingMode mode) {
      return modeClass.equals(mode.getClass());
    }
  }

  /**
   * The InfoOrRule class is an implementation of the IInfo interface that provides logic for
   * storing information about a group of addressing modes united by an OR-rule. The class is to be
   * used by generated classes that specify a set of addressing modes described by OR rules.
   * 
   * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
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
    public Type getType() {
      return childs[0].getType();
    }

    @Override
    public Map<String, AddressingModeBuilder> createBuilders() {
      final Map<String, AddressingModeBuilder> result = new HashMap<>();

      for (final IInfo i : childs) {
        result.putAll(i.createBuilders());
      }

      return Collections.unmodifiableMap(result);
    }

    @Override
    public boolean isSupported(final AddressingMode mode) {
      for (final IInfo i : childs) {
        if (i.isSupported(mode)) {
          return true;
        }
      }

      return false;
    }
  }
}
