/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.model.api.data.Type;
import ru.ispras.microtesk.model.api.memory.Location;
import ru.ispras.microtesk.model.api.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.api.metadata.MetaData;
import ru.ispras.microtesk.model.api.metadata.MetaGroup;

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
     * Returns a collection of meta data objects describing the addressing mode (or the group of
     * addressing modes) the info object refers to. In the case, when there is a single addressing
     * mode, the collection will contain only one item.
     * 
     * @return A collection of meta data objects for an addressing mode or a group of addressing
     *         modes.
     */
    Collection<MetaAddressingMode> getMetaData();

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

  public static final String IMM_TYPE_NAME = "#IMM";

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
    private final boolean exception;
    private final boolean memoryReference;
    private final boolean load;
    private final boolean store;
    private final int blockSize;

    private MetaAddressingMode metaData;

    public InfoAndRule(
        final Class<?> modeClass,
        final String name,
        final Type type,
        final ArgumentDecls decls,
        final boolean exception,
        final boolean memoryReference,
        final boolean load,
        final boolean store,
        final int blockSize) {
      this.modeClass = modeClass;
      this.name = name;
      this.type = type; 
      this.decls = decls;
      this.metaData = null;
      this.exception = exception;

      this.memoryReference = memoryReference;
      this.load = load;
      this.store = store;
      this.blockSize = blockSize;
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
    public final Collection<MetaAddressingMode> getMetaData() {
      return Collections.singletonList(getMetaDataItem());
    }

    public MetaAddressingMode getMetaDataItem() {
      if (null == metaData) {
        metaData = createMetaData();
      }
      return metaData;
    }

    private MetaAddressingMode createMetaData() {
      return new MetaAddressingMode(
          name,
          type,
          decls.getMetaData(),
          exception,
          memoryReference,
          load,
          store,
          blockSize
          );
    }

    @Override
    public final boolean isSupported(final AddressingMode mode) {
      return modeClass.equals(mode.getClass());
    }

    /**
     * Extracts the specified argument from the table of arguments and wraps it into a location
     * object.
     * 
     * @param name The name of the argument.
     * @param args A table of parameters.
     * @return The location that stores the specified addressing mode argument.
     */

    protected final Location getArgument(final String name, final Map<String, Object> args) {
      final Object arg = args.get(name);
      // TODO Check argument
      /*
      assert decls.getDecls().get(name).getType().equals(data.getType()) :
          String.format("The %s parameter does not exist.", name);
      */
      return (Location) arg;
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
    public Collection<MetaAddressingMode> getMetaData() {
      final List<MetaAddressingMode> result = new ArrayList<>();

      for (final IInfo i : childs) {
        result.addAll(i.getMetaData());
      }

      return Collections.unmodifiableCollection(result);
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

    public MetaGroup getMetaDataGroup() {
      final List<MetaData> items = new ArrayList<>();

      for (final IInfo i : childs) {
        if (i instanceof AddressingMode.InfoAndRule) {
          items.add(((AddressingMode.InfoAndRule) i).getMetaDataItem());
        } else {
          items.add(((AddressingMode.InfoOrRule) i).getMetaDataGroup());
        }
      }

      return new MetaGroup(MetaGroup.Kind.MODE, name, items);
    }
  }
}
