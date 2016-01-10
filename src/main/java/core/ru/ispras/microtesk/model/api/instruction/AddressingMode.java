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

import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.StandardFunctions;
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

public abstract class AddressingMode extends StandardFunctions implements IAddressingMode {
  /**
   * The AddressingMode.Info class is an implementation of the IInfo interface that provides logic
   * for storing information about a single addressing mode. The class is to be used by generated
   * classes that implement behavior of particular addressing modes.
   * 
   * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
   */

  protected static abstract class InfoAndRule implements IInfo, IFactory<IAddressingMode> {
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
    public final boolean isSupported(final IAddressingMode mode) {
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
    public boolean isSupported(final IAddressingMode mode) {
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

  /**
   * Default implementation of the syntax attribute. Provided to allow using addressing modes that
   * have no explicitly specified syntax attribute. This method does not do any useful work and
   * should never be called. It is needed only to let inherited classes compile.
   */

  @Override
  public String syntax() {
    Logger.error(
        "The 'syntax' attribute is not defined for the '%s' primitive. " + 
        "An empty string is returned.",
        getClass().getSimpleName()
        );

    return "";
  }

  /**
   * Default implementation of the image attribute. Provided to allow using addressing modes that
   * have no explicitly specified image attribute. This method does not do any useful work and
   * should never be called. It is needed only to let inherited classes compile.
   */

  @Override
  public String image() {
    Logger.error(
        "The 'image' attribute is not defined for the '%s' primitive. " + 
        "An empty string is returned. Primitive size will be calculated incorrectly.",
        getClass().getSimpleName()
        );

    return "";
  }

  /**
   * Default implementation of the action attribute. Provided to allow using addressing modes that
   * have no explicitly specified action attribute. This method does not do any useful work and
   * should never be called. It is needed only to let inherited classes compile.
   */

  public void action() {
    Logger.error(
        "The 'action' attribute is not defined for the '%s' primitive. " + 
        "No action will be performed.",
        getClass().getSimpleName()
        );
  }

  /**
   * Default implementation of the access method. The method is overridden in concrete addressing
   * mode class if the return expression was specified for this addressing mode.
   */

  @Override
  public Location access() {
    // This code should never be called!
    assert false : "AddressingMode.access - default implementation. Should never be called!";
    return null;
  }
}
