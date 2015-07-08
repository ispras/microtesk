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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.model.api.ArgumentMode;
import ru.ispras.microtesk.model.api.metadata.MetaAddressingMode;
import ru.ispras.microtesk.model.api.metadata.MetaArgument;
import ru.ispras.microtesk.model.api.metadata.MetaData;
import ru.ispras.microtesk.model.api.metadata.MetaGroup;
import ru.ispras.microtesk.model.api.metadata.MetaOperation;
import ru.ispras.microtesk.model.api.metadata.MetaShortcut;
import ru.ispras.microtesk.model.api.type.Type;

/**
 * The Operation abstract class is the base class for all classes that simulate behavior specified
 * by "op" Sim-nML statements. The class provides definitions of classes to be used by its
 * descendants (generated classes that are to implement the IOperation interface).
 * 
 * @author Andrei Tatarnikov
 */

public abstract class Operation extends StandardFunctions implements IOperation {
  interface Param {
    public enum Kind {
      IMM,
      MODE,
      OP
    }

    public String getName();
    public Kind getKind();
    public boolean isSupported(IPrimitive o);
    public Type getType();
    public MetaArgument getMetaData();
  }

  private static class ParamIMM implements Param {
    private final String name;
    private final Type type;

    private ParamIMM(final String name, final Type type) {
      this.name = name;
      this.type = type;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public Kind getKind() {
      return Kind.IMM;
    }

    @Override
    public boolean isSupported(IPrimitive o) {
      return false;
    }

    @Override
    public Type getType() {
      return type;
    }

    @Override
    public MetaArgument getMetaData() {
      return new MetaArgument(
          MetaArgument.Kind.IMM,
          ArgumentMode.IN,
          name,
          Collections.singleton(AddressingModeImm.NAME),
          getType()
          );
    }
  }

  private static class ParamMode implements Param {
    private final String name;
    private final ArgumentMode usageMode;
    private final IAddressingMode.IInfo info;

    private ParamMode(
        final String name,
        final ArgumentMode usageMode,
        final IAddressingMode.IInfo info) {
      this.name = name;
      this.usageMode = usageMode;
      this.info = info;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public Kind getKind() {
      return Kind.MODE;
    }

    @Override
    public boolean isSupported(final IPrimitive o) {
      return (o instanceof IAddressingMode) && info.isSupported((IAddressingMode) o);
    }

    @Override
    public Type getType() {
      return info.getType();
    }

    @Override
    public MetaArgument getMetaData() {
      final Set<String> modeNames = new LinkedHashSet<>(info.getMetaData().size());

      for (final MetaAddressingMode mode : info.getMetaData()) {
        modeNames.add(mode.getName());
      }

      return new MetaArgument(
          MetaArgument.Kind.MODE,
          usageMode, // IN/OUT/INOUT/NA (if no return type)
          name,
          modeNames,
          getType()
          );
    }
  }

  private static class ParamOp implements Param {
    private final String name;
    private final IOperation.IInfo info;

    private ParamOp(final String name, final IOperation.IInfo info) {
      this.name = name;
      this.info = info;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public Kind getKind() {
      return Kind.OP;
    }

    @Override
    public boolean isSupported(final IPrimitive o) {
      return (o instanceof IOperation) && info.isSupported((IOperation) o);
    }

    @Override
    public Type getType() {
      return null;
    }

    @Override
    public MetaArgument getMetaData() {
      final Set<String> opNames = new LinkedHashSet<>(info.getMetaData().size());

      for (final MetaOperation op : info.getMetaData()) {
        opNames.add(op.getName());
      }

      return new MetaArgument(
          MetaArgument.Kind.OP,
          ArgumentMode.NA,
          name,
          opNames,
          getType()
          );
    }
  }

  /**
   * The ParamDecl class is aimed to specify declarations operation parameters.
   * 
   * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
   */

  public final static class ParamDecls {
    private final Map<String, Param> decls;

    public ParamDecls() {
      this.decls = new LinkedHashMap<>();
    }

    public ParamDecls declareParam(
        final String name,
        final Type type) {
      decls.put(name, new ParamIMM(name, type));
      return this;
    }

    public ParamDecls declareParam(
        final String name,
        final ArgumentMode mode,
        final AddressingMode.IInfo info) {
      decls.put(name, new ParamMode(name, mode, info));
      return this;
    }

    public ParamDecls declareParam(
        final String name,
        final IOperation.IInfo info) {
      decls.put(name, new ParamOp(name, info));
      return this;
    }

    public Map<String, MetaArgument> getMetaData() {
      final Map<String, MetaArgument> metaData = new LinkedHashMap<>(decls.size());
      for (final Param p : decls.values()) {
        metaData.put(p.getName(), p.getMetaData());
      }

      return metaData;
    }

    public Map<String, Param> getDecls() {
      return decls;
    }
  }

  protected static Object getArgument(
      final String name,
      final ParamDecls decls,
      final Map<String, Object> args) {

    final Object arg = args.get(name);
    // TODO Check argument
    return arg;
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

  public static abstract class InfoAndRule implements IInfo, IFactory {
    private final Class<?> opClass;
    private final String name;
    private final boolean isRoot;
    private final ParamDecls decls;
    private final Shortcuts shortcuts;
    private final MetaOperation metaData;

    public InfoAndRule(
        final Class<?> opClass,
        final String name,
        final boolean isRoot,
        final ParamDecls decls,
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
        final ParamDecls decls,
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
    public final Map<String, IOperationBuilder> createBuilders() {
      final IOperationBuilder builder = new OperationBuilder(name, this, decls);
      return Collections.singletonMap(name, builder);
    }

    protected final Object getArgument(final String name, final Map<String, Object> args) {
      final Object arg = args.get(name);
      // TODO Check argument
      return arg;
    }

    public final Map<String, IOperationBuilder> createBuildersForShortcut(
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
    public Map<String, IOperationBuilder> createBuilders() {
      final Map<String, IOperationBuilder> result = new HashMap<>();
      for (IInfo i : childs) {
        result.putAll(i.createBuilders());
      }

      return Collections.unmodifiableMap(result);
    }

    @Override
    public Map<String, IOperationBuilder> createBuildersForShortcut(String contextName) {
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

  /**
   * Default implementation of the syntax attribute. Provided to allow using addressing modes that
   * have no explicitly specified syntax attribute. This method does not do any useful work and
   * should never be called. It is needed only to let inherited classes compile.
   */

  @Override
  public String syntax() {
    // This code should never be called!
    assert false : "Operation.syntax - default implementation. Should never be called!";
    return null;
  }

  /**
   * Default implementation of the image attribute. Provided to allow using addressing modes that
   * have no explicitly specified image attribute. This method does not do any useful work and
   * should never be called. It is needed only to let inherited classes compile.
   */

  @Override
  public String image() {
    // This code should never be called!
    assert false : "Operation.image - default implementation. Should never be called!";
    return null;
  }

  /**
   * Default implementation of the action attribute. Provided to allow using addressing modes that
   * have no explicitly specified action attribute. This method does not do any useful work and
   * should never be called. It is needed only to let inherited classes compile.
   */

  public void action() {
    // This code should never be called!
    assert false : "Operation.action - default implementation. Should never be called!";
  }
}
