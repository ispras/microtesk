/*
 * Copyright 2012-2015 ISP RAS (http://www.ispras.ru)
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
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.model.api.StandardFunctions;
import ru.ispras.microtesk.model.api.metadata.MetaData;
import ru.ispras.microtesk.model.api.metadata.MetaGroup;
import ru.ispras.microtesk.model.api.metadata.MetaOperation;
import ru.ispras.microtesk.model.api.metadata.MetaShortcut;

/**
 * The Operation abstract class is the base class for all classes that simulate behavior specified
 * by "op" nML statements. The class provides definitions of classes to be used by its
 * descendants (generated classes that are to implement the IOperation interface).
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public abstract class Operation extends StandardFunctions implements IOperation {

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

    public Map<String, MetaShortcut> getMetaData() {
      if (shortcuts.isEmpty()) {
        return Collections.emptyMap();
      }

      final Map<String, MetaShortcut> result = new LinkedHashMap<>(shortcuts.size());
      for (final Map.Entry<String, InfoAndRule> e : shortcuts.entrySet()) {
        final String contextName = e.getKey();
        final MetaOperation metaOperation = e.getValue().metaData;

        final MetaShortcut metaShortcut = new MetaShortcut(contextName, metaOperation);
        result.put(contextName, metaShortcut);
      }

      return result;
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

  public static abstract class InfoAndRule implements IInfo, IFactory<IOperation> {
    private final Class<?> opClass;
    private final String name;
    private final boolean isRoot;
    private final ArgumentDecls decls;
    private final Shortcuts shortcuts;
    private final MetaOperation metaData;

    public InfoAndRule(
        final Class<?> opClass,
        final String name,
        final boolean isRoot,
        final ArgumentDecls decls,
        final boolean isBranch,
        final boolean isConditionalBranch,
        final boolean canThrowException,
        final boolean load,
        final boolean store,
        final int blockSize,
        final Shortcuts shortcuts) {
      this.opClass = opClass;
      this.name = name;
      this.isRoot = isRoot;
      this.decls = decls;
      this.shortcuts = shortcuts;

      this.metaData = new MetaOperation(
          name,
          opClass.getSimpleName(),
          isRoot(),
          decls.getMetaData(),
          shortcuts.getMetaData(),
          isBranch,
          isConditionalBranch,
          canThrowException,
          load,
          store,
          blockSize
          );
    }

    public InfoAndRule(
        final Class<?> opClass,
        final String name,
        final boolean isRoot,
        final ArgumentDecls decls,
        final boolean isBranch,
        final boolean isConditionalBranch,
        final boolean canThrowException,
        final boolean load,
        final boolean store,
        final int blockSize) {
      this(
          opClass,
          name,
          isRoot,
          decls,
          isBranch,
          isConditionalBranch,
          canThrowException,
          load,
          store,
          blockSize,
          new Shortcuts()
          );
    }

    @Override
    public final String getName() {
      return name;
    }

    @Override
    public final boolean isRoot() {
      return isRoot;
    }

    @Override
    public final boolean isSupported(final IOperation op) {
      return opClass.equals(op.getClass());
    }

    @Override
    public final Collection<MetaOperation> getMetaData() {
      return Collections.singletonList(metaData);
    }

    public final MetaOperation getMetaDataItem() {
      return metaData;
    }

    @Override
    public final Map<String, OperationBuilder> createBuilders() {
      final OperationBuilder builder = new OperationBuilder(name, this, decls);
      return Collections.singletonMap(name, builder);
    }

    protected final Object getArgument(final String name, final Map<String, Object> args) {
      final Object arg = args.get(name);
      // TODO Check argument
      return arg;
    }

    public final Map<String, OperationBuilder> createBuildersForShortcut(
        final String contextName) {

      final IInfo shortcut = shortcuts.getShortcut(contextName);
      if (null == shortcut) {
        return null;
      }

      return shortcut.createBuilders();
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
    private final Collection<MetaOperation> metaData;

    public InfoOrRule(final String name, final IInfo... childs) {
      this.name = name;
      this.childs = childs;
      this.metaData = createMetaData(name, childs);
    }

    private static Collection<MetaOperation> createMetaData(
        final String name,
        final IInfo[] childs) {

      final List<MetaOperation> result = new ArrayList<>();
      for (final IInfo i : childs) {
        result.addAll(i.getMetaData());
      }

      return Collections.unmodifiableCollection(result);
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public boolean isRoot() {
      for (final IInfo child : childs) {
        if (!child.isRoot()) {
          return false;
        }
      }

      return true;
    }

    @Override
    public boolean isSupported(final IOperation op) {
      for (final IInfo i : childs) {
        if (i.isSupported(op)) {
          return true;
        }
      }

      return false;
    }

    @Override
    public Collection<MetaOperation> getMetaData() {
      return metaData;
    }

    @Override
    public Map<String, OperationBuilder> createBuilders() {
      final Map<String, OperationBuilder> result = new HashMap<>();
      for (final IInfo i : childs) {
        result.putAll(i.createBuilders());
      }

      return Collections.unmodifiableMap(result);
    }

    @Override
    public Map<String, OperationBuilder> createBuildersForShortcut(final String contextName) {
      return null;
    }

    public MetaGroup getMetaDataGroup() {
      final List<MetaData> items = new ArrayList<>();

      for (final IInfo i : childs) {
        if (i instanceof Operation.InfoAndRule) {
          items.add(((Operation.InfoAndRule) i).getMetaDataItem());
        } else {
          items.add(((Operation.InfoOrRule) i).getMetaDataGroup());
        }
      }

      return new MetaGroup(MetaGroup.Kind.OP, name, items);
    }
  }

  private static final Deque<String> CALL_STACK = new LinkedList<>();
  public static String getCurrentOperation() {
    return CALL_STACK.peek();
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

  protected final void actionBegin() {
    CALL_STACK.push(getClass().getSimpleName());
  }

  protected final void actionEnd() {
    CALL_STACK.pop();
  }
}
