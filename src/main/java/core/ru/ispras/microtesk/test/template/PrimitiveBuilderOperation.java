/*
 * Copyright 2014-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template;

import ru.ispras.fortress.randomizer.Variate;
import ru.ispras.fortress.randomizer.VariateSingleValue;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.metadata.MetaModel;
import ru.ispras.microtesk.model.metadata.MetaOperation;
import ru.ispras.microtesk.model.metadata.MetaShortcut;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class PrimitiveBuilderOperation implements PrimitiveBuilder {
  private final String name;
  private String contextName;
  private Variate<Situation> situation;

  private final MetaModel metaModel;
  private final AbstractCallBuilder callBuilder;

  private final List<Argument> argumentList;
  private final Map<String, Argument> argumentMap;

  private static final String ERR_WRONG_USE =
      "Illegal use: Arguments can be added using either "
          + "addArgument or setArgument methods, but not both.";

  PrimitiveBuilderOperation(
      final String name,
      final MetaModel metaModel,
      final AbstractCallBuilder callBuilder) {

    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(metaModel);
    InvariantChecks.checkNotNull(callBuilder);

    this.metaModel = metaModel;
    this.callBuilder = callBuilder;

    this.name = name;
    this.contextName = null;
    this.situation = null;

    this.argumentList = new ArrayList<Argument>();
    this.argumentMap = new LinkedHashMap<String, Argument>();
  }

  public Primitive build() {
    final MetaOperation metaData = metaModel.getOperation(name);
    if (null == metaData) {
      throw new IllegalArgumentException("No such operation: " + name);
    }

    final MetaShortcut metaShortcut = metaData.getShortcut(contextName);

    final PrimitiveBuilder builder;
    if (null != metaShortcut) {
      builder = new PrimitiveBuilderCommon(
          metaModel, callBuilder, metaShortcut.getOperation(), contextName);
    } else {
      // If there is no shortcut for the given context, the operation is used as it is.
      builder = new PrimitiveBuilderCommon(
          metaModel, callBuilder, metaData, null);
    }

    builder.setSituation(situation);

    if (!argumentList.isEmpty()) {
      for (final Argument argument : argumentList) {
        argument.addToBuilder(builder);
      }
    } else if (!argumentMap.isEmpty()) {
      for (final Argument argument : argumentMap.values()) {
        argument.addToBuilder(builder);
      }
    }

    return builder.build();
  }

  public void setContext(final String contextName) {
    this.contextName = contextName;
  }

  public void setSituation(final Situation situation) {
    this.situation = new VariateSingleValue<>(situation);
  }

  public void setSituation(final Variate<Situation> situation) {
    this.situation = situation;
  }

  private void registerArgument(final Argument argument) {
    if (argument.hasName()) {
      if (!argumentList.isEmpty()) {
        throw new IllegalStateException(ERR_WRONG_USE);
      }
      argumentMap.put(argument.getName(), argument);
    } else {
      if (!argumentMap.isEmpty()) {
        throw new IllegalStateException(ERR_WRONG_USE);
      }
      argumentList.add(argument);
    }
  }

  // /////////////////////////////////////////////////////////////////////////
  // For Array-based syntax

  public void addArgument(final BigInteger value) {
    addArgument(new FixedValue(value));
  }

  public void addArgument(final String value) {
    InvariantChecks.checkNotNull(value);
    registerArgument(new ArgumentStr(value));
  }

  public void addArgument(final Value value) {
    InvariantChecks.checkNotNull(value);
    registerArgument(new ArgumentVal(value));
  }

  public void addArgument(final Primitive value) {
    InvariantChecks.checkNotNull(value);
    registerArgument(new ArgumentPrim(value));
  }

  @Override
  public void addArgument(final PrimitiveBuilder value) {
    InvariantChecks.checkNotNull(value);
    registerArgument(new ArgumentPrimB(value));
  }

  // /////////////////////////////////////////////////////////////////////////
  // For Hash-based syntax

  public void setArgument(final String name, final BigInteger value) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(value);
    setArgument(name, new FixedValue(value));
  }

  public void setArgument(final String name, final String value) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(value);
    registerArgument(new ArgumentStr(name, value));
  }

  public void setArgument(final String name, final Value value) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(value);
    registerArgument(new ArgumentVal(name, value));
  }

  public void setArgument(final String name, final Primitive value) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(value);
    registerArgument(new ArgumentPrim(name, value));
  }

  @Override
  public void setArgument(final String name, final PrimitiveBuilder value) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(value);
    registerArgument(new ArgumentPrimB(name, value));
  }

  private interface Argument {
    boolean hasName();

    String getName();

    void addToBuilder(PrimitiveBuilder builder);
  }

  private abstract static class AbstractArgument<T> implements Argument {
    private final String name;
    private final T value;

    public AbstractArgument(final String name, final T value) {
      this.name = name;
      this.value = value;
    }

    public AbstractArgument(T value) {
      this(null, value);
    }

    @Override
    public final String getName() {
      return name;
    }

    @Override
    public final boolean hasName() {
      return null != name;
    }

    public final T getValue() {
      return value;
    }
  }

  private static class ArgumentStr extends AbstractArgument<String> {
    public ArgumentStr(final String name, final String value) {
      super(name, value);
    }

    public ArgumentStr(final String value) {
      super(value);
    }

    @Override
    public void addToBuilder(final PrimitiveBuilder builder) {
      if (hasName()) {
        builder.setArgument(getName(), getValue());
      } else {
        builder.addArgument(getValue());
      }
    }
  }

  private static class ArgumentVal extends AbstractArgument<Value> {
    public ArgumentVal(final String name, final Value value) {
      super(name, value);
    }

    public ArgumentVal(final Value value) {
      super(value);
    }

    @Override
    public void addToBuilder(final PrimitiveBuilder builder) {
      if (hasName()) {
        builder.setArgument(getName(), getValue());
      } else {
        builder.addArgument(getValue());
      }
    }
  }

  private static class ArgumentPrim extends AbstractArgument<Primitive> {
    public ArgumentPrim(final String name, final Primitive value) {
      super(name, value);
    }

    public ArgumentPrim(final Primitive value) {
      super(value);
    }

    @Override
    public void addToBuilder(final PrimitiveBuilder builder) {
      if (hasName()) {
        builder.setArgument(getName(), getValue());
      } else {
        builder.addArgument(getValue());
      }
    }
  }

  private static class ArgumentPrimB extends AbstractArgument<PrimitiveBuilder> {
    public ArgumentPrimB(final String name, final PrimitiveBuilder value) {
      super(name, value);
    }

    public ArgumentPrimB(final PrimitiveBuilder value) {
      super(value);
    }

    @Override
    public void addToBuilder(final PrimitiveBuilder builder) {
      if (hasName()) {
        builder.setArgument(getName(), getValue());
      } else {
        builder.addArgument(getValue());
      }
    }
  }
}
